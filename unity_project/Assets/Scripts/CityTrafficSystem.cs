using System.Collections.Generic;
using UnityEngine;

namespace VRSkyView
{
    /// <summary>
    /// High-performance Object-Pooled traffic system for mobile Android VR (Unity 6).
    /// Simulates 200+ headlights and taillights on ground avenues 1,000 ft below
    /// with zero garbage collection allocations to sustain a locked 60 FPS.
    /// </summary>
    public class CityTrafficSystem : MonoBehaviour
    {
        [System.Serializable]
        public class TrafficLane
        {
            public Vector3 startPoint;
            public Vector3 endPoint;
            public float speed = 25f;
            public bool isHeadlights = true;
        }

        [Header("Traffic Configuration")]
        [SerializeField] private GameObject headlightPrefab;
        [SerializeField] private GameObject taillightPrefab;
        [SerializeField] private int totalPooledVehicles = 180;
        [SerializeField] private TrafficLane[] lanes;

        private class PooledVehicle
        {
            public GameObject gameObject;
            public Transform transform;
            public TrafficLane lane;
            public float progress;
            public float speed;
        }

        private readonly List<PooledVehicle> activeVehicles = new List<PooledVehicle>();

        private void Start()
        {
            InitializeDefaultLanes();
            BuildVehiclePool();
        }

        private void InitializeDefaultLanes()
        {
            if (lanes == null || lanes.Length == 0)
            {
                // Default grid avenues 380 meters below rooftop
                lanes = new TrafficLane[]
                {
                    new TrafficLane { startPoint = new Vector3(-300f, -380f, 15f), endPoint = new Vector3(300f, -380f, 15f), speed = 28f, isHeadlights = true },
                    new TrafficLane { startPoint = new Vector3(300f, -380f, -15f), endPoint = new Vector3(-300f, -380f, -15f), speed = 24f, isHeadlights = false },
                    new TrafficLane { startPoint = new Vector3(20f, -380f, -300f), endPoint = new Vector3(20f, -380f, 300f), speed = 30f, isHeadlights = true },
                    new TrafficLane { startPoint = new Vector3(-20f, -380f, 300f), endPoint = new Vector3(-20f, -380f, -300f), speed = 26f, isHeadlights = false }
                };
            }
        }

        private void BuildVehiclePool()
        {
            if (headlightPrefab == null)
            {
                headlightPrefab = CreateDefaultVehiclePrimitive(Color.white);
            }
            if (taillightPrefab == null)
            {
                taillightPrefab = CreateDefaultVehiclePrimitive(new Color(1f, 0.1f, 0.1f));
            }

            for (int i = 0; i < totalPooledVehicles; i++)
            {
                TrafficLane lane = lanes[Random.Range(0, lanes.Length)];
                GameObject prefab = lane.isHeadlights ? headlightPrefab : taillightPrefab;
                GameObject instance = Instantiate(prefab, transform);

                PooledVehicle v = new PooledVehicle
                {
                    gameObject = instance,
                    transform = instance.transform,
                    lane = lane,
                    progress = Random.value,
                    speed = lane.speed * Random.Range(0.85f, 1.25f)
                };

                activeVehicles.Add(v);
            }
        }

        private GameObject CreateDefaultVehiclePrimitive(Color lightColor)
        {
            GameObject go = GameObject.CreatePrimitive(PrimitiveType.Cube);
            go.transform.localScale = new Vector3(2.5f, 1.2f, 4.5f);
            Destroy(go.GetComponent<Collider>());

            Renderer r = go.GetComponent<Renderer>();
            Material mat = new Material(Shader.Find("Mobile/Unlit (Supports Lightmap)") ?? Shader.Find("Unlit/Color"));
            mat.color = lightColor;
            r.sharedMaterial = mat;
            r.shadowCastingMode = UnityEngine.Rendering.ShadowCastingMode.Off;
            r.receiveShadows = false;

            return go;
        }

        private void Update()
        {
            float dt = Time.deltaTime;

            for (int i = 0; i < activeVehicles.Count; i++)
            {
                PooledVehicle v = activeVehicles[i];
                float totalDistance = Vector3.Distance(v.lane.startPoint, v.lane.endPoint);
                float step = (v.speed / totalDistance) * dt;

                v.progress += step;
                if (v.progress >= 1.0f)
                {
                    v.progress = 0f;
                }

                v.transform.position = Vector3.Lerp(v.lane.startPoint, v.lane.endPoint, v.progress);
                v.transform.rotation = Quaternion.LookRotation(v.lane.endPoint - v.lane.startPoint);
            }
        }
    }
}
