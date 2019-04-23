package com.juankysoriano.rainbow.demo.sketch.rainbow;

import android.view.MotionEvent;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.Modes;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.Precision;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public class RainbowLineCirclesSketch extends Rainbow implements RainbowInputController.RainbowInteractionListener {

    private RainbowDrawer.PointDetectedListener pointDetectedListener = new RainbowDrawer.PointDetectedListener() {

        @Override
        public void onPointDetected(float px, float py, float x, float y) {
            getRainbowDrawer().stroke(0, 30);
            getRainbowDrawer().fill(0, 0);
            getRainbowDrawer().ellipseMode(Modes.Draw.CENTER);
            getRainbowDrawer().ellipse(x, y, 200, 200);
        }

    };

    public RainbowLineCirclesSketch(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup() {
        stepRate(500);
        getRainbowDrawer().background(255);
        getRainbowDrawer().smooth();
        getRainbowDrawer().noFill();
    }

    @Override
    public void onDrawingStart() {
        getRainbowInputController().attach(this);
    }

    @Override
    public void onDrawingStop() {
        getRainbowInputController().detach();
    }

    @Override
    public void onSketchDestroy() {
        pointDetectedListener = null;
    }

    @Override
    public void onSketchTouched(MotionEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    @Override
    public void onSketchReleased(MotionEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    @Override
    public void onFingerDragged(MotionEvent event, RainbowDrawer rainbowDrawer) {
        RainbowInputController rainbowInputController = getRainbowInputController();
        float x = rainbowInputController.getX();
        float y = rainbowInputController.getY();
        float oldX = rainbowInputController.getPreviousX();
        float oldY = rainbowInputController.getPreviousY();
        getRainbowDrawer().exploreLine(oldX, oldY, x, y, Precision.VERY_HIGH, pointDetectedListener);
    }

    @Override
    public void onMotionEvent(MotionEvent event, RainbowDrawer rainbowDrawer) {

    }
}
