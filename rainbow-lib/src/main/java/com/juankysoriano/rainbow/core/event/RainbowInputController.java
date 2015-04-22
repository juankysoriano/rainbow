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
    private boolean fingerMoving;

    public RainbowInputController() {
        fingerPositionPredictor = new FingerPositionSmoother();
        x = y = px = py = -1;
    }

    public void postEvent(final MotionEvent motionEvent, final RainbowDrawer rainbowDrawer) {
        AsyncTask<MotionEvent, Void, MotionEvent> postTask = new AsyncTask<MotionEvent, Void, MotionEvent>() {
            @Override
            protected void onPreExecute() {
                preHandleEvent(motionEvent);
            }

            @Override
            protected MotionEvent doInBackground(MotionEvent... motionEvents) {
                MotionEvent motionEvent = motionEvents[0];
                handleSketchEvent(motionEvents[0], rainbowDrawer);
                paintStepListener.onDrawingStep();
                return motionEvent;
            }

            @Override
            protected void onPostExecute(MotionEvent motionEvent) {
                postHandleEvent(motionEvent);
            }
        };

        postTask.execute(motionEvent);
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
                fingerMoving = false;
                performTouch(event, rainbowDrawer);
                break;
            case MotionEvent.ACTION_UP:
                screenTouched = false;
                fingerMoving = false;
                performRelease(event, rainbowDrawer);
                break;
            case MotionEvent.ACTION_MOVE:
                screenTouched = true;
                fingerMoving = true;
                performMove(event, rainbowDrawer);
                break;
        }

        performMotion(event, rainbowDrawer);
    }

    private void performTouch(MotionEvent event, RainbowDrawer rainbowDrawer) {
        fingerPositionPredictor.resetTo(x, y);
        if (hasInteractionListener()) {
            rainbowInteractionListener.onSketchTouched(event, rainbowDrawer);
        }
    }

    private void performRelease(MotionEvent event, RainbowDrawer rainbowDrawer) {
        fingerPositionPredictor.resetTo(x, y);
        if (hasInteractionListener()) {
            rainbowInteractionListener.onSketchReleased(event, rainbowDrawer);
        }
    }

    private void performMove(MotionEvent event, RainbowDrawer rainbowDrawer) {
        fingerPositionPredictor.moveTo(x, y);
        if (hasInteractionListener()) {
            event.setLocation(x, y);
            rainbowInteractionListener.onFingerDragged(event, rainbowDrawer);
        }
    }

    private void performMotion(MotionEvent event, RainbowDrawer rainbowDrawer) {
        if (hasInteractionListener()) {
            rainbowInteractionListener.onMotionEvent(event, rainbowDrawer);
        }
    }

    public boolean isScreenTouched() {
        return screenTouched;
    }

    public boolean isFingerMoving() {
        return fingerMoving;
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
