/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
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

/**
 * Notification Local Data Source
 * Follows Single Responsibility Principle - handles local notification data storage
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
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
