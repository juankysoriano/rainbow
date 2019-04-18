package com.juankysoriano.rainbow.core;

import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.drawing.RainbowTextureView;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics2D;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class Rainbow {
    private int frameRate = 60;
    private int frameCount;
    private boolean surfaceReady;
    private int width;
    private int height;
    private boolean stopped = true;
    private boolean started = false;
    private boolean paused = true;
    private boolean resumed = false;
    private boolean isSetup = false;
    private final RainbowInputController rainbowInputController;
    private final RainbowDrawer rainbowDrawer;
    private RainbowTextureView drawingView;
    private SetupSketchTask setupSketchTask;
    private RainbowTaskScheduler rainbowTaskScheduler;
    private Scheduler setupScheduler;

    protected Rainbow(ViewGroup viewGroup) {
        this.rainbowDrawer = new RainbowDrawer();
        this.rainbowInputController = RainbowInputController.newInstance();
        this.setupSketchTask = new SetupSketchTask(this);
        this.setupScheduler = Schedulers.newThread();
        injectInto(viewGroup);
    }

    protected Rainbow(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        this.rainbowInputController = rainbowInputController;
        this.rainbowDrawer = rainbowDrawer;
        this.setupSketchTask = new SetupSketchTask(this);
        this.setupScheduler = Schedulers.newThread();
    }

    protected Rainbow(ViewGroup viewGroup, RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        this(rainbowDrawer, rainbowInputController);
        injectInto(viewGroup);
    }

    private void injectInto(ViewGroup viewGroup) {
        drawingView = new RainbowTextureView(viewGroup, this);
        addSurfaceTextureListener();
    }

    private void addSurfaceTextureListener() {
        drawingView.setSurfaceTextureListener(onSurfaceTextureListener);
    }

    private TextureView.SurfaceTextureListener onSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (isSetup) {
                drawingView.restoreView();
            } else {
                setupSketch();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            //no-op
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //no-op
        }
    };

    private void setupSketch() {
        initDimensions();
        setupScheduler.scheduleDirect(setupSketchTask);
        isSetup = true;
        surfaceReady = true;
    }

    private void initDimensions() {
        width = drawingView.getMeasuredWidth();
        height = drawingView.getMeasuredHeight();

        initPeriodicGraphics(width, height);
        initInputControllerGraphics(width, height);
    }

    private void initInputControllerGraphics(int width, int height) {
        RainbowGraphics graphics = new RainbowGraphics2D();
        graphics.setParent(Rainbow.this);
        graphics.setPrimary(true);
        if (width > 0 && height > 0) {
            graphics.setSize(width, height);
            rainbowInputController.getRainbowDrawer().setGraphics(graphics);
        }
    }

    private void initPeriodicGraphics(int width, int height) {
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
        if (!isStarted() || rainbowTaskScheduler.isTerminated()) {
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
            if (!hasScheduler()) {
                rainbowTaskScheduler = RainbowTaskScheduler.newInstance(this);
                rainbowTaskScheduler.scheduleAt(frameRate);
            }
        }
    }

    private boolean hasScheduler() {
        return rainbowTaskScheduler != null;
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
                && isSetup;
    }

    private void handleDraw() {
        if (canDraw()) {
            performDrawingStep();
        }
    }

    private void performDrawingStep() {
        frameCount++;
        onDrawingStep();
    }

    public void onDrawingStep() {
    }

    public void performStep() {
        handleDraw();
    }

    public void pause() {
        if (isResumed()) {
            paused = true;
            resumed = false;
            shutdownTasks();
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
            stopped = true;
            started = false;
        }
    }

    private void shutdownTasks() {
        if (rainbowTaskScheduler != null) {
            rainbowTaskScheduler.shutdown();
            rainbowTaskScheduler = null;
        }

        if (setupScheduler != null) {
            setupScheduler.shutdown();
            setupScheduler = null;
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
        isSetup = false;
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
    public View getParentView() {
        return (View) drawingView.getParent();
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

    public void frameRate(final int newRateTarget) {
        frameRate = newRateTarget;
        restart();
    }

    private void restart() {
        if (isResumed()) {
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
