package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public interface RainbowLifeCycleCallback {
    /**
     * Called when the rainbow is being setup.
     *
     * @param rainbowDrawer Used to draw into the rainbow sketch
     */
    void onSketchSetup(RainbowDrawer rainbowDrawer);

    /**
     * Called when the rainbow sketch is about to start.
     *
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingStart(RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is resumed
     */
    void onDrawingResume();

    /**
     * Called when the rainbow sketch is about to perform a new step
     * Here is where the animations should be done.
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is about to pause
     */
    void onDrawingPause();

    /**
     * Called when the rainbow sketch is stopped
     *
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingStop(RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is about to be destroyed
     * After this is executed, all the bitmaps will be recycled
     */
    void onSketchDestroy();
}
