package com.juankysoriano.rainbow.demo.sketch.rainbow;

import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowEvent;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.listeners.RainbowInteractionListener;
import com.juankysoriano.rainbow.demo.R;
import com.juankysoriano.rainbow.utils.RainbowMath;

public class RainbowLineCirclesSketch extends Rainbow implements RainbowInteractionListener {

    private static final int MAX_RADIUS = 200;
    private static final int MIN_RADIUS = 1;
    private static final int[] RAINBOW = {R.color.red, R.color.orange, R.color.yellow, R.color.green, R.color.blue, R.color.purple, R.color.white};
    private int radius;
    private int color;
    private int colorIndex;
    private RainbowDrawer.PointDetectedListener pointDetectedListener = new RainbowDrawer.PointDetectedListener() {

        @Override
        public void onPointDetected(float x, float y, RainbowDrawer rainbowDrawer) {
            int decodedColor = getContext().getResources().getColor(color);

            updateRadiusAndColor();
            drawEllipse(x, y, radius, decodedColor, rainbowDrawer);
        }

        private void updateRadiusAndColor() {
            radius = RainbowMath.constrain(++radius, MIN_RADIUS, MAX_RADIUS);
            if (radius == MAX_RADIUS) {
                radius = MIN_RADIUS;
                colorIndex++;
                color = RAINBOW[colorIndex % RAINBOW.length];
            }
        }

        private void drawEllipse(float x, float y, float radius, int color, RainbowDrawer rainbowDrawer) {
            rainbowDrawer.stroke(0, 60);
            rainbowDrawer.fill(color, 190);
            rainbowDrawer.ellipseMode(CENTER);
            rainbowDrawer.ellipse(x, y, radius, radius);
        }

    };

    public RainbowLineCirclesSketch(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup(RainbowDrawer rainbowDrawer) {
        radius = MIN_RADIUS;
        color = RAINBOW[colorIndex];
    }

    @Override
    public void onDrawingStart(RainbowInputController rainbowInputController) {
        rainbowInputController.setRainbowInteractionListener(this);
    }

    @Override
    public void onDrawingStop(RainbowInputController rainbowInputController) {
        rainbowInputController.removeSketchInteractionListener();
    }

    @Override
    public void onSketchDestroy() {
        pointDetectedListener = null;
    }

    @Override
    public void onSketchTouched(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    @Override
    public void onSketchReleased(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    @Override
    public void onFingerDragged(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    private void drawEllipsedLine(float x1, float y1, float x2, float y2) {
        final RainbowDrawer drawer = getRainbowDrawer();
        drawer.exploreLine(x1, y1, x2, y2, pointDetectedListener);
    }

    @Override
    public void onMotionEvent(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        drawEllipsedLine(event.getPreviousX(), event.getPreviousY(), event.getX(), event.getY());
    }
}
