# VR SKY VIEW – Unity 6 Android APK Build Instructions

Follow these step-by-step instructions to compile and export the **VR Sky View** Android APK in Unity 6 using the official Google Cardboard XR Plugin.

---

## Step 1: Open or Create Project in Unity 6

1. Launch **Unity Hub**.
2. Click **New Project** > Select **3D (URP)** or **3D Core** with Unity 6 (6000.0 LTS).
3. Name the project `VRSkyView`.
4. Copy the scripts from `/unity_project/Assets/Scripts/` and shaders from `/unity_project/Assets/Shaders/` into your Unity project's `Assets/` directory.

---

## Step 2: Install Cardboard XR Plugin in Unity 6

1. Open **Window > Package Manager**.
2. Click the `+` button in the top left > Select **Add package from git URL...**
3. Enter:
   ```text
   https://github.com/googlevr/cardboard-xr-plugin.git
   ```
4. Click **Add** and wait for Unity to compile the package.

---

## Step 3: Configure Project Settings

1. Open **Edit > Project Settings > XR Plug-in Management**:
   - Select the **Android** tab (robot icon).
   - Check **Cardboard XR**.
2. Open **Edit > Project Settings > Player**:
   - **Company Name:** `VRSkyViewStudio`
   - **Product Name:** `VR Sky View`
   - **Package Name:** `com.aistudio.vrskyview`
   - **Default Orientation:** `Landscape Left`
   - **Minimum API Level:** Android 7.0 'Nougat' (API Level 24)
   - **Target API Level:** Android 14 (API Level 34) or Automatic
   - **Scripting Backend:** `IL2CPP`
   - **Target Architectures:** Check `ARM64` (uncheck ARMv7)
   - **Graphics APIs:** Ensure `OpenGLES3` and `Vulkan` are listed.

---

## Step 4: Add Scenes to Build

1. Open **File > Build Profiles** (or **Build Settings**).
2. Switch platform to **Android**.
3. Under **Scenes in Build**, add:
   1. `Assets/Scenes/MainMenu.unity` (Index 0)
   2. `Assets/Scenes/ObservationDeck.unity` (Index 1)

---

## Step 5: Build and Run APK

1. Connect your Android smartphone via USB with **USB Debugging** enabled.
2. Click **Build and Run** (or **Build** to generate an standalone `.apk` file).
3. Save the file as `VRSkyView.apk`.
4. Once installed, launch **VR Sky View**, mount the phone into your Google Cardboard, JioDive, or VR Box viewer, and enjoy the 1,000-ft skyscraper observation deck!
