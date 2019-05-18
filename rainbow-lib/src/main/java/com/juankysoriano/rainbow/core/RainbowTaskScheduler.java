package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.utils.schedulers.RainbowScheduler;
import com.juankysoriano.rainbow.utils.schedulers.RainbowSchedulers;

import java.util.concurrent.TimeUnit;

class RainbowTaskScheduler {
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
    private final Rainbow rainbow;
    private final DrawingTask.Step stepTask;
    private final DrawingTask.Invalidate invalidateTask;
    private RainbowScheduler scheduler;

    static RainbowTaskScheduler newInstance(final Rainbow rainbow) {
        DrawingTask.Step stepTask = new DrawingTask.Step(rainbow);
        DrawingTask.Invalidate invalidateTask = new DrawingTask.Invalidate(rainbow);
        return new RainbowTaskScheduler(rainbow, stepTask, invalidateTask);
    }

    private RainbowTaskScheduler(Rainbow rainbow,
                                 DrawingTask.Step stepTask,
                                 DrawingTask.Invalidate invalidateTask) {
        this.rainbow = rainbow;
        this.stepTask = stepTask;
        this.invalidateTask = invalidateTask;
    }

    void scheduleSetup() {
        scheduler().scheduleNow(new Runnable() {
            @Override
            public void run() {
                rainbow.setupSketch();
                rainbow.getRainbowDrawer().beginDraw();
                rainbow.onSketchSetup();
                rainbow.getRainbowDrawer().endDraw();
                rainbow.start();
            }
        });
    }

    void scheduleSingleDraw() {
        scheduler().scheduleNow(new Runnable() {
            @Override
            public void run() {
                rainbow.getRainbowDrawer().beginDraw();
                rainbow.onDrawingStep();
                rainbow.getRainbowDrawer().endDraw();
            }
        });
    }

    void scheduleDrawing(int stepRate, int frameRate) {
        scheduler().scheduleAtRate(stepTask, SECOND / stepRate, TimeUnit.NANOSECONDS);
        scheduler().scheduleAtRate(invalidateTask, SECOND / frameRate, TimeUnit.NANOSECONDS);
    }

    boolean isTerminated() {
        return scheduler().isTerminated();

    }

    void shutdown() {
        scheduler().shutdown();
    }

    private RainbowScheduler scheduler() {
        if (scheduler == null || scheduler.isTerminated()) {
            scheduler = RainbowSchedulers.single("Drawing", RainbowSchedulers.Priority.MAX);
        }
        return scheduler;
    }

}
