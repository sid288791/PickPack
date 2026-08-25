package com.autopart.ordermgmt.data.remote.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.autopart.ordermgmt.R
import com.autopart.ordermgmt.data.repository.NotificationRepository
import com.autopart.ordermgmt.data.util.NotificationChannelManager
import com.google.firebase.messaging.FirebaseMessagingService as GoogleFirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Messaging Service
 * Handles incoming FCM messages and displays notifications
 * Follows Single Responsibility Principle - only handles message receiving and notification display
 */
@AndroidEntryPoint
class OrderManagementFirebaseMessagingService : GoogleFirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    companion object {
        private const val NOTIFICATION_ID_BASE = 1000
        private var notificationCounter = 0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val orderId = remoteMessage.data["order_id"]
        
        remoteMessage.notification?.let { notification ->
            sendNotification(
                title = notification.title ?: "Order Management",
                body = notification.body ?: "You have a new notification",
                orderId = orderId
            )
        }
        
       /* remoteMessage.data.isNotEmpty().let {
            handleDataMessage(remoteMessage.data)
        }*/
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMToken", "New FCM Token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.saveFCMToken(token)
        }
    }

    private fun sendNotification(title: String, body: String, orderId: String? = null) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationChannelManager.createNotificationChannel()

        notificationCounter++
        val notificationId = NOTIFICATION_ID_BASE + notificationCounter

        val clickIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_NOTIFICATION_CLICKED
            orderId?.let { putExtra("order_id", it) }
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId * 2,
            clickIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val deleteIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_NOTIFICATION_DISMISSED
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId * 2 + 1,
            deleteIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, notificationChannelManager.getChannelId())
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(clickPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(notificationId, notification)
        NotificationSoundService.notifyNotificationPosted(this)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Order Management"
        val body = data["body"] ?: "You have a new notification"
       // sendNotification(title, body)
    }
}
