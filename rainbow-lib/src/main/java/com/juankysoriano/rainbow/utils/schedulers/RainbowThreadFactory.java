package com.juankysoriano.rainbow.utils.schedulers;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class RainbowThreadFactory extends AtomicLong implements ThreadFactory {

    private final String prefix;

    private final int priority;

    private final ThreadGroup threadGroup;

    private final int stackSize;

    static RainbowThreadFactory newInstance(String name, int priority) {
        return new RainbowThreadFactory(name, priority, null, 0);
    }

    static RainbowThreadFactory newInstanceForRecursion(String name, int priority) {
        ThreadGroup threadGroup = new ThreadGroup(name + "-group");
        return new RainbowThreadFactory(name, priority, threadGroup, Integer.MAX_VALUE);
    }

    private RainbowThreadFactory(String prefix, int priority, ThreadGroup threadGroup, int stackSize) {
        this.prefix = prefix;
        this.priority = priority;
        this.threadGroup = threadGroup;
        this.stackSize = stackSize;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + '-' + incrementAndGet();
        Thread thread = new Thread(threadGroup, r, name, stackSize);
        thread.setPriority(priority);
        thread.setDaemon(true);
        return thread;
    }
}
