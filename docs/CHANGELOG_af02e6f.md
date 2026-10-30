# Changelog — Commit `af02e6f`

**Branch:** `fetaure/notification_service_optimization`  
**Date:** 25 October 2026  
**Author:** singhVinayGB  
**Summary:** Fixed battery drain issue, updated unused code base, and fixed missing copyright headers.

---

## Step-by-Step Changes

### 1. Battery Drain Fix — `NotificationSoundService`

**File:** `app/.../data/remote/firebase/NotificationSoundService.kt`

| # | What was done | Why |
|---|---------------|-----|
| 1.1 | Added **30-second auto-stop timer** (`AUTO_STOP_DELAY_MS = 30_000L`). A `Handler` posts `autoStopRunnable` that stops the looping sound, removes the foreground service, and calls `stopSelf()`. | Prevents the sound from looping indefinitely and draining battery. |
| 1.2 | Added **`PARTIAL_WAKE_LOCK`** with auto-release (`acquireWakeLock` / `releaseWakeLock`). Wake lock is acquired when looping sound starts and released when it stops. Timeout = 35 s (30 s + 5 s buffer). | Keeps CPU alive just long enough for the sound to play, then auto-releases. |
| 1.3 | **Alarm stream fallback** — checks `AudioManager.STREAM_NOTIFICATION` volume; if it is 0, uses `AudioAttributes.USAGE_ALARM` instead of `USAGE_NOTIFICATION`. | Zebra enterprise devices often have notification volume set to 0 by device policy; alarm stream bypasses this. |
| 1.4 | Timer **resets** on every new notification start command (`handler.removeCallbacks` → `handler.postDelayed`). | Ensures back-to-back notifications extend the sound window instead of cutting it short. |
| 1.5 | `onDestroy()` now removes the auto-stop callback to prevent leaks. | Clean lifecycle management. |

---

### 2. Data-Only FCM Messages & Full-Screen Intent — `FirebaseMessagingService`

**File:** `app/.../data/remote/firebase/FirebaseMessagingService.kt`

| # | What was done | Why |
|---|---------------|-----|
| 2.1 | Uncommented and **enabled data-only message handling**. Replaced the old commented-out block with an active `if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty())` check. | Data-only messages with high priority are delivered even in Doze mode — critical for Zebra devices with aggressive battery management. |
| 2.2 | `handleDataMessage` now receives the `orderId` parameter and calls `sendNotification(title, body, orderId)`. | Ensures tapping a data-only notification navigates to the correct order. |
| 2.3 | Added `.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)` and `.setFullScreenIntent(clickPendingIntent, true)` to the notification builder. | Shows notification on lock screen and triggers a full-screen alert for high-priority orders. |

---

### 3. `USE_FULL_SCREEN_INTENT` Permission — `AndroidManifest.xml`

**File:** `app/src/main/AndroidManifest.xml`

| # | What was done | Why |
|---|---------------|-----|
| 3.1 | Added `<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />`. | Required by Android for `setFullScreenIntent()` to work on API 34+. |

---

### 4. Network Layer Migration — Replaced Ktor with In-House `NetworkService`

#### 4a. New API Request Classes (Added)

| File | Description |
|------|-------------|
| `orderdetail/data/remote/GetOrderRequest.kt` | Implements `ApiRequest` for `GET /orders/{orderId}`. Builds URL from a base URL + orderId. |
| `orderdetail/data/remote/AdvanceOrderRequest.kt` | Implements `ApiRequest` for `POST /store-tasks/{orderId}/advance`. Builds URL and sets `AdvanceRequestBody` with `employeeId`. |

#### 4b. `OrderRepositoryImpl` Refactored

**File:** `app/.../orderdetail/data/repository/OrderRepositoryImpl.kt`

| # | What was done | Why |
|---|---------------|-----|
| 4b.1 | Removed `HttpClient` (Ktor) dependency from constructor injection. | Migrating away from Ktor to the in-house `NetworkService` / `ApiUseCase`. |
| 4b.2 | Added `ApiUseCase` (lazy), `NetworkService`, and `kotlinx.serialization.json.Json` as internal members. | These drive the new network calls. |
| 4b.3 | Rewrote `getOrder()` — now calls `fetchOrder(orderId)` which uses `GetOrderRequest` + `ApiUseCase.execute()` + `networkService.processNonEssentialApiResponse()`. Deserializes JSON response with `Json.decodeFromString`. | Consistent with project's own network client. |
| 4b.4 | Renamed `advanceOrder()` → `orderStatus()` and rewrote it to use `fetchAdvanceOrder()` → `AdvanceOrderRequest` + `ApiUseCase`. | Same migration; also renamed for clarity. |
| 4b.5 | Removed inline `AdvanceRequestDto` data class (moved to `AdvanceOrderRequest.kt` as `AdvanceRequestBody`). | Proper separation of concerns. |

#### 4c. `OrderModule` Cleaned Up

**File:** `app/.../orderdetail/data/di/OrderModule.kt`

| # | What was done | Why |
|---|---------------|-----|
| 4c.1 | Removed `companion object` that provided `HttpClient` via `@Provides`. Removed Ktor imports (`HttpClient`, `ContentNegotiation`, `json`, etc.). | No longer needed after migration to `NetworkService`. |

#### 4d. Domain Layer Updated

| File | Change |
|------|--------|
| `OrderRepository.kt` | Renamed `advanceOrder()` → `orderStatus()`. |
| `AdvanceOrderUseCase.kt` | Updated call from `orderRepository.advanceOrder()` → `orderRepository.orderStatus()`. |

---

### 5. Removed Unused Code — `PositionUtils.kt`

**File:** `app/.../panorama/presentation/util/PositionUtils.kt` — **DELETED**

| # | What was done | Why |
|---|---------------|-----|
| 5.1 | Deleted `PositionUtils` object (yaw/pitch → 3D position conversion). | Unused utility; was dead code in the panorama module. |

---

### 6. Copyright Header Fix — All Source Files

**Scope:** 50+ `.kt` files across `networkclient`, `ordermgmt`, `orderdetail`, `panorama`, and `ui/theme` packages.

| # | What was done | Why |
|---|---------------|-----|
| 6.1 | Added missing copyright block to files that had none: `/* Copyright (c) Koninklijke Advance N.V.,2026 — All rights reserved */`. | Compliance with corporate licensing requirements. |
| 6.2 | Fixed existing copyright in `networkclient` files: changed `Koninklijke Philips N.V.,2025` → `Koninklijke Advance N.V.,2025`. | Company name correction. |
| 6.3 | Removed duplicate copyright blocks in `ApiUseCase.kt`. | Cleanup. |

---

### 7. Added Documentation — `NOTIFICATION_USE_CASES.md`

**File:** `docs/NOTIFICATION_USE_CASES.md` (409 lines added)

Comprehensive documentation of all notification-related use cases for the Order Management system.

---

### 8. IDE / Config Cleanup

| File | Change |
|------|--------|
| `.idea/deploymentTargetSelector.xml` | Removed stale deployment target entries (33 lines deleted). |
| `.idea/misc.xml` | Minor update. |

---

## Files Summary

| Category | Files Changed | Files Added | Files Deleted |
|----------|:---:|:---:|:---:|
| Battery drain / Sound service | 1 | 0 | 0 |
| FCM / Notification | 1 | 0 | 0 |
| AndroidManifest | 1 | 0 | 0 |
| Network migration (Order) | 3 | 2 | 0 |
| Domain rename (`orderStatus`) | 2 | 0 | 0 |
| Removed dead code | 0 | 0 | 1 |
| Copyright headers | 50+ | 0 | 0 |
| Documentation | 0 | 1 | 0 |
| IDE config | 2 | 0 | 0 |
| **Total** | **63** | **3** | **1** |
