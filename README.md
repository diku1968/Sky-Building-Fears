# VR SKY VIEW – Android VR Skyscraper Rooftop Experience

A realistic, breathtaking skyscraper rooftop observation experience for Android smartphones, designed for **Google Cardboard**, **JioDive / Jio VR**, and **Generic VR Box** headsets with 3DoF gyroscope-based head tracking.

---

## 🌟 Overview & Experience

The player is seated on a virtual observation deck 1,000 feet atop a supertall skyscraper.
- **Large Realistic City:** 40+ skyscrapers of varying heights with illuminated facades, illuminated corporate towers, spires, and flashing red aviation warning beacons.
- **Moving Traffic:** Ground avenues bustling 1,000 feet below with 200+ animated vehicle light trails (white headlights and ruby taillights).
- **Distant Mountains & Sky:** Silhouetted mountain ranges framing the horizon; realistic sky dome with Sunset Golden Hour, Twilight City Lights, and Clear Sky Daylight.
- **Observation Deck:** High-strength protective railings, tempered glass windbreak panels with subtle reflections, and observation binoculars.
- **Seated Comfort:** Designed for comfortable seated viewing without forced motion, artificial walking, or physical balance requirements.

---

## 📱 Hardware & VR Features

1. **Stereoscopic Rendering:** True dual-eye left and right viewports with adjustable Inter-Pupillary Distance (IPD 56–74 mm) and center divider line.
2. **Monoscopic 360 Mode:** One-tap toggle for flat handheld exploration without a headset.
3. **Hardware Sensor Fusion (3DoF):** Utilizes Android `Sensor.TYPE_ROTATION_VECTOR` (9-axis fusion) with low-pass jitter smoothing and landscape axis remapping.
4. **Touch-Drag Fallback:** Drag across the screen or emulator to rotate the view if physical sensors are absent.
5. **Recenter Control:** Instant heading recalibration with zero drift.
6. **Cardboard Viewer Button:** Supports screen taps, conductive top triggers, and Bluetooth controller keys.
7. **Hands-Free Gaze Dwell:** Focus on any sightseeing landmark for 1.3 seconds to trigger the voice tour guide and informational card.
8. **Atmospheric Audio Engine:**
   - Procedural high-altitude skyscraper wind gusts
   - Distant city traffic rumble (sub-bass low-pass drone)
   - Soft atmospheric ambient musical chords
   - Spoken tour narration guide (via Android TextToSpeech)

---

## 🥽 Headset Profiles & Compatibility

- **Google Cardboard V2:** 64 mm IPD, 90° FOV, conductive touch button.
- **Google Cardboard V1:** 60 mm IPD, 80° FOV, magnetic pull ring.
- **JioDive / Jio VR:** 66 mm IPD, 90° FOV, adjustable optical dials.
- **Generic VR Box (2.0 / 3.0):** 62 mm IPD, 84° FOV, dual focal sliders.
- **Custom Profile:** User-adjustable IPD, FOV, and comfort vignette.

---

## 🏗️ Repository Architecture

This repository delivers two production-ready solutions:

### 1. Native Android Application (Runs directly in AI Studio & on Android devices)
- `app/src/main/java/com/example/MainActivity.kt`: Entry activity with immersive sticky fullscreen mode.
- `app/src/main/java/com/example/vr/gl/VrRenderer.kt`: 3D OpenGL ES 2.0/3.0 stereoscopic and mono engine.
- `app/src/main/java/com/example/vr/gl/VrGlSurfaceView.kt`: Custom GLSurfaceView with touch navigation and Cardboard trigger.
- `app/src/main/java/com/example/vr/sensor/HeadTracker.kt`: 9-axis sensor fusion, landscape coordinate remapping, recentering.
- `app/src/main/java/com/example/vr/audio/VrAudioEngine.kt`: Real-time procedural audio synthesizer and TTS tour guide.
- `app/src/main/java/com/example/vr/ui/MainMenuScreen.kt`: M3 landscape menu, viewpoint selection, headset selector.
- `app/src/main/java/com/example/vr/ui/VrExperienceScreen.kt`: In-VR HUD, gaze dwell indicator, sightseeing cards.
- `app/src/main/java/com/example/vr/ui/SettingsDialog.kt`: Optics, IPD slider, comfort vignette, audio mix.
- `app/src/main/java/com/example/vr/ui/DeviceTestingDialog.kt`: Comprehensive headset testing checklist.

### 2. Complete Unity 6 Project Deliverables (`/unity_project/`)
- `Assets/Scripts/CardboardVRManager.cs`: Cardboard XR Plugin manager for Unity 6.
- `Assets/Scripts/HeadTracking3DoF.cs`: 3DoF gyroscope tracking with seated pitch constraints.
- `Assets/Scripts/ObservationDeckController.cs`: Deck railings, glass panels, viewpoint transitions.
- `Assets/Scripts/CityTrafficSystem.cs`: High-performance object-pooled vehicle light simulation.
- `Assets/Scripts/SightseeingGuide.cs`: Gaze reticle dwell detection and tour audio cards.
- `Assets/Scripts/VRAudioManager.cs`: Spatial wind, traffic drone, and ambient music mixing.
- `Assets/Scripts/ComfortAndSettings.cs`: Seated mode, vignette, and graphics quality tier scaler.
- `Assets/Scripts/CardboardTrigger.cs`: Conductive touch and screen tap event listener.
- `Assets/Scripts/MainMenuUI.cs`: Main menu scene controller.
- `Assets/Shaders/MobileCityUnlit.shader`: Mobile unlit shader with window glow emission.
- `Assets/Shaders/CardboardVignette.shader`: Comfort FOV reduction vignette shader.
- `ProjectSettings/AndroidPlayerSettings.json`: Complete Android manifest and XR Player Settings for Unity 6.
- `SCENE_SETUP_GUIDE.md`: Step-by-step scene creation for MainMenu, SkyCity, and ObservationDeck.
- `DEVICE_TESTING_CHECKLIST.md`: In-depth testing checklist for JioDive, VR Box, and Cardboard.
- `TROUBLESHOOTING.md`: Optical double-vision fixes, sensor calibration, and APK troubleshooting.
- `BUILD_INSTRUCTIONS.md`: Unity 6 Android APK export guide.

---

## 🚀 Running the App

### On Android / AI Studio
The app is fully compiled and ready to launch in the Streaming Android Emulator or on your connected device.
- Use the **Start Experience** button to enter the observation deck.
- Tilt the device or drag on screen to look around 360°.
- Tap **Recenter** at any time to re-align your forward view.
