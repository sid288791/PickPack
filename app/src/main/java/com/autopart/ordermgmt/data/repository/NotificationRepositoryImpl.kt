/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.autopart.ordermgmt.data.local.datasource.NotificationLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Notification Repository Implementation
 * Follows Single Responsibility Principle - handles notification data operations
 * Follows Dependency Inversion Principle - implements interface, depends on abstractions
 */
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
            // Save store ID locally
            localDataSource.saveStoreId(storeId)
            // Subscribe to store-specific topic
            val storeTopic = "store_$storeId"
            firebaseMessaging.subscribeToTopic(storeTopic).await()
        } catch (e: Exception) {
            throw NotificationException("Failed to register store: $storeId", e)
        }
    }
}

class NotificationException(message: String, cause: Throwable? = null) : Exception(message, cause)
