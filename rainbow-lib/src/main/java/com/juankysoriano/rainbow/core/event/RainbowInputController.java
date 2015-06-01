package com.juankysoriano.rainbow.core.event;

import android.os.AsyncTask;
import android.view.MotionEvent;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;

import java.util.ArrayList;
import java.util.List;

public class RainbowInputController {
    private final FingerPositionSmoother fingerPositionPredictor;
    private final RainbowDrawer rainbowDrawer;
    private RainbowInteractionListener rainbowInteractionListener;
    private float x, y;
    private float px, py;
    private boolean screenTouched;
    private boolean fingerMoving;
    private List<AsyncTask> runningTasks;

    public static RainbowInputController newInstance() {
        return new RainbowInputController(new ArrayList<AsyncTask>(), new FingerPositionSmoother(), new RainbowDrawer());
    }

    protected RainbowInputController(List<AsyncTask> tasks, FingerPositionSmoother predictor, RainbowDrawer drawer) {
        runningTasks = tasks;
        fingerPositionPredictor = predictor;
        rainbowDrawer = drawer;
        x = y = px = py = -1;
    }

    public void postEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            cancelAllRunningTasks();
            new RainbowInputEventTask(event).execute();
        } else {
            runningTasks.add(new RainbowInputEventTask(event).execute());
        }
    }

    private void cancelAllRunningTasks() {
        if (!runningTasks.isEmpty()) {
            for (AsyncTask task : runningTasks) {
                task.cancel(true);
            }
            runningTasks.clear();
        }
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

    private void handleEvent(final MotionEvent event) {
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

    private void performTouch(MotionEvent event) {
        fingerPositionPredictor.resetTo(x, y);
        if (hasInteractionListener()) {
            rainbowInteractionListener.onSketchTouched(event, rainbowDrawer);
        }
    }

    private void performRelease(MotionEvent event) {
        fingerPositionPredictor.resetTo(x, y);
        if (hasInteractionListener()) {
            rainbowInteractionListener.onSketchReleased(event, rainbowDrawer);
        }
    }

    private void performMove(MotionEvent event) {
        fingerPositionPredictor.moveTo(x, y);
        if (hasInteractionListener()) {
            rainbowInteractionListener.onFingerDragged(event, rainbowDrawer);
        }
    }

    private void performMotion(MotionEvent event) {
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

    private class RainbowInputEventTask extends AsyncTask<Void, Void, Void> {

        private MotionEvent motionEvent;

        public RainbowInputEventTask(MotionEvent motionEvent) {
            this.motionEvent = motionEvent;
        }

        @Override
        protected void onPreExecute() {
            preHandleEvent(motionEvent);
        }

        @Override
        protected Void doInBackground(Void... params) {
            handleEvent(motionEvent);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            postHandleEvent(motionEvent);
            runningTasks.remove(this);
        }
    }
}
