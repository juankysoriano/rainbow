package com.juankysoriano.rainbow.core.event;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.juankysoriano.rainbow.SafeScheduledExecutor;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

import java.util.concurrent.ExecutorService;

public class RainbowInputController {
    private static final int DIVISIONS = 2;
    private final RainbowDrawer rainbowDrawer;
    private final FingerPositionSmoother fingerPositionPredictor;
    private float x, y;
    private float px, py;
    private final ExecutorService executorService;
    private RainbowInteractionListener rainbowInteractionListener;
    private boolean screenTouched;
    private boolean fingerMoving;

    public static RainbowInputController newInstance(RainbowDrawer rainbowDrawer) {
        ExecutorService service = SafeScheduledExecutor.newInstance();
        FingerPositionSmoother positionSmoother = new FingerPositionSmoother();
        return new RainbowInputController(
                service,
                positionSmoother,
                rainbowDrawer
        );
    }

    private RainbowInputController(ExecutorService executor,
                                   FingerPositionSmoother predictor,
                                   RainbowDrawer drawer) {
        executorService = executor;
        fingerPositionPredictor = predictor;
        rainbowDrawer = drawer;
        x = y = px = py = -1;
    }

    public void postEvent(final MotionEvent motionEvent) {
        executorService.execute(inputEventFor(MotionEvent.obtain(motionEvent)));
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

    private void postHandleEvent() {
        px = x;
        py = y;
    }

    private void performTouch(MotionEvent event, RainbowDrawer rainbowDrawer) {
        fingerPositionPredictor.resetTo(event.getX(), event.getY());
        if (hasInteractionListener()) {
            rainbowInteractionListener.onSketchTouched(event, rainbowDrawer);
        }
    }

    private void performRelease(MotionEvent event, RainbowDrawer rainbowDrawer) {
        fingerPositionPredictor.resetTo(event.getX(), event.getY());
        if (hasInteractionListener()) {
            rainbowInteractionListener.onSketchReleased(event, rainbowDrawer);
        }
    }

    private void performMove(MotionEvent event, RainbowDrawer rainbowDrawer) {
        fingerPositionPredictor.moveTo(event.getX(), event.getY());
        if (hasInteractionListener()) {
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPreviousX() {
        return px == -1 ? x : px;
    }

    public float getPreviousY() {
        return py == -1 ? y : py;
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

    public RainbowDrawer getRainbowDrawer() {
        return rainbowDrawer;
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
