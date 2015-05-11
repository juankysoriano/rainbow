package com.juankysoriano.rainbow.core;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

public class DrawingTask extends TimerTask {

    private final WeakReference<Rainbow> weakRainbow;

    DrawingTask(Rainbow rainbow) {
        this.weakRainbow = new WeakReference<>(rainbow);
    }

    @Override
    public void run() {
        Rainbow rainbow = weakRainbow.get();
        if (rainbow != null && !rainbow.isPaused()) {
            rainbow.performStep();
        }
    }
}
