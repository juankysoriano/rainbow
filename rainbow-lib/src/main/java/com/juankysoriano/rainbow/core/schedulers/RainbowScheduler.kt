package com.juankysoriano.rainbow.core.schedulers

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RainbowScheduler(private val scheduler: ScheduledExecutorService) {
    private var running: Boolean = false

    val isTerminated: Boolean
        get() = !running

    fun scheduleNow(runnable: Runnable) {
        scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS)
    }

    fun scheduleAtRate(runnable: Runnable, delay: Long, timeUnit: TimeUnit) {
        running = true
        scheduler.scheduleAtFixedRate(runnable, 0, delay, timeUnit)
    }

    fun shutdown() {
        scheduler.shutdownNow()
        running = false
    }
}
