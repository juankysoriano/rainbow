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
    private final DrawingTask.Step stepTask;
    private final DrawingTask.Invalidate invalidateTask;
    private final Scheduler scheduler;
    private Disposable disposable;

    public static RainbowTaskScheduler newInstance(Rainbow rainbow) {
        RainbowDrawer rainbowDrawer = rainbow.getRainbowDrawer();
        DrawingTask.Step stepTask = new DrawingTask.Step(rainbow);
        DrawingTask.Invalidate invalidateTask = new DrawingTask.Invalidate(rainbow, rainbowDrawer);
        ThreadFactory threadFactory = new RxThreadFactory("RainbowDrawing", Thread.MAX_PRIORITY, true);
        SingleScheduler scheduler = new SingleScheduler(threadFactory);
        return new RainbowTaskScheduler(stepTask, invalidateTask, scheduler);
    }

    private RainbowTaskScheduler(DrawingTask.Step stepTask,
                                 DrawingTask.Invalidate invalidateTask,
                                 Scheduler scheduler) {
        this.stepTask = stepTask;
        this.invalidateTask = invalidateTask;
        this.scheduler = scheduler;
    }

    void scheduleAt(int frameRate) {
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
