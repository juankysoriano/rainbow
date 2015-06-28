package com.juankysoriano.rainbow.core.drawing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;

public class RainbowTextureView extends TextureView {
    private static final String ALPHA = "alpha";
    private static final float OPAQUE = 1f;
    private static final float TRANSPARENT = 0f;
    private static final long DURATION = 1000;
    private static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final ViewGroup.LayoutParams MATCH_PARENT_PARAMS = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    private final Rainbow rainbow;

    public RainbowTextureView(ViewGroup parent, Rainbow rainbow) {
        super(parent.getContext());
        this.rainbow = rainbow;
        parent.addView(this, 0, MATCH_PARENT_PARAMS);
        setBackground(parent.getBackground());
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        rainbow.getRainbowInputController().postEvent(event);
        getParent().requestDisallowInterceptTouchEvent(true);
        return true;
    }

    public void restoreAnimated() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, ALPHA, TRANSPARENT, OPAQUE);
        objectAnimator.setDuration(DURATION);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setAlpha(TRANSPARENT);
                rainbow.getRainbowDrawer().beginDraw();
                rainbow.onDrawingStep();
                rainbow.getRainbowDrawer().endDraw();
            }
        });
        objectAnimator.start();
    }
}
