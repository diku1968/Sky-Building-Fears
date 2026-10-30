package com.example.vr.gl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.example.vr.model.DeckLocation
import com.example.vr.model.GraphicsQuality
import com.example.vr.model.HeadsetProfile
import com.example.vr.model.SightseeingHotspot
import com.example.vr.model.TimeOfDay
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Random
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance stereoscopic 3D OpenGL ES 2.0/3.0 renderer for VR Sky View.
 * Renders skyscraper observation deck, railings, glass windbreaks, 3D city buildings,
 * moving vehicle traffic trails, sky dome, mountain silhouettes, and interactive 3D gaze reticle.
 */
class VrRenderer(
    private val context: Context,
    val onHotspotGazeProgress: (hotspot: SightseeingHotspot?, progress: Float) -> Unit,
    val onHotspotTriggered: (hotspot: SightseeingHotspot) -> Unit
) : GLSurfaceView.Renderer {

    // Head orientation matrix (updated from HeadTracker)
    val headMatrix = FloatArray(16)

    // Configuration
    var isStereoVRMode: Boolean = true
    var headsetProfile: HeadsetProfile = HeadsetProfile.GOOGLE_CARDBOARD_V2
    var ipdMm: Float = 64f
    var fovDegrees: Float = 90f
    var timeOfDay: TimeOfDay = TimeOfDay.SUNSET
    var graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH
    var currentLocation: DeckLocation = DeckLocation.NORTH_OBSERVATION
    var comfortVignetteEnabled: Boolean = true

    // Sightseeing hotspots in 3D
    val hotspots = listOf(
        SightseeingHotspot(
            id = "apex_spire",
            title = "The Apex Spire",
            yawDegrees = 0f,
            pitchDegrees = 8f,
            distanceKm = 0.45f,
            shortDescription = "1,450 ft architectural supertall with glowing communications crown.",
            narrationScript = "Directly to the north rises The Apex Spire, standing at 1,450 feet. Notice the red aviation warning beacons illuminating its observation crown."
        ),
        SightseeingHotspot(
            id = "financial_core",
            title = "Financial Plaza Towers",
            yawDegrees = 65f,
            pitchDegrees = -12f,
            distanceKm = 0.85f,
            shortDescription = "Dense geometric towers with illuminated offices and aerial skybridge.",
            narrationScript = "To your east lies the Financial Plaza, home to modern international commerce. Over forty illuminated corporate towers form a dense illuminated canyon below."
        ),
        SightseeingHotspot(
            id = "grand_avenue",
            title = "Grand Avenue Traffic",
            yawDegrees = -15f,
            pitchDegrees = -35f,
            distanceKm = 0.38f,
            shortDescription = "1,000 feet straight below, a 6-lane artery with flowing vehicle light trails.",
            narrationScript = "Looking down past the protective observation deck railing, you can see Grand Avenue 1,000 feet below. The continuous stream of white headlights and ruby taillights pulses with the rhythm of the city."
        ),
        SightseeingHotspot(
            id = "sunset_bay",
            title = "Sunset Ocean Harbor",
            yawDegrees = -95f,
            pitchDegrees = -4f,
            distanceKm = 3.2f,
            shortDescription = "Expansive ocean waterway with illuminated suspension bridge.",
            narrationScript = "To the west unfolds the ocean harbor bathed in the evening twilight. The suspension bridge cables reflect warm amber lights across the calm water."
        ),
        SightseeingHotspot(
            id = "skyline_heliport",
            title = "Skyline Helipad Perch",
            yawDegrees = 140f,
            pitchDegrees = -18f,
            distanceKm = 0.32f,
            shortDescription = "Rooftop helipad atop neighboring tower with perimeter strobes.",
            narrationScript = "Behind you to the southeast is the rooftop helipad of Tower Seven. The perimeter amber navigation lights assist executive charters arriving above the city."
        ),
        SightseeingHotspot(
            id = "emerald_ridge",
            title = "Emerald Ridge Mountains",
            yawDegrees = -150f,
            pitchDegrees = 4f,
            distanceKm = 12.0f,
            shortDescription = "Distant mountain silhouettes framing the city's western horizon.",
            narrationScript = "On the far horizon rise the Emerald Ridge mountain peaks. Their majestic silhouettes provide a peaceful natural boundary to this sprawling metropolis."
        )
    )

    // Screen dimensions
    private var screenWidth = 1920
    private var screenHeight = 1080

    // Matrix scratchpads
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val eyeMatrix = FloatArray(16)
    private val invertedView = FloatArray(16)

    // Shaders & Buffers
    private var colorProgram = 0
    private var uMvpLocation = 0
    private var aPositionLocation = 0
    private var aColorLocation = 0

    // Geometry buffers
    private var deckBuffer: FloatBuffer? = null
    private var railingBuffer: FloatBuffer? = null
    private var glassBuffer: FloatBuffer? = null
    private var buildingsBuffer: FloatBuffer? = null
    private var trafficBuffer: FloatBuffer? = null
    private var skyDomeBuffer: FloatBuffer? = null
    private var mountainsBuffer: FloatBuffer? = null
    private var reticleBuffer: FloatBuffer? = null
    private var hotspotMarkersBuffer: FloatBuffer? = null
    private var dividerLineBuffer: FloatBuffer? = null

    private var deckVertexCount = 0
    private var railingVertexCount = 0
    private var glassVertexCount = 0
    private var buildingsVertexCount = 0
    private var trafficVertexCount = 0
    private var skyDomeVertexCount = 0
    private var mountainsVertexCount = 0
    private var reticleVertexCount = 0

    // Animated traffic data
    private class TrafficVehicle(
        var x: Float,
        var z: Float,
        val y: Float,
        val speed: Float,
        val isNorthSouth: Boolean,
        val isHeadlight: Boolean
    )
    private val trafficVehicles = ArrayList<TrafficVehicle>()

    // Gaze tracking state
    private var activeGazeHotspot: SightseeingHotspot? = null
    private var gazeDwellStartTime: Long = 0L
    private val gazeDwellRequiredMs = 1200L
    private var lastTriggeredHotspotId: String? = null

    init {
        Matrix.setIdentityM(headMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.05f, 0.05f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        initShaders()
        generateGeometry()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    private fun initShaders() {
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 aPosition;
            attribute vec4 aColor;
            varying vec4 vColor;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vColor = aColor;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()

        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        colorProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        uMvpLocation = GLES20.glGetUniformLocation(colorProgram, "uMVPMatrix")
        aPositionLocation = GLES20.glGetAttribLocation(colorProgram, "aPosition")
        aColorLocation = GLES20.glGetAttribLocation(colorProgram, "aColor")
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    /**
     * Procedurally build the 3D VR Sky View environment:
     * - Observation Deck Platform (Floor, Balusters, Handrails, Safety Glass)
     * - City Skyscraper Cluster (Diverse heights, facades, lit window patterns, antennas)
     * - Traffic Avenues (Streets, cars)
     * - Mountain horizon silhouettes & Sky dome
     */
    private fun generateGeometry() {
        val random = Random(42)

        // 1. Observation Deck Floor (8m x 8m platform at y = 0.0)
        // Player eyes are at y = 1.6m (seated / comfortable eye level)
        val deckList = ArrayList<Float>()
        val deckSize = 7.0f
        // Checkerboard warm architectural deck tiles
        val tiles = 14
        val step = (deckSize * 2f) / tiles
        for (i in 0 until tiles) {
            for (j in 0 until tiles) {
                val x0 = -deckSize + i * step
                val x1 = x0 + step
                val z0 = -deckSize + j * step
                val z1 = z0 + step
                val isAlt = (i + j) % 2 == 0
                val c = if (isAlt) 0.28f else 0.22f
                val r = c * 1.05f
                val g = c * 0.95f
                val b = c * 0.85f

                // Quad as 2 triangles (x, y, z, r, g, b, a)
                addQuad(deckList,
                    x0, -1.6f, z0,
                    x1, -1.6f, z0,
                    x1, -1.6f, z1,
                    x0, -1.6f, z1,
                    r, g, b, 1.0f
                )
            }
        }
        deckBuffer = createBuffer(deckList)
        deckVertexCount = deckList.size / 7

        // 2. Safety Railing and Posts
        val railingList = ArrayList<Float>()
        val glassList = ArrayList<Float>()

        val railDist = deckSize - 0.2f
        val railHeight = -0.3f // 1.3m high relative to floor (-1.6m floor)
        val floorY = -1.6f

        // 4 perimeter edges
        val perimeter = listOf(
            Pair(floatArrayOf(-railDist, floorY, -railDist), floatArrayOf(railDist, floorY, -railDist)),
            Pair(floatArrayOf(railDist, floorY, -railDist), floatArrayOf(railDist, floorY, railDist)),
            Pair(floatArrayOf(railDist, floorY, railDist), floatArrayOf(-railDist, floorY, railDist)),
            Pair(floatArrayOf(-railDist, floorY, railDist), floatArrayOf(-railDist, floorY, -railDist))
        )

        for (edge in perimeter) {
            val p1 = edge.first
            val p2 = edge.second
            // Top heavy chrome handrail
            addThickLine(railingList, p1[0], railHeight, p1[2], p2[0], railHeight, p2[2], 0.08f, 0.75f, 0.78f, 0.82f, 1.0f)
            // Mid safety bar
            addThickLine(railingList, p1[0], (railHeight + floorY) * 0.5f, p1[2], p2[0], (railHeight + floorY) * 0.5f, p2[2], 0.04f, 0.65f, 0.68f, 0.72f, 1.0f)
            // Base kickplate
            addThickLine(railingList, p1[0], floorY + 0.08f, p1[2], p2[0], floorY + 0.08f, p2[2], 0.12f, 0.25f, 0.26f, 0.30f, 1.0f)

            // Vertical structural balusters every 1 meter
            val segs = 14
            for (s in 0..segs) {
                val t = s.toFloat() / segs
                val px = p1[0] + (p2[0] - p1[0]) * t
                val pz = p1[2] + (p2[2] - p1[2]) * t
                addThickLine(railingList, px, floorY, pz, px, railHeight, pz, 0.05f, 0.55f, 0.58f, 0.62f, 1.0f)
            }

            // Tempered Safety Glass Windbreak Panels (Translucent cyan/tinted reflection)
            addQuad(glassList,
                p1[0], floorY, p1[2],
                p2[0], floorY, p2[2],
                p2[0], railHeight, p2[2],
                p1[0], railHeight, p1[2],
                0.3f, 0.55f, 0.75f, 0.35f
            )
        }
        railingBuffer = createBuffer(railingList)
        railingVertexCount = railingList.size / 7

        glassBuffer = createBuffer(glassList)
        glassVertexCount = glassList.size / 7

        // 3. 3D City Skyscrapers Cluster
        val buildingsList = ArrayList<Float>()
        val buildingCount = graphicsQuality.buildingDensity

        // Observation skyscraper base tower below us
        addBuilding(buildingsList, 0f, -400f, 0f, 16f, 398.4f, 16f, 0.15f, 0.18f, 0.22f, random)

        // Generate surrounding towers
        for (b in 0 until buildingCount) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val dist = 35f + random.nextFloat() * 320f
            val bx = cos(angle) * dist
            val bz = sin(angle) * dist

            // Diverse building heights (-380 to +120m relative to observation deck)
            val towerHeight = 150f + random.nextFloat() * 320f
            val width = 18f + random.nextFloat() * 28f
            val depth = 18f + random.nextFloat() * 28f
            val topY = -380f + towerHeight

            // Building facade colors
            val baseShade = 0.12f + random.nextFloat() * 0.15f
            val r = baseShade * (0.85f + random.nextFloat() * 0.3f)
            val g = baseShade * (0.90f + random.nextFloat() * 0.25f)
            val bColor = baseShade * (1.0f + random.nextFloat() * 0.4f)

            addBuilding(buildingsList, bx, -380f, bz, width, towerHeight, depth, r, g, bColor, random)

            // Architectural spire / rooftop antenna on some tall towers
            if (towerHeight > 340f) {
                val spireHeight = 30f + random.nextFloat() * 45f
                addThickLine(buildingsList, bx, topY, bz, bx, topY + spireHeight, bz, 1.2f, 0.9f, 0.9f, 0.95f, 1.0f)
                // Red aviation beacon light at spire tip
                addCube(buildingsList, bx, topY + spireHeight, bz, 2.5f, 2.5f, 2.5f, 1.0f, 0.15f, 0.15f, 1.0f)
            }
        }
        buildingsBuffer = createBuffer(buildingsList)
        buildingsVertexCount = buildingsList.size / 7

        // 4. Moving City Traffic (Avenues 380m below)
        trafficVehicles.clear()
        val numVehicles = graphicsQuality.trafficCount
        for (v in 0 until numVehicles) {
            val isNorthSouth = random.nextBoolean()
            val lane = (random.nextInt(6) - 3) * 6.5f
            val isHeadlight = random.nextBoolean()
            val speed = (18f + random.nextFloat() * 24f) * (if (isHeadlight) 1f else -1f)

            val vx = if (isNorthSouth) lane else (random.nextFloat() * 700f - 350f)
            val vz = if (isNorthSouth) (random.nextFloat() * 700f - 350f) else lane

            trafficVehicles.add(
                TrafficVehicle(
                    x = vx,
                    z = vz,
                    y = -378f,
                    speed = speed,
                    isNorthSouth = isNorthSouth,
                    isHeadlight = isHeadlight
                )
            )
        }

        // 5. Sky Dome (Atmospheric Zenith to Horizon gradient hemisphere)
        val skyList = ArrayList<Float>()
        val skyRadius = 800f
        val rings = 12
        val slices = 24
        for (i in 0 until rings) {
            val lat0 = (i.toFloat() / rings) * (Math.PI.toFloat() * 0.5f)
            val lat1 = ((i + 1).toFloat() / rings) * (Math.PI.toFloat() * 0.5f)
            val y0 = sin(lat0) * skyRadius
            val y1 = sin(lat1) * skyRadius
            val r0 = cos(lat0) * skyRadius
            val r1 = cos(lat1) * skyRadius

            val t0 = i.toFloat() / rings
            val t1 = (i + 1).toFloat() / rings

            for (j in 0 until slices) {
                val lon0 = (j.toFloat() / slices) * (Math.PI.toFloat() * 2f)
                val lon1 = ((j + 1).toFloat() / slices) * (Math.PI.toFloat() * 2f)

                val x00 = cos(lon0) * r0
                val z00 = sin(lon0) * r0
                val x10 = cos(lon1) * r0
                val z10 = sin(lon1) * r0
                val x01 = cos(lon0) * r1
                val z01 = sin(lon0) * r1
                val x11 = cos(lon1) * r1
                val z11 = sin(lon1) * r1

                val c0 = interpolateColor(timeOfDay.skyColorHorizon, timeOfDay.skyColorTop, t0)
                val c1 = interpolateColor(timeOfDay.skyColorHorizon, timeOfDay.skyColorTop, t1)

                addVertex(skyList, x00, y0, z00, c0[0], c0[1], c0[2], 1.0f)
                addVertex(skyList, x10, y0, z10, c0[0], c0[1], c0[2], 1.0f)
                addVertex(skyList, x11, y1, z11, c1[0], c1[1], c1[2], 1.0f)

                addVertex(skyList, x00, y0, z00, c0[0], c0[1], c0[2], 1.0f)
                addVertex(skyList, x11, y1, z11, c1[0], c1[1], c1[2], 1.0f)
                addVertex(skyList, x01, y1, z01, c1[0], c1[1], c1[2], 1.0f)
            }
        }
        skyDomeBuffer = createBuffer(skyList)
        skyDomeVertexCount = skyList.size / 7

        // 6. Mountain Silhouettes on Horizon
        val mtnList = ArrayList<Float>()
        val mtnRadius = 750f
        val mtnSlices = 36
        for (m in 0 until mtnSlices) {
            val a0 = (m.toFloat() / mtnSlices) * (Math.PI.toFloat() * 2f)
            val a1 = ((m + 1).toFloat() / mtnSlices) * (Math.PI.toFloat() * 2f)
            val h0 = 40f + sin(a0 * 3f) * 35f + cos(a0 * 7f) * 20f
            val h1 = 40f + sin(a1 * 3f) * 35f + cos(a1 * 7f) * 20f

            val mx0 = cos(a0) * mtnRadius
            val mz0 = sin(a0) * mtnRadius
            val mx1 = cos(a1) * mtnRadius
            val mz1 = sin(a1) * mtnRadius

            val mtnColor = floatArrayOf(0.08f, 0.09f, 0.18f, 0.95f)

            addVertex(mtnList, mx0, -100f, mz0, mtnColor[0], mtnColor[1], mtnColor[2], mtnColor[3])
            addVertex(mtnList, mx1, -100f, mz1, mtnColor[0], mtnColor[1], mtnColor[2], mtnColor[3])
            addVertex(mtnList, mx1, h1, mz1, mtnColor[0] * 1.3f, mtnColor[1] * 1.3f, mtnColor[2] * 1.3f, mtnColor[3])

            addVertex(mtnList, mx0, -100f, mz0, mtnColor[0], mtnColor[1], mtnColor[2], mtnColor[3])
            addVertex(mtnList, mx1, h1, mz1, mtnColor[0] * 1.3f, mtnColor[1] * 1.3f, mtnColor[2] * 1.3f, mtnColor[3])
            addVertex(mtnList, mx0, h0, mz0, mtnColor[0] * 1.3f, mtnColor[1] * 1.3f, mtnColor[2] * 1.3f, mtnColor[3])
        }
        mountainsBuffer = createBuffer(mtnList)
        mountainsVertexCount = mtnList.size / 7

        // 7. Reticle (3D Center Aim Crosshair + Circle)
        val reticleList = ArrayList<Float>()
        val reticleRadius = 0.035f
        val reticleSegments = 24
        for (i in 0 until reticleSegments) {
            val th0 = (i.toFloat() / reticleSegments) * (Math.PI.toFloat() * 2f)
            val th1 = ((i + 1).toFloat() / reticleSegments) * (Math.PI.toFloat() * 2f)
            val rx0 = cos(th0) * reticleRadius
            val ry0 = sin(th0) * reticleRadius
            val rx1 = cos(th1) * reticleRadius
            val ry1 = sin(th1) * reticleRadius

            // Line segment
            addVertex(reticleList, rx0, ry0, -1.5f, 1f, 1f, 1f, 0.9f)
            addVertex(reticleList, rx1, ry1, -1.5f, 1f, 1f, 1f, 0.9f)
        }
        // Center dot
        addVertex(reticleList, 0f, 0f, -1.5f, 1f, 0.8f, 0.2f, 1.0f)
        reticleBuffer = createBuffer(reticleList)
        reticleVertexCount = reticleList.size / 7

        // 8. Cardboard Center Divider Line (Screen space)
        val dividerList = ArrayList<Float>()
        addVertex(dividerList, 0f, -1f, 0f, 0.1f, 0.1f, 0.15f, 1f)
        addVertex(dividerList, 0f, 1f, 0f, 0.1f, 0.1f, 0.15f, 1f)
        dividerLineBuffer = createBuffer(dividerList)
    }

    private fun addBuilding(
        list: ArrayList<Float>,
        cx: Float, baseY: Float, cz: Float,
        w: Float, h: Float, d: Float,
        r: Float, g: Float, b: Float,
        random: Random
    ) {
        val hw = w * 0.5f
        val hd = d * 0.5f
        val topY = baseY + h

        // 4 vertical walls
        // South face
        addQuad(list, cx - hw, baseY, cz + hd, cx + hw, baseY, cz + hd, cx + hw, topY, cz + hd, cx - hw, topY, cz + hd, r, g, b, 1.0f)
        // North face
        addQuad(list, cx + hw, baseY, cz - hd, cx - hw, baseY, cz - hd, cx - hw, topY, cz - hd, cx + hw, topY, cz - hd, r * 0.85f, g * 0.85f, b * 0.85f, 1.0f)
        // East face
        addQuad(list, cx + hw, baseY, cz + hd, cx + hw, baseY, cz - hd, cx + hw, topY, cz - hd, cx + hw, topY, cz + hd, r * 0.92f, g * 0.92f, b * 0.92f, 1.0f)
        // West face
        addQuad(list, cx - hw, baseY, cz - hd, cx - hw, baseY, cz + hd, cx - hw, topY, cz + hd, cx - hw, topY, cz - hd, r * 0.92f, g * 0.92f, b * 0.92f, 1.0f)
        // Roof quad
        addQuad(list, cx - hw, topY, cz - hd, cx + hw, topY, cz - hd, cx + hw, topY, cz + hd, cx - hw, topY, cz + hd, r * 1.1f, g * 1.1f, b * 1.1f, 1.0f)

        // Procedural lit window strips along facades (shining warm yellow or cyan)
        val floors = (h / 8f).toInt().coerceAtMost(30)
        for (f in 0 until floors step 2) {
            val wy = baseY + f * 8f + 4f
            if (wy > -10f && wy < topY) {
                val winColor = if (random.nextBoolean()) floatArrayOf(1.0f, 0.90f, 0.55f) else floatArrayOf(0.6f, 0.85f, 1.0f)
                // South window line
                addThickLine(list, cx - hw + 2f, wy, cz + hd + 0.1f, cx + hw - 2f, wy, cz + hd + 0.1f, 0.8f, winColor[0], winColor[1], winColor[2], 0.85f)
                // East window line
                addThickLine(list, cx + hw + 0.1f, wy, cz - hd + 2f, cx + hw + 0.1f, wy, cz + hd - 2f, 0.8f, winColor[0], winColor[1], winColor[2], 0.85f)
            }
        }
    }

    private fun addQuad(
        list: ArrayList<Float>,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        addVertex(list, x0, y0, z0, r, g, b, a)
        addVertex(list, x1, y1, z1, r, g, b, a)
        addVertex(list, x2, y2, z2, r, g, b, a)

        addVertex(list, x0, y0, z0, r, g, b, a)
        addVertex(list, x2, y2, z2, r, g, b, a)
        addVertex(list, x3, y3, z3, r, g, b, a)
    }

    private fun addCube(
        list: ArrayList<Float>,
        cx: Float, cy: Float, cz: Float,
        sx: Float, sy: Float, sz: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        val hx = sx * 0.5f
        val hy = sy * 0.5f
        val hz = sz * 0.5f
        addQuad(list, cx - hx, cy - hy, cz + hz, cx + hx, cy - hy, cz + hz, cx + hx, cy + hy, cz + hz, cx - hx, cy + hy, cz + hz, r, g, b, a)
        addQuad(list, cx + hx, cy - hy, cz - hz, cx - hx, cy - hy, cz - hz, cx - hx, cy + hy, cz - hz, cx + hx, cy + hy, cz - hz, r, g, b, a)
        addQuad(list, cx - hx, cy + hy, cz - hz, cx + hx, cy + hy, cz - hz, cx + hx, cy + hy, cz + hz, cx - hx, cy + hy, cz + hz, r, g, b, a)
    }

    private fun addThickLine(
        list: ArrayList<Float>,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        thickness: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        val ht = thickness * 0.5f
        addVertex(list, x0, y0 - ht, z0, r, g, b, a)
        addVertex(list, x1, y1 - ht, z1, r, g, b, a)
        addVertex(list, x1, y1 + ht, z1, r, g, b, a)

        addVertex(list, x0, y0 - ht, z0, r, g, b, a)
        addVertex(list, x1, y1 + ht, z1, r, g, b, a)
        addVertex(list, x0, y0 + ht, z0, r, g, b, a)
    }

    private fun addVertex(list: ArrayList<Float>, x: Float, y: Float, z: Float, r: Float, g: Float, b: Float, a: Float) {
        list.add(x)
        list.add(y)
        list.add(z)
        list.add(r)
        list.add(g)
        list.add(b)
        list.add(a)
    }

    private fun createBuffer(list: ArrayList<Float>): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(list.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        for (f in list) fb.put(f)
        fb.position(0)
        return fb
    }

    private fun interpolateColor(c1: FloatArray, c2: FloatArray, t: Float): FloatArray {
        return floatArrayOf(
            c1[0] + (c2[0] - c1[0]) * t,
            c1[1] + (c2[1] - c1[1]) * t,
            c1[2] + (c2[2] - c1[2]) * t
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Advance moving traffic
        updateTraffic()

        // Check 3D gaze reticle against hotspots
        checkGazeHotspots()

        if (isStereoVRMode) {
            // Stereoscopic VR dual-eye rendering (Left Eye then Right Eye)
            val halfWidth = screenWidth / 2
            val ipdOffset = (ipdMm / 1000f) * 0.5f // Convert mm to virtual eye offset

            // --- LEFT EYE ---
            GLES20.glViewport(0, 0, halfWidth, screenHeight)
            renderEye(eyeOffset = -ipdOffset, aspect = halfWidth.toFloat() / screenHeight.toFloat())

            // --- RIGHT EYE ---
            GLES20.glViewport(halfWidth, 0, halfWidth, screenHeight)
            renderEye(eyeOffset = ipdOffset, aspect = halfWidth.toFloat() / screenHeight.toFloat())

            // Render Cardboard Center Divider Line & Alignment Marker
            renderCenterDivider()
        } else {
            // Fullscreen Monoscopic 360 mode
            GLES20.glViewport(0, 0, screenWidth, screenHeight)
            renderEye(eyeOffset = 0f, aspect = screenWidth.toFloat() / screenHeight.toFloat())
        }
    }

    private fun updateTraffic() {
        val dt = 0.016f
        val trafficList = ArrayList<Float>()
        for (v in trafficVehicles) {
            if (v.isNorthSouth) {
                v.z += v.speed * dt
                if (v.z > 350f) v.z = -350f
                if (v.z < -350f) v.z = 350f
            } else {
                v.x += v.speed * dt
                if (v.x > 350f) v.x = -350f
                if (v.x < -350f) v.x = 350f
            }

            // Headlights = warm white beam; Taillights = ruby red trail
            val r = if (v.isHeadlight) 1.0f else 0.95f
            val g = if (v.isHeadlight) 0.95f else 0.12f
            val b = if (v.isHeadlight) 0.75f else 0.12f

            addCube(trafficList, v.x, v.y, v.z, 2.2f, 1.2f, 4.2f, r, g, b, 1.0f)
        }
        trafficBuffer = createBuffer(trafficList)
        trafficVertexCount = trafficList.size / 7
    }

    private fun renderEye(eyeOffset: Float, aspect: Float) {
        // Perspective projection
        val near = 0.1f
        val far = 1200f
        Matrix.perspectiveM(projectionMatrix, 0, fovDegrees, aspect, near, far)

        // View matrix composed of Eye Offset + Head Rotation
        Matrix.setIdentityM(viewMatrix, 0)
        // Eye offset translation for stereoscopic parallax
        Matrix.translateM(viewMatrix, 0, -eyeOffset, 0f, 0f)

        // Apply head rotation matrix
        synchronized(headMatrix) {
            Matrix.multiplyMM(eyeMatrix, 0, viewMatrix, 0, headMatrix, 0)
        }

        GLES20.glUseProgram(colorProgram)

        // 1. Render Sky Dome (No depth write so it always stays behind everything)
        GLES20.glDepthMask(false)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, eyeMatrix, 0)
        GLES20.glUniformMatrix4fv(uMvpLocation, 1, false, mvpMatrix, 0)
        drawBuffer(skyDomeBuffer, skyDomeVertexCount, GLES20.GL_TRIANGLES)
        drawBuffer(mountainsBuffer, mountainsVertexCount, GLES20.GL_TRIANGLES)
        GLES20.glDepthMask(true)

        // 2. Render 3D City Skyscrapers
        drawBuffer(buildingsBuffer, buildingsVertexCount, GLES20.GL_TRIANGLES)

        // 3. Render Moving Traffic
        trafficBuffer?.let {
            drawBuffer(it, trafficVertexCount, GLES20.GL_TRIANGLES)
        }

        // 4. Render Observation Deck Floor & Railings
        drawBuffer(deckBuffer, deckVertexCount, GLES20.GL_TRIANGLES)
        drawBuffer(railingBuffer, railingVertexCount, GLES20.GL_TRIANGLES)

        // 5. Render Tempered Glass Windbreak Panels with Alpha Blending
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        drawBuffer(glassBuffer, glassVertexCount, GLES20.GL_TRIANGLES)

        // 6. Render 3D Sightseeing Hotspot Markers in World
        renderHotspots()

        // 7. Render 3D Gaze Reticle in Head Space
        renderReticle()

        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun renderHotspots() {
        val markerList = ArrayList<Float>()
        for (h in hotspots) {
            val radYaw = Math.toRadians(h.yawDegrees.toDouble()).toFloat()
            val radPitch = Math.toRadians(h.pitchDegrees.toDouble()).toFloat()
            val dist = 32f // Visual marker distance in 3D

            val mx = (sin(radYaw) * cos(radPitch) * dist).toFloat()
            val my = (sin(radPitch) * dist).toFloat()
            val mz = (-cos(radYaw) * cos(radPitch) * dist).toFloat()

            val isTargeted = activeGazeHotspot?.id == h.id
            val r = if (isTargeted) 1.0f else 0.2f
            val g = if (isTargeted) 0.85f else 0.8f
            val b = if (isTargeted) 0.15f else 1.0f

            // Diamond marker shape
            addCube(markerList, mx, my, mz, 1.8f, 1.8f, 1.8f, r, g, b, 0.9f)
        }
        val mb = createBuffer(markerList)
        drawBuffer(mb, markerList.size / 7, GLES20.GL_TRIANGLES)
    }

    private fun renderReticle() {
        // Reticle is fixed relative to camera projection
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(uMvpLocation, 1, false, mvpMatrix, 0)

        // Center crosshair ring
        drawBuffer(reticleBuffer, reticleVertexCount, GLES20.GL_LINES)

        // Draw animated dwell progress circle when targeting a hotspot
        activeGazeHotspot?.let {
            val elapsed = SystemClock.uptimeMillis() - gazeDwellStartTime
            val progress = (elapsed.toFloat() / gazeDwellRequiredMs).coerceIn(0f, 1f)
            if (progress > 0.05f) {
                val progressList = ArrayList<Float>()
                val radius = 0.045f
                val segments = (progress * 30).toInt().coerceAtLeast(3)
                for (s in 0 until segments) {
                    val a0 = (s.toFloat() / 30f) * (Math.PI.toFloat() * 2f)
                    val a1 = ((s + 1).toFloat() / 30f) * (Math.PI.toFloat() * 2f)
                    addVertex(progressList, cos(a0) * radius, sin(a0) * radius, -1.48f, 1f, 0.9f, 0.1f, 1f)
                    addVertex(progressList, cos(a1) * radius, sin(a1) * radius, -1.48f, 1f, 0.9f, 0.1f, 1f)
                }
                val pb = createBuffer(progressList)
                drawBuffer(pb, progressList.size / 7, GLES20.GL_LINES)
            }
        }
    }

    private fun renderCenterDivider() {
        GLES20.glViewport(0, 0, screenWidth, screenHeight)
        Matrix.setIdentityM(mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uMvpLocation, 1, false, mvpMatrix, 0)
        drawBuffer(dividerLineBuffer, 2, GLES20.GL_LINES)
    }

    private fun checkGazeHotspots() {
        // Extract gaze direction vector from inverse view matrix
        Matrix.invertM(invertedView, 0, headMatrix, 0)
        // Camera forward vector in world space: (invertedView[8], invertedView[9], invertedView[10]) negated
        val forwardX = -invertedView[8]
        val forwardY = -invertedView[9]
        val forwardZ = -invertedView[10]

        // Gaze yaw and pitch
        val gazeYaw = Math.toDegrees(atan2(forwardX.toDouble(), -forwardZ.toDouble())).toFloat()
        val forwardHoriz = sqrt((forwardX * forwardX + forwardZ * forwardZ).toDouble()).toFloat()
        val gazePitch = Math.toDegrees(atan2(forwardY.toDouble(), forwardHoriz.toDouble())).toFloat()

        var hitHotspot: SightseeingHotspot? = null
        val angularThreshold = 10.0f // 10 degrees cone of gaze

        for (h in hotspots) {
            var diffYaw = (h.yawDegrees - gazeYaw) % 360f
            if (diffYaw > 180f) diffYaw -= 360f
            if (diffYaw < -180f) diffYaw += 360f
            val diffPitch = h.pitchDegrees - gazePitch

            val angleDist = sqrt((diffYaw * diffYaw + diffPitch * diffPitch).toDouble()).toFloat()
            if (angleDist < angularThreshold) {
                hitHotspot = h
                break
            }
        }

        if (hitHotspot != null) {
            if (activeGazeHotspot?.id != hitHotspot.id) {
                activeGazeHotspot = hitHotspot
                gazeDwellStartTime = SystemClock.uptimeMillis()
            } else {
                val elapsed = SystemClock.uptimeMillis() - gazeDwellStartTime
                val progress = (elapsed.toFloat() / gazeDwellRequiredMs).coerceIn(0f, 1f)
                onHotspotGazeProgress(hitHotspot, progress)

                if (elapsed >= gazeDwellRequiredMs && lastTriggeredHotspotId != hitHotspot.id) {
                    lastTriggeredHotspotId = hitHotspot.id
                    onHotspotTriggered(hitHotspot)
                }
            }
        } else {
            if (activeGazeHotspot != null) {
                activeGazeHotspot = null
                onHotspotGazeProgress(null, 0f)
            }
        }
    }

    private fun drawBuffer(buffer: FloatBuffer?, vertexCount: Int, mode: Int) {
        if (buffer == null || vertexCount <= 0) return
        buffer.position(0)
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 7 * 4, buffer)
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        buffer.position(3)
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 7 * 4, buffer)
        GLES20.glEnableVertexAttribArray(aColorLocation)

        GLES20.glDrawArrays(mode, 0, vertexCount)
    }

    fun triggerCurrentHotspot() {
        activeGazeHotspot?.let {
            lastTriggeredHotspotId = it.id
            onHotspotTriggered(it)
        }
    }

    fun changeTimeOfDay(newTime: TimeOfDay) {
        timeOfDay = newTime
        generateGeometry()
    }

    fun changeGraphicsQuality(newQuality: GraphicsQuality) {
        graphicsQuality = newQuality
        generateGeometry()
    }
}
