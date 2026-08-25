package com.autopart.ordermgmt.data.remote.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
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

    companion object {
        private const val ACTION_STOP = "com.autopart.ordermgmt.action.STOP_SOUND"
        private const val SERVICE_CHANNEL_ID = "notification_sound_service_channel"
        private const val SERVICE_NOTIFICATION_ID = 999

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
        if (mediaPlayer == null) {
            startLoopingSound()
        }
        return START_STICKY
    }

    private fun startLoopingSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                val afd = resources.openRawResourceFd(R.raw.notification_sound) ?: return
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
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
        stopLoopingSound()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
