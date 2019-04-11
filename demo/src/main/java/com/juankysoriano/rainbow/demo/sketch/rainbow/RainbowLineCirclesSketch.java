package com.juankysoriano.rainbow.demo.sketch.rainbow;

import android.view.MotionEvent;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.Precision;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;

public class RainbowLineCirclesSketch extends Rainbow implements RainbowInputController.RainbowInteractionListener {

    private RainbowDrawer.PointDetectedListener pointDetectedListener = new RainbowDrawer.PointDetectedListener() {

        @Override
        public void onPointDetected(float px, float py, float x, float y, RainbowDrawer rainbowDrawer) {
            drawEllipse(x, y, rainbowDrawer);
        }
    };

    public RainbowLineCirclesSketch(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup() {
        frameRate(500);
        getRainbowDrawer().background(255);
        getRainbowDrawer().smooth();
        getRainbowDrawer().noFill();
    }

    @Override
    public void onDrawingStart() {
        getRainbowInputController().setRainbowInteractionListener(this);
    }

    @Override
    public void onDrawingStop() {
        getRainbowInputController().removeSketchInteractionListener();
    }

    @Override
    public void onSketchDestroy() {
        pointDetectedListener = null;
    }

    @Override
    public void onSketchTouched(MotionEvent event, RainbowDrawer rainbowDrawer) {
        drawEllipse(getRainbowInputController().getX(), getRainbowInputController().getY(), rainbowDrawer);
    }

    @Override
    public void onSketchReleased(MotionEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    @Override
    public void onFingerDragged(MotionEvent event, RainbowDrawer rainbowDrawer) {
        RainbowInputController rainbowInputController = getRainbowInputController();
        float x = rainbowInputController.getSmoothX();
        float y = rainbowInputController.getSmoothY();
        float oldX = rainbowInputController.getPreviousSmoothX();
        float oldY = rainbowInputController.getPreviousSmoothY();

        drawEllipsedLine(oldX, oldY, x, y);
    }

    private void drawEllipsedLine(float x1, float y1, float x2, float y2) {
        final RainbowDrawer drawer = getRainbowDrawer();
        drawer.exploreLine(x1, y1, x2, y2, Precision.HIGH, pointDetectedListener);
    }

    @Override
    public void onMotionEvent(MotionEvent event, RainbowDrawer rainbowDrawer) {

    }

    private void drawEllipse(float x, float y, RainbowDrawer rainbowDrawer) {
        rainbowDrawer.stroke(0, 255);
        rainbowDrawer.fill(255, 0);
        rainbowDrawer.ellipseMode(RainbowGraphics.CENTER);
        rainbowDrawer.ellipse(x, y, (float) 200, (float) 200);
    }
}
