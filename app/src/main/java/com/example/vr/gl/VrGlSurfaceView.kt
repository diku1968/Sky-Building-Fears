package com.example.vr.gl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.vr.sensor.HeadTracker

/**
 * Custom GLSurfaceView for VR Sky View.
 * Dispatches touch drag to HeadTracker (allowing testing in emulator or non-gyro devices)
 * and touch taps to the Cardboard VR trigger system.
 */
@SuppressLint("ViewConstructor")
class VrGlSurfaceView(
    context: Context,
    val renderer: VrRenderer,
    private val headTracker: HeadTracker,
    private val onCardboardTriggerTap: () -> Unit
) : GLSurfaceView(context) {

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var touchDownTime: Long = 0L
    private var hasMovedSignificantly = false

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = x
                previousY = y
                touchDownTime = System.currentTimeMillis()
                hasMovedSignificantly = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY

                if (kotlin.math.abs(dx) > 4f || kotlin.math.abs(dy) > 4f) {
                    hasMovedSignificantly = true
                    // Feed touch delta into head tracker
                    headTracker.addTouchDelta(-dx, -dy)
                    // Synchronize orientation matrix to renderer
                    synchronized(renderer.headMatrix) {
                        System.arraycopy(headTracker.headRotationMatrix, 0, renderer.headMatrix, 0, 16)
                    }
                }

                previousX = x
                previousY = y
            }

            MotionEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - touchDownTime
                if (!hasMovedSignificantly && duration < 350) {
                    // Tap registered - triggers Cardboard viewer action
                    onCardboardTriggerTap()
                    renderer.triggerCurrentHotspot()
                }
            }
        }
        return true
    }
}
