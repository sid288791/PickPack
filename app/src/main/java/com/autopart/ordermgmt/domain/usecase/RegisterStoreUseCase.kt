/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.domain.usecase

import com.autopart.ordermgmt.data.repository.NotificationRepository
import javax.inject.Inject

/**
 * Register Store Use Case
 * Follows Single Responsibility Principle - handles store registration logic
 * Subscribes device to store-specific topic for targeted notifications
 */
class RegisterStoreUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(storeId: String) {
        notificationRepository.registerStore(storeId)
    }
}
