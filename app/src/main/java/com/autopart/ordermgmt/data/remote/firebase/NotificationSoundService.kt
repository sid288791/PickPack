/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.data.remote.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.autopart.ordermgmt.R
import java.util.concurrent.atomic.AtomicInteger

/**
 * Foreground service that loops the notification alert sound
 * while there are unread notifications in the tray.
 * Stops automatically once all notifications have been dismissed or opened.
 */
class NotificationSoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable {
        stopLoopingSound()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val ACTION_STOP = "com.autopart.ordermgmt.action.STOP_SOUND"
        private const val SERVICE_CHANNEL_ID = "notification_sound_service_channel"
        private const val SERVICE_NOTIFICATION_ID = 999
        private const val AUTO_STOP_DELAY_MS = 30_000L // 30 seconds

        private val activeNotificationCount = AtomicInteger(0)

        fun notifyNotificationPosted(context: Context) {
            activeNotificationCount.incrementAndGet()
            val intent = Intent(context, NotificationSoundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun notifyNotificationDismissed(context: Context) {
            if (activeNotificationCount.updateAndGet { current -> (current - 1).coerceAtLeast(0) } == 0) {
                val intent = Intent(context, NotificationSoundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            }
        }

        fun clearAll(context: Context) {
            activeNotificationCount.set(0)
            val intent = Intent(context, NotificationSoundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopLoopingSound()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(SERVICE_NOTIFICATION_ID, buildServiceNotification())
        // Reset the auto-stop timer on every new notification
        handler.removeCallbacks(autoStopRunnable)
        handler.postDelayed(autoStopRunnable, AUTO_STOP_DELAY_MS)
        if (mediaPlayer == null) {
            startLoopingSound()
        }
        return START_STICKY
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PickPack:NotificationSoundLock"
            ).apply {
                acquire(AUTO_STOP_DELAY_MS + 5_000L) // auto-release slightly after timeout
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun startLoopingSound() {
        try {
            acquireWakeLock()
            // Use ALARM stream if notification volume is 0 (common on Zebra enterprise profiles)
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val notifVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val audioUsage = if (notifVolume == 0) {
                AudioAttributes.USAGE_ALARM
            } else {
                AudioAttributes.USAGE_NOTIFICATION
            }

            mediaPlayer = MediaPlayer().apply {
                val afd = resources.openRawResourceFd(R.raw.notification_sound) ?: return
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(audioUsage)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopLoopingSound() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        releaseWakeLock()
    }

    private fun buildServiceNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (notificationManager.getNotificationChannel(SERVICE_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    "Alert Sound",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Plays the alert sound while notifications are pending"
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("Order Management")
            .setContentText("You have unread notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(autoStopRunnable)
        stopLoopingSound()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
