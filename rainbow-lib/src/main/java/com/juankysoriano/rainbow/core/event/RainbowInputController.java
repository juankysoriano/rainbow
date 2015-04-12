package com.juankysoriano.rainbow.core.event;

import android.os.AsyncTask;
import android.view.MotionEvent;

import com.juankysoriano.rainbow.core.PaintStepListener;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

public class RainbowInputController {
    private final FingerPositionSmoother fingerPositionPredictor;
    private PaintStepListener paintStepListener;
    private RainbowInteractionListener rainbowInteractionListener;
    private float x, y;
    private float px, py;
    private boolean screenTouched;

    public RainbowInputController() {
        fingerPositionPredictor = new FingerPositionSmoother();
        x = y = px = py = -1;
    }

    public void postEvent(final MotionEvent motionEvent, final RainbowDrawer rainbowDrawer) {
        AsyncTask<Void, Void, Void> postTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                preHandleEvent(motionEvent);
                handleSketchEvent(motionEvent, rainbowDrawer);
                if (isScreenTouched()) {
                    paintStepListener.onDrawingStep();
                }
                postHandleEvent(motionEvent);
                return null;
            }
        };

        postTask.execute();
    }

    private void preHandleEvent(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_DOWN)
                || (event.getAction() == MotionEvent.ACTION_UP)
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            px = x;
            py = y;
            x = event.getX();
            y = event.getY();
        }
    }

    private void handleSketchEvent(final MotionEvent event, final RainbowDrawer rainbowDrawer) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                screenTouched = true;
                fingerPositionPredictor.resetTo(x, y);
                if (hasInteractionListener()) {
                    rainbowInteractionListener.onSketchTouched(event, rainbowDrawer);
                }
                break;
            case MotionEvent.ACTION_UP:
                screenTouched = false;
                if (hasInteractionListener()) {
                    rainbowInteractionListener.onSketchReleased(event, rainbowDrawer);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                screenTouched = true;
                performMove(event, rainbowDrawer);
                break;
        }

        if (hasInteractionListener()) {
            rainbowInteractionListener.onMotionEvent(event, rainbowDrawer);
        }
    }

    private void performMove(MotionEvent event, RainbowDrawer rainbowDrawer) {
        float startX = px;
        float startY = py;
        float lowMiddleX = px + (x - px) / 3;
        float lowMiddleY = py + (y - py) / 3;
        float highMiddleX = px + 2 * (x - px) / 3;
        float highMiddleY = py + 2 * (y - py) / 3;
        float endX = x;
        float endY = y;

        notifyFingerDraggedFor(startX, startY, lowMiddleX, lowMiddleY, event, rainbowDrawer);
        notifyFingerDraggedFor(lowMiddleX, lowMiddleY, highMiddleX, highMiddleY, event, rainbowDrawer);
        notifyFingerDraggedFor(highMiddleX, highMiddleY, endX, endY, event, rainbowDrawer);
    }

    private void notifyFingerDraggedFor(float px, float py, float x, float y, MotionEvent event, RainbowDrawer rainbowDrawer) {
        this.px = px;
        this.py = py;
        this.x = x;
        this.y = y;
        fingerPositionPredictor.moveTo(x, y);
        if (hasInteractionListener()) {
            event.setLocation(x, y);
            rainbowInteractionListener.onFingerDragged(event, rainbowDrawer);
        }
    }

    public boolean isScreenTouched() {
        return screenTouched;
    }

    private void postHandleEvent(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_DOWN)
                || (event.getAction() == MotionEvent.ACTION_UP)
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            px = x;
            py = y;
        }
    }

    private boolean hasInteractionListener() {
        return rainbowInteractionListener != null;
    }

    /**
     * Used to set a RainbowInteractionListener which will listen for different interaction events
     *
     * @param rainbowInteractionListener
     */
    public void setRainbowInteractionListener(RainbowInteractionListener rainbowInteractionListener) {
        this.rainbowInteractionListener = rainbowInteractionListener;
    }

    /**
     * Used to remove the attached RainbowInteractionListener
     */
    public void removeSketchInteractionListener() {
        this.rainbowInteractionListener = null;
    }

    public boolean hasPaintStepListener() {
        return this.paintStepListener != null;
    }

    public void setPaintStepListener(PaintStepListener paintStepListener) {
        this.paintStepListener = paintStepListener;
    }

    public void removePaintStepListener() {
        this.paintStepListener = null;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPreviousX() {
        if (px == -1) {
            return x;
        } else {
            return px;
        }
    }

    public float getPreviousY() {
        if (py == -1) {
            return y;
        } else {
            return py;
        }
    }

    public MovementDirection getVerticalDirection() {
        if (fingerPositionPredictor.getY() > fingerPositionPredictor.getOldY()) {
            return MovementDirection.DOWN;
        } else {
            return MovementDirection.UP;
        }
    }

    public MovementDirection getHorizontalDirection() {
        if (fingerPositionPredictor.getX() > fingerPositionPredictor.getOldX()) {
            return MovementDirection.RIGHT;
        } else {
            return MovementDirection.LEFT;
        }
    }

    public float getSmoothX() {
        return fingerPositionPredictor.getX();
    }

    public float getSmoothY() {
        return fingerPositionPredictor.getY();
    }

    public float getPreviousSmoothX() {
        return fingerPositionPredictor.getOldX();
    }

    public float getPreviousSmoothY() {
        return fingerPositionPredictor.getOldY();
    }

    public float getFingerVelocity() {
        return fingerPositionPredictor.getFingerVelocity();
    }

    public enum MovementDirection {
        UP, DOWN, LEFT, RIGHT
    }

    public interface RainbowInteractionListener {
        void onSketchTouched(final MotionEvent event, final RainbowDrawer rainbowDrawer);

        void onSketchReleased(final MotionEvent event, final RainbowDrawer rainbowDrawer);

        void onFingerDragged(final MotionEvent event, final RainbowDrawer rainbowDrawer);

        void onMotionEvent(final MotionEvent event, final RainbowDrawer rainbowDrawer);
    }
}
