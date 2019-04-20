package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

interface DrawingTask extends Runnable {
    class Step implements DrawingTask {
        private final Rainbow rainbow;

        Step(Rainbow rainbow) {
            this.rainbow = rainbow;
        }

        @Override
        public void run() {
            if (rainbow.isResumed()) {
                rainbow.performStep();
            }
        }
    }

    class Invalidate implements DrawingTask {
        private final Rainbow rainbow;
        private final RainbowDrawer rainbowDrawer;

        Invalidate(Rainbow rainbow, RainbowDrawer rainbowDrawer) {
            this.rainbow = rainbow;
            this.rainbowDrawer = rainbowDrawer;
        }

        @Override
        public void run() {
            if (rainbow.isResumed()) {
                rainbowDrawer.invalidate();
            }
        }
    }
}
