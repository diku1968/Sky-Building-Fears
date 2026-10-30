# VR SKY VIEW – Troubleshooting & Diagnostics Guide

This document resolves common issues encountered when building, running, and testing Cardboard-compatible VR applications on Android and Unity 6.

---

## 1. Visual & Optical Issues

### Issue: "Double Vision" (Seeing two overlapping images instead of one 3D image)
- **Cause 1: Center line misalignment.** The phone is not horizontally centered in the headset tray.
  - *Fix:* Align the black screen divider line exactly with the physical plastic partition of your headset.
- **Cause 2: Incorrect IPD (Inter-Pupillary Distance).**
  - *Fix:* Open **Settings > Headset Profile** in VR Sky View. Try **JioDive** (66 mm), **VR Box** (62 mm), or use the custom IPD slider to adjust between 56 mm and 72 mm.
  - On headsets with physical dials (VR Box / JioDive), rotate the dials to move the lenses closer together or farther apart.
- **Cause 3: Phone screen scaling / display zoom.**
  - *Fix:* Check Android system settings: **Display > Screen Zoom** (set to standard/default) and disable "Full screen display notch cutout crop".

### Issue: Image Appears Blurry or Distorted
- **Fix:** Adjust the focal distance sliders (available on VR Box and JioDive). Move the phone closer to or further from the lenses until text is crisp.
- **Clean lenses:** Wipe acrylic lenses with a microfiber cloth; finger oils significantly degrade VR contrast.

---

## 2. Sensor & Head Tracking Issues

### Issue: View Slowly Drifts to the Left or Right (Gyroscope Drift)
- **Cause:** Earth's magnetic field interference or thermal drift in smartphone IMU sensors.
- **Fix 1: One-tap Recenter.** Press the **Recenter** button in the top HUD or tap the Cardboard viewer button while looking straight ahead.
- **Fix 2: Sensor calibration.** Lay phone flat on a table for 10 seconds before placing inside headset, or do a "figure-8" wave motion with the phone to calibrate the compass/accelerometer.

### Issue: Head Tracking Does Not Respond at All
- **Cause:** Phone lacks a hardware gyroscope, or sensor permissions are blocked.
- **Diagnostic:** The app's diagnostic badge will show *"Touch Drag Mode Active"*. You can drag your finger or mouse across the screen to look around in 360°.

---

## 3. Trigger & Interaction Issues

### Issue: Pressing the Cardboard Button Does Not Trigger Sightseeing Points
- **Cause 1: Conductive pad not touching screen.** Some thick cases or tempered glass screen protectors prevent the capacitive pulse from reaching the touchscreen digitizer.
  - *Fix:* Remove phone case.
- **Cause 2: Gaze not centered on hotspot.**
  - *Fix:* Ensure the white reticle circle is directly positioned over the landmark diamond marker. The app features **Hands-Free Gaze Dwell**: holding your gaze on the attraction for 1.3 seconds automatically triggers the audio tour without needing any button!

---

## 4. Unity 6 Build & Android Packaging Issues

### Issue: Unity 6 Build Fails with "Gradle Build Failed / NDK Missing"
- **Cause:** Missing Android NDK r25 or r26 in Unity Hub.
- **Fix:** In Unity Hub, open Installs > Unity 6 > Add Modules > Check **Android Build Support**, **Android SDK & NDK Tools**, and **OpenJDK**.

### Issue: Black Screen upon Launching on Android
- **Cause:** Vulkan graphics API incompatibility on older budget GPU chipsets (Mali-T, PowerVR).
- **Fix:** In Unity **Player Settings > Other Settings > Graphics APIs**, move **OpenGLES3** to the top of the list above Vulkan.

### Issue: Framerate Drops Below 60 FPS
- **Fix 1:** In **VR Sky View Settings**, switch Graphics Quality to **Low** or **Medium**.
- **Fix 2:** Turn off real-time shadows: in Unity Lighting settings, verify all skyscrapers have lightmaps baked rather than real-time shadows.
