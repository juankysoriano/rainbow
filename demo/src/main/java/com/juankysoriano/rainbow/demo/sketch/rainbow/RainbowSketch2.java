package com.juankysoriano.rainbow.demo.sketch.rainbow;

import android.graphics.Color;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public class RainbowSketch2 extends Rainbow {
    protected RainbowSketch2(ViewGroup parentView) {
        super(parentView);
    }

    public void onSketchSetup(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
      //Called when the rainbow is being setup.
    }

    public void onDrawingStart(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
      //Called when the rainbow sketch is about to start.
    }

    public void onDrawingResume(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
       //Called when the rainbow sketch is resumed
    }

    public void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
       //Called when the rainbow sketch is about to perform a new step
       //Here is where the animations should be done
        rainbowDrawer.fill(Color.RED);
    }

    public void onDrawingPause(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
       //Called when the rainbow sketch is about to pause
    }

    public void onDrawingStop(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        //Called when the rainbow sketch is about to stop
    }

    public void onSketchDestroy(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController){
        //Called when the rainbow sketch is about to be destroyed
    }
}
