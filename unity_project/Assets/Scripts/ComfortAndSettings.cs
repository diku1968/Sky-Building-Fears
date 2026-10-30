using UnityEngine;

namespace VRSkyView
{
    /// <summary>
    /// Comfort and Graphics Configuration Controller for Unity 6.
    /// Manages graphics scalability tiers (Low, Med, High for stable 60 FPS on Android),
    /// field-of-view tunneling vignette for motion sickness prevention,
    /// and optical presets for Google Cardboard, JioDive, and VR Box.
    /// </summary>
    public class ComfortAndSettings : MonoBehaviour
    {
        public enum GraphicsPreset { Low, Medium, High }

        [Header("Comfort Settings")]
        [SerializeField] private GameObject comfortVignetteQuad;
        [SerializeField] private Material vignetteMaterial;
        [SerializeField] private bool seatedMode = true;

        [Header("Graphics Tiers")]
        [SerializeField] private GraphicsPreset currentGraphicsPreset = GraphicsPreset.High;

        private void Start()
        {
            ApplyGraphicsPreset(currentGraphicsPreset);
        }

        public void SetSeatedMode(bool enabled)
        {
            seatedMode = enabled;
            HeadTracking3DoF tracker = FindFirstObjectByType<HeadTracking3DoF>();
            if (tracker != null)
            {
                tracker.SetSeatedMode(enabled);
            }
        }

        public void SetComfortVignette(bool enabled)
        {
            if (comfortVignetteQuad != null)
            {
                comfortVignetteQuad.SetActive(enabled);
            }
        }

        public void ApplyGraphicsPreset(GraphicsPreset preset)
        {
            currentGraphicsPreset = preset;
            switch (preset)
            {
                case GraphicsPreset.Low:
                    QualitySettings.SetQualityLevel(0, true);
                    QualitySettings.vSyncCount = 0;
                    QualitySettings.shadowDistance = 0f; // Bake lighting for mobile
                    QualitySettings.maximumLODLevel = 1;
                    Application.targetFrameRate = 60;
                    break;

                case GraphicsPreset.Medium:
                    QualitySettings.SetQualityLevel(1, true);
                    QualitySettings.vSyncCount = 0;
                    QualitySettings.shadowDistance = 35f;
                    QualitySettings.maximumLODLevel = 0;
                    Application.targetFrameRate = 60;
                    break;

                case GraphicsPreset.High:
                    QualitySettings.SetQualityLevel(2, true);
                    QualitySettings.vSyncCount = 0;
                    QualitySettings.shadowDistance = 80f;
                    QualitySettings.maximumLODLevel = 0;
                    Application.targetFrameRate = 60;
                    break;
            }
            Debug.Log($"[ComfortAndSettings] Applied {preset} graphics preset.");
        }

        public void SetHeadsetPreset(string profileName)
        {
            float ipd = 64f;
            switch (profileName.ToLower())
            {
                case "jiodive":
                    ipd = 66f;
                    break;
                case "vrbox":
                    ipd = 62f;
                    break;
                case "cardboard_v1":
                    ipd = 60f;
                    break;
                case "cardboard_v2":
                default:
                    ipd = 64f;
                    break;
            }

            if (CardboardVRManager.Instance != null)
            {
                CardboardVRManager.Instance.SetIPDMm(ipd);
            }
        }
    }
}
