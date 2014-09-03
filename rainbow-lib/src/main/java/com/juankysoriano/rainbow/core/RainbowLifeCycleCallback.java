package com.juankysoriano.rainbow.core;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public interface RainbowLifeCycleCallback {
    /**
     * Called when the rainbow is being setup.
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onSketchSetup(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is about to start.
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingStart(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is resumed
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingResume(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is about to perform a new step
     * Here is where the animations should be done
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is about to pause
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingPause(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is stopped
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onDrawingStop(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);

    /**
     * Called when the rainbow sketch is about to be destroyed
     * This will be the last chance to draw before the bitmaps are recycled and released
     *
     * @param rainbowDrawer          Used to draw into the rainbow sketch
     * @param rainbowInputController Used to control the input events.
     */
    void onSketchDestroy(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController);
}
