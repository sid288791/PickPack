/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get FCM Token Use Case
 * Follows Single Responsibility Principle - handles FCM token retrieval logic
 */
class GetFCMTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<String?> {
        return notificationRepository.getFCMToken()
    }
}
