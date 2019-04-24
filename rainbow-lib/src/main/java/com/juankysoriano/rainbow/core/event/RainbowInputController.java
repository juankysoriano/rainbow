package com.juankysoriano.rainbow.core.event;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.utils.schedulers.RainbowScheduler;
import com.juankysoriano.rainbow.utils.schedulers.RainbowSchedulers;

public class RainbowInputController {
    private static final int DIVISIONS = 2;
    private final RainbowDrawer rainbowDrawer;
    private final FingerPositionSmoother smoother;
    private final RainbowScheduler scheduler;
    private float x, y;
    private float px, py;
    private RainbowInteractionListener rainbowInteractionListener;
    private boolean screenTouched;
    private boolean fingerMoving;

    public static RainbowInputController newInstance() {
        RainbowDrawer rainbowDrawer = new RainbowDrawer();
        RainbowScheduler scheduler = RainbowSchedulers.single("InputController", RainbowSchedulers.Priority.NORMAL);
        FingerPositionSmoother positionSmoother = new FingerPositionSmoother();
        return new RainbowInputController(rainbowDrawer, scheduler, positionSmoother);
    }

    private RainbowInputController(RainbowDrawer rainbowDrawer,
                                   RainbowScheduler rainbowScheduler,
                                   FingerPositionSmoother predictor) {
        this.rainbowDrawer = rainbowDrawer;
        scheduler = rainbowScheduler;
        smoother = predictor;
        x = y = px = py = -1;
    }

    public void postEvent(final MotionEvent motionEvent) {
        scheduler.scheduleNow(inputEventFor(MotionEvent.obtain(motionEvent)));
    }

    private Runnable inputEventFor(final MotionEvent motionEvent) {
        return new Runnable() {
            @Override
            public void run() {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_DOWN:
                        process(motionEvent);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        splitIntoMultipleEvents(motionEvent);
                        break;
                    default://no-op
                }
            }
        };
    }

    private void process(MotionEvent motionEvent) {
        preHandleEvent(motionEvent);
        handleSketchEvent(motionEvent);
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
        this.scheduler.shutdown();
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
