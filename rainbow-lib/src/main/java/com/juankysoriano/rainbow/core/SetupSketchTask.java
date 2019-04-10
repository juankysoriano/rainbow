package com.juankysoriano.rainbow.core;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

class SetupSketchTask {
    private AsyncTask<Void, Void, Void> task;

    public static SetupSketchTask newInstance(Rainbow rainbow) {
        return new SetupSketchTask(new WeakReference<>(rainbow));
    }

    private SetupSketchTask(WeakReference<Rainbow> rainbowWeakReference) {
        this.task = new SketchTask(rainbowWeakReference);
    }

    void start() {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void cancel() {
        if (task != null) {
            task.cancel(true);
        }
    }

    private static class SketchTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<Rainbow> rainbowWeakReference;

        SketchTask(WeakReference<Rainbow> rainbowWeakReference) {
            this.rainbowWeakReference = rainbowWeakReference;
        }

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
    }

}
