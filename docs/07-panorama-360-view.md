# Step 7: Panorama 360° View

This step creates the 360-degree panorama feature: domain models for item locations and panorama images, a 2D equirectangular panorama view with dynamic markers, a SceneView-based panorama with zoom/pan animation, a full-screen immersive panorama screen, a PositionUtils utility for 3D coordinate conversion, and a PanoramaViewModel.

**Package:** `com.autopart.ordermgmt.panorama`

---

## 7.1 Domain Models

### ItemLocation (Panorama)

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/domain/model/ItemLocation.kt`

```kotlin
package com.autopart.ordermgmt.panorama.domain.model

data class ItemLocation(
    val id: String,
    val name: String,
    val yaw: Float,
    val pitch: Float
)
```

**Key points:**
- `yaw` — horizontal angle in degrees (-180 to 180)
- `pitch` — vertical angle in degrees (-90 to 90)
- Used to plot markers on the 360° panorama

### PanoramaImage

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/domain/model/PanoramaImage.kt`

```kotlin
package com.autopart.ordermgmt.panorama.domain.model

data class PanoramaImage(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val itemLocations: List<ItemLocation> = emptyList(),
    val isExpanded: Boolean = false
)
```

**Key points:**
- `imageUrl` — resolved to a drawable resource name (e.g., `"panorama_dummy"` → `R.drawable.paronama`)
- `itemLocations` — list of markers to plot on the panorama

---

## 7.2 PanoramaViewModel

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/presentation/viewmodel/PanoramaViewModel.kt`

```kotlin
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
```

**Key points:**
- `@HiltViewModel` with no dependencies (empty constructor)
- `setPanoramaFromOrderItems()` converts `OrderItem` list to panorama `ItemLocation` markers
- Maps `orderItem.location.yaw/pitch` (Double) to `ItemLocation.yaw/pitch` (Float)
- Exposes `panoramaImages` as `StateFlow<List<PanoramaImage>>`

---

## 7.3 PositionUtils (3D Coordinate Conversion)

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/presentation/util/PositionUtils.kt`

```kotlin
package com.autopart.ordermgmt.panorama.presentation.util

import com.autopart.ordermgmt.panorama.domain.model.ItemLocation
import io.github.sceneview.math.Position

object PositionUtils {
    
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

        return Position(x.toFloat(), y.toFloat(), z.toFloat())
    }
    
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
```

**Key points:**
- Converts yaw/pitch spherical coordinates to 3D Cartesian (x, y, z) on a sphere surface
- Uses SceneView's `Position` class
- Default radius of 18f for markers inside a 3D sphere
- Used for potential 3D SceneView integration

---

## 7.4 Panorama360View (2D Equirectangular)

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/presentation/ui/Panorama360View.kt`

```kotlin
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

private fun resolvePanoramaDrawable(context: android.content.Context, imageUrl: String): Int {
    val drawableName = imageUrl.substringBeforeLast(".")
    val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.paronama
}

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
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val boxWidth = maxWidth
            val boxHeight = maxHeight

            val panoramaDrawableRes = remember(panoramaImage.imageUrl) {
                resolvePanoramaDrawable(context, panoramaImage.imageUrl)
            }
            Image(
                painter = painterResource(id = panoramaDrawableRes),
                contentDescription = panoramaImage.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            panoramaImage.itemLocations.forEach { item ->
                val (xOffset, yOffset) = calculateMarkerOffset(
                    yaw = item.yaw, pitch = item.pitch,
                    boxWidth = boxWidth, boxHeight = boxHeight
                )
                Box(modifier = Modifier.offset(x = xOffset - 14.dp, y = yOffset - 28.dp)) {
                    MarkerPin(
                        selected = selectedItem?.id == item.id,
                        onClick = {
                            selectedItem = item
                            onMarkerSelected(item)
                            Toast.makeText(context, item.name, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            selectedItem?.let { item ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = item.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
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
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                val panoramaDrawableRes = remember(panoramaImage.imageUrl) {
                    resolvePanoramaDrawable(context, panoramaImage.imageUrl)
                }
                Image(
                    painter = painterResource(id = panoramaDrawableRes),
                    contentDescription = panoramaImage.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                panoramaImage.itemLocations.forEach { item ->
                    val (xOffset, yOffset) = calculateMarkerOffset(
                        yaw = item.yaw, pitch = item.pitch,
                        boxWidth = boxWidth, boxHeight = boxHeight
                    )
                    Box(modifier = Modifier.offset(x = xOffset - 14.dp, y = yOffset - 28.dp)) {
                        MarkerPin(
                            selected = selectedItem?.id == item.id,
                            onClick = {
                                selectedItem = item
                                Toast.makeText(context, item.name, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                selectedItem?.let { item ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = item.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
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
                Text(text = panoramaImage.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(text = panoramaImage.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MarkerPin(selected: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = Icons.Default.LocationOn,
        contentDescription = "Item marker",
        tint = if (selected) Color(0xFFFF5252) else Color(0xFF00BFA5),
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    )
}

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
```

**Key points:**
- Two modes: `isFullScreen = true` (no Card wrapper) and `isFullScreen = false` (Card with title/description)
- `resolvePanoramaDrawable()` — resolves `imageUrl` string to drawable resource ID, falls back to `R.drawable.paronama`
- `calculateMarkerOffset()` — maps yaw (-180..180) and pitch (-90..90) to 2D pixel offsets using equirectangular projection
- `MarkerPin` — LocationOn icon, teal when unselected, red when selected
- Selected marker shows info overlay at bottom with name, ID, yaw, and pitch
- Uses `BoxWithConstraints` to get actual dimensions for marker positioning

---

## 7.5 SceneViewPanorama (Zoom & Pan Animation)

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/presentation/ui/SceneViewPanorama.kt`

```kotlin
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
    
    val offsetX = remember(focusedItem?.yaw) {
        focusedItem?.let { item ->
            val normalizedYaw = (item.yaw + 180f) / 360f
            val screenWidth = 1f
            val targetX = normalizedYaw * screenWidth
            val centerOffset = 0.5f - targetX
            (centerOffset * 300).dp
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
                    focusedItemId = if (focusedItemId == item.id) null else item.id
                    focusedItem = if (focusedItemId == item.id) null else item
                    onMarkerSelected(item)
                }
            )
        }
    }
}
```

**Key points:**
- Wraps `Panorama360View` with zoom and pan animations
- Clicking a marker zooms in (scale 1.3x) and pans to center it
- Clicking the same marker again toggles focus off (zoom out)
- Uses `animateFloatAsState` for zoom and `animateDpAsState` for pan
- Delegates to `Panorama360View` with `isFullScreen = true`

---

## 7.6 Panorama Full Screen

**File:** `app/src/main/java/com/autopart/ordermgmt/panorama/presentation/ui/screens/PanoramaFullScreen.kt`

```kotlin
package com.autopart.ordermgmt.panorama.presentation.ui.screens

import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import com.autopart.ordermgmt.panorama.domain.model.PanoramaImage
import com.autopart.ordermgmt.panorama.presentation.ui.SceneViewPanorama
import com.autopart.ordermgmt.panorama.presentation.viewmodel.PanoramaViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PanoramaFullScreen(
    navController: NavController,
    panoramaViewModel: PanoramaViewModel = hiltViewModel(
        viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner
    )
) {
    val panoramaImages by panoramaViewModel.panoramaImages.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as? androidx.activity.ComponentActivity)?.window
    
    // Set landscape mode when entering
    LaunchedEffect(Unit) {
        val activity = context as? androidx.activity.ComponentActivity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        window?.let { w ->
            WindowCompat.setDecorFitsSystemWindows(w, false)
            w.insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            w.insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    // Restore portrait mode when leaving
    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? androidx.activity.ComponentActivity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            
            window?.let { w ->
                WindowCompat.setDecorFitsSystemWindows(w, true)
                w.insetsController?.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        SceneViewPanorama(
            panoramaImage = panoramaImages.firstOrNull() ?: PanoramaImage(
                id = "default",
                title = "Default View",
                description = "Default panoramic view",
                imageUrl = "panorama_dummy"
            ),
            modifier = Modifier.fillMaxSize()
        )
        
        // Close button
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(50.dp)),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
```

**Key points:**
- Forces landscape orientation on enter, restores portrait on dispose
- Hides system bars for immersive experience (transient bars on swipe)
- Uses `SceneViewPanorama` with zoom/pan animation
- Close button (top-right, rounded) navigates back
- Shares `PanoramaViewModel` with `OrderDetailFullScreen` via activity-level `ViewModelStoreOwner`

---

## Architecture Overview

```
com.autopart.ordermgmt.panorama/
├── domain/model/
│   ├── ItemLocation.kt              # Yaw/pitch marker position model
│   └── PanoramaImage.kt             # Panorama image + marker list model
├── presentation/
│   ├── ui/
│   │   ├── Panorama360View.kt       # 2D equirectangular view with markers
│   │   ├── SceneViewPanorama.kt     # Zoom/pan animation wrapper
│   │   └── screens/
│   │       └── PanoramaFullScreen.kt # Full-screen immersive landscape view
│   ├── util/
│   │   └── PositionUtils.kt         # Yaw/pitch → 3D Position conversion
│   └── viewmodel/
│       └── PanoramaViewModel.kt     # HiltViewModel for panorama state
```

## Data Flow

```
OrderDetailScreen
    ↓ LaunchedEffect(orderData)
PanoramaViewModel.setPanoramaFromOrderItems(orderItems)
    ↓ converts OrderItem.location.yaw/pitch → ItemLocation
PanoramaImages StateFlow updated
    ↓
OrderDetailScreen → Panorama360View (inline, expandable card)
    ↓ FAB click → navigate("panorama_full_screen")
PanoramaFullScreen → SceneViewPanorama → Panorama360View (full screen)
    ↓ marker click
Zoom animation + pan to center marker + Toast with item name
```

---

## Summary

After this step you should have:
- `ItemLocation` and `PanoramaImage` domain models for 360° view
- `PanoramaViewModel` — converts order items to panorama markers
- `PositionUtils` — yaw/pitch to 3D sphere position conversion (for SceneView)
- `Panorama360View` — 2D equirectangular panorama with dynamic marker plotting, info overlay, card/full-screen modes
- `SceneViewPanorama` — animated zoom/pan wrapper around Panorama360View
- `PanoramaFullScreen` — immersive landscape full-screen with system bar hiding and close button
