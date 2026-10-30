/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.orderdetail.domain.usecase

import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case to get order
 * Follows Clean Architecture principles
 */
class GetOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String?): Order? {
        System.out.print("GetOrderUseCase invoked with orderId: $orderId")
        return orderRepository.getOrder(orderId)
    }
}
