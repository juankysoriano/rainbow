package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

class ScreenUpdateTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;
    private final RainbowTaskScheduler.ScreenUpdate screenUpdate;

    ScreenUpdateTask(Rainbow rainbow, RainbowTaskScheduler.ScreenUpdate screenUpdate) {
        this.weakRainbow = new WeakReference<>(rainbow);
        this.screenUpdate = screenUpdate;
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused()) {
            if (rainbow.isVSync()) {
                screenUpdate.pending();
            } else {
                rainbow.getRainbowDrawer().invalidate();
            }
        }
    }
}
