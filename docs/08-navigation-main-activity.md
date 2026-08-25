# Step 8: Navigation & MainActivity

This step creates the `MainActivity` which serves as the app's entry point. It handles:
- Jetpack Compose Navigation with 3 routes
- Notification permission request (Android 13+)
- FCM token retrieval and logging
- Auto-registration to store topic
- Notification intent handling (order_id from push notifications)
- `onNewIntent` for receiving notification clicks while app is running

**Package:** `com.autopart.ordermgmt`

---

## 8.1 MainActivity

**File:** `app/src/main/java/com/autopart/ordermgmt/MainActivity.kt`

```kotlin
package com.autopart.ordermgmt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autopart.ordermgmt.data.repository.NotificationRepository
import com.autopart.ordermgmt.domain.usecase.RegisterStoreUseCase
import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderhistory.presentation.ui.screens.OrderHistoryScreen
import com.autopart.ordermgmt.orderdetail.presentation.ui.screens.OrderDetailFullScreen
import com.autopart.ordermgmt.panorama.presentation.ui.screens.PanoramaFullScreen
import com.autopart.ordermgmt.ui.theme.MyApplicationTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var registerStoreUseCase: RegisterStoreUseCase
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    private var navController: androidx.navigation.NavController? = null
    private var currentOrderId: String? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, notifications can be displayed
        } else {
            // Permission denied, notifications cannot be displayed
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()
        
        // Get order ID from notification intent
        val orderId = intent.getStringExtra("order_id") ?: "test1223"
        currentOrderId = orderId
        Log.d("MainActivity", "Order ID from intent: $orderId")
        
        // Auto-register with static store -1
        lifecycleScope.launch {
            try {
                registerStoreUseCase("-1")
                Log.d("AutoRegister", "Successfully registered to store -1")
            } catch (e: Exception) {
                Log.e("AutoRegister", "Registration failed: ${e.message}")
            }
        }
        
        // Get and print FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCMToken", "Current FCM Token: $token")
            } else {
                Log.e("FCMToken", "Failed to get FCM token", task.exception)
            }
        }
        
        // Also retrieve from local storage
        lifecycleScope.launch {
            notificationRepository.getFCMToken().collect { token ->
                token?.let {
                    Log.d("FCMToken", "Stored FCM Token: $it")
                }
            }
        }
        
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                // Store navController reference for use in onNewIntent
                this@MainActivity.navController = navController
                
                NavHost(
                    navController = navController,
                    startDestination = "order_history?orderId={orderId}",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("order_history?orderId={orderId}") { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: currentOrderId
                        OrderHistoryScreen(
                            navController = navController,
                            orderIdFromNotification = orderId
                        )
                    }
                    composable("order_detail_full") {
                        OrderDetailFullScreen(navController = navController)
                    }
                    composable("panorama_full_screen") {
                        PanoramaFullScreen(navController = navController)
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val orderId = intent.getStringExtra("order_id")
        Log.d("MainActivity", "Order ID from new intent: $orderId")
        orderId?.let {
            currentOrderId = it
            navController?.navigate("order_history?orderId=$it")
        }
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
```

---

## Key Points

### Hilt Injection
- `@AndroidEntryPoint` enables field injection in this Activity
- Injects `RegisterStoreUseCase` and `NotificationRepository`

### Notification Permission
- Checks `POST_NOTIFICATIONS` permission on Android 13+ (TIRAMISU)
- Uses `ActivityResultContracts.RequestPermission()` to request permission
- Three cases: already granted, should show rationale, first-time request

### FCM Token Handling
- Retrieves FCM token asynchronously via `FirebaseMessaging.getInstance().token`
- Logs current token and stored token from DataStore
- Token is saved to DataStore by `FirebaseMessagingService.onNewToken()`

### Auto-Registration
- Automatically registers to store `-1` on app launch via `RegisterStoreUseCase`
- This subscribes the device to `store_-1` FCM topic for receiving push notifications

### Notification Intent Handling
- On `onCreate`: extracts `order_id` from intent extras (set by `NotificationActionReceiver`)
- Falls back to `"test1223"` as default order ID
- On `onNewIntent`: handles notification clicks while app is already running, navigates to order history with new order ID

### Navigation Routes

| Route | Composable | Purpose |
|-------|-----------|---------|
| `order_history?orderId={orderId}` | `OrderHistoryScreen` | Home screen, displays order summary |
| `order_detail_full` | `OrderDetailFullScreen` | Order detail with items, bill, 360° view |
| `panorama_full_screen` | `PanoramaFullScreen` | Full-screen immersive 360° panorama |

### ViewModel Sharing
- `OrderDetailViewModel` and `PanoramaViewModel` are shared across `OrderHistoryScreen` and `OrderDetailFullScreen` via activity-level `ViewModelStoreOwner` (`LocalActivity.current`)
- This allows the Home screen to load order data and the Detail screen to display it without re-fetching

---

## Navigation Flow

```
App Launch
    ↓
MainActivity.onCreate()
    ├── checkNotificationPermission()
    ├── get order_id from intent (or default "test1223")
    ├── registerStoreUseCase("-1")  → subscribes to FCM topic
    ├── get FCM token → log
    └── setContent → NavHost
        ↓
        startDestination: "order_history?orderId={orderId}"
        ↓
        OrderHistoryScreen
            ├── loadOrder(orderId) via OrderDetailViewModel
            └── "View order details" click → navigate("order_detail_full")
                ↓
                OrderDetailFullScreen
                    ├── Shows order info, items, bill summary
                    ├── 360° panorama (expandable card)
                    └── FAB → navigate("panorama_full_screen")
                        ↓
                        PanoramaFullScreen (landscape, immersive)
                            └── Close → navigateUp()

Notification Click (app in background)
    ↓
NotificationActionReceiver → startActivity(MainActivity, order_id=xxx)
    ↓
MainActivity.onCreate() or onNewIntent()
    ↓
Navigate to "order_history?orderId=xxx"
```

---

## Summary

After this step you should have:
- `MainActivity` with `@AndroidEntryPoint` for Hilt injection
- Notification permission request flow (Android 13+)
- FCM token retrieval and logging
- Auto-registration to store topic on launch
- Jetpack Compose Navigation with 3 routes
- Notification intent handling for `order_id` (both `onCreate` and `onNewIntent`)
- Edge-to-edge display enabled
- Shared ViewModel scope across screens via activity-level `ViewModelStoreOwner`

This completes the final step. All 8 steps together create an exact replica of the AutoPart Order Management Android app.
