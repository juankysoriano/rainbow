package com.juankysoriano.rainbow.utils.schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class RainbowSchedulers {
    public static RainbowScheduler single(String name, Priority priority) {
        ThreadFactory threadFactory = RainbowThreadFactory.newInstance(name, priority.threadPriority);
        return new RainbowScheduler(Executors.newScheduledThreadPool(1, threadFactory));
    }

    /**
     * The stack size for the threads on this scheduler is enough to perform long recursion tasks
     **/
    public static RainbowScheduler singleForRecursion(String name, Priority priority) {
        ThreadFactory threadFactory = RainbowThreadFactory.newInstanceForRecursion(name, priority.threadPriority);
        return new RainbowScheduler(Executors.newScheduledThreadPool(1, threadFactory));
    }

    public static RainbowScheduler multiThreaded(String name, Priority priority) {
        ThreadFactory threadFactory = RainbowThreadFactory.newInstance(name, priority.threadPriority);
        return new RainbowScheduler(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory));
    }

    /**
     * The stack size for the threads on this scheduler is enough to perform long recursion tasks
     **/
    public static RainbowScheduler multiThreadedForRecursion(String name, Priority priority) {
        ThreadFactory threadFactory = RainbowThreadFactory.newInstanceForRecursion(name, priority.threadPriority);
        return new RainbowScheduler(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory));
    }

    public enum Priority {
        MAX(Thread.MAX_PRIORITY),
        NORMAL(Thread.NORM_PRIORITY),
        MIN(Thread.MIN_PRIORITY);

        private int threadPriority;

        Priority(int threadPriority) {
            this.threadPriority = threadPriority;
        }
    }
}
