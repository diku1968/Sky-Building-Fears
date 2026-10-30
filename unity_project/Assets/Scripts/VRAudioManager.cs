using UnityEngine;

namespace VRSkyView
{
    /// <summary>
    /// VR Audio Manager for Unity 6.
    /// Controls ambient rooftop wind, distant city traffic drone,
    /// relaxing background synth pads, and volume mixing.
    /// </summary>
    public class VRAudioManager : MonoBehaviour
    {
        public static VRAudioManager Instance { get; private set; }

        [Header("Audio Sources")]
        [SerializeField] private AudioSource windAudioSource;
        [SerializeField] private AudioSource cityTrafficAudioSource;
        [SerializeField] private AudioSource ambientMusicAudioSource;
        [SerializeField] private AudioSource narrationAudioSource;

        [Header("Volume Controls")]
        [Range(0f, 1f)] public float masterVolume = 0.85f;
        [Range(0f, 1f)] public float windVolume = 0.70f;
        [Range(0f, 1f)] public float trafficVolume = 0.55f;
        [Range(0f, 1f)] public float ambientMusicVolume = 0.45f;
        [Range(0f, 1f)] public float narrationVolume = 1.0f;

        private void Awake()
        {
            if (Instance != null && Instance != this)
            {
                Destroy(gameObject);
                return;
            }
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }

        private void Start()
        {
            ApplyVolumes();
        }

        public void SetMasterVolume(float vol)
        {
            masterVolume = Mathf.Clamp01(vol);
            ApplyVolumes();
        }

        public void SetWindVolume(float vol)
        {
            windVolume = Mathf.Clamp01(vol);
            ApplyVolumes();
        }

        public void SetTrafficVolume(float vol)
        {
            trafficVolume = Mathf.Clamp01(vol);
            ApplyVolumes();
        }

        public void SetAmbientMusicVolume(float vol)
        {
            ambientMusicVolume = Mathf.Clamp01(vol);
            ApplyVolumes();
        }

        public void SetNarrationVolume(float vol)
        {
            narrationVolume = Mathf.Clamp01(vol);
            ApplyVolumes();
        }

        public void ApplyVolumes()
        {
            if (windAudioSource != null) windAudioSource.volume = windVolume * masterVolume;
            if (cityTrafficAudioSource != null) cityTrafficAudioSource.volume = trafficVolume * masterVolume;
            if (ambientMusicAudioSource != null) ambientMusicAudioSource.volume = ambientMusicVolume * masterVolume;
            if (narrationAudioSource != null) narrationAudioSource.volume = narrationVolume * masterVolume;
        }

        public void ToggleMute()
        {
            AudioListener.pause = !AudioListener.pause;
        }
    }
}
