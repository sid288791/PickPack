/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.orderdetail.data.repository

import com.autopart.networkclient.domain.repository.NetworkService
import com.autopart.networkclient.domain.usecase.ApiUseCase
import com.autopart.ordermgmt.orderdetail.data.remote.AdvanceOrderRequest
import com.autopart.ordermgmt.orderdetail.data.remote.GetOrderRequest
import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.model.OrderItem
import com.autopart.ordermgmt.orderdetail.domain.model.ItemLocation
import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of OrderRepository
 * Follows Clean Architecture - data layer implements the domain contract
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(

) : OrderRepository {
    private val apiUseCase: ApiUseCase by lazy { ApiUseCase() }
    internal val networkService = NetworkService()

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val BASE_URL = "http://192.168.31.78:8080"
        private const val TAG_GET_ORDER = "GetOrder"
        private const val TAG_ADVANCE_ORDER = "AdvanceOrder"
    }

    override suspend fun getOrder(orderId: String?): Order? {
        return try {
            System.out.print("GetOrderUseCase invoked with orderId: $orderId")
            val responseJson = fetchOrder(orderId ?: return null)
            print("Response_from_API: $responseJson")
            responseJson?.let {
                val dto = json.decodeFromString<OrderResponseDto>(it)
                mapToOrder(dto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            System.out.println("HTTP_ERROR: ${e.javaClass.simpleName} - ${e.message}")
            null
        }
    }

    override suspend fun orderStatus(orderId: String, employeeId: String): StoreTaskAdvanceResponse? {
        return try {
            val responseJson = fetchAdvanceOrder(orderId, employeeId) ?: return null
            val response = json.decodeFromString<AdvanceOrderDto>(responseJson)
            StoreTaskAdvanceResponse(
                id = response.id,
                taskNumber = response.taskNumber,
                status = response.status,
                nextAction = response.nextAction
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchOrder(orderId: String): String? {
        val request = GetOrderRequest(baseUrl = BASE_URL)
        request.buildUrl(orderId)
        return networkService.processNonEssentialApiResponse(
            apiUseCase.execute(request),
            TAG_GET_ORDER
        )
    }

    private suspend fun fetchAdvanceOrder(orderId: String, employeeId: String): String? {
        val request = AdvanceOrderRequest(baseUrl = BASE_URL)
        request.buildUrl(orderId, employeeId)
        return networkService.processNonEssentialApiResponse(
            apiUseCase.execute(request),
            TAG_ADVANCE_ORDER
        )
    }

    private fun mapToOrder(dto: OrderResponseDto): Order {
        val items = dto.items?.map { itemDto ->
            OrderItem(
                id = (itemDto.id ?: 0).toInt(),
                orderId = (itemDto.orderId ?: 0).toInt(),
                productId = (itemDto.productId ?: 0).toInt(),
                productName = itemDto.productName ?: "",
                sku = itemDto.sku ?: "",
                quantity = itemDto.quantity ?: 0,
                unitPrice = itemDto.unitPrice ?: 0.0,
                totalPrice = itemDto.totalPrice ?: 0.0,
                location = ItemLocation(
                    aisle = itemDto.location?.aisle ?: "",
                    shelf = itemDto.location?.shelf ?: "",
                    bin = itemDto.location?.bin ?: "",
                    yaw = itemDto.location?.yaw ?: 0.0,
                    pitch = itemDto.location?.pitch ?: 0.0
                ),
                createdAt = itemDto.createdAt ?: "",
                updatedAt = itemDto.updatedAt ?: "",
                taskId = itemDto.taskId ?: ""
            )
        } ?: emptyList()

        return Order(
            id = (dto.id ?: 0).toInt(),
            orderNumber = dto.orderNumber ?: "",
            customerId = (dto.customerId ?: 0).toInt(),
            customerName = dto.customerName ?: "",
            customerEmail = dto.customerEmail ?: "",
            customerPhone = dto.customerPhone ?: "",
            shippingAddress = dto.shippingAddress ?: "",
            billingAddress = dto.billingAddress ?: "",
            orderStatus = dto.orderStatus ?: "UNKNOWN",
            totalAmount = dto.totalAmount ?: 0.0,
            currency = dto.currency ?: "USD",
            paymentMethod = dto.paymentMethod ?: "",
            paymentStatus = dto.paymentStatus ?: "",
            createdAt = dto.createdAt ?: "",
            updatedAt = dto.updatedAt ?: "",
            items = items
        )
    }
}

@Serializable
private data class AdvanceOrderDto(
    val id: Int? = null,
    val taskNumber: String? = null,
    val status: String? = null,
    val nextAction: String? = null
)

@Serializable
private data class OrderResponseDto(
    val id: Int? = null,
    val orderNumber: String? = null,
    val customerId: Int? = null,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val shippingAddress: String? = null,
    val billingAddress: String? = null,
    val orderStatus: String? = null,
    val totalAmount: Double? = null,
    val currency: String? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val items: List<OrderItemDto>? = null
)

@Serializable
private data class OrderItemDto(
    val id: Int? = null,
    val orderId: Int? = null,
    val productId: Int? = null,
    val productName: String? = null,
    val sku: String? = null,
    val quantity: Int? = null,
    val unitPrice: Double? = null,
    val totalPrice: Double? = null,
    val location: ItemLocationDto? = null,
    val taskId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
private data class ItemLocationDto(
    val aisle: String? = null,
    val shelf: String? = null,
    val bin: String? = null,
    val yaw: Double? = null,
    val pitch: Double? = null
)
