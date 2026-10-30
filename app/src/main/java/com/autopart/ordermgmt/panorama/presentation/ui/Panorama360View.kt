/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.panorama.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.autopart.ordermgmt.R
import com.autopart.ordermgmt.panorama.domain.model.ItemLocation
import com.autopart.ordermgmt.panorama.domain.model.PanoramaImage

/**
 * Resolves the drawable resource id for the given panorama image name.
 * Falls back to [R.drawable.panorama_dummy] if no matching drawable is found,
 * allowing each store/rack to have its own panorama image by simply adding a
 * drawable with the same name as the `imageUrl` value in panorama_data.json
 * (e.g. "store_rack_view" -> res/drawable/store_rack_view.jpg).
 */
private fun resolvePanoramaDrawable(context: android.content.Context, imageUrl: String): Int {
    val drawableName = imageUrl.substringBeforeLast(".")
    val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.paronama
}

/**
 * 360 Panorama View
 * Displays the panorama image as background with markers plotted dynamically
 * at their yaw/pitch positions (equirectangular projection), similar to an
 * in-store 360 view with pin markers on aisles/racks.
 */
@Composable
fun Panorama360View(
    panoramaImage: PanoramaImage,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    onMarkerSelected: (ItemLocation) -> Unit = {}
) {
    var selectedItem by remember(panoramaImage.id) { mutableStateOf<ItemLocation?>(null) }
    val context = LocalContext.current

    if (isFullScreen) {
        // Full screen version without Card wrapper
        BoxWithConstraints(
            modifier = modifier.fillMaxSize()
        ) {
            val boxWidth = maxWidth
            val boxHeight = maxHeight

            // Background 360 panorama image - dynamically resolved per store/rack
            val panoramaDrawableRes = remember(panoramaImage.imageUrl) {
                resolvePanoramaDrawable(context, panoramaImage.imageUrl)
            }
            Image(
                painter = painterResource(id = panoramaDrawableRes),
                contentDescription = panoramaImage.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Plot each item marker at its yaw/pitch position
            panoramaImage.itemLocations.forEach { item ->
                val (xOffset, yOffset) = calculateMarkerOffset(
                    yaw = item.yaw,
                    pitch = item.pitch,
                    boxWidth = boxWidth,
                    boxHeight = boxHeight
                )

                Box(
                    modifier = Modifier
                        .offset(x = xOffset - 14.dp, y = yOffset - 28.dp)
                ) {
                    MarkerPin(
                        selected = selectedItem?.id == item.id,
                        onClick = {
                            selectedItem = item
                            onMarkerSelected(item)
                            Toast.makeText(
                                context,
                                item.name,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Info overlay for selected marker
            selectedItem?.let { item ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = "ID: ${item.id}  •  Yaw: ${item.yaw}°  •  Pitch: ${item.pitch}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    } else {
        // Card version for normal use
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Background 360 panorama image - dynamically resolved per store/rack
                val panoramaDrawableRes = remember(panoramaImage.imageUrl) {
                    resolvePanoramaDrawable(context, panoramaImage.imageUrl)
                }
                Image(
                    painter = painterResource(id = panoramaDrawableRes),
                    contentDescription = panoramaImage.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Plot each item marker at its yaw/pitch position
                panoramaImage.itemLocations.forEach { item ->
                    val (xOffset, yOffset) = calculateMarkerOffset(
                        yaw = item.yaw,
                        pitch = item.pitch,
                        boxWidth = boxWidth,
                        boxHeight = boxHeight
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset - 14.dp, y = yOffset - 28.dp)
                    ) {
                        MarkerPin(
                            selected = selectedItem?.id == item.id,
                            onClick = {
                                selectedItem = item
                                Toast.makeText(
                                    context,
                                    item.name,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }

                // Info overlay for selected marker
                selectedItem?.let { item ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = "ID: ${item.id}  •  Yaw: ${item.yaw}°  •  Pitch: ${item.pitch}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = panoramaImage.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = panoramaImage.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MarkerPin(
    selected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.LocationOn,
        contentDescription = "Item marker",
        tint = if (selected) Color(0xFFFF5252) else Color(0xFF00BFA5),
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable {
                onClick()
            }
    )
}

/**
 * Maps yaw (-180..180) and pitch (-90..90) to a 2D pixel offset within the
 * given box dimensions, using an equirectangular projection. This makes the
 * marker position dynamic based on the item's yaw/pitch values.
 */
private fun calculateMarkerOffset(
    yaw: Float,
    pitch: Float,
    boxWidth: Dp,
    boxHeight: Dp
): Pair<Dp, Dp> {
    val normalizedYaw = ((yaw + 180f) / 360f).coerceIn(0f, 1f)
    val normalizedPitch = ((90f - pitch) / 180f).coerceIn(0f, 1f)

    val x = boxWidth * normalizedYaw
    val y = boxHeight * normalizedPitch

    return Pair(x, y)
}
