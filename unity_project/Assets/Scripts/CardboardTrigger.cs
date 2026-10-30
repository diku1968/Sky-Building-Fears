using UnityEngine;
using UnityEngine.Events;

namespace VRSkyView
{
    /// <summary>
    /// Handles Cardboard hardware trigger events:
    /// - Conductive touch button (Cardboard V2 taps screen)
    /// - Touch screen tap
    /// - Mouse click / Space bar in Unity Editor
    /// - Volume button trigger on compatible mobile VR headsets
    /// </summary>
    public class CardboardTrigger : MonoBehaviour
    {
        [Header("Events")]
        public UnityEvent onTriggerPressed;

        private void Update()
        {
            bool triggered = false;

            // 1. Android screen tap (conductive touch button from Cardboard V2 / JioDive)
            if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Began)
            {
                triggered = true;
            }

            // 2. Mouse click / Spacebar (Editor testing)
            if (Input.GetMouseButtonDown(0) || Input.GetKeyDown(KeyCode.Space))
            {
                triggered = true;
            }

            // 3. Hardware Key Trigger (Volume Down on some Bluetooth VR controllers)
            if (Input.GetKeyDown(KeyCode.JoystickButton0) || Input.GetKeyDown(KeyCode.VolumeDown))
            {
                triggered = true;
            }

            if (triggered)
            {
                Debug.Log("[CardboardTrigger] Trigger activated.");
                onTriggerPressed?.Invoke();

                SightseeingGuide guide = FindFirstObjectByType<SightseeingGuide>();
                if (guide != null)
                {
                    guide.TriggerCurrentGaze();
                }
            }
        }
    }
}
