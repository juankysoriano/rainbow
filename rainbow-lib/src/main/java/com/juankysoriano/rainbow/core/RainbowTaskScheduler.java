package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.utils.schedulers.RainbowScheduler;
import com.juankysoriano.rainbow.utils.schedulers.RainbowSchedulers;

import java.util.concurrent.TimeUnit;

class RainbowTaskScheduler {
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
    private final Rainbow rainbow;
    private final DrawingTask.Step stepTask;
    private final DrawingTask.Invalidate invalidateTask;
    private final DrawingTask.Input inputTask;
    private RainbowScheduler screenScheduler;
    private RainbowScheduler inputScheduler;

    static RainbowTaskScheduler newInstance(final Rainbow rainbow) {
        DrawingTask.Step stepTask = new DrawingTask.Step(rainbow);
        DrawingTask.Invalidate invalidateTask = new DrawingTask.Invalidate(rainbow);
        DrawingTask.Input inputTask = new DrawingTask.Input(rainbow, rainbow.getRainbowInputController());
        return new RainbowTaskScheduler(rainbow, stepTask, invalidateTask, inputTask);
    }

    private RainbowTaskScheduler(Rainbow rainbow,
                                 DrawingTask.Step stepTask,
                                 DrawingTask.Invalidate invalidateTask,
                                 DrawingTask.Input inputTask) {
        this.rainbow = rainbow;
        this.stepTask = stepTask;
        this.invalidateTask = invalidateTask;
        this.inputTask = inputTask;
    }

    void scheduleSetup() {
        screenScheduler().scheduleNow(new Runnable() {
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
        screenScheduler().scheduleNow(new Runnable() {
            @Override
            public void run() {
                rainbow.getRainbowDrawer().beginDraw();
                rainbow.onDrawingStep();
                rainbow.getRainbowDrawer().endDraw();
            }
        });
    }

    void scheduleDrawing(int stepRate, int frameRate, int inputRate) {
        screenScheduler().scheduleAtRate(stepTask, SECOND / stepRate, TimeUnit.NANOSECONDS);
        screenScheduler().scheduleAtRate(invalidateTask, SECOND / frameRate, TimeUnit.NANOSECONDS);
        inputScheduler().scheduleAtRate(inputTask, SECOND / inputRate, TimeUnit.NANOSECONDS);
    }

    boolean isTerminated() {
        return screenScheduler().isTerminated() || inputScheduler().isTerminated();

    }

    void shutdown() {
        stepTask.shutdown();
        invalidateTask.shutdown();
        inputTask.shutdown();
        screenScheduler().shutdown();
        inputScheduler().shutdown();
    }

    private RainbowScheduler screenScheduler() {
        if (screenScheduler == null || screenScheduler.isTerminated()) {
            screenScheduler = RainbowSchedulers.single("Drawing", RainbowSchedulers.Priority.MAX);
        }
        return screenScheduler;
    }

    private RainbowScheduler inputScheduler() {
        if (inputScheduler == null || inputScheduler.isTerminated()) {
            inputScheduler = RainbowSchedulers.single("Input", RainbowSchedulers.Priority.MAX);
        }
        return inputScheduler;
    }

}
