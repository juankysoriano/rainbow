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
    private boolean surfaceReady;
    private boolean paused;
    private boolean isSetup;
    private int width;
    private int height;
    private int frameCount;
    private float frameRate;
    private ScheduledExecutorService drawingScheduler;
    private RainbowInputController rainbowInputController;
    private DrawingTask drawingTask;
    private RainbowDrawer rainbowDrawer;
    private RainbowTextureView drawingView;
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

    protected Rainbow(ViewGroup viewGroup) {
        frameRate = RainbowConstants.DEFAULT_FRAME_RATE;
        paused = true;
        isSetup = false;
        rainbowInputController = new RainbowInputController();
        rainbowDrawer = new RainbowDrawer();
        drawingTask = new DrawingTask();
        injectSketchInto(viewGroup);
        addOnPreDrawListener();
    }

    private void injectSketchInto(ViewGroup viewGroup) {
        drawingView = new RainbowTextureView(viewGroup, this);
    }

    private void addOnPreDrawListener() {
        drawingView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    public Context getContext() {
        return drawingView.getContext();
    }

    private void setupSketch() {
        setupSketchTask.execute();
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
            rainbowDrawer.beginDraw();
            Rainbow.this.onSketchSetup(rainbowDrawer);
            rainbowDrawer.endDraw();
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            isSetup = true;
            surfaceReady = true;
        }

        private void initGraphics(int width, int height) {
            RainbowGraphics graphics = new RainbowGraphics2D();
            graphics.setParent(Rainbow.this);
            graphics.setPrimary(true);
            if(width > 0 && height > 0) {
                graphics.setSize(width, height);
                rainbowDrawer.setGraphics(graphics);
            }
        }
    };

    @Override
    public void onSketchSetup(RainbowDrawer rainbowDrawer) {
    }

    public void start() {
        if (!isRunning() || drawingScheduler.isTerminated()) {
            onDrawingStart(rainbowInputController);
            resume();
        }
    }

    public boolean isRunning() {
        return !paused || drawingScheduler != null;
    }

    @Override
    public void onDrawingStart(RainbowInputController rainbowInputController) {
    }

    public void resume() {
        onDrawingResume();
        paused = false;
        if(drawingScheduler == null || drawingScheduler.isTerminated()) {
            drawingScheduler = Executors.newSingleThreadScheduledExecutor();
            drawingScheduler.scheduleAtFixedRate(drawingTask, 0, drawingTask.getDelay(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onDrawingResume() {
    }


    private void handleDraw() {

        if (canDraw()) {
            fireDrawStep();
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
        surfaceReady = true;
    }

    private boolean canDraw() {
        return rainbowDrawer != null
                && rainbowDrawer.hasGraphics()
                && surfaceReady
                && isSetup;
    }

    private void fireDrawStep() {
        synchronized (this) {
            frameCount++;
            rainbowDrawer.beginDraw();
            onDrawingStep(rainbowDrawer, rainbowInputController);
            rainbowInputController.dequeueEvents(rainbowDrawer);
            rainbowDrawer.endDraw();
        }
    }

    @Override
    public void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    public void pause() {
        paused = true;
        onDrawingPause();
    }

    @Override
    public void onDrawingPause() {
    }

    public void stop() {
        pause();
        shutDownExecutioner();
        onDrawingStop(rainbowInputController);
    }

    private void shutDownExecutioner() {
        try {
            drawingScheduler.shutdownNow();
            drawingScheduler.awaitTermination(DrawingTask.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawingStop(RainbowInputController rainbowInputController) {
    }

    public void destroy() {
        stop();
        onSketchDestroy();
        RainbowGraphics graphics = rainbowDrawer.getGraphics();
        if (graphics != null) {
            graphics.dispose();
        }
        rainbowDrawer.setGraphics(null);
        rainbowDrawer = null;
        rainbowInputController = null;
        drawingTask = null;
    }

    @Override
    public void onSketchDestroy() {
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
     * Used to retrieve a RainbowDrawer object.
     *
     * If you need to call this manually, probably you will also need need to call
     * rainbowDrawer.beginDraw() and rainbowDrawer.endDraw() to make your drawing effective.
     *
     * Also, be aware of drawing offline. Drawing outside of the UI thread is allowed here,
     * and long running drawings will block the UI thread.
     *
     *
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
        setupDrawingSurface(rainbowDrawer.getGraphics());
    }

    class DrawingTask extends TimerTask {
        private static final long TIMEOUT = 10;
        private static final float SECOND = 1000F;

        @Override
        public void run() {
            if (!paused && !drawingScheduler.isShutdown()) {
                handleDraw();
            }
        }

        public long getDelay() {
            return Math.max(1, (long) (SECOND / frameRate));
        }
    }
}
