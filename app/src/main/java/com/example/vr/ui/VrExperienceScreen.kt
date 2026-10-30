package com.example.vr.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vr.gl.VrGlSurfaceView
import com.example.vr.gl.VrRenderer
import com.example.vr.viewmodel.VrUiState
import com.example.vr.viewmodel.VrViewModel
import kotlinx.coroutines.delay

@Composable
fun VrExperienceScreen(
    uiState: VrUiState,
    viewModel: VrViewModel
) {
    val context = LocalContext.current

    // Handle system back to return to main menu
    BackHandler {
        viewModel.returnToMainMenu()
    }

    // Instantiate VR OpenGL renderer
    val renderer = remember {
        VrRenderer(
            context = context,
            onHotspotGazeProgress = { hotspot, progress ->
                viewModel.onHotspotGazeUpdate(hotspot, progress)
            },
            onHotspotTriggered = { hotspot ->
                viewModel.onHotspotTriggered(hotspot)
            }
        )
    }

    // Keep renderer synced with state
    LaunchedEffect(uiState.isStereoVRMode) {
        renderer.isStereoVRMode = uiState.isStereoVRMode
    }
    LaunchedEffect(uiState.ipdMm) {
        renderer.ipdMm = uiState.ipdMm
    }
    LaunchedEffect(uiState.fovDegrees) {
        renderer.fovDegrees = uiState.fovDegrees
    }
    LaunchedEffect(uiState.timeOfDay) {
        renderer.changeTimeOfDay(uiState.timeOfDay)
    }
    LaunchedEffect(uiState.graphicsQuality) {
        renderer.changeGraphicsQuality(uiState.graphicsQuality)
    }
    LaunchedEffect(uiState.comfortVignetteEnabled) {
        renderer.comfortVignetteEnabled = uiState.comfortVignetteEnabled
    }

    // Clear notification after 3 seconds
    LaunchedEffect(uiState.statusNotification) {
        if (uiState.statusNotification != null) {
            delay(3000)
            viewModel.clearNotification()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vr_experience_screen")
    ) {
        // 1. OpenGL 3D VR Surface View
        AndroidView(
            factory = { ctx ->
                VrGlSurfaceView(
                    context = ctx,
                    renderer = renderer,
                    headTracker = viewModel.headTracker,
                    onCardboardTriggerTap = {
                        // Cardboard viewer screen-tap trigger
                        renderer.triggerCurrentHotspot()
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. In-VR Comfort Vignette Overlay
        if (uiState.comfortVignetteEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
        }

        // 3. Top Floating Minimalist VR HUD (Translucent to avoid blocking immersion)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exit button
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                contentColor = Color.White
            ) {
                IconButton(
                    onClick = { viewModel.returnToMainMenu() },
                    modifier = Modifier.testTag("vr_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit to Menu")
                }
            }

            // Gaze targeting banner when looking at a landmark
            AnimatedVisibility(
                visible = uiState.activeHotspot != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.activeHotspot?.let { hotspot ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        contentColor = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Target: ${hotspot.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uiState.hotspotGazeProgress },
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            // Right VR Controls: Recenter, Toggle Stereo, Audio Mute, Settings
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.65f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recenter Headset
                    IconButton(
                        onClick = { viewModel.recenterHead() },
                        modifier = Modifier.testTag("vr_recenter_button")
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Recenter Headset",
                            tint = Color.White
                        )
                    }

                    // Stereo / Mono Toggle
                    IconButton(
                        onClick = { viewModel.toggleStereoVRMode() },
                        modifier = Modifier.testTag("vr_stereo_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isStereoVRMode) Icons.Default.Headset else Icons.Default.Visibility,
                            contentDescription = "Toggle Stereo/Mono",
                            tint = if (uiState.isStereoVRMode) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    // Audio Mute Toggle
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.testTag("vr_audio_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Audio Toggle",
                            tint = Color.White
                        )
                    }

                    // Settings Dialog Trigger
                    IconButton(
                        onClick = { viewModel.openSettingsDialog() },
                        modifier = Modifier.testTag("vr_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 4. Floating Sightseeing Narration Card (when hotspot is triggered)
        AnimatedVisibility(
            visible = uiState.triggeredHotspot != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            uiState.triggeredHotspot?.let { hotspot ->
                Card(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("hotspot_narration_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xEE0F172A)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = hotspot.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissTriggeredHotspot() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("dismiss_hotspot_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = hotspot.shortDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Voice Narration Active (TTS Guide)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF86EFAC)
                            )
                        }
                    }
                }
            }
        }

        // 5. Ephemeral Status Toast notification
        AnimatedVisibility(
            visible = uiState.statusNotification != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            uiState.statusNotification?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
