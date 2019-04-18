package com.juankysoriano.rainbow.core;

class SetupSketchTask implements Runnable {

    private final Rainbow rainbow;

    SetupSketchTask(Rainbow rainbow) {
        this.rainbow = rainbow;
    }

    @Override
    public void run() {
        rainbow.getRainbowDrawer().beginDraw();
        rainbow.onSketchSetup();
        rainbow.getRainbowDrawer().endDraw();
    }
}
