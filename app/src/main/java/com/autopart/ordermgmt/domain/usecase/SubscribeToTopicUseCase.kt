/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import javax.inject.Inject

/**
 * Subscribe to Topic Use Case
 * Follows Single Responsibility Principle - handles topic subscription logic
 */
class SubscribeToTopicUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(topic: String) {
        notificationRepository.subscribeToTopic(topic)
    }
}
