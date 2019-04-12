package com.juankysoriano.rainbow.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class RainbowTaskScheduler {
    private static final long TIMEOUT = 10;
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
    private final ScheduledExecutorService drawingScheduler;
    private final ScheduledExecutorService screenUpdateScheduler;
    private final DrawingStepTask drawingStepTask;
    private final ScreenUpdateTask screenUpdateTask;

    public static RainbowTaskScheduler newInstance(Rainbow rainbow) {
        ScreenUpdate screenUpdate = new ScreenUpdate();
        ScheduledExecutorService drawingScheduler = Executors.newSingleThreadScheduledExecutor();
        DrawingStepTask drawingStepTask = new DrawingStepTask(rainbow, screenUpdate);
        ScheduledExecutorService screenUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        ScreenUpdateTask screenUpdateTask = new ScreenUpdateTask(rainbow, screenUpdate);
        return new RainbowTaskScheduler(drawingScheduler, drawingStepTask, screenUpdateScheduler, screenUpdateTask);
    }

    private RainbowTaskScheduler(ScheduledExecutorService drawingScheduler,
                                 DrawingStepTask drawingStepTask,
                                 ScheduledExecutorService screenUpdateScheduler,
                                 ScreenUpdateTask screenUpdateTask) {
        this.drawingScheduler = drawingScheduler;
        this.drawingStepTask = drawingStepTask;
        this.screenUpdateScheduler = screenUpdateScheduler;
        this.screenUpdateTask = screenUpdateTask;
    }

    void scheduleAt(int frameRate, int vSyncRate) {
        drawingScheduler.scheduleAtFixedRate(drawingStepTask, SECOND, SECOND / frameRate, TimeUnit.NANOSECONDS);
        screenUpdateScheduler.scheduleAtFixedRate(screenUpdateTask, SECOND, SECOND / vSyncRate, TimeUnit.NANOSECONDS);
    }

    boolean isTerminated() {
        return drawingScheduler.isTerminated() && screenUpdateScheduler.isTerminated();
    }

    void shutdown() throws InterruptedException {
        drawingScheduler.shutdownNow();
        drawingScheduler.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        screenUpdateScheduler.shutdownNow();
        screenUpdateScheduler.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
    }

    static class ScreenUpdate {
        private boolean pending;

        void pending() {
            pending = true;
        }

        boolean isPending() {
            return pending;
        }

        void notPending() {
            pending = false;
        }
    }
}
