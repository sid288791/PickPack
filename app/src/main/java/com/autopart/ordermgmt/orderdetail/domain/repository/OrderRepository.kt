package com.autopart.ordermgmt.orderdetail.domain.repository

import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse

/**
 * Repository interface for Order data
 * Follows Clean Architecture - domain layer defines the contract
 */
interface OrderRepository {
    suspend fun getOrder(orderId: String?): Order?
    suspend fun advanceOrder(orderId: String, employeeId: String): StoreTaskAdvanceResponse?
}
