package com.juankysoriano.rainbow.demo.sketch.rainbow.forces

import android.view.MotionEvent
import android.view.ViewGroup
import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.core.touch.RainbowInputController

class RainbowParticleSystem(viewGroup: ViewGroup) : Rainbow(viewGroup), RainbowInputController.RainbowInteractionListener {
    private lateinit var particleWorld: ParticleWorld

    override fun onSketchSetup() {
        super.onSketchSetup()
        with(rainbowDrawer) {
            noSmooth()
            noFill()
            background(0)
            invalidate()
        }
        particleWorld = ParticleWorld.create(rainbowDrawer.width, rainbowDrawer.height)
    }

    override fun onDrawingStart() {
        super.onDrawingStart()
        rainbowInputController.attach(this)
    }

    override fun onDrawingStep() {
        super.onDrawingStep()
        rainbowDrawer.background(0)
        particleWorld.updateAndDisplay(this)
    }

    override fun onDrawingStop() {
        rainbowInputController.detach()
        super.onDrawingStop()
    }

    override fun onFingerDragged(event: MotionEvent) {
        onSketchTouched(event)
    }

    override fun onMotionEvent(event: MotionEvent) {
        //no-op
    }

    override fun onSketchTouched(event: MotionEvent) {
        val x = event.x
        val y = event.y
        particleWorld.moveNucleusTo(x, y)
    }

    override fun onSketchReleased(event: MotionEvent) {
        //no-op
    }
}
