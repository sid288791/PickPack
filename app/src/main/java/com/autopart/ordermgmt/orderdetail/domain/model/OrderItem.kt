package com.autopart.ordermgmt.orderdetail.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model for Order
 * Represents the complete order response from the getOrder API
 */
@Parcelize
data class Order(
    val id: Int,
    val orderNumber: String,
    val customerId: Int,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val shippingAddress: String,
    val billingAddress: String,
    val orderStatus: String,
    val totalAmount: Double,
    val currency: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItem>
) : Parcelable

/**
 * Domain model for Order Item
 * Represents an item in an order with real API response structure
 */
@Parcelize
data class OrderItem(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val location: ItemLocation,
    val taskId: String,
    val createdAt: String,
    val updatedAt: String,
    val isExpanded: Boolean = false
) : Parcelable

/**
 * Domain model for Item Location
 * Represents the location of an item in the warehouse
 */
@Parcelize
data class ItemLocation(
    val aisle: String,
    val shelf: String,
    val bin: String,
    val yaw: Double,
    val pitch: Double
) : Parcelable
