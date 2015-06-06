package com.juankysoriano.rainbow.core;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.drawing.RainbowTextureView;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics;
import com.juankysoriano.rainbow.core.graphics.RainbowGraphics2D;

public class Rainbow {
    public static final int DEFAULT_FRAME_RATE = 60;
    private int frameRate = DEFAULT_FRAME_RATE;
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
    private RainbowTaskScheduler rainbowTaskScheduler;

    protected Rainbow(ViewGroup viewGroup) {
        this.rainbowDrawer = new RainbowDrawer();
        this.rainbowInputController = RainbowInputController.newInstance();
        this.setupSketchTask = SetupSketchTask.newInstance(this);
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

    public Context getContext() {
        return drawingView.getContext();
    }

    public void injectInto(ViewGroup viewGroup) {
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
                restoreSketch();
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

    private void restoreSketch() {
        drawingView.hide();
        drawingView.animateShow();
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

        RainbowGraphics2D.releasePrimaryBitmap();
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
            rainbowTaskScheduler.shutdown();
            rainbowTaskScheduler = null;
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
