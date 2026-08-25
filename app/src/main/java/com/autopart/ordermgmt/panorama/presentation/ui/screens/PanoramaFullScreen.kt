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
    panoramaViewModel: PanoramaViewModel = hiltViewModel(viewModelStoreOwner = androidx.activity.compose.LocalActivity.current as androidx.lifecycle.ViewModelStoreOwner)
) {
    val panoramaImages by panoramaViewModel.panoramaImages.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as? androidx.activity.ComponentActivity)?.window
    
    // Set landscape mode when entering
    LaunchedEffect(Unit) {
        val activity = context as? androidx.activity.ComponentActivity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Hide system bars for immersive experience
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
            
            // Show system bars
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
