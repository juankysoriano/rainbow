package com.juankysoriano.rainbow.core

import com.juankysoriano.rainbow.core.schedulers.RainbowScheduler
import com.juankysoriano.rainbow.core.schedulers.RainbowSchedulers
import java.util.concurrent.TimeUnit

internal class RainbowTaskScheduler internal constructor(private val rainbow: Rainbow,
                                                         private val stepTask: DrawingTask.Step = DrawingTask.Step(rainbow),
                                                         private val invalidateTask: DrawingTask.Invalidate = DrawingTask.Invalidate(rainbow)) {
    private var scheduler: RainbowScheduler = RainbowSchedulers.single("Drawing", RainbowSchedulers.Priority.MAX)

    val isTerminated: Boolean
        get() = scheduler.isTerminated

    fun scheduleSetup() {
        scheduler.scheduleNow(Runnable {
            rainbow.setupSketch()
            rainbow.rainbowDrawer.beginDraw()
            rainbow.onSketchSetup()
            rainbow.rainbowDrawer.endDraw()
            rainbow.start()
        })
    }

    fun scheduleSingleDraw() {
        scheduler.scheduleNow(Runnable {
            rainbow.rainbowDrawer.beginDraw()
            rainbow.onDrawingStep()
            rainbow.rainbowDrawer.endDraw()
        })
    }

    fun scheduleDrawing(stepRate: Int, frameRate: Int) {
        val second = TimeUnit.SECONDS.toNanos(1)
        scheduler.scheduleAtRate(stepTask, second / stepRate, TimeUnit.NANOSECONDS)
        scheduler.scheduleAtRate(invalidateTask, second / frameRate, TimeUnit.NANOSECONDS)
    }

    fun shutdown() {
        scheduler.shutdown()
        scheduler = RainbowSchedulers.single("Drawing", RainbowSchedulers.Priority.MAX)
    }

}
