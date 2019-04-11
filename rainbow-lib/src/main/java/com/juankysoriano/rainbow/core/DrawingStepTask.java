package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

class DrawingStepTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;

    DrawingStepTask(Rainbow rainbow) {
        this.weakRainbow = new WeakReference<>(rainbow);
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused() && rainbow.isSetup) {
            rainbow.performStep();
            updateScreenIfVSync();
        }
    }

    private void updateScreenIfVSync() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && rainbow.getRainbowDrawer().isVSync() && rainbow.isSetup) {
            rainbow.getRainbowDrawer().invalidate();
        }
    }
}
