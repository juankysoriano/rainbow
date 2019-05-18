package com.juankysoriano.rainbow.utils.schedulers;

import android.util.Log;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class RainbowThreadFactory extends AtomicLong implements ThreadFactory {

    private final String prefix;

    private final int priority;

    private final ThreadGroup threadGroup;

    private final int stackSize;

    private final Thread.UncaughtExceptionHandler handler;

    static RainbowThreadFactory newInstance(String name, int priority) {
        return new RainbowThreadFactory(name, priority, null, 0, create());
    }

    static RainbowThreadFactory newInstanceForRecursion(String name, int priority) {
        ThreadGroup threadGroup = new ThreadGroup(name + "-group");
        return new RainbowThreadFactory(name, priority, threadGroup, 1024, create());
    }

    private static Thread.UncaughtExceptionHandler create() {
        return new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e("Rainbow", t.getName(), e);
            }
        };
    }

    private RainbowThreadFactory(String prefix, int priority, ThreadGroup threadGroup, int stackSize, Thread.UncaughtExceptionHandler handler) {
        this.prefix = prefix;
        this.priority = priority;
        this.threadGroup = threadGroup;
        this.stackSize = stackSize;
        this.handler = handler;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + '-' + incrementAndGet();
        Thread thread = new Thread(threadGroup, r, name, stackSize);
        thread.setPriority(priority);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }
}
