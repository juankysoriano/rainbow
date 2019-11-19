package com.juankysoriano.rainbow.core.event;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

public class RainbowInputController {
    private static final int DIVISIONS = 3;
    private final RainbowDrawer rainbowDrawer;
    private final FingerPositionSmoother smoother;
    private float x, y;
    private float px, py;
    private RainbowInteractionListener rainbowInteractionListener;
    private boolean screenTouched;
    private boolean fingerMoving;
    private float scaleFactor;
    private MotionEvent nextEvent;

    public static RainbowInputController newInstance() {
        RainbowDrawer rainbowDrawer = new RainbowDrawer();
        FingerPositionSmoother positionSmoother = new FingerPositionSmoother();
        return new RainbowInputController(rainbowDrawer, positionSmoother);
    }

    private RainbowInputController(RainbowDrawer rainbowDrawer,
                                   FingerPositionSmoother predictor) {
        this.rainbowDrawer = rainbowDrawer;
        smoother = predictor;
        x = y = px = py = -1;
    }

    public void setScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void postEvent(final MotionEvent motionEvent) {
        nextEvent = motionEvent;
    }

    public void dispatchEvent() {
        if (nextEvent == null) {
            return;
        }

        switch (nextEvent.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
                process(nextEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                splitIntoMultipleEvents(nextEvent);
                break;
            default://no-op
        }
        nextEvent = null;
    }

    public void clearEvents() {
        nextEvent = null;
    }

    private void process(@NonNull MotionEvent motionEvent) {
        MotionEvent scaledEvent = MotionEvent.obtain(motionEvent);
        scaledEvent.setLocation(motionEvent.getX() * scaleFactor, motionEvent.getY() * scaleFactor);
        preHandleEvent(scaledEvent);
        handleSketchEvent(scaledEvent);
        postHandleEvent();
    }

    private void splitIntoMultipleEvents(@NonNull MotionEvent event) {
        float px = getX();
        float py = getY();
        float diffX = event.getX() - px;
        float diffY = event.getY() - py;

        for (int i = 1; i <= DIVISIONS; i++) {
            float newEventX = px + diffX * i / DIVISIONS;
            float newEventY = py + diffY * i / DIVISIONS;
            MotionEvent subEvent = obtainEventWithNewPosition(event, newEventX, newEventY);
            process(subEvent);
        }
    }

    private MotionEvent obtainEventWithNewPosition(@NonNull MotionEvent event, float newEventX, float newEventY) {
        MotionEvent motionEvent = MotionEvent.obtain(event);
        motionEvent.setLocation(newEventX, newEventY);
        return motionEvent;
    }

    private void preHandleEvent(MotionEvent motionEvent) {
        x = motionEvent.getX();
        y = motionEvent.getY();
    }

    private void handleSketchEvent(final MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                screenTouched = true;
                fingerMoving = false;
                performTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                screenTouched = false;
                fingerMoving = false;
                performRelease(event);
                break;
            case MotionEvent.ACTION_MOVE:
                screenTouched = true;
                fingerMoving = true;
                performMove(event);
                break;
        }

        performMotion(event);
    }

    private void postHandleEvent() {
        px = x;
        py = y;
    }

    private void performTouch(MotionEvent event) {
        smoother.resetTo(event.getX(), event.getY());
        if (rainbowInteractionListener != null) {
            rainbowInteractionListener.onSketchTouched(event);
        }
    }

    private void performRelease(MotionEvent event) {
        smoother.resetTo(event.getX(), event.getY());
        if (rainbowInteractionListener != null) {
            rainbowInteractionListener.onSketchReleased(event);
        }
    }

    private void performMove(MotionEvent event) {
        smoother.moveTo(event.getX(), event.getY());
        if (rainbowInteractionListener != null) {
            rainbowInteractionListener.onFingerDragged(event);
        }
    }

    private void performMotion(MotionEvent event) {
        if (rainbowInteractionListener != null) {
            rainbowInteractionListener.onMotionEvent(event);
        }
    }

    public boolean isScreenTouched() {
        return screenTouched;
    }

    public boolean isFingerMoving() {
        return fingerMoving;
    }

    /**
     * Used to set a RainbowInteractionListener which will listen for different interaction events
     *
     * @param rainbowInteractionListener
     */
    public void attach(RainbowInteractionListener rainbowInteractionListener) {
        this.rainbowInteractionListener = rainbowInteractionListener;
    }

    public RainbowDrawer getRainbowDrawer() {
        return rainbowDrawer;
    }

    /**
     * Used to remove the attached RainbowInteractionListener
     * Also stops any pending input event processing task.
     */
    public void detach() {
        this.rainbowInteractionListener = null;
        clearEvents();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPreviousX() {
        return px;
    }

    public float getPreviousY() {
        return py;
    }

    public MovementDirection getVerticalDirection() {
        if (smoother.getY() > smoother.getOldY()) {
            return MovementDirection.DOWN;
        } else {
            return MovementDirection.UP;
        }
    }

    public MovementDirection getHorizontalDirection() {
        if (smoother.getX() > smoother.getOldX()) {
            return MovementDirection.RIGHT;
        } else {
            return MovementDirection.LEFT;
        }
    }

    public float getSmoothX() {
        return smoother.getX();
    }

    public float getSmoothY() {
        return smoother.getY();
    }

    public float getPreviousSmoothX() {
        return smoother.getOldX();
    }

    public float getPreviousSmoothY() {
        return smoother.getOldY();
    }

    public float getFingerVelocity() {
        return smoother.getFingerVelocity();
    }

    public enum MovementDirection {
        UP, DOWN, LEFT, RIGHT
    }

    public interface RainbowInteractionListener {
        void onSketchTouched(final MotionEvent event);

        void onSketchReleased(final MotionEvent event);

        void onFingerDragged(final MotionEvent event);

        void onMotionEvent(final MotionEvent event);
    }
}
