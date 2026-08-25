package com.autopart.ordermgmt.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Notification Repository Interface
 * Follows Dependency Inversion Principle - depends on abstraction
 * Follows Interface Segregation Principle - focused on notification-related operations
 */
interface NotificationRepository {
    suspend fun saveFCMToken(token: String)
    fun getFCMToken(): Flow<String?>
    suspend fun subscribeToTopic(topic: String)
    suspend fun unsubscribeFromTopic(topic: String)
    suspend fun registerStore(storeId: String)
}
