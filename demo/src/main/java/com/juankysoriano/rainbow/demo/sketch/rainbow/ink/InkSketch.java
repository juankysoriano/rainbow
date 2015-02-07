package com.juankysoriano.rainbow.demo.sketch.rainbow.ink;

import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowEvent;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public class InkSketch extends Rainbow implements RainbowInputController.RainbowInteractionListener {

    private StepDrawer inkDrawer;
    private final RainbowInputController rainbowInputController;
    private final RainbowDrawer rainbowDrawer;

    public static InkSketch newInstance(ViewGroup viewGroup) {
        RainbowDrawer rainbowDrawer = new RainbowDrawer();
        RainbowInputController rainbowInputController = new RainbowInputController();
        return new InkSketch(viewGroup, rainbowDrawer, rainbowInputController);
    }

    InkSketch(ViewGroup viewGroup,
              RainbowDrawer rainbowDrawer,
              RainbowInputController rainbowInputController) {
        super(viewGroup, rainbowDrawer, rainbowInputController);
        this.rainbowDrawer = rainbowDrawer;
        this.rainbowInputController = rainbowInputController;
    }

    @Override
    public void onSketchSetup() {
        this.inkDrawer = InkDrawer.newInstance(rainbowDrawer, rainbowInputController);
        rainbowInputController.setRainbowInteractionListener(this);
    }

    @Override
    public void onDrawingStart() {
        super.onDrawingStart();
    }

    @Override
    public void onDrawingStep() {
        inkDrawer.paintStep();
    }

    @Override
    public void onDrawingStop() {
        super.onDrawingStop();
    }

    @Override
    public void onSketchDestroy() {
    }

    @Override
    public void onSketchTouched(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        inkDrawer.initDrawingAt(event.getX(), event.getY());
    }

    @Override
    public void onSketchReleased(RainbowEvent event, RainbowDrawer rainbowDrawer) {
    }

    @Override
    public void onFingerDragged(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        inkDrawer.paintStep();
    }

    @Override
    public void onMotionEvent(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }
}
