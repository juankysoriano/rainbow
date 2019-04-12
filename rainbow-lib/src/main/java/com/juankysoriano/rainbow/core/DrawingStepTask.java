package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

class DrawingStepTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;
    private final RainbowTaskScheduler.Progress progress;

    DrawingStepTask(Rainbow rainbow, RainbowTaskScheduler.Progress progress) {
        this.weakRainbow = new WeakReference<>(rainbow);
        this.progress = progress;
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused()) {
            if (rainbow.isVSync()) {
                waitForScreenUpdated();
            }

            progress.performingStep();
            rainbow.performStep();
            progress.stepPerformed();
        }
    }

    @SuppressWarnings({"LoopConditionNotUpdatedInsideLoop", "StatementWithEmptyBody"})
    private void waitForScreenUpdated() {
        while (progress.isUpdatingScreen()) {
        }
    }

}
