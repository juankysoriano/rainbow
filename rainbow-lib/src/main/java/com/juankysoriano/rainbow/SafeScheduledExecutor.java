package com.juankysoriano.rainbow;

import android.util.Log;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SafeScheduledExecutor extends ScheduledThreadPoolExecutor {

    private SafeScheduledExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public static SafeScheduledExecutor newInstance() {
        return new SafeScheduledExecutor(1);
    }

    public static SafeScheduledExecutor newInstance(int corePoolSize) {
        return new SafeScheduledExecutor(corePoolSize);
    }

    @Override
    public ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
    }

    private Runnable wrapRunnable(Runnable command) {
        return new LogOnExceptionRunnable(command);
    }

    private class LogOnExceptionRunnable implements Runnable {
        private Runnable runnable;

        LogOnExceptionRunnable(Runnable runnable) {
            super();
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Exception e) {
                Log.e("Rainbow:", e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
