package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

class DrawingStepTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;
    private final RainbowTaskScheduler.ScreenUpdate screenUpdate;

    DrawingStepTask(Rainbow rainbow, RainbowTaskScheduler.ScreenUpdate screenUpdate) {
        this.weakRainbow = new WeakReference<>(rainbow);
        this.screenUpdate = screenUpdate;
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused()) {
            rainbow.performStep();
            if (rainbow.isVSync() && screenUpdate.isPending()){
                rainbow.getRainbowDrawer().invalidate();
                screenUpdate.notPending();
            }
        }
    }

}
