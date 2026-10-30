using UnityEngine;

namespace VRSkyView
{
    /// <summary>
    /// 3DoF Rotational Head Tracking for Google Cardboard, JioDive, and VR Box.
    /// Utilizes Android hardware gyroscope with smooth slerp damping,
    /// seated mode vertical pitch clamping, and editor/touch mouse-drag fallback.
    /// </summary>
    public class HeadTracking3DoF : MonoBehaviour
    {
        [Header("Tracking Settings")]
        [SerializeField] private bool useHardwareGyro = true;
        [SerializeField] private float smoothingFactor = 15f;

        [Header("Comfort & Ergonomics")]
        [SerializeField] private bool seatedModeEnabled = true;
        [SerializeField] private float maxUpPitch = 45f;
        [SerializeField] private float maxDownPitch = 48f;

        [Header("Fallback Testing")]
        [SerializeField] private float mouseSensitivity = 2.5f;

        private Quaternion gyroOffset = Quaternion.identity;
        private float mouseYaw = 0f;
        private float mousePitch = 0f;
        private bool hasGyro = false;

        private void Start()
        {
            if (SystemInfo.supportsGyroscope && useHardwareGyro)
            {
                Input.gyro.enabled = true;
                Input.gyro.updateInterval = 0.0167f; // 60 Hz
                hasGyro = true;
                Recenter();
            }
            else
            {
                Debug.Log("[HeadTracking3DoF] Hardware gyro not available. Fallback touch/mouse controls active.");
            }

            if (CardboardVRManager.Instance != null)
            {
                CardboardVRManager.Instance.OnRecenterRequested += Recenter;
            }
        }

        private void OnDestroy()
        {
            if (CardboardVRManager.Instance != null)
            {
                CardboardVRManager.Instance.OnRecenterRequested -= Recenter;
            }
        }

        private void Update()
        {
            Quaternion targetRotation;

            if (hasGyro && Input.gyro.enabled)
            {
                // Convert Android Gyro right-handed coordinate system to Unity left-handed system
                Quaternion rawGyro = Input.gyro.attitude;
                Quaternion converted = new Quaternion(rawGyro.x, rawGyro.y, -rawGyro.z, -rawGyro.w);
                converted = Quaternion.Euler(90f, 0f, 0f) * converted;

                targetRotation = gyroOffset * converted;
            }
            else
            {
                // Fallback touch / mouse looking for Editor and devices without gyro
                HandleFallbackInput();
                targetRotation = Quaternion.Euler(mousePitch, mouseYaw, 0f);
            }

            // Apply seated comfort constraints
            if (seatedModeEnabled)
            {
                Vector3 euler = targetRotation.eulerAngles;
                float pitch = euler.x;
                if (pitch > 180f) pitch -= 360f;
                pitch = Mathf.Clamp(pitch, -maxUpPitch, maxDownPitch);
                targetRotation = Quaternion.Euler(pitch, euler.y, 0f); // Restrict roll for seated comfort
            }

            // Smooth interpolation to avoid micro-tremors
            transform.localRotation = Quaternion.Slerp(transform.localRotation, targetRotation, Time.deltaTime * smoothingFactor);
        }

        private void HandleFallbackInput()
        {
            if (Input.GetMouseButton(0) || Input.touchCount > 0)
            {
                float deltaX = Input.GetAxis("Mouse X") * mouseSensitivity * 10f;
                float deltaY = Input.GetAxis("Mouse Y") * mouseSensitivity * 10f;

                mouseYaw += deltaX;
                mousePitch -= deltaY;
                mousePitch = Mathf.Clamp(mousePitch, -maxUpPitch, maxDownPitch);
            }
        }

        public void Recenter()
        {
            if (hasGyro && Input.gyro.enabled)
            {
                Quaternion rawGyro = Input.gyro.attitude;
                Quaternion converted = new Quaternion(rawGyro.x, rawGyro.y, -rawGyro.z, -rawGyro.w);
                converted = Quaternion.Euler(90f, 0f, 0f) * converted;
                gyroOffset = Quaternion.Inverse(converted);
            }
            mouseYaw = 0f;
            mousePitch = 0f;
            Debug.Log("[HeadTracking3DoF] Headset orientation zeroed.");
        }

        public void SetSeatedMode(bool isSeated)
        {
            seatedModeEnabled = isSeated;
        }
    }
}
