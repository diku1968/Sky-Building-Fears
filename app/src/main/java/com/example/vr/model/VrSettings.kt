package com.example.vr.model

/**
 * Headset hardware profiles with standard optical and physical properties.
 */
enum class HeadsetProfile(
    val displayName: String,
    val description: String,
    val defaultIpdMm: Float,
    val defaultFovDegrees: Float,
    val distortionK1: Float
) {
    GOOGLE_CARDBOARD_V2(
        displayName = "Google Cardboard V2",
        description = "Standard 37mm biconvex lenses, conductive touch button (64mm IPD)",
        defaultIpdMm = 64.0f,
        defaultFovDegrees = 90.0f,
        distortionK1 = 0.22f
    ),
    GOOGLE_CARDBOARD_V1(
        displayName = "Google Cardboard V1",
        description = "Classic cardboard with magnetic pull ring (60mm IPD)",
        defaultIpdMm = 60.0f,
        defaultFovDegrees = 80.0f,
        distortionK1 = 0.28f
    ),
    JIO_DIVE(
        displayName = "JioDive / Jio VR",
        description = "Adjustable optical barrels with enhanced eye-relief (66mm IPD)",
        defaultIpdMm = 66.0f,
        defaultFovDegrees = 90.0f,
        distortionK1 = 0.18f
    ),
    VR_BOX_GENERIC(
        displayName = "Generic VR Box (2.0 / 3.0)",
        description = "Dual focal-slider phone headset with standard resin lenses (62mm IPD)",
        defaultIpdMm = 62.0f,
        defaultFovDegrees = 84.0f,
        distortionK1 = 0.20f
    ),
    CUSTOM(
        displayName = "Custom Calibration",
        description = "Manually tuned IPD, field of view, and comfort settings",
        defaultIpdMm = 64.0f,
        defaultFovDegrees = 90.0f,
        distortionK1 = 0.20f
    )
}

/**
 * Graphics quality presets for balancing visuals and framerate on diverse Android devices.
 */
enum class GraphicsQuality(val label: String, val buildingDensity: Int, val trafficCount: Int, val useAntiAliasing: Boolean) {
    LOW(label = "Low (Battery Saver / Smooth 60fps)", buildingDensity = 24, trafficCount = 100, useAntiAliasing = false),
    MEDIUM(label = "Medium (Balanced Clarity)", buildingDensity = 48, trafficCount = 200, useAntiAliasing = true),
    HIGH(label = "High (Maximum Detail & Lighting)", buildingDensity = 72, trafficCount = 360, useAntiAliasing = true)
}

/**
 * Time of day sky setting for the observation deck.
 */
enum class TimeOfDay(val label: String, val skyColorTop: FloatArray, val skyColorHorizon: FloatArray, val sunDirection: FloatArray) {
    SUNSET(
        label = "Sunset Golden Hour",
        skyColorTop = floatArrayOf(0.12f, 0.15f, 0.38f),
        skyColorHorizon = floatArrayOf(0.95f, 0.48f, 0.22f),
        sunDirection = floatArrayOf(-0.7f, 0.25f, -0.6f)
    ),
    TWILIGHT_NIGHT(
        label = "Twilight City Lights",
        skyColorTop = floatArrayOf(0.04f, 0.05f, 0.14f),
        skyColorHorizon = floatArrayOf(0.18f, 0.14f, 0.32f),
        sunDirection = floatArrayOf(-0.5f, 0.1f, -0.8f)
    ),
    DAYLIGHT(
        label = "Clear Sky Daylight",
        skyColorTop = floatArrayOf(0.20f, 0.45f, 0.88f),
        skyColorHorizon = floatArrayOf(0.65f, 0.80f, 0.95f),
        sunDirection = floatArrayOf(-0.4f, 0.8f, -0.4f)
    )
}

/**
 * Observation viewpoint location on the rooftop complex.
 */
enum class DeckLocation(val displayName: String, val subtitle: String, val deckHeightMeters: Float) {
    NORTH_OBSERVATION(
        displayName = "North Vista Promenade",
        subtitle = "Main glass-walled platform overlooking Grand Avenue & Financial Core",
        deckHeightMeters = 380f
    ),
    WEST_SUNSET_PERCH(
        displayName = "Sunset Sky Lounge",
        subtitle = "Relaxed corner terrace facing oceanic horizon & distant mountain ranges",
        deckHeightMeters = 385f
    ),
    EAST_HELIPAD_OVERLOOK(
        displayName = "Helipad Balcony",
        subtitle = "Industrial glass deck with direct line-of-sight to the neighboring spire",
        deckHeightMeters = 390f
    )
}

/**
 * Sightseeing point of interest in the virtual city.
 */
data class SightseeingHotspot(
    val id: String,
    val title: String,
    val yawDegrees: Float,
    val pitchDegrees: Float,
    val distanceKm: Float,
    val shortDescription: String,
    val narrationScript: String
)
