package com.example.vr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vr.model.DeckLocation
import com.example.vr.model.HeadsetProfile
import com.example.vr.model.TimeOfDay
import com.example.vr.viewmodel.VrUiState
import com.example.vr.viewmodel.VrViewModel

@Composable
fun MainMenuScreen(
    uiState: VrUiState,
    viewModel: VrViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D1A),
                        Color(0xFF131D38),
                        Color(0xFF1E293B)
                    )
                )
            )
            .padding(16.dp)
            .testTag("main_menu_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column: Branding, Primary CTA, Deck Location
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Title & Subtitle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "VR SKY VIEW",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Skyscraper Rooftop 360 Observation",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Step onto a breathtaking 1,000-ft observation deck. Experience 360-degree vistas of sprawling city skyscrapers, illuminated traffic below, distant mountains, atmospheric wind, and guided sightseeing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Observation Deck Platform selection
                        Text(
                            text = "Select Observation Platform:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DeckLocation.values().forEach { location ->
                            val isSelected = uiState.currentLocation == location
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDeckLocation(location) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(location.displayName, fontWeight = FontWeight.Bold)
                                        Text(
                                            location.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .testTag("deck_chip_${location.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Launch Button
                    Button(
                        onClick = { viewModel.startExperience() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_experience_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.isStereoVRMode) "ENTER VR EXPERIENCE (DUAL EYE)" else "START 360 VIEW (MONO)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right Column: Headset Profile, Time of Day, Comfort & Settings
            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // VR Mode Toggle: Stereo vs Mono
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Viewing Mode:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedButton(
                                onClick = { viewModel.toggleStereoVRMode() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("toggle_stereo_mode_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isStereoVRMode) Icons.Default.Headset else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (uiState.isStereoVRMode) "VR Stereo (Cardboard)" else "Mono 360 (Handheld)")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Headset Profile Chips
                        Text(
                            text = "Target VR Headset:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        listOf(
                            HeadsetProfile.GOOGLE_CARDBOARD_V2,
                            HeadsetProfile.JIO_DIVE,
                            HeadsetProfile.VR_BOX_GENERIC
                        ).forEach { profile ->
                            val isSelected = uiState.headsetProfile == profile
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setHeadsetProfile(profile) },
                                label = { Text(profile.displayName) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .testTag("menu_headset_${profile.name.lowercase()}")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Time of Day
                        Text(
                            text = "Atmosphere & Lighting:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeOfDay.values().forEach { time ->
                                val isSelected = uiState.timeOfDay == time
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setTimeOfDay(time) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (time) {
                                                TimeOfDay.SUNSET -> Icons.Default.WbTwilight
                                                TimeOfDay.TWILIGHT_NIGHT -> Icons.Default.NightsStay
                                                TimeOfDay.DAYLIGHT -> Icons.Default.LightMode
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(time.label.substringBefore(" ")) },
                                    modifier = Modifier.testTag("chip_time_${time.name.lowercase()}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Action Buttons: Settings & Device Testing
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openSettingsDialog() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("menu_settings_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Optics & Audio")
                            }

                            OutlinedButton(
                                onClick = { viewModel.openTestingDialog() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("menu_testing_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing Guide")
                            }
                        }

                        // Gyro Status Tag
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.hasHardwareSensor) Icons.Default.CheckCircle else Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (uiState.hasHardwareSensor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.hasHardwareSensor) "9-Axis Gyro Active (3DoF Sensor Tracking)" else "Touch Drag Mode (Drag on screen to look)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
