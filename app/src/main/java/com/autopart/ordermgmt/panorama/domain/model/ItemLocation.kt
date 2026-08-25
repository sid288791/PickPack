package com.autopart.ordermgmt.panorama.domain.model

/**
 * Domain model for Item Location in 360 panorama
 * Represents the position of an item in the panoramic view using yaw and pitch coordinates
 */
data class ItemLocation(
    val id: String,
    val name: String,
    val yaw: Float,
    val pitch: Float
)
