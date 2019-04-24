package com.juankysoriano.rainbow.core

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.view.ViewGroup
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer
import com.juankysoriano.rainbow.core.drawing.RainbowTextureView
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics2D
import com.juankysoriano.rainbow.core.touch.RainbowInputController

open class Rainbow private constructor(viewGroup: ViewGroup,
                                       val rainbowDrawer: RainbowDrawer = RainbowDrawer(),
                                       val rainbowInputController: RainbowInputController = RainbowInputController()) {
    var drawingView: RainbowTextureView
        private set

    init {
        drawingView = RainbowTextureView(viewGroup, this)
        drawingView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                if (isSetup) {
                    rainbowTaskScheduler.scheduleSingleDraw()
                } else {
                    rainbowTaskScheduler.scheduleSetup()
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    var frameRate = 60
        set(value) {
            field = value
            restart()
        }

    var stepRate = 60
        set(value) {
            field = value
            restart()
        }

    var stepCount: Int = 0
        private set
    var width: Int = 0
        private set
    var height: Int = 0
        private set
    var isStopped = true
        private set
    var isStarted = false
        private set
    var isPaused = true
        private set
    var isResumed = false
        private set
    var isSetup = false
        private set

    private var surfaceReady: Boolean = false

    private val rainbowTaskScheduler: RainbowTaskScheduler = RainbowTaskScheduler(this)

    internal fun setupSketch() {
        isSetup = true
        surfaceReady = true
        width = drawingView!!.measuredWidth
        height = drawingView!!.measuredHeight

        initPeriodicGraphics()
        initInputControllerGraphics()
    }

    private fun initPeriodicGraphics() {
        val graphics = RainbowGraphics2D()
        graphics.setParent(this@Rainbow)
        graphics.setPrimary(true)
        if (width > 0 && height > 0) {
            graphics.setSize(width, height)
            rainbowDrawer.setGraphics(graphics)
        }
    }

    private fun initInputControllerGraphics() {
        val graphics = RainbowGraphics2D()
        graphics.setParent(this@Rainbow)
        graphics.setPrimary(true)
        if (width > 0 && height > 0) {
            graphics.setSize(width, height)
            rainbowInputController.rainbowDrawer.setGraphics(graphics)
        }
    }

    open fun onSketchSetup() {}

    fun start() {
        if (!isSetup) {
            return
        }
        if (isStopped || rainbowTaskScheduler.isTerminated) {
            onDrawingStart()
            isStarted = true
            isStopped = false
            resume()
        }
    }

    open fun onDrawingStart() {}

    fun resume() {
        if (!isSetup) {
            return
        }

        if (isPaused) {
            onDrawingResume()
            isResumed = true
            isPaused = false
            rainbowTaskScheduler.scheduleDrawing(stepRate, frameRate)
        }
    }

    fun onDrawingResume() {}

    internal fun performStep() {
        if (canDraw()) {
            stepCount++
            onDrawingStep()
        }
    }

    private fun canDraw(): Boolean {
        return rainbowDrawer.graphics != null && surfaceReady && isSetup
    }

    open fun onDrawingStep() {}

    fun pause() {
        if (!isSetup) {
            return
        }

        if (isResumed) {
            isPaused = true
            isResumed = false
            shutdownTasks()
            onDrawingPause()
        }
    }

    private fun shutdownTasks() {
        rainbowTaskScheduler.shutdown()
    }

    fun onDrawingPause() {}

    fun stop() {
        if (!isSetup) {
            return
        }

        if (isStarted) {
            pause()
            onDrawingStop()
            isStopped = true
            isStarted = false
        }
    }

    open fun onDrawingStop() {}

    fun destroy() {
        stop()
        onSketchDestroy()
        rainbowDrawer.graphics?.dispose()
        isSetup = false
    }

    open fun onSketchDestroy() {}

    fun reset() {
        setupDrawingSurface(rainbowDrawer.graphics)
    }

    private fun setupDrawingSurface(graphics: RainbowGraphics) {
        val newWidth = drawingView.width
        val newHeight = drawingView.height
        if (newWidth != width || newHeight != height) {
            width = newWidth
            height = newHeight
            graphics.setSize(width, height)
        }
        surfaceReady = true
    }

    private fun restart() {
        if (isResumed) {
            stop()
            start()
        }
    }
}
