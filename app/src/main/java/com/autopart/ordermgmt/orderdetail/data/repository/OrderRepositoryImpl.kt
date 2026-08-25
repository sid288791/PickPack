package com.autopart.ordermgmt.orderdetail.data.repository

import com.autopart.ordermgmt.orderdetail.domain.model.Order
import com.autopart.ordermgmt.orderdetail.domain.model.OrderItem
import com.autopart.ordermgmt.orderdetail.domain.model.ItemLocation
import com.autopart.ordermgmt.orderdetail.domain.model.StoreTaskAdvanceResponse
import com.autopart.ordermgmt.orderdetail.domain.repository.OrderRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of OrderRepository
 * Follows Clean Architecture - data layer implements the domain contract
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient
) : OrderRepository {

    companion object {
        private const val BASE_URL = "http://192.168.31.78:8080"
    }
    override suspend fun getOrder(orderId: String?): Order? {
        return try {
            System.out.print("GetOrderUseCase invoked with orderId: $orderId")
            val response: OrderResponseDto = httpClient.get("$BASE_URL/orders/$orderId").body()
            print("Response_from_API: $response")
            mapToOrder(response)
        } catch (e: Exception) {
            e.printStackTrace()
            System.out.println("HTTP_ERROR: ${e.javaClass.simpleName} - ${e.message}")
            null
        }
    }

    override suspend fun advanceOrder(orderId: String, employeeId: String): StoreTaskAdvanceResponse? {
        return try {
            val response: AdvanceOrderDto = httpClient.post("$BASE_URL/store-tasks/$orderId/advance") {
                contentType(ContentType.Application.Json)
                setBody(AdvanceRequestDto(employeeId = employeeId))
            }.body()

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
private data class AdvanceRequestDto(
    val employeeId: String
)

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
