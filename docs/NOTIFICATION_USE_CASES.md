# Notification Use Cases — PickPack Order Management

## Overview

The notification system handles incoming FCM (Firebase Cloud Messaging) push notifications for order alerts. It covers message reception, sound alerting, lock screen display, user interaction, and device compatibility for enterprise (Zebra) hardware.

---

## Architecture

```
FCM Server
    │
    ▼
OrderManagementFirebaseMessagingService   ← receives FCM messages
    │
    ├── sendNotification()                ← builds & posts Android notification
    │       │
    │       ├── NotificationChannelManager ← creates/manages notification channel
    │       └── NotificationSoundService   ← foreground service for looping alert sound
    │
    └── onNewToken()                      ← persists new FCM token via repository
            │
            └── NotificationRepository    ← saves token locally + remotely
                    │
                    ├── NotificationLocalDataSource  ← DataStore persistence (token, store ID)
                    └── FirebaseMessaging            ← topic subscribe/unsubscribe

NotificationActionReceiver               ← handles click / dismiss broadcast events
    │
    └── MainActivity                     ← deep-link navigation to order
            │
            ├── onCreate()               ← reads order_id from launch intent
            ├── onNewIntent()            ← handles order_id when app already running
            ├── checkNotificationPermission() ← POST_NOTIFICATIONS runtime permission
            ├── getFCMTokenWithRetry()    ← fetches token with GMS availability check
            └── registerStoreUseCase()   ← auto-registers store topic on startup

NotificationViewModel                    ← manages UI state (token, topic subscription)
NotificationModule (Hilt)                ← DI bindings for repository + FirebaseMessaging
```

---

## Use Cases

### UC-1: Receive FCM Notification Message

| Field       | Detail |
|-------------|--------|
| **Trigger** | FCM sends a message with `notification` payload |
| **Handler** | `OrderManagementFirebaseMessagingService.onMessageReceived()` |
| **Flow**    | Extract `order_id` from data → build notification with title/body → post to system tray |
| **File**    | `FirebaseMessagingService.kt` |

### UC-2: Receive FCM Data-Only Message (Doze-Safe)

| Field       | Detail |
|-------------|--------|
| **Trigger** | FCM sends a high-priority data-only message (no `notification` key) |
| **Handler** | `OrderManagementFirebaseMessagingService.handleDataMessage()` |
| **Flow**    | Extract `title`, `body`, `order_id` from data payload → call `sendNotification()` |
| **Why**     | Data-only messages with high priority bypass Android Doze mode — critical for Zebra enterprise devices with aggressive battery management |
| **File**    | `FirebaseMessagingService.kt` |

### UC-3: Looping Alert Sound on Notification

| Field       | Detail |
|-------------|--------|
| **Trigger** | `sendNotification()` calls `NotificationSoundService.notifyNotificationPosted()` |
| **Handler** | `NotificationSoundService` (foreground service) |
| **Flow**    | Start foreground service → acquire `PARTIAL_WAKE_LOCK` → play `res/raw/notification_sound.mp3` in loop via `MediaPlayer` |
| **Auto-stop** | Sound automatically stops after **30 seconds** (`AUTO_STOP_DELAY_MS`) to prevent battery drain |
| **Audio fallback** | If notification volume is 0 (common on Zebra MDM profiles), falls back to `USAGE_ALARM` stream |
| **File**    | `NotificationSoundService.kt` |

### UC-4: Stop Sound on Notification Click

| Field       | Detail |
|-------------|--------|
| **Trigger** | User taps the notification |
| **Handler** | `NotificationActionReceiver` receives `ACTION_NOTIFICATION_CLICKED` |
| **Flow**    | Decrement active count → if count reaches 0, stop `NotificationSoundService` → launch `MainActivity` with `order_id` extra |
| **File**    | `NotificationActionReceiver.kt` |

### UC-5: Stop Sound on Notification Dismiss (Swipe)

| Field       | Detail |
|-------------|--------|
| **Trigger** | User swipes away the notification |
| **Handler** | `NotificationActionReceiver` receives `ACTION_NOTIFICATION_DISMISSED` |
| **Flow**    | Decrement active count → if count reaches 0, stop `NotificationSoundService` |
| **File**    | `NotificationActionReceiver.kt` |

### UC-6: Notification Visible on Lock Screen

| Field       | Detail |
|-------------|--------|
| **Trigger** | Notification posted while device screen is locked |
| **Handled by** | `VISIBILITY_PUBLIC` on both notification builder and channel |
| **Behavior** | Full notification content (title + body) is shown on lock screen instead of "Contents hidden" |
| **File**    | `FirebaseMessagingService.kt`, `NotificationChannelManager.kt` |

### UC-7: Wake Screen When Off

| Field       | Detail |
|-------------|--------|
| **Trigger** | Notification posted while screen is completely off |
| **Handled by** | `setFullScreenIntent(clickPendingIntent, true)` on notification builder |
| **Behavior** | Android turns the screen on and shows a full-screen heads-up alert |
| **Permission** | `USE_FULL_SCREEN_INTENT` (required for Android 14+) |
| **File**    | `FirebaseMessagingService.kt`, `AndroidManifest.xml` |

### UC-8: FCM Token Refresh

| Field       | Detail |
|-------------|--------|
| **Trigger** | Firebase rotates the device FCM token |
| **Handler** | `OrderManagementFirebaseMessagingService.onNewToken()` |
| **Flow**    | Log new token → persist via `NotificationRepository.saveFCMToken()` on IO dispatcher |
| **File**    | `FirebaseMessagingService.kt` |

### UC-9: Subscribe / Unsubscribe to FCM Topic

| Field       | Detail |
|-------------|--------|
| **Trigger** | UI action (e.g., user toggles topic preference) |
| **Handler** | `NotificationViewModel.subscribeToTopic()` / `unsubscribeFromTopic()` |
| **Flow**    | ViewModel → Use Case → `NotificationRepository` → Firebase topic API |
| **State**   | Exposes `isLoading` and `error` StateFlows for UI feedback |
| **File**    | `NotificationViewModel.kt`, `SubscribeToTopicUseCase.kt`, `UnsubscribeFromTopicUseCase.kt` |

### UC-10: Observe FCM Token in UI

| Field       | Detail |
|-------------|--------|
| **Trigger** | ViewModel initialization |
| **Handler** | `NotificationViewModel.observeFCMToken()` |
| **Flow**    | Collects token Flow from `GetFCMTokenUseCase` → exposes as `fcmToken` StateFlow |
| **File**    | `NotificationViewModel.kt`, `GetFCMTokenUseCase.kt` |

### UC-11: Request POST_NOTIFICATIONS Runtime Permission (Android 13+)

| Field       | Detail |
|-------------|--------|
| **Trigger** | `MainActivity.onCreate()` |
| **Handler** | `MainActivity.checkNotificationPermission()` |
| **Flow**    | Check SDK ≥ TIRAMISU → check if already granted → show rationale if needed → launch permission request via `ActivityResultContracts.RequestPermission` |
| **Behavior** | Without this permission on Android 13+, notifications are silently dropped and never shown |
| **File**    | `MainActivity.kt` |

### UC-12: FCM Token Fetch with Google Play Services Retry

| Field       | Detail |
|-------------|--------|
| **Trigger** | `MainActivity.onCreate()` |
| **Handler** | `MainActivity.getFCMTokenWithRetry()` |
| **Flow**    | Check `GoogleApiAvailability` status → if unavailable, show error notification and retry up to 3 times (5s interval) → if available, call `FirebaseMessaging.getInstance().token` → log token |
| **Why**     | Google Play Services may not be immediately ready on device boot or Zebra cold-start; retry ensures the token is eventually fetched |
| **File**    | `MainActivity.kt` |

### UC-13: Auto-Register Store Topic on Startup

| Field       | Detail |
|-------------|--------|
| **Trigger** | `MainActivity.onCreate()` |
| **Handler** | `RegisterStoreUseCase` → `NotificationRepository.registerStore()` |
| **Flow**    | Save store ID locally via `NotificationLocalDataSource.saveStoreId()` → subscribe to FCM topic `store_{storeId}` → device receives store-specific push notifications |
| **Current** | Hardcoded to store ID `"-1"` (all-store broadcast) |
| **File**    | `MainActivity.kt`, `RegisterStoreUseCase.kt`, `NotificationRepositoryImpl.kt` |

### UC-14: Deep-Link to Order from Notification (Cold Start)

| Field       | Detail |
|-------------|--------|
| **Trigger** | User taps notification when app is not running |
| **Handler** | `NotificationActionReceiver` launches `MainActivity` with `order_id` extra |
| **Flow**    | `MainActivity.onCreate()` reads `intent.getStringExtra("order_id")` → passes to `OrderHistoryScreen(orderIdFromNotification)` → `LaunchedEffect` calls `orderDetailViewModel.loadOrder(orderId)` |
| **File**    | `NotificationActionReceiver.kt`, `MainActivity.kt`, `OrderHistoryScreen.kt` |

### UC-15: Deep-Link to Order from Notification (App Already Running)

| Field       | Detail |
|-------------|--------|
| **Trigger** | User taps notification when app is already in foreground/background |
| **Handler** | `MainActivity.onNewIntent()` |
| **Flow**    | Read `order_id` from new intent → update `currentOrderId` → navigate via `navController.navigate("order_history?orderId=$orderId")` → `OrderHistoryScreen` loads the order |
| **File**    | `MainActivity.kt`, `OrderHistoryScreen.kt` |

---

## Notification Channels

| Channel ID | Name | Importance | Sound | Visibility | Purpose |
|------------|------|------------|-------|------------|---------|
| `order_management_channel_v3` | Order Management | HIGH | Silent (handled by service) | PUBLIC | Main order notifications |
| `notification_sound_service_channel` | Alert Sound | LOW | None | — | Foreground service persistent notification |

---

## Android Permissions

| Permission | Purpose |
|------------|---------|
| `POST_NOTIFICATIONS` | Post notifications (Android 13+) |
| `WAKE_LOCK` | Keep CPU alive during sound playback |
| `FOREGROUND_SERVICE` | Run foreground service |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Foreground service type for audio |
| `USE_FULL_SCREEN_INTENT` | Wake screen on lock (Android 14+) |
| `INTERNET` | FCM connectivity |

---

## Manifest Components

| Component | Type | Purpose |
|-----------|------|---------|
| `OrderManagementFirebaseMessagingService` | Service | Receives FCM messages |
| `NotificationSoundService` | Service (foreground, mediaPlayback) | Loops alert sound |
| `NotificationActionReceiver` | BroadcastReceiver | Handles notification click/dismiss |

---

## Device Behavior Matrix

This section documents how the notification system behaves across every possible device state.

### 1. Normal Mode (Screen On, Unlocked, App in Foreground)

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | Immediate | `onMessageReceived()` called directly by GMS |
| **Notification display** | Heads-up popup at top of screen | `PRIORITY_HIGH` + `IMPORTANCE_HIGH` channel |
| **Sound** | Looping alert via `NotificationSoundService` | Foreground service + `MediaPlayer` |
| **Vibration** | Yes | `enableVibration(true)` on channel |
| **Click action** | Navigates to order via `onNewIntent()` | `NotificationActionReceiver` → `MainActivity` |
| **Auto-stop** | Sound stops after 30 seconds | `Handler.postDelayed(autoStopRunnable)` |

### 2. Normal Mode (Screen On, Unlocked, App in Background)

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | Immediate | GMS wakes the `FirebaseMessagingService` |
| **Notification display** | Heads-up popup | Same as foreground |
| **Sound** | Looping alert | `startForegroundService()` works from background service |
| **Click action** | Brings app to foreground, navigates to order | `FLAG_ACTIVITY_NEW_TASK` + `onNewIntent()` |

### 3. Normal Mode (Screen On, Unlocked, App Killed / Not Running)

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | Immediate | GMS restarts `FirebaseMessagingService` process |
| **Notification display** | Heads-up popup | Standard notification flow |
| **Sound** | Looping alert | Service starts fresh |
| **Click action** | Cold-starts app, loads order | `NotificationActionReceiver` → `MainActivity.onCreate()` reads `order_id` |

### 4. Lock Screen Mode (Screen On, Locked)

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | Immediate | GMS delivers regardless of lock state |
| **Notification display** | Full content visible on lock screen | `VISIBILITY_PUBLIC` on builder + channel `lockscreenVisibility` |
| **Sound** | Looping alert | Foreground service runs independently of lock state |
| **Vibration** | Yes | Channel setting |
| **Click action** | User must unlock first, then navigates to order | Standard Android lock-screen notification tap behavior |

### 5. Screen Off Mode (Display Completely Off)

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | Immediate (if not in Doze) | GMS high-priority messages wake device |
| **Screen wake** | Screen turns on with full-screen alert | `setFullScreenIntent(clickPendingIntent, true)` |
| **Notification display** | Full-screen heads-up over lock screen | `USE_FULL_SCREEN_INTENT` permission |
| **Sound** | Looping alert | `PARTIAL_WAKE_LOCK` keeps CPU alive for `MediaPlayer` |
| **WakeLock** | Auto-releases after 35 seconds | `acquire(AUTO_STOP_DELAY_MS + 5_000L)` |
| **Click action** | Wakes screen → unlock → navigates to order | `fullScreenIntent` PendingIntent |

### 6. Doze Mode (Android 6.0+)

Doze activates when device is stationary, screen off, and on battery for an extended period.

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM notification messages** | ⚠️ May be **delayed** until maintenance window | Android batches low-priority work in Doze |
| **FCM data-only high-priority** | ✅ Delivered **immediately** | `handleDataMessage()` processes data payloads; high-priority FCM bypasses Doze |
| **Notification display** | Shows immediately once `onMessageReceived()` fires | Standard `NotificationManager.notify()` is not blocked by Doze |
| **Sound service** | Starts immediately | `startForegroundService()` is allowed even in Doze |
| **WakeLock** | Keeps CPU alive for playback | `PARTIAL_WAKE_LOCK` prevents CPU sleep during sound loop |
| **Screen wake** | Screen turns on | `fullScreenIntent` works in Doze |

> **Server requirement:** Backend must send **data-only messages** with `"priority": "high"` for guaranteed Doze delivery:
> ```json
> {
>   "to": "<device_token>",
>   "priority": "high",
>   "data": {
>     "title": "New Order",
>     "body": "Order #123 received",
>     "order_id": "123"
>   }
> }
> ```

### 7. App Standby Mode (Android 6.0+)

App Standby restricts apps that haven't been used recently.

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | ✅ Delivered | FCM high-priority messages are exempt from App Standby restrictions |
| **Foreground service** | ✅ Starts normally | `startForegroundService()` is allowed |
| **Network access** | ✅ Available | FCM uses GMS process which is never in standby |
| **Notification** | ✅ Shows normally | Not restricted by App Standby |

### 8. Battery Saver Mode

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM delivery** | ✅ Delivered | High-priority FCM bypasses battery saver |
| **Foreground service** | ✅ Runs | Foreground services are not restricted by battery saver |
| **Sound** | ✅ Plays | `PARTIAL_WAKE_LOCK` + foreground service keeps audio alive |
| **Background restrictions** | N/A | Sound service is a foreground service, not background |

### 9. After Device Reboot

| Aspect | Behavior | How |
|--------|----------|-----|
| **FCM registration** | ✅ Automatic | GMS re-registers FCM token on boot; `onNewToken()` called if token changes |
| **Pending notifications** | Delivered once GMS initializes | Typically within seconds of boot |
| **FCM token** | May change | `onNewToken()` persists new token via `NotificationRepository.saveFCMToken()` |
| **GMS readiness** | Handled | `getFCMTokenWithRetry()` retries 3 times with 5s delay if GMS not ready |
| **Store topic** | Re-subscribed on app launch | `RegisterStoreUseCase` runs in `MainActivity.onCreate()` |

---

## Zebra Enterprise Device Compatibility

Zebra devices (TC52, TC72, TC21, EC50, MC93, etc.) run hardened Android with aggressive power management and enterprise MDM profiles.

### Zebra-Specific Challenges & Solutions

| Challenge | Impact | Solution in Code | File |
|-----------|--------|------------------|------|
| **Zebra PowerManager kills services** | Sound service terminated prematurely | `PARTIAL_WAKE_LOCK` with timed auto-release; `START_STICKY` for restart | `NotificationSoundService.kt` |
| **Aggressive Doze on Zebra firmware** | Notification-type FCM messages delayed/dropped | Data-only high-priority FCM messages bypass Doze completely | `FirebaseMessagingService.kt` |
| **Notification volume muted by MDM** | Sound plays on silent stream → user hears nothing | Checks `STREAM_NOTIFICATION` volume; falls back to `USAGE_ALARM` stream | `NotificationSoundService.kt` |
| **Screen off on cradle** | Some Zebra firmware enters Doze even while charging | `fullScreenIntent` wakes screen; `PARTIAL_WAKE_LOCK` keeps CPU | `FirebaseMessagingService.kt`, `NotificationSoundService.kt` |
| **GMS not ready on cold boot** | `FirebaseMessaging.getInstance().token` fails | `getFCMTokenWithRetry()` with 3 retries, 5s interval, GMS availability check | `MainActivity.kt` |
| **Enterprise lock screen** | Notification content hidden by device policy | `VISIBILITY_PUBLIC` + channel `lockscreenVisibility = PUBLIC` | `FirebaseMessagingService.kt`, `NotificationChannelManager.kt` |
| **App force-stopped by user/system** | `deleteIntent` broadcast never fires → sound loops indefinitely | 30-second `AUTO_STOP_DELAY_MS` timer auto-kills service regardless | `NotificationSoundService.kt` |
| **Restricted background execution** | Background services blocked on Android 12+ | Uses `startForegroundService()` (exempt from background restrictions) | `NotificationSoundService.kt` |

### Zebra Device Behavior Summary

| Device State | FCM Delivery | Notification | Sound | Screen Wake |
|-------------|-------------|--------------|-------|-------------|
| **Normal (screen on, unlocked)** | ✅ Immediate | ✅ Heads-up | ✅ Looping 30s | N/A |
| **Lock screen** | ✅ Immediate | ✅ Full content visible | ✅ Looping 30s | N/A |
| **Screen off** | ✅ Immediate | ✅ Full-screen alert | ✅ Looping 30s | ✅ Wakes screen |
| **Doze (on battery, stationary)** | ✅ Via high-priority data msg | ✅ Shows | ✅ Looping 30s | ✅ Wakes screen |
| **On cradle (charging)** | ✅ Immediate | ✅ Shows | ✅ Looping 30s | ✅ Wakes screen |
| **After reboot** | ✅ After GMS init (retry) | ✅ Shows | ✅ Looping 30s | ✅ Wakes screen |
| **App killed** | ✅ GMS restarts service | ✅ Shows | ✅ Looping 30s | ✅ Wakes screen |
| **MDM notification vol = 0** | ✅ Delivered | ✅ Shows | ✅ Via ALARM stream | ✅ Wakes screen |

### Recommended Zebra MDM Configuration

1. **Battery optimization whitelist** — Settings → Battery → Battery Optimization → PickPack → "Don't Optimize"
2. **StageNow/OEMConfig** — Push battery whitelist via MDM profile to all devices
3. **FCM server** — Send **data-only** messages with `"priority": "high"`
4. **Alarm volume** — Ensure alarm volume is non-zero on device profile (fallback audio stream)
5. **Notification permission** — Pre-grant `POST_NOTIFICATIONS` via MDM for Android 13+ devices
6. **Full-screen intent** — Pre-grant `USE_FULL_SCREEN_INTENT` via MDM for Android 14+ devices

---

## Data Layer

| Component | Responsibility |
|-----------|----------------|
| `NotificationRepository` (interface) | Abstraction for token, topic, and store operations |
| `NotificationRepositoryImpl` | Implements repository; delegates to local + Firebase |
| `NotificationLocalDataSource` | DataStore-backed persistence for FCM token and store ID |
| `NotificationModule` (Hilt) | DI bindings — binds `NotificationRepositoryImpl` → `NotificationRepository`, provides `FirebaseMessaging` singleton |
| `NotificationException` | Custom exception for topic/store registration failures |

---

## File Reference

| File | Path |
|------|------|
| FCM Service | `data/remote/firebase/FirebaseMessagingService.kt` |
| Sound Service | `data/remote/firebase/NotificationSoundService.kt` |
| Action Receiver | `data/remote/firebase/NotificationActionReceiver.kt` |
| Channel Manager | `data/util/NotificationChannelManager.kt` |
| ViewModel | `presentation/viewmodel/NotificationViewModel.kt` |
| Repository Interface | `data/repository/NotificationRepository.kt` |
| Repository Impl | `data/repository/NotificationRepositoryImpl.kt` |
| Local DataSource | `data/local/datasource/NotificationLocalDataSource.kt` |
| DI Module | `di/NotificationModule.kt` |
| Token Use Case | `domain/usecase/GetFCMTokenUseCase.kt` |
| Subscribe Use Case | `domain/usecase/SubscribeToTopicUseCase.kt` |
| Unsubscribe Use Case | `domain/usecase/UnsubscribeFromTopicUseCase.kt` |
| Register Store Use Case | `domain/usecase/RegisterStoreUseCase.kt` |
| MainActivity | `MainActivity.kt` |
| Order History Screen | `orderhistory/presentation/ui/screens/OrderHistoryScreen.kt` |
| Sound Resource | `res/raw/notification_sound.mp3` |
| Manifest | `AndroidManifest.xml` |
