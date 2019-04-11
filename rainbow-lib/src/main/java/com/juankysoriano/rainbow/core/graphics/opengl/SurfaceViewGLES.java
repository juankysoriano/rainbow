package com.juankysoriano.rainbow.core.graphics.opengl;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

import com.juankysoriano.rainbow.core.Rainbow;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("ViewConstructor")
public class SurfaceViewGLES extends GLSurfaceView {
    private final Rainbow rainbow;

    public SurfaceViewGLES(Context context, Rainbow rainbow) {
        super(context);
        this.rainbow = rainbow;

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsGLES2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (!supportsGLES2) {
            throw new RuntimeException("OpenGL ES 2.0 is not supported by this device.");
        }

        getHolder().addCallback(this);

        // Tells the default EGLContextFactory and EGLConfigChooser to create an GLES2 context.
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        // The renderer can be set only once.
        setRenderer(getRenderer());
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    @Override
    public SurfaceHolder getHolder() {
        return super.getHolder();
    }

    public void dispose() {
        super.destroyDrawingCache();
        super.onDetachedFromWindow();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    // Inform the view that the window focus has changed.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

///////////////////////////////////////////////////////////

// Android specific classes (Renderer, ConfigChooser)

    public RendererGLES getRenderer() {
        return new RendererGLES(rainbow);
    }

    protected class RendererGLES implements Renderer {

        private Rainbow rainbow;

        RendererGLES(Rainbow rainbow) {
            this.rainbow = rainbow;
        }

        private RainbowGLES getGLES() {
            return (RainbowGLES) ((RainbowGraphicsOpenGL) rainbow.getRainbowDrawer().getGraphics()).rainbowGl;
        }

        @Override
        public void onDrawFrame(GL10 igl) {
            getGLES().getGL(igl);
            rainbow.getRainbowDrawer().beginDraw();
            rainbow.performStep();
            rainbow.getRainbowDrawer().endDraw();
        }

        @Override
        public void onSurfaceChanged(GL10 igl, int iwidth, int iheight) {
            getGLES().getGL(igl);
        }

        @Override
        public void onSurfaceCreated(GL10 igl, EGLConfig config) {
            rainbow.setupSketch();
            getGLES().init(igl);
            rainbow.onSketchSetup();
        }
    }

}
