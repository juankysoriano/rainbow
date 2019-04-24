package com.juankysoriano.rainbow.demo.sketch.rainbow

import android.view.MotionEvent
import android.view.ViewGroup

import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.core.drawing.Modes
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.Precision
import com.juankysoriano.rainbow.core.touch.RainbowInputController

class RainbowLineCirclesSketch(viewGroup: ViewGroup) : Rainbow(viewGroup), RainbowInputController.RainbowInteractionListener {

    override fun onSketchSetup() {
        with(rainbowDrawer) {
            background(255)
            smooth()
            noFill()
        }
    }

    override fun onDrawingStart() = rainbowInputController.attach(this)
    override fun onDrawingStop() = rainbowInputController.detach()

    override fun onSketchDestroy() {
        //no-op
    }

    override fun onSketchTouched(event: MotionEvent) {
        //no-op
    }

    override fun onSketchReleased(event: MotionEvent) {
        //no-op
    }

    override fun onFingerDragged(event: MotionEvent) {
        val rainbowInputController = rainbowInputController
        rainbowDrawer.exploreLine(
                x1 = rainbowInputController.previousX,
                y1 = rainbowInputController.previousY,
                x2 = rainbowInputController.x,
                y2 = rainbowInputController.y,
                precision = Precision.HIGH,
                listener = object : RainbowDrawer.PointDetectedListener {
                    override fun onPointDetected(px: Float, py: Float, x: Float, y: Float) {
                        with(rainbowDrawer) {
                            stroke(0, 30f)
                            fill(0, 0f)
                            ellipseMode(Modes.Draw.DIAMETER)
                            ellipse(x, y, 200f, 200f)
                        }
                    }

                }
        )
    }

    override fun onMotionEvent(event: MotionEvent) {
        //no-op
    }
}
