package com.autopart.ordermgmt.panorama.presentation.util

import com.autopart.ordermgmt.panorama.domain.model.ItemLocation
import io.github.sceneview.math.Position

/**
 * Utility class for converting yaw/pitch coordinates to 3D positions
 * Used for plotting markers on 360 panorama sphere
 */
object PositionUtils {
    
    /**
     * Convert yaw/pitch coordinates to 3D position on sphere surface
     * @param yaw Horizontal rotation in degrees
     * @param pitch Vertical rotation in degrees  
     * @param radius Sphere radius
     * @return 3D position coordinates
     */
    fun calculatePosition(
        yaw: Float,
        pitch: Float,
        radius: Float
    ): Position {
        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())

        val x = radius * kotlin.math.cos(pitchRad) * kotlin.math.sin(yawRad)
        val y = radius * kotlin.math.sin(pitchRad)
        val z = radius * kotlin.math.cos(pitchRad) * kotlin.math.cos(yawRad)

        return Position(
            x.toFloat(),
            y.toFloat(),
            z.toFloat()
        )
    }
    
    /**
     * Convert ItemLocation to 3D position
     * @param itemLocation Item with yaw/pitch coordinates
     * @param radius Sphere radius (default 18f for markers inside sphere)
     * @return 3D position coordinates
     */
    fun itemLocationToPosition(
        itemLocation: ItemLocation,
        radius: Float = 18f
    ): Position {
        return calculatePosition(
            itemLocation.yaw,
            itemLocation.pitch,
            radius
        )
    }
}
