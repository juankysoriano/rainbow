package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.SafeScheduledExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class RainbowTaskScheduler {
    private static final long TIMEOUT = 10;
    private static final long SECOND = 1000;
    private final ScheduledExecutorService drawingScheduler;
    private final ScheduledExecutorService screenUpdateScheduler;
    private final DrawingStepTask drawingStepTask;
    private final ScreenUpdateTask screenUpdateTask;

    public static RainbowTaskScheduler newInstance(Rainbow rainbow) {
        ScheduledExecutorService drawingScheduler = SafeScheduledExecutor.newInstance();
        DrawingStepTask drawingStepTask = new DrawingStepTask(rainbow);
        ScheduledExecutorService screenUpdateScheduler = SafeScheduledExecutor.newInstance();
        ScreenUpdateTask screenUpdateTask = new ScreenUpdateTask(rainbow);
        return new RainbowTaskScheduler(drawingScheduler, drawingStepTask, screenUpdateScheduler, screenUpdateTask);
    }

    protected RainbowTaskScheduler(ScheduledExecutorService drawingScheduler,
                                   DrawingStepTask drawingStepTask,
                                   ScheduledExecutorService screenUpdateScheduler,
                                   ScreenUpdateTask screenUpdateTask) {
        this.drawingScheduler = drawingScheduler;
        this.drawingStepTask = drawingStepTask;
        this.screenUpdateScheduler = screenUpdateScheduler;
        this.screenUpdateTask = screenUpdateTask;
    }

    public void scheduleAt(int frameRate) {
        drawingScheduler.scheduleAtFixedRate(drawingStepTask, SECOND, SECOND / frameRate, TimeUnit.MILLISECONDS);
        screenUpdateScheduler.scheduleAtFixedRate(screenUpdateTask, SECOND, SECOND / Rainbow.DEFAULT_FRAME_RATE, TimeUnit.MILLISECONDS);
    }

    public boolean isTerminated() {
        return drawingScheduler.isTerminated() && screenUpdateScheduler.isTerminated();
    }

    public void shutdown() throws InterruptedException {
        drawingScheduler.shutdownNow();
        drawingScheduler.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        screenUpdateScheduler.shutdownNow();
        screenUpdateScheduler.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
    }
}
