using UnityEngine;

namespace VRSkyView
{
    /// <summary>
    /// Manages the rooftop observation deck environment:
    /// - Protective perimeter railings and safety glass windbreaks
    /// - Seated observation viewpoints (North Vista, Sunset Lounge, Helipad)
    /// - Gaze reticle positioning and comfort boundaries
    /// </summary>
    public class ObservationDeckController : MonoBehaviour
    {
        [Header("Observation Viewpoints")]
        [SerializeField] private Transform[] viewpointPositions;
        [SerializeField] private Transform playerRigTransform;

        [Header("Deck Architecture")]
        [SerializeField] private GameObject protectiveRailings;
        [SerializeField] private GameObject temperedGlassWindbreaks;
        [SerializeField] private GameObject observationTelescopes;

        [Header("Gaze Reticle")]
        [SerializeField] private Transform reticleTransform;
        [SerializeField] private float defaultReticleDistance = 2.0f;

        private int currentViewpointIndex = 0;

        private void Start()
        {
            // Position player safely at default North observation spot
            if (viewpointPositions != null && viewpointPositions.Length > 0 && playerRigTransform != null)
            {
                SwitchViewpoint(0);
            }
        }

        private void Update()
        {
            UpdateReticlePosition();
        }

        /// <summary>
        /// Updates the 3D gaze reticle distance based on gaze raycast.
        /// </summary>
        private void UpdateReticlePosition()
        {
            if (reticleTransform == null || Camera.main == null) return;

            Ray ray = new Ray(Camera.main.transform.position, Camera.main.transform.forward);
            float targetDistance = defaultReticleDistance;

            if (Physics.Raycast(ray, out RaycastHit hit, 50f))
            {
                targetDistance = Mathf.Clamp(hit.distance * 0.9f, 0.5f, defaultReticleDistance);
            }

            reticleTransform.position = Camera.main.transform.position + Camera.main.transform.forward * targetDistance;
            reticleTransform.rotation = Quaternion.LookRotation(Camera.main.transform.forward);
        }

        /// <summary>
        /// Comfortably transitions player between protected observation zones without motion sickness.
        /// </summary>
        public void SwitchViewpoint(int index)
        {
            if (viewpointPositions == null || index < 0 || index >= viewpointPositions.Length) return;

            currentViewpointIndex = index;
            Transform targetSpot = viewpointPositions[currentViewpointIndex];

            if (playerRigTransform != null && targetSpot != null)
            {
                // Instant comfortable transition (zero motion sickness)
                playerRigTransform.position = targetSpot.position;
                if (CardboardVRManager.Instance != null)
                {
                    CardboardVRManager.Instance.Recenter();
                }
            }
        }
    }
}
