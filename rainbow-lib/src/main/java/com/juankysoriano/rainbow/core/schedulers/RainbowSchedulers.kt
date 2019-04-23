package com.juankysoriano.rainbow.core.schedulers

import java.util.concurrent.Executors

object RainbowSchedulers {

    fun single(name: String, priority: Priority): RainbowScheduler {
        val threadFactory = RainbowThreadFactory.create(name, priority.threadPriority)
        return RainbowScheduler(Executors.newScheduledThreadPool(1, threadFactory))
    }

    /**
     * The stack size for the threads on this scheduler is enough to perform long recursion tasks
     */
    fun singleForRecursion(name: String, priority: Priority): RainbowScheduler {
        val threadFactory = RainbowThreadFactory.createForRecursion(name, priority.threadPriority)
        return RainbowScheduler(Executors.newScheduledThreadPool(1, threadFactory))
    }

    fun multiThreaded(name: String, priority: Priority): RainbowScheduler {
        val threadFactory = RainbowThreadFactory.create(name, priority.threadPriority)
        return RainbowScheduler(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory))
    }

    /**
     * The stack size for the threads on this scheduler is enough to perform long recursion tasks
     */
    fun multiThreadedForRecursion(name: String, priority: Priority): RainbowScheduler {
        val threadFactory = RainbowThreadFactory.createForRecursion(name, priority.threadPriority)
        return RainbowScheduler(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory))
    }

    enum class Priority private constructor(val threadPriority: Int) {
        MAX(Thread.MAX_PRIORITY),
        NORMAL(Thread.NORM_PRIORITY),
        MIN(Thread.MIN_PRIORITY)
    }
}
