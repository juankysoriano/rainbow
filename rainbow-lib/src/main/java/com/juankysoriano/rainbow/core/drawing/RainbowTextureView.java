package com.juankysoriano.rainbow.core.drawing;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.R;
import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public class RainbowTextureView extends TextureView implements SurfaceTextureListener {
    private final Rainbow rainbow;

    public RainbowTextureView(ViewGroup parent, Rainbow rainbow) {
        super(parent.getContext());
        this.rainbow = rainbow;

        setSurfaceTextureListener(this);
        parent.addView(this, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackground(parent.getBackground());
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        RainbowInputController rainbowInputController = rainbow.getRainbowInputController();
        if (rainbowInputController != null) {
            splitIntoMultipleEventsAndPost(event, rainbowInputController);
        }
        return true;
    }

    private void splitIntoMultipleEventsAndPost(@NonNull MotionEvent event, RainbowInputController rainbowInputController) {
        float px = rainbowInputController.getX();
        float py = rainbowInputController.getY();
        float diffX = event.getX() - px;
        float diffY = event.getY() - py;
        int divisions = getContext().getResources().getInteger(R.integer.dragging_divisions);
        for (int i = 0; i < divisions; i++) {
            float newEventX = px + diffX * (i + 1) / 2;
            float newEventY = py + diffY * (i + 1) / 2;
            MotionEvent subEvent = obtainEventWithNewPosition(event, newEventX, newEventY);
            rainbowInputController.postEvent(subEvent, rainbow.getRainbowDrawer());
        }
    }

    private MotionEvent obtainEventWithNewPosition(@NonNull MotionEvent event, float newEventX, float newEventY) {
        MotionEvent motionEvent = MotionEvent.obtain(event);
        motionEvent.setLocation(newEventX, newEventY);
        return motionEvent;
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        //no-op
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        //no-op
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //no-op
    }
}
