package com.example.vr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vr.model.GraphicsQuality
import com.example.vr.model.HeadsetProfile
import com.example.vr.viewmodel.VrUiState
import com.example.vr.viewmodel.VrViewModel

@Composable
fun SettingsDialog(
    uiState: VrUiState,
    viewModel: VrViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("settings_dialog_surface"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VR Headset & Experience Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Settings")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Headset Profile
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Headset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Headset Optics Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Optimizes stereoscopic lens separation and field of view for your viewer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                HeadsetProfile.GOOGLE_CARDBOARD_V2,
                                HeadsetProfile.JIO_DIVE,
                                HeadsetProfile.VR_BOX_GENERIC
                            ).forEach { profile ->
                                val selected = uiState.headsetProfile == profile
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setHeadsetProfile(profile) },
                                    label = { Text(profile.displayName) },
                                    modifier = Modifier.testTag("chip_${profile.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // IPD Slider
                        Text(
                            text = "Inter-Pupillary Distance (IPD): ${uiState.ipdMm.toInt()} mm",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = uiState.ipdMm,
                            onValueChange = { viewModel.setIpdMm(it) },
                            valueRange = 56f..74f,
                            steps = 18,
                            modifier = Modifier.testTag("ipd_slider")
                        )

                        // FOV Slider
                        Text(
                            text = "Field of View (FOV): ${uiState.fovDegrees.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = uiState.fovDegrees,
                            onValueChange = { viewModel.setFovDegrees(it) },
                            valueRange = 75f..105f,
                            steps = 30,
                            modifier = Modifier.testTag("fov_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Comfort & Motion
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Comfort & Ergonomics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Seated Viewing Mode (Recommended)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Limits vertical pitch to ergonomic neck angles. No artificial walking or sudden motion.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.seatedComfortMode,
                                onCheckedChange = { viewModel.setSeatedComfortMode(it) },
                                modifier = Modifier.testTag("seated_mode_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Comfort Vignette",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Softens peripheral edges to reduce motion sensitivity during quick head rotations.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.comfortVignetteEnabled,
                                onCheckedChange = { viewModel.setComfortVignette(it) },
                                modifier = Modifier.testTag("comfort_vignette_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Audio Mix
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Atmospheric Audio Levels",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Wind
                        Text(
                            text = "Rooftop Wind (1,000 ft): ${(uiState.windVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = uiState.windVolume,
                            onValueChange = {
                                viewModel.updateVolumes(
                                    uiState.masterVolume,
                                    it,
                                    uiState.trafficVolume,
                                    uiState.musicVolume,
                                    uiState.narrationVolume
                                )
                            },
                            modifier = Modifier.testTag("wind_slider")
                        )

                        // Traffic
                        Text(
                            text = "Distant City Traffic: ${(uiState.trafficVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = uiState.trafficVolume,
                            onValueChange = {
                                viewModel.updateVolumes(
                                    uiState.masterVolume,
                                    uiState.windVolume,
                                    it,
                                    uiState.musicVolume,
                                    uiState.narrationVolume
                                )
                            },
                            modifier = Modifier.testTag("traffic_slider")
                        )

                        // Ambient Music
                        Text(
                            text = "Soft Ambient Music: ${(uiState.musicVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = uiState.musicVolume,
                            onValueChange = {
                                viewModel.updateVolumes(
                                    uiState.masterVolume,
                                    uiState.windVolume,
                                    uiState.trafficVolume,
                                    it,
                                    uiState.narrationVolume
                                )
                            },
                            modifier = Modifier.testTag("music_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Graphics Presets
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Performance & Graphics Preset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GraphicsQuality.values().forEach { quality ->
                                val selected = uiState.graphicsQuality == quality
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setGraphicsQuality(quality) },
                                    label = { Text(quality.label.substringBefore(" ")) },
                                    modifier = Modifier.testTag("chip_quality_${quality.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.recenterHead() },
                        modifier = Modifier.testTag("settings_recenter_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recenter Headset")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("apply_settings_button")
                    ) {
                        Text("Save & Close")
                    }
                }
            }
        }
    }
}
