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
    private RainbowTaskScheduler rainbowTaskScheduler;

    public Rainbow(ViewGroup viewGroup) {
        this(viewGroup, new RainbowDrawer(), RainbowInputController.newInstance());
    }

    private Rainbow(ViewGroup viewGroup, RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        this.rainbowInputController = rainbowInputController;
        this.rainbowDrawer = rainbowDrawer;
        this.rainbowTaskScheduler = RainbowTaskScheduler.newInstance(this);
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
                rainbowTaskScheduler.scheduleSingleDraw();
            } else {
                rainbowTaskScheduler.scheduleSetup();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    void setupSketch() {
        isSetup = true;
        surfaceReady = true;
        width = drawingView.getMeasuredWidth();
        height = drawingView.getMeasuredHeight();

        initPeriodicGraphics();
        initInputControllerGraphics();
    }

    private void initPeriodicGraphics() {
        RainbowGraphics graphics = new RainbowGraphics2D();
        graphics.setParent(Rainbow.this);
        graphics.setPrimary(true);
        if (width > 0 && height > 0) {
            graphics.setSize(width, height);
            rainbowDrawer.setGraphics(graphics);
        }
    }

    private void initInputControllerGraphics() {
        RainbowGraphics graphics = new RainbowGraphics2D();
        graphics.setParent(Rainbow.this);
        graphics.setPrimary(true);
        if (width > 0 && height > 0) {
            graphics.setSize(width, height);
            rainbowInputController.getRainbowDrawer().setGraphics(graphics);
        }
    }

    public boolean isSetup() {
        return isSetup;
    }

    public void onSketchSetup() {
    }

    public void start() {
        if (!isSetup) {
            return;
        }
        if (isStopped() || rainbowTaskScheduler.isTerminated()) {
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
    }

    public void resume() {
        if (!isSetup) {
            return;
        }

        if (isPaused()) {
            onDrawingResume();
            resumed = true;
            paused = false;
            rainbowTaskScheduler.scheduleDrawing(frameRate);
        }
    }

    public boolean isResumed() {
        return resumed;
    }

    public void onDrawingResume() {
    }

    void performStep() {
        if (canDraw()) {
            frameCount++;
            onDrawingStep();
        }
    }

    private boolean canDraw() {
        return rainbowDrawer != null && rainbowDrawer.hasGraphics() && surfaceReady && isSetup;
    }

    public void onDrawingStep() {
    }

    public void pause() {
        if (!isSetup) {
            return;
        }

        if (isResumed()) {
            paused = true;
            resumed = false;
            shutdownTasks();
            onDrawingPause();
        }
    }

    private void shutdownTasks() {
        rainbowTaskScheduler.shutdown();
    }

    public boolean isPaused() {
        return paused;
    }

    public void onDrawingPause() {
    }

    public void stop() {
        if (!isSetup) {
            return;
        }

        if (isStarted()) {
            pause();
            onDrawingStop();
            stopped = true;
            started = false;
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void onDrawingStop() {
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
            stop();
            start();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
