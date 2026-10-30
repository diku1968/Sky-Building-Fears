# VR SKY VIEW – Headset & Device Testing Checklist

This checklist provides step-by-step verification procedures for **Google Cardboard**, **JioDive / Jio VR**, and **Generic VR Box (2.0 / 3.0)** headsets on Android smartphones.

---

## 1. Device Compatibility Overview

| Headset / Viewer | Optics / Lenses | Trigger Mechanism | IPD Adjustment | Compatibility Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Google Cardboard V2** | 37 mm biconvex | Top conductive conductive button | Fixed 64 mm | Fully native; standard Google Cardboard profile. |
| **Google Cardboard V1** | 25 mm biconvex | Magnetic pull ring | Fixed 60 mm | Magnetic trigger requires magnetometer; touch tap works. |
| **JioDive / Jio VR** | 32 mm acrylic resin | Center capacitive trigger or touch | Side optical gear (62–68 mm) | Verify phone thickness (fits 4.7" to 6.7" phones); clamp may press volume buttons if not centered. |
| **Generic VR Box (2.0/3.0)** | 42 mm spherical resin | No built-in button (requires screen tap or BT controller) | Dual independent top sliders (58–72 mm) | Use Gaze Dwell Reticle or touch tap; calibrate focal sliders for sharpness. |

---

## 2. Pre-Flight Device Hardware Verification

- [ ] **Android OS Version:** Verify phone is running Android 7.0 (API 24) or newer.
- [ ] **9-Axis Gyroscope:** Verify phone possesses a physical hardware gyroscope (`Sensor.TYPE_ROTATION_VECTOR` or `Sensor.TYPE_GYROSCOPE`).
  - *Fallback check:* If phone lacks gyroscope, VR Sky View gracefully enters Touch Drag Mode for testing.
- [ ] **Screen Resolution & Refresh Rate:** Optimal at 1080p (FHD) or higher, 60 Hz or 90 Hz display.
- [ ] **Screen Auto-Rotate / Orientation Lock:** Turn OFF system orientation lock before inserting phone into headset.
- [ ] **Phone Case:** Remove bulky phone cases or kickstands to ensure proper optical focal distance and prevent overheating.

---

## 3. Headset Mounting & Alignment Checklist

- [ ] **Center Line Optical Alignment:**
  - The vertical black divider line on the screen must line up with the center physical partition inside the headset.
  - A misalignment of even 2–3 mm causes eye strain and double vision (diplopia).
- [ ] **Volume Button Clearance:**
  - Especially on **JioDive** and **VR Box** spring clamps, ensure the rubber grips do not continuously press the power or volume keys.
- [ ] **Lens Focus & IPD Calibration:**
  - On **JioDive:** Turn the side optical dials until distant skyscrapers appear sharp.
  - On **VR Box:** Adjust both top sliders horizontally to match your personal pupillary distance (IPD, typically 62–65 mm).

---

## 4. In-VR Functional Test Matrix

| Test Case | Procedure | Expected Result | Pass / Fail |
| :--- | :--- | :--- | :--- |
| **1. 360° Gyroscope Tracking** | Slowly turn full 360° circle while seated. | Smooth panoramic view without jitter, tearing, or abrupt flips. | [ ] |
| **2. Horizon Stability** | Look up 45° to the sky dome and down 45° to the deck floor. | Horizon remains level; pitch stops comfortably within safe seated limits. | [ ] |
| **3. Recenter Function** | Face forward and tap the Recenter button (or Cardboard trigger). | Current heading immediately snaps to zero forward azimuth. | [ ] |
| **4. Stereoscopic 3D Depth** | View safety railing handrail vs. distant skyscraper spire. | Clear binocular parallax; railing appears close, city appears far below. | [ ] |
| **5. Gaze Dwell Detection** | Align center reticle ring onto "The Apex Spire". | Dwell progress ring fills for 1.3 seconds, triggers voice narration and info card. | [ ] |
| **6. Cardboard Trigger Tap** | Press headset conductive trigger button. | Current focused sightseeing landmark immediately activates. | [ ] |
| **7. Audio Spatialization** | Listen with stereo headphones. | High-altitude wind gusting in ears, deep city traffic drone from below, soft music. | [ ] |
| **8. Performance / FPS** | Monitor framerate during continuous 360° look. | Solid 60 FPS maintained without thermal throttling. | [ ] |

---

## 5. Troubleshooting JioDive Specifics

1. **Double Vision on JioDive:**
   - Tap **Settings** in VR Sky View > Select **JioDive / Jio VR** profile (applies 66 mm IPD and modified optical distortion).
   - Adjust the wheel on the top/side of JioDive to synchronize lens separation.
2. **Capacitive Touch Not Registering:**
   - Ensure the conductive rubber pad inside the viewer makes clean contact with the touchscreen glass.
   - Screen protectors thicker than 0.3 mm may dampen capacitive triggers; enable Gaze Dwell mode in settings.
