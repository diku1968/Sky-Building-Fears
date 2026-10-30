using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

namespace VRSkyView
{
    /// <summary>
    /// Controls the MainMenu scene in Unity 6:
    /// - Start Experience (loads ObservationDeck / SkyCity)
    /// - Headset optics selection (Cardboard, JioDive, VR Box)
    /// - Settings modal
    /// - Exit app
    /// </summary>
    public class MainMenuUI : MonoBehaviour
    {
        [Header("Scene Routing")]
        [SerializeField] private string vrExperienceSceneName = "ObservationDeck";

        [Header("UI Panels")]
        [SerializeField] private GameObject settingsPanel;
        [SerializeField] private Text statusText;

        private void Start()
        {
            if (settingsPanel != null) settingsPanel.SetActive(false);
            if (statusText != null)
            {
                statusText.text = SystemInfo.supportsGyroscope
                    ? "3DoF Hardware Gyroscope Ready"
                    : "Touch Drag Mode Active (No Gyro)";
            }
        }

        public void StartVRExperience()
        {
            Debug.Log($"[MainMenuUI] Loading VR scene: {vrExperienceSceneName}");
            SceneManager.LoadScene(vrExperienceSceneName);
        }

        public void OpenSettings()
        {
            if (settingsPanel != null) settingsPanel.SetActive(true);
        }

        public void CloseSettings()
        {
            if (settingsPanel != null) settingsPanel.SetActive(false);
        }

        public void SelectHeadsetProfile(string profile)
        {
            if (CardboardVRManager.Instance != null)
            {
                ComfortAndSettings comfort = FindFirstObjectByType<ComfortAndSettings>();
                if (comfort != null)
                {
                    comfort.SetHeadsetPreset(profile);
                }
            }
            if (statusText != null) statusText.text = $"Profile set: {profile}";
        }

        public void RecenterHeadset()
        {
            if (CardboardVRManager.Instance != null)
            {
                CardboardVRManager.Instance.Recenter();
            }
        }

        public void ExitApplication()
        {
            Debug.Log("[MainMenuUI] Exiting VR Sky View.");
            Application.Quit();
        }
    }
}
