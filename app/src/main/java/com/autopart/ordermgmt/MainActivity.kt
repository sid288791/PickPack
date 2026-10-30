/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
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
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
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
        val orderId = intent.getStringExtra("order_id")
        currentOrderId = orderId
        Log.d("MainActivity", "ORDER_IDDDDDD: $orderId")
        
        // Auto-register with static store -1
        lifecycleScope.launch {
            try {
                registerStoreUseCase("-1")
                Log.d("AutoRegister", "Successfully registered to store -1")
            } catch (e: Exception) {
                Log.e("AutoRegister", "Registration failed: ${e.message}")
            }
        }
        
        // Get and print FCM token (with Google Play Services check and retry)
        getFCMTokenWithRetry()
        
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
                        OrderHistoryScreen(navController = navController, orderIdFromNotification = orderId)
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
        // Handle new intent when app is already running
        val orderId = intent.getStringExtra("order_id")
        Log.d("MainActivity", "ORDER_IDDDDDD: $orderId")
        orderId?.let {
            currentOrderId = it
            // Navigate to order history screen with order ID
            navController?.navigate("order_history?orderId=$it")
        }
    }
    
    private fun getFCMTokenWithRetry(maxRetries: Int = 3, attempt: Int = 0) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (status != ConnectionResult.SUCCESS) {
            Log.e("FCMToken", "Google Play Services not available: ${googleApiAvailability.getErrorString(status)}")
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.showErrorNotification(this, status)
            }
            if (attempt < maxRetries) {
                mainHandler.postDelayed({ getFCMTokenWithRetry(maxRetries, attempt + 1) }, 5000L)
            }
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCMToken", "Current FCM Token: $token")
            } else {
                Log.e("FCMToken", "Failed to get FCM token (attempt ${attempt + 1}/$maxRetries)", task.exception)
                if (attempt < maxRetries) {
                    mainHandler.postDelayed({ getFCMTokenWithRetry(maxRetries, attempt + 1) }, 5000L)
                }
            }
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
                    // Show rationale to user
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
