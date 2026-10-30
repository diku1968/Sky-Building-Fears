using System;
using System.Collections;
using UnityEngine;
using UnityEngine.XR;
using UnityEngine.XR.Management;

namespace VRSkyView
{
    /// <summary>
    /// Core Google Cardboard XR Plugin Manager for Unity 6.
    /// Handles XR subsystem initialization, stereoscopic rendering mode toggle,
    /// device profile scanning (QR code), recentering, and framerate stabilization (60 FPS).
    /// Compatible with Google Cardboard, JioDive, and Generic VR Box viewers.
    /// </summary>
    public class CardboardVRManager : MonoBehaviour
    {
        public static CardboardVRManager Instance { get; private set; }

        [Header("XR Configuration")]
        [SerializeField] private bool initializeXROnStart = true;
        [SerializeField] private int targetFrameRate = 60;

        [Header("Stereoscopic Camera Rig")]
        [SerializeField] private Camera centerCamera;
        [SerializeField] private Camera leftEyeCamera;
        [SerializeField] private Camera rightEyeCamera;
        [SerializeField] private GameObject stereoCameraRig;
        [SerializeField] private GameObject monoCameraRig;

        [Header("State")]
        public bool IsVREnabled { get; private set; } = true;
        public float CurrentIPD { get; private set; } = 0.064f; // 64mm standard

        public event Action OnRecenterRequested;
        public event Action<bool> OnVRModeChanged;

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
            DontDestroyOnLoad(gameObject);

            // Lock target framerate for battery longevity and thermal stability
            Application.targetFrameRate = targetFrameRate;
            Screen.sleepTimeout = SleepTimeout.NeverSleep;
        }

        private IEnumerator Start()
        {
            if (initializeXROnStart)
            {
                yield return StartXRCoroutine();
            }
        }

        /// <summary>
        /// Initializes XR Display Subsystem using the Google Cardboard XR Plugin.
        /// </summary>
        public IEnumerator StartXRCoroutine()
        {
            Debug.Log("[CardboardVRManager] Initializing Cardboard XR Subsystem in Unity 6...");

            if (XRGeneralSettings.Instance == null || XRGeneralSettings.Instance.Manager == null)
            {
                Debug.LogWarning("[CardboardVRManager] XRGeneralSettings not configured. Falling back to dual-camera stereo rig.");
                EnableDualCameraStereo(true);
                yield break;
            }

            yield return XRGeneralSettings.Instance.Manager.InitializeLoader();

            if (XRGeneralSettings.Instance.Manager.activeLoader == null)
            {
                Debug.LogWarning("[CardboardVRManager] Failed to initialize Cardboard XR Loader. Using manual stereoscopic rig fallback.");
                EnableDualCameraStereo(true);
            }
            else
            {
                XRGeneralSettings.Instance.Manager.StartSubsystems();
                IsVREnabled = true;
                Debug.Log("[CardboardVRManager] Cardboard XR Subsystems started successfully.");
            }
        }

        /// <summary>
        /// Stops the XR subsystem.
        /// </summary>
        public void StopXR()
        {
            Debug.Log("[CardboardVRManager] Stopping XR Subsystems.");
            if (XRGeneralSettings.Instance != null && XRGeneralSettings.Instance.Manager != null && XRGeneralSettings.Instance.Manager.isInitializationComplete)
            {
                XRGeneralSettings.Instance.Manager.StopSubsystems();
                XRGeneralSettings.Instance.Manager.DeinitializeLoader();
            }
            EnableDualCameraStereo(false);
            IsVREnabled = false;
            OnVRModeChanged?.Invoke(false);
        }

        /// <summary>
        /// Toggle between Stereoscopic Cardboard Dual-Eye and Monoscopic 360 Fullscreen.
        /// </summary>
        public void ToggleVRMode()
        {
            SetVRMode(!IsVREnabled);
        }

        public void SetVRMode(bool enableVR)
        {
            IsVREnabled = enableVR;
            EnableDualCameraStereo(enableVR);
            OnVRModeChanged?.Invoke(enableVR);
        }

        private void EnableDualCameraStereo(bool enableStereo)
        {
            if (stereoCameraRig != null) stereoCameraRig.SetActive(enableStereo);
            if (monoCameraRig != null) monoCameraRig.SetActive(!enableStereo);

            if (enableStereo && leftEyeCamera != null && rightEyeCamera != null)
            {
                // Left Eye Viewport: Left half of screen (0, 0, 0.5, 1)
                leftEyeCamera.rect = new Rect(0f, 0f, 0.5f, 1f);
                leftEyeCamera.transform.localPosition = new Vector3(-CurrentIPD * 0.5f, 0f, 0f);

                // Right Eye Viewport: Right half of screen (0.5, 0, 0.5, 1)
                rightEyeCamera.rect = new Rect(0.5f, 0f, 0.5f, 1f);
                rightEyeCamera.transform.localPosition = new Vector3(CurrentIPD * 0.5f, 0f, 0f);
            }
        }

        /// <summary>
        /// Recenter camera heading to current gaze forward.
        /// </summary>
        public void Recenter()
        {
            InputTracking.Recenter();
            OnRecenterRequested?.Invoke();
            Debug.Log("[CardboardVRManager] Headset orientation recentered.");
        }

        /// <summary>
        /// Adjust inter-pupillary distance (IPD) in millimeters.
        /// </summary>
        public void SetIPDMm(float ipdMm)
        {
            CurrentIPD = Mathf.Clamp(ipdMm, 55f, 75f) / 1000f;
            if (leftEyeCamera != null && rightEyeCamera != null)
            {
                leftEyeCamera.transform.localPosition = new Vector3(-CurrentIPD * 0.5f, 0f, 0f);
                rightEyeCamera.transform.localPosition = new Vector3(CurrentIPD * 0.5f, 0f, 0f);
            }
        }

        /// <summary>
        /// Opens native Cardboard QR Code scanning profile activity on Android.
        /// </summary>
        public void ScanCardboardQRCode()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            try
            {
                using (AndroidJavaClass playerClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                {
                    AndroidJavaObject activity = playerClass.GetStatic<AndroidJavaObject>("currentActivity");
                    using (AndroidJavaClass cardboardApi = new AndroidJavaClass("com.google.cardboard.sdk.Cardboard"))
                    {
                        cardboardApi.CallStatic("scanQrCodeAndSaveDeviceParams", activity);
                    }
                }
            }
            catch (Exception ex)
            {
                Debug.LogWarning("[CardboardVRManager] Cardboard QR scan call failed: " + ex.Message);
            }
#else
            Debug.Log("[CardboardVRManager] Cardboard QR scan simulated in Editor.");
#endif
        }

        private void OnDestroy()
        {
            StopXR();
        }
    }
}
