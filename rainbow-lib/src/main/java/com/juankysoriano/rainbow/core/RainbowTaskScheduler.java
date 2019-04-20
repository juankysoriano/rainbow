package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.internal.schedulers.SingleScheduler;

class RainbowTaskScheduler {
    private static final long SECOND = TimeUnit.SECONDS.toMillis(1);
    private final Rainbow rainbow;
    private final DrawingTask.Step stepTask;
    private final DrawingTask.Invalidate invalidateTask;
    private final Scheduler scheduler;
    private Disposable disposable;

    static RainbowTaskScheduler newInstance(final Rainbow rainbow) {
        final RainbowDrawer rainbowDrawer = rainbow.getRainbowDrawer();
        DrawingTask.Step stepTask = new DrawingTask.Step(rainbow);
        DrawingTask.Invalidate invalidateTask = new DrawingTask.Invalidate(rainbow, rainbowDrawer);
        ThreadFactory threadFactory = new RxThreadFactory("RainbowDrawing", Thread.MAX_PRIORITY, true);
        SingleScheduler scheduler = new SingleScheduler(threadFactory);
        return new RainbowTaskScheduler(rainbow, stepTask, invalidateTask, scheduler);
    }

    private RainbowTaskScheduler(Rainbow rainbow,
                                 DrawingTask.Step stepTask,
                                 DrawingTask.Invalidate invalidateTask,
                                 Scheduler scheduler) {
        this.rainbow = rainbow;
        this.stepTask = stepTask;
        this.invalidateTask = invalidateTask;
        this.scheduler = scheduler;
    }

    void scheduleSetup() {
        scheduler.scheduleDirect(new Runnable() {
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
        scheduler.scheduleDirect(new Runnable() {
            @Override
            public void run() {
                rainbow.getRainbowDrawer().beginDraw();
                rainbow.onDrawingStep();
                rainbow.getRainbowDrawer().endDraw();
            }
        });
    }

    void scheduleDrawing(int frameRate) {
        if (isTerminated()) {
            Disposable stepDisposable = scheduler.schedulePeriodicallyDirect(stepTask, SECOND, SECOND / frameRate, TimeUnit.MILLISECONDS);
            Disposable invalidateDisposable = scheduler.schedulePeriodicallyDirect(invalidateTask, SECOND, SECOND / 60, TimeUnit.MILLISECONDS);
            disposable = new CompositeDisposable(stepDisposable, invalidateDisposable);
        }

    }

    boolean isTerminated() {
        return disposable == null || disposable.isDisposed();

    }

    void shutdown() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

}
