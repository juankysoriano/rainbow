package com.juankysoriano.rainbow.core;

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

        Invalidate(Rainbow rainbow) {
            this.rainbow = rainbow;
        }

        @Override
        public void run() {
            if (rainbow.isResumed()) {
                rainbow.performDraw();
            }
        }
    }
}
