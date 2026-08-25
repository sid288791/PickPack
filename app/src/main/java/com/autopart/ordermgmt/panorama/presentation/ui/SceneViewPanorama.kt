package com.autopart.ordermgmt.panorama.presentation.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.autopart.ordermgmt.panorama.domain.model.ItemLocation
import com.autopart.ordermgmt.panorama.domain.model.PanoramaImage

/**
 * SceneView-based 360 Panorama View with camera focus animation
 * For now, delegates to the existing 2D Panorama360View with zoom animation
 * Full 3D SceneView integration requires proper 3D models and texture setup
 */
@Composable
fun SceneViewPanorama(
    panoramaImage: PanoramaImage,
    modifier: Modifier = Modifier,
    onMarkerSelected: (ItemLocation) -> Unit = {}
) {
    var focusedItemId by remember { mutableStateOf<String?>(null) }
    var focusedItem by remember { mutableStateOf<ItemLocation?>(null) }
    
    val scale by animateFloatAsState(
        targetValue = if (focusedItemId != null) 1.3f else 1f,
        label = "camera_zoom"
    )
    
    // Calculate offset to center the focused item
    val offsetX = remember(focusedItem?.yaw) {
        focusedItem?.let { item ->
            // Convert yaw to horizontal offset (yaw -180 to 180 maps to screen width)
            val normalizedYaw = (item.yaw + 180f) / 360f
            val screenWidth = 1f // normalized
            val targetX = normalizedYaw * screenWidth
            val centerOffset = 0.5f - targetX
            (centerOffset * 300).dp // Adjust multiplier as needed
        } ?: 0.dp
    }
    
    val animatedOffsetX by animateDpAsState(
        targetValue = offsetX,
        label = "camera_pan"
    )
    
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(androidx.compose.ui.graphics.RectangleShape)
        ) {
            Panorama360View(
                panoramaImage = panoramaImage,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .offset(x = animatedOffsetX),
                isFullScreen = true,
                onMarkerSelected = { item ->
                    // Toggle focus: if same item clicked, unfocus; if different item, switch focus
                    focusedItemId = if (focusedItemId == item.id) null else item.id
                    focusedItem = if (focusedItemId == item.id) null else item
                    onMarkerSelected(item)
                }
            )
        }
    }
}
