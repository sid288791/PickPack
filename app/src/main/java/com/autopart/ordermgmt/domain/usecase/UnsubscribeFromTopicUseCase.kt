package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import javax.inject.Inject

/**
 * Unsubscribe from Topic Use Case
 * Follows Single Responsibility Principle - handles topic unsubscription logic
 */
class UnsubscribeFromTopicUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(topic: String) {
        notificationRepository.unsubscribeFromTopic(topic)
    }
}
