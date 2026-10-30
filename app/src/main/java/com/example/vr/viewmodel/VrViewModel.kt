package com.example.vr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vr.audio.VrAudioEngine
import com.example.vr.model.DeckLocation
import com.example.vr.model.GraphicsQuality
import com.example.vr.model.HeadsetProfile
import com.example.vr.model.SightseeingHotspot
import com.example.vr.model.TimeOfDay
import com.example.vr.sensor.HeadTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    VR_EXPERIENCE
}

data class VrUiState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val isStereoVRMode: Boolean = true,
    val headsetProfile: HeadsetProfile = HeadsetProfile.GOOGLE_CARDBOARD_V2,
    val ipdMm: Float = 64f,
    val fovDegrees: Float = 90f,
    val timeOfDay: TimeOfDay = TimeOfDay.SUNSET,
    val currentLocation: DeckLocation = DeckLocation.NORTH_OBSERVATION,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
    val seatedComfortMode: Boolean = true,
    val comfortVignetteEnabled: Boolean = true,
    val hasHardwareSensor: Boolean = true,
    val isAudioMuted: Boolean = false,
    val masterVolume: Float = 0.85f,
    val windVolume: Float = 0.70f,
    val trafficVolume: Float = 0.55f,
    val musicVolume: Float = 0.45f,
    val narrationVolume: Float = 1.0f,
    val activeHotspot: SightseeingHotspot? = null,
    val hotspotGazeProgress: Float = 0f,
    val triggeredHotspot: SightseeingHotspot? = null,
    val showSettingsDialog: Boolean = false,
    val showTestingDialog: Boolean = false,
    val statusNotification: String? = null
)

class VrViewModel(application: Application) : AndroidViewModel(application) {

    val headTracker = HeadTracker(application)
    val audioEngine = VrAudioEngine(application)

    private val _uiState = MutableStateFlow(
        VrUiState(
            hasHardwareSensor = headTracker.hasHardwareSensor
        )
    )
    val uiState: StateFlow<VrUiState> = _uiState.asStateFlow()

    init {
        audioEngine.masterVolume = _uiState.value.masterVolume
        audioEngine.windVolume = _uiState.value.windVolume
        audioEngine.trafficVolume = _uiState.value.trafficVolume
        audioEngine.ambientMusicVolume = _uiState.value.musicVolume
        audioEngine.narrationVolume = _uiState.value.narrationVolume
    }

    fun startExperience() {
        headTracker.start()
        audioEngine.start()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.VR_EXPERIENCE,
            statusNotification = "VR Sky View active. Look around 360°!"
        )
    }

    fun returnToMainMenu() {
        headTracker.stop()
        audioEngine.stop()
        audioEngine.stopNarration()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.MAIN_MENU,
            activeHotspot = null,
            triggeredHotspot = null
        )
    }

    fun toggleStereoVRMode() {
        val newMode = !_uiState.value.isStereoVRMode
        _uiState.value = _uiState.value.copy(
            isStereoVRMode = newMode,
            statusNotification = if (newMode) "Stereo VR Cardboard Mode (Dual Eye)" else "Monoscopic 360 Mode (Fullscreen)"
        )
    }

    fun setHeadsetProfile(profile: HeadsetProfile) {
        _uiState.value = _uiState.value.copy(
            headsetProfile = profile,
            ipdMm = profile.defaultIpdMm,
            fovDegrees = profile.defaultFovDegrees,
            statusNotification = "Profile applied: ${profile.displayName}"
        )
    }

    fun setIpdMm(ipd: Float) {
        _uiState.value = _uiState.value.copy(ipdMm = ipd)
    }

    fun setFovDegrees(fov: Float) {
        _uiState.value = _uiState.value.copy(fovDegrees = fov)
    }

    fun setTimeOfDay(time: TimeOfDay) {
        _uiState.value = _uiState.value.copy(timeOfDay = time)
    }

    fun setDeckLocation(location: DeckLocation) {
        _uiState.value = _uiState.value.copy(
            currentLocation = location,
            statusNotification = "Teleported to ${location.displayName}"
        )
    }

    fun setGraphicsQuality(quality: GraphicsQuality) {
        _uiState.value = _uiState.value.copy(graphicsQuality = quality)
    }

    fun setSeatedComfortMode(enabled: Boolean) {
        headTracker.seatedComfortMode = enabled
        _uiState.value = _uiState.value.copy(seatedComfortMode = enabled)
    }

    fun setComfortVignette(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(comfortVignetteEnabled = enabled)
    }

    fun recenterHead() {
        headTracker.recenter()
        _uiState.value = _uiState.value.copy(
            statusNotification = "Headset recentered forward"
        )
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isAudioMuted
        audioEngine.masterVolume = if (newMuted) 0f else _uiState.value.masterVolume
        _uiState.value = _uiState.value.copy(isAudioMuted = newMuted)
    }

    fun updateVolumes(master: Float, wind: Float, traffic: Float, music: Float, narration: Float) {
        audioEngine.masterVolume = master
        audioEngine.windVolume = wind
        audioEngine.trafficVolume = traffic
        audioEngine.ambientMusicVolume = music
        audioEngine.narrationVolume = narration

        _uiState.value = _uiState.value.copy(
            masterVolume = master,
            windVolume = wind,
            trafficVolume = traffic,
            musicVolume = music,
            narrationVolume = narration
        )
    }

    fun onHotspotGazeUpdate(hotspot: SightseeingHotspot?, progress: Float) {
        _uiState.value = _uiState.value.copy(
            activeHotspot = hotspot,
            hotspotGazeProgress = progress
        )
    }

    fun onHotspotTriggered(hotspot: SightseeingHotspot) {
        _uiState.value = _uiState.value.copy(
            triggeredHotspot = hotspot,
            statusNotification = "Sightseeing: ${hotspot.title}"
        )
        // Play voice tour narration
        audioEngine.speakNarration(hotspot.narrationScript)
    }

    fun dismissTriggeredHotspot() {
        _uiState.value = _uiState.value.copy(triggeredHotspot = null)
        audioEngine.stopNarration()
    }

    fun openSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = true)
    }

    fun closeSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = false)
    }

    fun openTestingDialog() {
        _uiState.value = _uiState.value.copy(showTestingDialog = true)
    }

    fun closeTestingDialog() {
        _uiState.value = _uiState.value.copy(showTestingDialog = false)
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(statusNotification = null)
    }

    override fun onCleared() {
        super.onCleared()
        headTracker.stop()
        audioEngine.release()
    }
}
