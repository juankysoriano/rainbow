package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

class ScreenUpdateTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;
    private final RainbowTaskScheduler.Progress progress;

    ScreenUpdateTask(Rainbow rainbow, RainbowTaskScheduler.Progress progress) {
        this.weakRainbow = new WeakReference<>(rainbow);
        this.progress = progress;
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused()) {
            if (rainbow.isVSync()) {
                waitForDrawingStep();
            }

            progress.drawingScreen();
            rainbow.getRainbowDrawer().invalidate();
            progress.screenDrawn();
        }
    }

    @SuppressWarnings({"LoopConditionNotUpdatedInsideLoop", "StatementWithEmptyBody"})
    private void waitForDrawingStep() {
        while (progress.isDrawingStep()) {
        }
    }
}
