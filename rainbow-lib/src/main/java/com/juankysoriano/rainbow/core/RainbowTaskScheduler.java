package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.SafeScheduledExecutor;

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
        Progress progress = new Progress();
        ScheduledExecutorService drawingScheduler = SafeScheduledExecutor.newInstance();
        DrawingStepTask drawingStepTask = new DrawingStepTask(rainbow, progress);
        ScheduledExecutorService screenUpdateScheduler = SafeScheduledExecutor.newInstance();
        ScreenUpdateTask screenUpdateTask = new ScreenUpdateTask(rainbow, progress);
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

    static class Progress {
        private boolean paintLocked = true;
        private boolean stepLocked = false;

        boolean isUpdatingScreen() {
            return stepLocked;
        }

        void performingStep() {
            paintLocked = true;
        }

        void stepPerformed() {
            paintLocked = false;
        }

        boolean isDrawingStep() {
            return paintLocked;
        }

        void drawingScreen() {
            stepLocked = true;
        }

        void screenDrawn() {
            stepLocked = false;
        }
    }
}
