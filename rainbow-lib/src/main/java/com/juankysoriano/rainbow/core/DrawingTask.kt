package com.juankysoriano.rainbow.core

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer

internal sealed class DrawingTask : Runnable {
    class Step internal constructor(private val rainbow: Rainbow) : DrawingTask() {
        override fun run() {
            if (rainbow.isResumed) {
                rainbow.performStep()
            }
        }
    }

    class Invalidate internal constructor(
            private val rainbow: Rainbow,
            private val rainbowDrawer: RainbowDrawer = rainbow.rainbowDrawer
    ) : DrawingTask() {
        override fun run() {
            if (rainbow.isResumed) {
                rainbowDrawer.invalidate()
            }
        }
    }
}
