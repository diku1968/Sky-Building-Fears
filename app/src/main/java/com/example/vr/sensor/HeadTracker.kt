package com.example.vr.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Matrix
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-precision 3DoF Head Tracking engine for Google Cardboard, JioDive, and VR headsets.
 * Uses Android 9-axis sensor fusion (Rotation Vector) with smooth slerp/damping,
 * landscape orientation axis remapping, zero-drift recentering, and touch-drag fallback.
 */
class HeadTracker(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var hasHardwareSensor: Boolean = rotationSensor != null
        private set

    // Raw sensor rotation matrix
    private val rawMatrix = FloatArray(16)
    private val remappedMatrix = FloatArray(16)

    // Current output orientation matrix (4x4 OpenGL format)
    val headRotationMatrix = FloatArray(16)

    // Recenter reference offset (yaw in radians)
    private var yawRecenterOffset: Float = 0f

    // Touch fallback angles (when user drags on screen or emulator)
    var touchYaw: Float = 0f
    var touchPitch: Float = 0f

    // Current smoothed Euler angles (degrees)
    var currentYawDegrees: Float = 0f
        private set
    var currentPitchDegrees: Float = 0f
        private set
    var currentRollDegrees: Float = 0f
        private set

    // Seated mode comfort limit
    var seatedComfortMode: Boolean = true

    // Tracking state
    private var isTracking = false
    private var firstSample = true

    init {
        Matrix.setIdentityM(headRotationMatrix, 0)
        Matrix.setIdentityM(rawMatrix, 0)
        Matrix.setIdentityM(remappedMatrix, 0)
    }

    fun start() {
        if (isTracking) return
        isTracking = true
        firstSample = true

        rotationSensor?.let { sensor ->
            // Use SENSOR_DELAY_GAME (~20ms / 50Hz) or FASTEST for minimal latency
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (!isTracking) return
        isTracking = false
        sensorManager.unregisterListener(this)
    }

    fun recenter() {
        // Reset yaw to zero relative to current heading
        yawRecenterOffset = currentYawDegrees
        touchYaw = 0f
        touchPitch = 0f
    }

    fun addTouchDelta(deltaX: Float, deltaY: Float) {
        // Sensitivity factor for touch dragging
        val sensitivity = 0.25f
        touchYaw += deltaX * sensitivity
        touchPitch += deltaY * sensitivity

        // Clamp touch pitch to prevent unnatural neck rotation
        val maxPitch = if (seatedComfortMode) 45f else 75f
        val minPitch = if (seatedComfortMode) -45f else -75f
        touchPitch = touchPitch.coerceIn(minPitch, maxPitch)

        updateMatrixFromTouch()
    }

    private fun updateMatrixFromTouch() {
        val finalYaw = touchYaw - yawRecenterOffset
        val finalPitch = touchPitch

        currentYawDegrees = finalYaw
        currentPitchDegrees = finalPitch
        currentRollDegrees = 0f

        val tempMatrix = FloatArray(16)
        Matrix.setIdentityM(tempMatrix, 0)
        Matrix.rotateM(tempMatrix, 0, finalPitch, 1f, 0f, 0f)
        Matrix.rotateM(tempMatrix, 0, finalYaw, 0f, 1f, 0f)

        synchronized(headRotationMatrix) {
            System.arraycopy(tempMatrix, 0, headRotationMatrix, 0, 16)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isTracking) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
        ) {
            SensorManager.getRotationMatrixFromVector(rawMatrix, event.values)

            // Landscape-oriented VR:
            // Remap coordinate system for landscape orientation:
            // X becomes Y, Y becomes -X or Z-axis
            SensorManager.remapCoordinateSystem(
                rawMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedMatrix
            )

            // Extract Euler angles for seated comfort constraints and UI compass/reticle
            val orientation = FloatArray(3)
            SensorManager.getOrientation(remappedMatrix, orientation)

            var rawYaw = Math.toDegrees(orientation[0].toDouble()).toFloat()
            var rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

            if (firstSample) {
                yawRecenterOffset = rawYaw
                firstSample = false
            }

            // Apply recenter yaw offset + touch delta
            var adjustedYaw = (rawYaw - yawRecenterOffset + touchYaw) % 360f
            if (adjustedYaw < -180f) adjustedYaw += 360f
            if (adjustedYaw > 180f) adjustedYaw -= 360f

            var adjustedPitch = rawPitch + touchPitch
            if (seatedComfortMode) {
                adjustedPitch = adjustedPitch.coerceIn(-50f, 45f)
            } else {
                adjustedPitch = adjustedPitch.coerceIn(-80f, 80f)
            }

            // Damping / low-pass filter to smooth micro-jitter
            val alpha = 0.35f
            currentYawDegrees += (adjustedYaw - currentYawDegrees) * alpha
            currentPitchDegrees += (adjustedPitch - currentPitchDegrees) * alpha
            currentRollDegrees += (rawRoll - currentRollDegrees) * alpha

            // Build rotation matrix from clamped/smoothed Euler angles
            val m = FloatArray(16)
            Matrix.setIdentityM(m, 0)
            Matrix.rotateM(m, 0, currentRollDegrees, 0f, 0f, 1f)
            Matrix.rotateM(m, 0, currentPitchDegrees, 1f, 0f, 0f)
            Matrix.rotateM(m, 0, -currentYawDegrees, 0f, 1f, 0f)

            synchronized(headRotationMatrix) {
                System.arraycopy(m, 0, headRotationMatrix, 0, 16)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Calibration handled automatically by Android Sensor fusion
    }
}
