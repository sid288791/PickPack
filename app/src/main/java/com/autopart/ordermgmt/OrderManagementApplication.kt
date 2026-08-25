package com.autopart.ordermgmt

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * Order Management Application
 * Follows Single Responsibility Principle - provides application-level configuration
 * Enables Hilt dependency injection throughout the application
 */
@HiltAndroidApp
class OrderManagementApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("OrderManagementApp", "Firebase initialization failed", e)
        }
    }
}
