package com.autopart.ordermgmt.orderdetail.domain.usecase

import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case to advance order task to next action
 * Follows Clean Architecture principles
 */
class AdvanceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, employeeId: String): StoreTaskAdvanceResponse? {
        return orderRepository.advanceOrder(orderId, employeeId)
    }
}
