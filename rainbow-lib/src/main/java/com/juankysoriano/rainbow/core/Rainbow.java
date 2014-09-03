package com.juankysoriano.rainbow.core;

import android.content.Context;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.drawing.RainbowTextureView;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics2D;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Rainbow implements RainbowConstants, RainbowLifeCycleCallback {
    private boolean surfaceChanged;
    private boolean surfaceReady;
    private boolean paused;
    private int width;
    private int height;
    private int frameCount;
    private float frameRate;
    private ScheduledExecutorService drawingScheduler;
    private RainbowInputController rainbowInputController;
    private DrawingTask drawingTask;
    private RainbowDrawer rainbowDrawer;
    private RainbowTextureView drawingView;

    protected Rainbow(ViewGroup viewGroup) {
        frameRate = RainbowConstants.DEFAULT_FRAME_RATE;
        rainbowInputController = new RainbowInputController();
        paused = true;
        rainbowDrawer = new RainbowDrawer();
        drawingTask = new DrawingTask();
        injectSketchInto(viewGroup);
        addOnPreDrawListener();
    }

    public Context getContext() {
        return drawingView.getContext();
    }

    private void injectSketchInto(ViewGroup viewGroup) {
        drawingView = new RainbowTextureView(viewGroup, this);
    }

    private void addOnPreDrawListener() {
        drawingView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    private ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            removeOnPreDrawListener();
            setupSketch();
            return true;
        }

        private void removeOnPreDrawListener() {
            drawingView.getViewTreeObserver().removeOnPreDrawListener(this);
        }
    };

    private void setupSketch() {
        setupSketchTask.execute();
    }

    private void initGraphics(int width, int height) {
        RainbowGraphics graphics = new RainbowGraphics2D();
        graphics.setParent(this);
        graphics.setPrimary(true);
        graphics.setSize(width, height);
        rainbowDrawer.setGraphics(graphics);
    }

    @Override
    public void onSketchSetup(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    private AsyncTask<Void, Void, Void> setupSketchTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected void onPreExecute() {
            width = drawingView.getMeasuredWidth();
            height = drawingView.getMeasuredHeight();
            initGraphics(width, height);
        }

        @Override
        protected Void doInBackground(Void... params) {
            rainbowDrawer.getGraphics().beginDraw();
            Rainbow.this.onSketchSetup(rainbowDrawer, rainbowInputController);
            rainbowDrawer.getGraphics().endDraw();
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            surfaceReady = true;
        }
    };

    public boolean isRunning() {
        return !paused || drawingScheduler != null;
    }

    @Override
    public void onDrawingStart(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    public void start() {
        if (!isRunning() || drawingScheduler.isTerminated()) {
            onDrawingStart(rainbowDrawer, rainbowInputController);
            resume();
        } else {
            // throw error.
        }
    }

    @Override
    public void onDrawingResume(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    public void resume() {
        onDrawingResume(rainbowDrawer, rainbowInputController);
        paused = false;
        drawingScheduler = Executors.newSingleThreadScheduledExecutor();
        drawingScheduler.scheduleAtFixedRate(drawingTask, 0, drawingTask.getDelay(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDrawingPause(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    public void pause() {
        paused = true;
        onDrawingPause(rainbowDrawer, rainbowInputController);
    }

    @Override
    public void onDrawingStop(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    public void stop() {
        pause();
        shutDownExecutioner();
        onDrawingStop(rainbowDrawer, rainbowInputController);
    }

    @Override
    public void onSketchDestroy(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    public void destroy() {
        stop();
        onSketchDestroy(rainbowDrawer, rainbowInputController);
        RainbowGraphics graphics = rainbowDrawer.getGraphics();
        if (graphics != null) {
            graphics.dispose();
        }
        rainbowDrawer = null;
        rainbowInputController = null;
        drawingTask = null;
    }

    private void shutDownExecutioner() {
        try {
            drawingScheduler.shutdownNow();
            drawingScheduler.awaitTermination(DrawingTask.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleDraw(RainbowGraphics graphics) {

        if (surfaceChanged) {
            setupDrawingSurface(graphics);
        }

        if (canDraw()) {
            fireDrawStep(graphics);
        }
    }

    @Override
    public void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    private void fireDrawStep(RainbowGraphics graphics) {
        synchronized (this) {
            frameCount++;
            graphics.beginDraw();
            onDrawingStep(rainbowDrawer, rainbowInputController);
            rainbowInputController.dequeueEvents(rainbowDrawer);
            graphics.endDraw();
        }
    }

    private void setupDrawingSurface(RainbowGraphics graphics) {
        final int newWidth = drawingView.getWidth();
        final int newHeight = drawingView.getHeight();
        if ((newWidth != width) || (newHeight != height)) {
            width = newWidth;
            height = newHeight;
            graphics.setSize(width, height);
        }
        surfaceChanged = false;
        surfaceReady = true;
    }

    /**
     * @return View where the drawing is performed
     */
    public RainbowTextureView getDrawingView() {
        return drawingView;
    }

    /**
     * @return the View where this rainbow sketch has been injected to
     */
    public ViewParent getParentView() {
        return drawingView.getParent();
    }

    /**
     * @return RainbowDrawer, used to draw into the rainbow sketch
     */
    public RainbowDrawer getRainbowDrawer() {
        return rainbowDrawer;
    }

    /**
     * @return RainbowInputController, used to control user interaction with the rainbow sketch
     */
    public RainbowInputController getRainbowInputController() {
        return rainbowInputController;
    }

    public float frameRate() {
        return frameRate;
    }

    public int frameCount() {
        return frameCount;
    }

    public void frameRate(final float newRateTarget) {
        frameRate = newRateTarget;
        if (isRunning()) {
            pause();
            resume();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void invalidate() {
        surfaceChanged = true;
    }

    private boolean canDraw() {
        return rainbowDrawer != null
                && rainbowDrawer.hasGraphics()
                && surfaceReady;
    }

    class DrawingTask extends TimerTask {
        private static final long TIMEOUT = 10;
        private static final float SECOND = 1000F;

        @Override
        public void run() {
            if (!paused && !drawingScheduler.isShutdown()) {
                handleDraw(rainbowDrawer.getGraphics());
            }
        }

        public long getDelay() {
            return Math.max(1, (long) (SECOND / frameRate));
        }
    }
}
