package com.autopart.ordermgmt.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification Channel Manager
 * Follows Single Responsibility Principle - manages notification channel creation
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "order_management_channel_v3"
        private const val CHANNEL_NAME = "Order Management"
        private const val CHANNEL_DESCRIPTION = "Notifications for order management"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Channel settings (including sound) are immutable once created.
            // Skip re-creation if the channel already exists to avoid disrupting active notifications.
            if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
                return
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                // Sound is played via the looping NotificationSoundService instead of the
                // channel itself, so keep the channel silent to avoid double playback.
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getChannelId(): String = CHANNEL_ID
}
