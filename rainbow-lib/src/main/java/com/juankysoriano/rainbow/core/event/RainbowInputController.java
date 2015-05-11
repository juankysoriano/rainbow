package com.juankysoriano.rainbow.core.event;

import android.os.AsyncTask;
import android.view.MotionEvent;

import com.juankysoriano.rainbow.core.InputEventListener;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RainbowInputController {
    private final FingerPositionSmoother fingerPositionPredictor;
    private InputEventListener inputEventListener;
    private RainbowInteractionListener rainbowInteractionListener;
    private float x, y;
    private float px, py;
    private boolean screenTouched;
    private boolean fingerMoving;
    private List<AsyncTask> runningInputEventTasks;

    public RainbowInputController() {
        runningInputEventTasks = new ArrayList<>();
        fingerPositionPredictor = new FingerPositionSmoother();
        x = y = px = py = -1;
    }

    public void postEvent(final MotionEvent motionEvent, final RainbowDrawer rainbowDrawer) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                new RainbowInputEventTask(rainbowDrawer).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, motionEvent);
                break;
            case MotionEvent.ACTION_UP:
                cancelAllRunningTasks();
                new RainbowInputEventTask(rainbowDrawer).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                runningInputEventTasks.add(new RainbowInputEventTask(rainbowDrawer).execute(motionEvent));
                break;
        }
    }

    private void cancelAllRunningTasks() {
        for(AsyncTask asyncTask: runningInputEventTasks) {
            asyncTask.cancel(true);
        }
        runningInputEventTasks.clear();
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
        return this.inputEventListener != null;
    }

    public void setInputEventListener(InputEventListener inputEventListener) {
        this.inputEventListener = inputEventListener;
    }

    public void removePaintStepListener() {
        this.inputEventListener = null;
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

    private class RainbowInputEventTask extends AsyncTask<MotionEvent, Void, MotionEvent> {
        private final WeakReference<RainbowDrawer> rainbowDrawer;

        public RainbowInputEventTask(RainbowDrawer rainbowDrawer) {
            this.rainbowDrawer = new WeakReference<>(rainbowDrawer);
        }

        @Override
        protected MotionEvent doInBackground(MotionEvent... motionEvents) {
            MotionEvent motionEvent = motionEvents[0];
            preHandleEvent(motionEvent);
            if (this.rainbowDrawer.get() != null) {
                handleSketchEvent(motionEvents[0], rainbowDrawer.get());
            }
            inputEventListener.onInputEvent();
            return motionEvent;
        }

        @Override
        protected void onPostExecute(MotionEvent motionEvent) {
            postHandleEvent(motionEvent);
        }


    }
}
