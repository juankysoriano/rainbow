package com.juankysoriano.rainbow.core;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

class SetupSketchTask {
    private final WeakReference<Rainbow> rainbowWeakReference;
    private AsyncTask<Void, Void, Void> task;

    public static SetupSketchTask newInstance(Rainbow rainbow) {
        return new SetupSketchTask(new WeakReference<>(rainbow));
    }

    public SetupSketchTask(WeakReference<Rainbow> rainbowWeakReference) {
        this.rainbowWeakReference = rainbowWeakReference;
    }

    public void start() {
        task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Rainbow rainbow = rainbowWeakReference.get();
                if (rainbow != null && !isCancelled()) {
                    rainbow.getRainbowDrawer().beginDraw();
                    rainbow.onSketchSetup();
                    rainbow.getRainbowDrawer().endDraw();
                }
                return null;
            }
        };

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancel() {
        if (hasTask()) {
            task.cancel(true);
        }
    }

    private boolean hasTask() {
        return task != null;
    }

}
