package com.juankysoriano.rainbow.core;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.juankysoriano.rainbow.R;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.drawing.RainbowTextureView;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics2D;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Rainbow implements InputEventListener {
    private static final int DEFAULT_FRAME_RATE = 60;
    private static final long TIMEOUT = 10;
    private static final long SECOND = 1000;
    private float frameRate = DEFAULT_FRAME_RATE;
    private boolean surfaceReady;
    private int width;
    private int height;
    private int frameCount;
    private boolean stopped = true;
    private boolean started = false;
    private boolean paused = true;
    private boolean resumed = false;
    private boolean isSetup = false;
    private final RainbowInputController rainbowInputController;
    private final RainbowDrawer rainbowDrawer;
    private RainbowTextureView drawingView;
    private SetupSketchTask setupSketchTask;
    private ScheduledExecutorService drawingScheduler;

    protected Rainbow(ViewGroup viewGroup) {
        this(new RainbowDrawer(), new RainbowInputController());
        injectInto(viewGroup);
    }

    protected Rainbow(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        this.rainbowInputController = rainbowInputController;
        this.rainbowDrawer = rainbowDrawer;
        this.setupSketchTask = SetupSketchTask.newInstance(this);
    }

    protected Rainbow(ViewGroup viewGroup, RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        this(rainbowDrawer, rainbowInputController);
        injectInto(viewGroup);
    }

    public void injectInto(ViewGroup viewGroup) {
        this.drawingView = new RainbowTextureView(viewGroup, this);
        this.frameRate = frameRate * viewGroup.getContext().getResources().getInteger(R.integer.dragging_divisions);
        addOnPreDrawListener();
    }

    private void addOnPreDrawListener() {
        drawingView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    private ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            removeOnPreDrawListener();

            rainbowInputController.setInputEventListener(Rainbow.this);
            setupSketch();
            return true;
        }

        private void removeOnPreDrawListener() {
            drawingView.getViewTreeObserver().removeOnPreDrawListener(this);
        }
    };

    public Context getContext() {
        return drawingView.getContext();
    }

    private void setupSketch() {
        initDimensions();
        setupSketchTask.start();
        isSetup = true;
        surfaceReady = true;
    }

    private void initDimensions() {
        width = drawingView.getMeasuredWidth();
        height = drawingView.getMeasuredHeight();
        initGraphics(width, height);
    }

    private void initGraphics(int width, int height) {
        RainbowGraphics graphics = new RainbowGraphics2D();
        graphics.setParent(Rainbow.this);
        graphics.setPrimary(true);
        if (width > 0 && height > 0) {
            graphics.setSize(width, height);
            rainbowDrawer.setGraphics(graphics);
        }
    }

    public void onSketchSetup() {
    }

    public void start() {
        if (!isStarted() || drawingScheduler.isTerminated()) {
            onDrawingStart();
            started = true;
            stopped = false;
            resume();
        }
    }

    public boolean isStarted() {
        return started;
    }

    public void onDrawingStart() {
        //no-op
    }

    public void resume() {
        if (!isResumed()) {
            onDrawingResume();
            resumed = true;
            paused = false;
            if (!hasDrawingScheduler()) {
                drawingScheduler = Executors.newSingleThreadScheduledExecutor();
                drawingScheduler.scheduleAtFixedRate(new DrawingTask(this), SECOND, getDelay(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private long getDelay() {
        return Math.max(1, (long) (SECOND / frameRate));
    }

    private boolean hasDrawingScheduler() {
        return drawingScheduler != null;
    }

    public boolean isResumed() {
        return resumed;
    }

    public void onDrawingResume() {
        //no-op
    }

    private boolean canDraw() {
        return rainbowDrawer != null
                && rainbowDrawer.hasGraphics()
                && surfaceReady
                && isSetup
                && !rainbowInputController.isFingerMoving();
    }

    private void handleDraw() {
        if (canDraw()) {
            performDrawingStep();
        }
    }

    private void performDrawingStep() {
        frameCount++;
        if (hasToPaintIntoBuffer()) {
            onDrawingStep();
        } else {
            rainbowDrawer.beginDraw();
            onDrawingStep();
            rainbowDrawer.endDraw();
        }
    }

    private boolean hasToPaintIntoBuffer() {
        return frameCount % 3 != 0;
    }

    public void onDrawingStep() {
    }

    public void performStep() {
        handleDraw();
    }

    @Override
    public void onInputEvent() {
        performDrawingStep();
    }

    public void pause() {
        if (!isPaused()) {
            paused = true;
            resumed = false;
            shutDownExecutioner();
            onDrawingPause();

        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void onDrawingPause() {
        //no-op
    }

    public void stop() {
        if (!isStopped()) {
            pause();
            onDrawingStop();
            setupSketchTask.cancel();
            stopped = true;
            started = false;
        }
    }

    private void shutDownExecutioner() {
        try {
            drawingScheduler.shutdownNow();
            drawingScheduler.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
            drawingScheduler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void onDrawingStop() {
        //no-op
    }

    public void destroy() {
        stop();
        onSketchDestroy();
        RainbowGraphics graphics = rainbowDrawer.getGraphics();
        if (graphics != null) {
            graphics.dispose();
        }
        drawingView = null;
    }

    public void onSketchDestroy() {
        //no-op
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
     * <p/>
     * If you need to call this manually, probably you will also need need to call
     * rainbowDrawer.beginDraw() and rainbowDrawer.endDraw() to make your drawing effective.
     * <p/>
     * Also, be aware of drawing offline. Drawing outside of the UI thread is allowed here,
     * and long running drawings will block the UI thread.
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
        if (!isPaused()) {
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

    public void reset() {
        setupDrawingSurface(rainbowDrawer.getGraphics());
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

}
