package com.juankysoriano.rainbow.core.cv.blobdetector;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class BlobDetectionThreadFactory extends AtomicLong implements ThreadFactory {

    private static final long serialVersionUID = -7789753024099756196L;

    private final String prefix;
    private final ThreadGroup threadGroup;

    public BlobDetectionThreadFactory(String prefix) {
        this.prefix = prefix;
        this.threadGroup = new ThreadGroup("BLOB detection");
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + '-' + incrementAndGet();
        Thread thread = new Thread(threadGroup, r, name, Integer.MAX_VALUE);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        return thread;
    }

    @Override
    public String toString() {
        return "RxThreadFactory[" + prefix + "]";
    }
}
