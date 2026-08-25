package com.autopart.ordermgmt.panorama.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.autopart.ordermgmt.orderdetail.domain.model.OrderItem
import com.autopart.ordermgmt.panorama.domain.model.ItemLocation
import com.autopart.ordermgmt.panorama.domain.model.PanoramaImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for Panorama View
 * Follows Clean Architecture - presentation layer manages UI state
 */
@HiltViewModel
class PanoramaViewModel @Inject constructor() : ViewModel() {
    private val _panoramaImages = MutableStateFlow<List<PanoramaImage>>(emptyList())
    val panoramaImages: StateFlow<List<PanoramaImage>> = _panoramaImages.asStateFlow()

    fun setPanoramaFromOrderItems(orderItems: List<OrderItem>) {
        val itemLocations = orderItems.map { orderItem ->
            ItemLocation(
                id = orderItem.sku,
                name = orderItem.productName,
                yaw = orderItem.location.yaw.toFloat(),
                pitch = orderItem.location.pitch.toFloat()
            )
        }
        val panoramaImage = PanoramaImage(
            id = "order_items",
            title = "Order Items Location",
            description = "360-degree view with order item locations",
            imageUrl = "panorama_dummy",
            itemLocations = itemLocations
        )
        _panoramaImages.value = listOf(panoramaImage)
    }

}
