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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vr.viewmodel.VrUiState
import com.example.vr.viewmodel.VrViewModel

@Composable
fun DeviceTestingDialog(
    uiState: VrUiState,
    viewModel: VrViewModel,
    onDismiss: () -> Unit
) {
    val checklist = remember {
        mutableStateMapOf(
            "gyro" to uiState.hasHardwareSensor,
            "center_divider" to false,
            "cardboard_trigger" to false,
            "jiodive_clamp" to false,
            "audio_soundscape" to true,
            "comfort_ipd" to false
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("device_testing_dialog_surface"),
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
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Device Testing",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VR Headset Testing & Calibration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_testing_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Testing")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Verify proper stereoscopic alignment, 3DoF tracking, and viewer mechanics for Google Cardboard, JioDive, and VR Box headsets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Diagnostic Sensor Status Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.hasHardwareSensor) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.hasHardwareSensor) Icons.Default.CheckCircle else Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = if (uiState.hasHardwareSensor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (uiState.hasHardwareSensor) "Hardware 9-Axis Gyroscope Detected" else "Hardware Gyroscope Not Found (Touch Simulation Active)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.hasHardwareSensor) {
                                    "Rotational vector fusion active. Head movements translate accurately in 360 degrees."
                                } else {
                                    "Running in touch-drag simulation mode. You can look around smoothly by dragging the screen or using test hotkeys."
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pre-Flight Headset Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checklist Items
                ChecklistItem(
                    checked = checklist["gyro"] == true,
                    onCheckedChange = { checklist["gyro"] = it },
                    title = "1. Gyroscope & 3DoF Calibration",
                    description = "Tilt and rotate phone. Horizon stays level; looking up displays sky dome and down displays observation deck railing."
                )

                ChecklistItem(
                    checked = checklist["center_divider"] == true,
                    onCheckedChange = { checklist["center_divider"] = it },
                    title = "2. Center Divider Alignment",
                    description = "Ensure the black vertical center line matches the viewer's optical partition to prevent double vision."
                )

                ChecklistItem(
                    checked = checklist["cardboard_trigger"] == true,
                    onCheckedChange = { checklist["cardboard_trigger"] = it },
                    title = "3. Conductive Trigger / Screen Tap",
                    description = "Press the top Cardboard button or touch the screen to activate sightseeing points or recenter."
                )

                ChecklistItem(
                    checked = checklist["jiodive_clamp"] == true,
                    onCheckedChange = { checklist["jiodive_clamp"] = it },
                    title = "4. JioDive / VR Box Phone Tray Clamp",
                    description = "For JioDive and generic VR Box, ensure volume rocker buttons are not pinched by the spring clamp."
                )

                ChecklistItem(
                    checked = checklist["comfort_ipd"] == true,
                    onCheckedChange = { checklist["comfort_ipd"] = it },
                    title = "5. IPD & Optical Dial Tuning",
                    description = "Adjust the headset's mechanical lens sliders until the 3D text and buildings appear sharp and clear without eye strain."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { viewModel.recenterHead() }
                    ) {
                        Text("Recenter Now")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
