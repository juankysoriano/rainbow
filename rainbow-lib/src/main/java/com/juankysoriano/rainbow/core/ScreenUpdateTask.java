package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

class ScreenUpdateTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;

    ScreenUpdateTask(Rainbow rainbow) {
        this.weakRainbow = new WeakReference<>(rainbow);
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused() && isVSyncDisabled()) {
            rainbow.getRainbowDrawer().invalidate();
        }
    }

    private boolean isVSyncDisabled() {
        Rainbow rainbow = weakRainbow.get();
        return rainbow != null && !rainbow.getRainbowDrawer().isVSync();
    }
}
