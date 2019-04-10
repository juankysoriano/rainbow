package com.juankysoriano.rainbow.core.drawing;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;

@SuppressLint("ViewConstructor")
public class RainbowTextureView extends TextureView {
    private static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final ViewGroup.LayoutParams MATCH_PARENT_PARAMS = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    private final Rainbow rainbow;

    public RainbowTextureView(ViewGroup parent, Rainbow rainbow) {
        super(parent.getContext());
        this.rainbow = rainbow;
        parent.addView(this, 0, MATCH_PARENT_PARAMS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        rainbow.getRainbowInputController().postEvent(event);
        getParent().requestDisallowInterceptTouchEvent(true);
        return true;
    }

    public void restoreView() {
        rainbow.getRainbowDrawer().beginDraw();
        rainbow.onDrawingStep();
        rainbow.getRainbowDrawer().endDraw();
    }
}
