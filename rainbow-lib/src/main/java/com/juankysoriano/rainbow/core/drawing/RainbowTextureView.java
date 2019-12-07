package com.juankysoriano.rainbow.core.drawing;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.event.RainbowInputController;

@SuppressLint("ViewConstructor")
public class RainbowTextureView extends TextureView {
    private static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final ViewGroup.LayoutParams MATCH_PARENT_PARAMS = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    private final RainbowInputController inputController;

    public RainbowTextureView(ViewGroup parent, RainbowInputController inputController) {
        super(parent.getContext());
        this.inputController = inputController;
        parent.addView(this, 0, MATCH_PARENT_PARAMS);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        inputController.postEvent(event);
        getParent().requestDisallowInterceptTouchEvent(true);
        return true;
    }
}
