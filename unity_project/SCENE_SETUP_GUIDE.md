# VR SKY VIEW – Unity 6 Scene Setup Guide

This document describes how to construct and wire the three primary scenes in **Unity 6** using the **Google Cardboard XR Plugin**.

---

## 1. Project Prerequisites in Unity 6

1. **Unity Version:** Unity 6 LTS (6000.0.x or newer).
2. **Package Manager Dependencies:**
   - **XR Plugin Management** (com.unity.xr.management)
   - **Google Cardboard XR Plugin for Unity**:
     Add from Git URL: `https://github.com/googlevr/cardboard-xr-plugin.git`
   - **Universal Render Pipeline (URP)** or Built-in Render Pipeline (Mobile/Unlit).
3. **Project Settings > XR Plug-in Management:**
   - Under the Android tab, check **Cardboard XR**.

---

## 2. Scene 1: MainMenu (`Assets/Scenes/MainMenu.unity`)

### Hierarchy
```text
MainMenu
├── Directional Light (Warm Sunset color #FFA45B, Intensity 1.1)
├── Canvas (Render Mode: Screen Space - Overlay or World Space)
│   ├── Background Panel (Subtle dark blue gradient)
│   ├── TitleText: "VR SKY VIEW"
│   ├── SubtitleText: "Skyscraper Rooftop 360 Observation"
│   ├── Button_StartExperience ("ENTER VR EXPERIENCE")
│   ├── Button_ToggleVRMode ("VR Mode: ON/OFF")
│   ├── Button_Settings ("Headset Settings")
│   ├── Button_Exit ("Exit")
│   └── Panel_HeadsetSelection
│       ├── Button_CardboardV2 ("Google Cardboard V2")
│       ├── Button_JioDive ("JioDive / Jio VR")
│       └── Button_VRBox ("Generic VR Box")
├── MainMenuController (Attach MainMenuUI.cs)
└── EventSystem
```

### Script & Inspector Wiring
- Attach `MainMenuUI.cs` to `MainMenuController`.
- In Button `Button_StartExperience`'s `OnClick()` event, wire `MainMenuUI.StartVRExperience()`.
- In Button `Button_Exit`'s `OnClick()` event, wire `MainMenuUI.ExitApplication()`.
- In Headset buttons, wire `MainMenuUI.SelectHeadsetProfile("CardboardV2")`, `("JioDive")`, etc.

---

## 3. Scene 2 & 3: ObservationDeck & SkyCity (`Assets/Scenes/ObservationDeck.unity`)

### Hierarchy
```text
ObservationDeck
├── --- MANAGERS ---
│   ├── CardboardVRManager (Attach CardboardVRManager.cs)
│   ├── VRAudioManager (Attach VRAudioManager.cs + 4 AudioSources)
│   ├── ComfortAndSettings (Attach ComfortAndSettings.cs)
│   └── CityTrafficSystem (Attach CityTrafficSystem.cs)
│
├── --- PLAYER RIG ---
│   └── CameraRig (Position: 0, 1.6, 0)
│       ├── HeadTracking (Attach HeadTracking3DoF.cs)
│       ├── CardboardTrigger (Attach CardboardTrigger.cs)
│       ├── Main Camera (Tag: MainCamera)
│       ├── GazeReticle (Small ring quad at Z = 2.0m, unlit white)
│       └── ComfortVignetteQuad (Screen quad with CardboardVignette.shader)
│
├── --- OBSERVATION DECK ---
│   ├── ObservationDeckController (Attach ObservationDeckController.cs)
│   ├── DeckFloor (14m x 14m wooden deck at Y = 0)
│   ├── SafetyRailings (Chrome handrails at Y = 1.3m + vertical balusters)
│   ├── TemperedGlassWindbreaks (Transparent blue tint quad #3080C0 with 35% alpha)
│   └── ViewpointMarkers
│       ├── Point_NorthVista (0, 0, 5)
│       ├── Point_SunsetLounge (-5, 0, 0)
│       └── Point_HelipadBalcony (5, 0, 0)
│
├── --- SKYCITY ENVIRONMENT ---
│   ├── SkyscraperCluster (40+ extruded cube towers from Y = -380 to Y = +100)
│   │   ├── Materials: MobileCityUnlit.shader with window emission masks
│   │   └── ApexSpire (Super tall 1,450-ft tower to the North)
│   ├── TrafficAvenues (4 intersecting ground streets at Y = -380)
│   ├── SkyDome (Hemisphere with sunset/twilight gradient)
│   └── MountainHorizon (Silhouette ring at radius = 800m)
│
├── --- SIGHTSEEING TOUR SYSTEM ---
│   └── SightseeingGuide (Attach SightseeingGuide.cs)
│       ├── Hotspot_ApexSpire (World Target: ApexSpire tip)
│       ├── Hotspot_FinancialCore (World Target: South tower cluster)
│       ├── Hotspot_GrandAvenue (World Target: Avenue traffic directly below)
│       ├── Hotspot_SunsetBay (World Target: Western horizon)
│       └── SpatialNarrationCard (World Space Canvas at Z = 2.2m)
│
└── --- LIGHTING & ENVIRONMENT ---
    ├── Directional Light (Rotation: 25, -45, 0)
    └── Reflection Probe (Rooftop panoramic capture)
```

---

## 4. Lighting & Performance Optimization Setup

1. **Window > Rendering > Lighting Settings:**
   - **Lightmapping Mode:** Baked Indirect or Subtractive.
   - **Lightmapper:** Progressive GPU.
   - Mark all static skyscraper buildings and observation deck geometry as **Static** (Lightmap Static & Batching Static).
   - Bake lightmaps at 10-15 texels/unit to keep memory under 35 MB.
2. **Quality Settings:**
   - Real-time shadows: Disabled or Hard Shadows Only (Distance = 40m).
   - Anti-aliasing: 2x MSAA (or disabled on budget devices).
   - V-Sync: Off (Application.targetFrameRate = 60).
3. **Occlusion Culling:**
   - Run **Bake** in the Occlusion Culling window to cull occluded skyscrapers.
