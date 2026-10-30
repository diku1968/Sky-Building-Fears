using System;
using UnityEngine;
using UnityEngine.UI;

namespace VRSkyView
{
    /// <summary>
    /// Interactive Sightseeing Tour Guide for Unity 6 VR.
    /// Handles 3D gaze-dwell detection on points of interest,
    /// triggers audio narration, and presents spatial billboard informational cards.
    /// </summary>
    public class SightseeingGuide : MonoBehaviour
    {
        [System.Serializable]
        public class SightseeingSpot
        {
            public string id;
            public string title;
            [TextArea] public string description;
            public AudioClip narrationClip;
            public Transform worldTarget;
            public float triggerGazeAngle = 12f;
        }

        [Header("Sightseeing Spots")]
        [SerializeField] private SightseeingSpot[] spots;
        [SerializeField] private float dwellTimeRequired = 1.3f;

        [Header("Spatial HUD UI")]
        [SerializeField] private GameObject narrationCardUI;
        [SerializeField] private Text titleText;
        [SerializeField] private Text descriptionText;
        [SerializeField] private Image dwellProgressImage;

        [Header("Audio")]
        [SerializeField] private AudioSource narrationAudioSource;

        private SightseeingSpot currentTargetSpot = null;
        private float currentDwellTimer = 0f;
        private string lastTriggeredId = "";

        private void Start()
        {
            if (narrationCardUI != null) narrationCardUI.SetActive(false);
            if (dwellProgressImage != null) dwellProgressImage.fillAmount = 0f;
        }

        private void Update()
        {
            CheckGaze();
        }

        private void CheckGaze()
        {
            if (Camera.main == null || spots == null) return;

            Vector3 cameraForward = Camera.main.transform.forward;
            Vector3 cameraPosition = Camera.main.transform.position;

            SightseeingSpot focusedSpot = null;

            foreach (var spot in spots)
            {
                if (spot.worldTarget == null) continue;

                Vector3 directionToSpot = (spot.worldTarget.position - cameraPosition).normalized;
                float angle = Vector3.Angle(cameraForward, directionToSpot);

                if (angle < spot.triggerGazeAngle)
                {
                    focusedSpot = spot;
                    break;
                }
            }

            if (focusedSpot != null)
            {
                if (currentTargetSpot == focusedSpot)
                {
                    currentDwellTimer += Time.deltaTime;
                    float progress = Mathf.Clamp01(currentDwellTimer / dwellTimeRequired);

                    if (dwellProgressImage != null)
                    {
                        dwellProgressImage.fillAmount = progress;
                    }

                    if (currentDwellTimer >= dwellTimeRequired && lastTriggeredId != focusedSpot.id)
                    {
                        TriggerSpot(focusedSpot);
                    }
                }
                else
                {
                    currentTargetSpot = focusedSpot;
                    currentDwellTimer = 0f;
                }
            }
            else
            {
                currentTargetSpot = null;
                currentDwellTimer = 0f;
                if (dwellProgressImage != null)
                {
                    dwellProgressImage.fillAmount = 0f;
                }
            }
        }

        public void TriggerCurrentGaze()
        {
            if (currentTargetSpot != null)
            {
                TriggerSpot(currentTargetSpot);
            }
        }

        private void TriggerSpot(SightseeingSpot spot)
        {
            lastTriggeredId = spot.id;
            Debug.Log($"[SightseeingGuide] Triggered tour point: {spot.title}");

            if (narrationCardUI != null)
            {
                narrationCardUI.SetActive(true);
                if (titleText != null) titleText.text = spot.title;
                if (descriptionText != null) descriptionText.text = spot.description;

                // Position card in front of camera at comfortable reading distance
                narrationCardUI.transform.position = Camera.main.transform.position + Camera.main.transform.forward * 2.2f + Vector3.down * 0.35f;
                narrationCardUI.transform.rotation = Quaternion.LookRotation(narrationCardUI.transform.position - Camera.main.transform.position);
            }

            if (narrationAudioSource != null && spot.narrationClip != null)
            {
                narrationAudioSource.Stop();
                narrationAudioSource.clip = spot.narrationClip;
                narrationAudioSource.Play();
            }
        }

        public void DismissNarration()
        {
            if (narrationCardUI != null) narrationCardUI.SetActive(false);
            if (narrationAudioSource != null) narrationAudioSource.Stop();
        }
    }
}
