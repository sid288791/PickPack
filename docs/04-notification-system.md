# Step 4: Notification System

This step creates the complete Firebase Cloud Messaging (FCM) notification system including:
- Firebase messaging service (receives push notifications)
- Notification sound service (looping alert sound foreground service)
- Notification action receiver (click/dismiss handling)
- Notification channel manager
- Local data source (DataStore for FCM token & store ID)
- Repository (interface + implementation)
- Hilt DI module
- Domain use cases
- Notification ViewModel

**Package:** `com.autopart.ordermgmt`

---

## 4.1 Notification Local Data Source

**File:** `app/src/main/java/com/autopart/ordermgmt/data/local/datasource/NotificationLocalDataSource.kt`

```kotlin
package com.autopart.ordermgmt.data.local.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

@Singleton
class NotificationLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
        private val STORE_ID_KEY = stringPreferencesKey("store_id")
    }

    suspend fun saveFCMToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    fun getFCMToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }
    }

    suspend fun saveStoreId(storeId: String) {
        context.dataStore.edit { preferences ->
            preferences[STORE_ID_KEY] = storeId
        }
    }

    fun getStoreId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[STORE_ID_KEY]
        }
    }
}
```

**Key points:**
- Uses Jetpack DataStore Preferences for local storage
- Stores FCM token and store ID
- `@Singleton` scoped, injected with `@ApplicationContext`

---

## 4.2 Notification Repository Interface

**File:** `app/src/main/java/com/autopart/ordermgmt/data/repository/NotificationRepository.kt`

```kotlin
package com.autopart.ordermgmt.data.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun saveFCMToken(token: String)
    fun getFCMToken(): Flow<String?>
    suspend fun subscribeToTopic(topic: String)
    suspend fun unsubscribeFromTopic(topic: String)
    suspend fun registerStore(storeId: String)
}
```

---

## 4.3 Notification Repository Implementation

**File:** `app/src/main/java/com/autopart/ordermgmt/data/repository/NotificationRepositoryImpl.kt`

```kotlin
package com.autopart.ordermgmt.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.autopart.ordermgmt.data.local.datasource.NotificationLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val localDataSource: NotificationLocalDataSource
) : NotificationRepository {

    override suspend fun saveFCMToken(token: String) {
        localDataSource.saveFCMToken(token)
    }

    override fun getFCMToken(): Flow<String?> {
        return localDataSource.getFCMToken()
    }

    override suspend fun subscribeToTopic(topic: String) {
        try {
            firebaseMessaging.subscribeToTopic(topic).await()
        } catch (e: Exception) {
            throw NotificationException("Failed to subscribe to topic: $topic", e)
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
        } catch (e: Exception) {
            throw NotificationException("Failed to unsubscribe from topic: $topic", e)
        }
    }

    override suspend fun registerStore(storeId: String) {
        try {
            localDataSource.saveStoreId(storeId)
            val storeTopic = "store_$storeId"
            firebaseMessaging.subscribeToTopic(storeTopic).await()
        } catch (e: Exception) {
            throw NotificationException("Failed to register store: $storeId", e)
        }
    }
}

class NotificationException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

**Key points:**
- Implements `NotificationRepository` interface
- Uses `FirebaseMessaging` for topic subscribe/unsubscribe
- `registerStore()` saves store ID locally and subscribes to `store_{storeId}` topic
- Uses `kotlinx.coroutines.tasks.await()` to convert Firebase Task to suspend function

---

## 4.4 Notification Channel Manager

**File:** `app/src/main/java/com/autopart/ordermgmt/data/util/NotificationChannelManager.kt`

```kotlin
package com.autopart.ordermgmt.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

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
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getChannelId(): String = CHANNEL_ID
}
```

**Key points:**
- Creates a high-importance notification channel on Android O+
- Channel sound is set to `null` because sound is handled by `NotificationSoundService`
- Skips re-creation if channel already exists (channel settings are immutable once created)

---

## 4.5 Firebase Messaging Service

**File:** `app/src/main/java/com/autopart/ordermgmt/data/remote/firebase/FirebaseMessagingService.kt`

```kotlin
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
        
        remoteMessage.data.isNotEmpty().let {
            handleDataMessage(remoteMessage.data)
        }
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
    }
}
```

**Key points:**
- `@AndroidEntryPoint` enables Hilt injection in this service
- Handles both notification payloads and data payloads
- Creates click and delete PendingIntents using `NotificationActionReceiver`
- Triggers `NotificationSoundService` on each notification posted
- Saves new FCM tokens via repository on `onNewToken()`
- Registered in manifest with `com.google.firebase.MESSAGING_EVENT` intent filter

---

## 4.6 Notification Action Receiver

**File:** `app/src/main/java/com/autopart/ordermgmt/data/remote/firebase/NotificationActionReceiver.kt`

```kotlin
package com.autopart.ordermgmt.data.remote.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.autopart.ordermgmt.MainActivity

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
```

**Key points:**
- Handles notification click: stops sound, launches `MainActivity` with `order_id` extra
- Handles notification dismiss: decrements active notification count to stop sound
- Registered in manifest as a `<receiver>`

---

## 4.7 Notification Sound Service

**File:** `app/src/main/java/com/autopart/ordermgmt/data/remote/firebase/NotificationSoundService.kt`

```kotlin
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
```

**Key points:**
- Foreground service with `mediaPlayback` type
- Loops `notification_sound.mp3` from `res/raw/` using `MediaPlayer`
- Uses `AtomicInteger` to track active notification count
- Starts when a notification is posted, stops when all notifications are dismissed/clicked
- Creates its own low-importance notification channel for the foreground service notification
- Registered in manifest with `foregroundServiceType="mediaPlayback"`

---

## 4.8 Hilt DI Module

**File:** `app/src/main/java/com/autopart/ordermgmt/di/NotificationModule.kt`

```kotlin
package com.autopart.ordermgmt.di

import com.autopart.ordermgmt.data.repository.NotificationRepository
import com.autopart.ordermgmt.data.repository.NotificationRepositoryImpl
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging {
            return FirebaseMessaging.getInstance()
        }
    }
}
```

**Key points:**
- `@Binds` maps `NotificationRepositoryImpl` to `NotificationRepository` interface
- `@Provides` supplies singleton `FirebaseMessaging` instance
- Installed in `SingletonComponent` for app-wide scope

---

## 4.9 Domain Use Cases

### GetFCMTokenUseCase

**File:** `app/src/main/java/com/autopart/ordermgmt/domain/usecase/GetFCMTokenUseCase.kt`

```kotlin
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFCMTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<String?> {
        return notificationRepository.getFCMToken()
    }
}
```

### RegisterStoreUseCase

**File:** `app/src/main/java/com/autopart/ordermgmt/domain/usecase/RegisterStoreUseCase.kt`

```kotlin
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import javax.inject.Inject

class RegisterStoreUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(storeId: String) {
        notificationRepository.registerStore(storeId)
    }
}
```

### SubscribeToTopicUseCase

**File:** `app/src/main/java/com/autopart/ordermgmt/domain/usecase/SubscribeToTopicUseCase.kt`

```kotlin
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import javax.inject.Inject

class SubscribeToTopicUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(topic: String) {
        notificationRepository.subscribeToTopic(topic)
    }
}
```

### UnsubscribeFromTopicUseCase

**File:** `app/src/main/java/com/autopart/ordermgmt/domain/usecase/UnsubscribeFromTopicUseCase.kt`

```kotlin
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import javax.inject.Inject

class UnsubscribeFromTopicUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(topic: String) {
        notificationRepository.unsubscribeFromTopic(topic)
    }
}
```

---

## 4.10 Notification ViewModel

**File:** `app/src/main/java/com/autopart/ordermgmt/presentation/viewmodel/NotificationViewModel.kt`

```kotlin
package com.autopart.ordermgmt.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autopart.ordermgmt.domain.usecase.GetFCMTokenUseCase
import com.autopart.ordermgmt.domain.usecase.SubscribeToTopicUseCase
import com.autopart.ordermgmt.domain.usecase.UnsubscribeFromTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
    private val subscribeToTopicUseCase: SubscribeToTopicUseCase,
    private val unsubscribeFromTopicUseCase: UnsubscribeFromTopicUseCase
) : ViewModel() {

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeFCMToken()
    }

    private fun observeFCMToken() {
        viewModelScope.launch {
            getFCMTokenUseCase().collect { token ->
                _fcmToken.value = token
            }
        }
    }

    fun subscribeToTopic(topic: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                subscribeToTopicUseCase(topic)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to subscribe to topic"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unsubscribeFromTopic(topic: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                unsubscribeFromTopicUseCase(topic)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to unsubscribe from topic"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
```

---

## Architecture Overview

```
com.autopart.ordermgmt/
├── data/
│   ├── local/datasource/
│   │   └── NotificationLocalDataSource.kt     # DataStore for FCM token & store ID
│   ├── remote/firebase/
│   │   ├── FirebaseMessagingService.kt         # Receives FCM messages, shows notifications
│   │   ├── NotificationActionReceiver.kt       # Handles click/dismiss actions
│   │   └── NotificationSoundService.kt         # Foreground service for looping alert sound
│   ├── repository/
│   │   ├── NotificationRepository.kt           # Interface
│   │   └── NotificationRepositoryImpl.kt       # Firebase + DataStore implementation
│   └── util/
│       └── NotificationChannelManager.kt       # Notification channel creation
├── di/
│   └── NotificationModule.kt                   # Hilt DI module
├── domain/usecase/
│   ├── GetFCMTokenUseCase.kt
│   ├── RegisterStoreUseCase.kt
│   ├── SubscribeToTopicUseCase.kt
│   └── UnsubscribeFromTopicUseCase.kt
└── presentation/viewmodel/
    └── NotificationViewModel.kt                # UI state for notifications
```

## Notification Flow

```
FCM Message Received
    ↓
OrderManagementFirebaseMessagingService.onMessageReceived()
    ↓
sendNotification() → builds NotificationCompat with click/delete intents
    ↓
NotificationSoundService.notifyNotificationPosted() → starts foreground service, loops sound
    ↓
User clicks notification → NotificationActionReceiver → launches MainActivity with order_id
    ↓
NotificationSoundService.notifyNotificationDismissed() → stops sound when count reaches 0
```

---

## Summary

After this step you should have:
- `NotificationLocalDataSource` - DataStore persistence for FCM token and store ID
- `NotificationRepository` / `NotificationRepositoryImpl` - Firebase topic management
- `NotificationChannelManager` - Notification channel creation (importance high, silent sound)
- `OrderManagementFirebaseMessagingService` - FCM message handler with Hilt injection
- `NotificationActionReceiver` - Click/dismiss broadcast receiver
- `NotificationSoundService` - Looping alert sound foreground service with AtomicInteger tracking
- `NotificationModule` - Hilt DI bindings
- 4 use cases: GetFCMToken, RegisterStore, SubscribeToTopic, UnsubscribeFromTopic
- `NotificationViewModel` - UI state management for FCM token and topic operations
