package com.juankysoriano.rainbow.utils.schedulers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RainbowScheduler {
    private final ScheduledExecutorService scheduler;
    private boolean running;

    RainbowScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleNow(Runnable runnable) {
        scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    public void scheduleAtRate(Runnable runnable, long delay, TimeUnit timeUnit) {
        running = true;
        scheduler.scheduleAtFixedRate(runnable, 0, delay, timeUnit);
    }

    public boolean isTerminated() {
        return !running;
    }

    public void shutdown() {
        scheduler.shutdownNow();
        running = false;
    }

}
