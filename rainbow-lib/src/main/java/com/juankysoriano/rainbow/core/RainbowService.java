package com.juankysoriano.rainbow.core;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RainbowService extends Service {
    private ScheduledExecutorService drawingScheduler;
    private DrawingTask drawingTask;
    private RainbowStepCallback rainbowStepCallback;
    private RainbowBinder rainbowBinder = new RainbowBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return rainbowBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.drawingTask = new DrawingTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startExecutioner();
        return START_STICKY;
    }

    private boolean hasRainbowStepCallback() {
        return this.rainbowStepCallback != null;
    }

    private void startExecutioner() {
        if (!hasDrawingScheduler() || drawingScheduler.isTerminated()) {
            drawingScheduler = Executors.newSingleThreadScheduledExecutor();
            drawingScheduler.scheduleAtFixedRate(drawingTask, 0, drawingTask.getDelay(), TimeUnit.MILLISECONDS);
        }
    }

    private boolean hasDrawingScheduler() {
        return drawingScheduler != null;
    }

    private void shutDownExecutioner() {
        try {
            drawingScheduler.shutdownNow();
            drawingScheduler.awaitTermination(DrawingTask.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        shutDownExecutioner();
        drawingScheduler = null;
        drawingTask = null;
        rainbowStepCallback = null;
        super.onDestroy();
    }

    public void setDrawingStepCallback(RainbowStepCallback drawingStepCallback) {
        this.rainbowStepCallback = drawingStepCallback;
    }

    class DrawingTask extends TimerTask {
        private static final long TIMEOUT = 10;
        private static final float SECOND = 1000F;

        @Override
        public void run() {
            if (!drawingScheduler.isShutdown() && hasRainbowStepCallback()) {
                rainbowStepCallback.performStep();
            }
        }

        public long getDelay() {
            return Math.max(1, (long) (SECOND / 60));
        }
    }

    class RainbowBinder extends Binder {
        public RainbowService getService() {
            return RainbowService.this;
        }
    }
}
