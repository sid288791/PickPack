package com.autopart.ordermgmt.data.remote.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.autopart.ordermgmt.MainActivity

/**
 * Handles notification click and dismiss events so the active notification
 * count can be tracked and the looping alert sound stopped once the tray is empty.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_NOTIFICATION_CLICKED = "com.autopart.ordermgmt.action.NOTIFICATION_CLICKED"
        const val ACTION_NOTIFICATION_DISMISSED = "com.autopart.ordermgmt.action.NOTIFICATION_DISMISSED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NOTIFICATION_DISMISSED -> {
                NotificationSoundService.notifyNotificationDismissed(context)
            }
            ACTION_NOTIFICATION_CLICKED -> {
                NotificationSoundService.notifyNotificationDismissed(context)
                val orderId = intent.getStringExtra("order_id")
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    orderId?.let { putExtra("order_id", it) }
                }
                context.startActivity(launchIntent)
            }
        }
    }
}
