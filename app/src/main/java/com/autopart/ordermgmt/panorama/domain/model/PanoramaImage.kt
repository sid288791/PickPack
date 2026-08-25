package com.autopart.ordermgmt.panorama.domain.model

/**
 * Domain model for Panorama Image
 * Represents a panoramic image that can be displayed in the panorama view with item locations
 */
data class PanoramaImage(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val itemLocations: List<ItemLocation> = emptyList(),
    val isExpanded: Boolean = false
)
