package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.core.event.RainbowInputController;

interface DrawingTask extends Runnable {
    void shutdown();

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

        @Override
        public void shutdown() {
            // no op
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

        @Override
        public void shutdown() {
            // no op
        }
    }

    class Input implements DrawingTask {
        private final Rainbow rainbow;
        private final RainbowInputController inputController;

        Input(Rainbow rainbow, RainbowInputController inputController) {
            this.rainbow = rainbow;
            this.inputController = inputController;
        }

        @Override
        public void run() {
            if (rainbow.isResumed()) {
                inputController.dispatchEvent();
            }
        }

        @Override
        public void shutdown() {
            inputController.clearEvents();
        }
    }
}
