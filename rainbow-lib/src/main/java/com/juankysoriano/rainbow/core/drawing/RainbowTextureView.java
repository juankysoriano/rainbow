package com.juankysoriano.rainbow.core.drawing;

import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.juankysoriano.rainbow.core.Rainbow;

public class RainbowTextureView extends TextureView {
    private static final String ALPHA = "alpha";
    private static final float OPAQUE = 1f;
    private static final float TRANSPARENT = 0f;
    private static final long SECOND = 1000;
    private final Rainbow rainbow;

    public RainbowTextureView(ViewGroup parent, Rainbow rainbow) {
        super(parent.getContext());
        this.rainbow = rainbow;
        parent.addView(this, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackground(parent.getBackground());
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        rainbow.getRainbowInputController().postEvent(event);
        return true;
    }

    public void animateShow() {
        rainbow.getRainbowDrawer().invalidate();
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(this, ALPHA, TRANSPARENT, OPAQUE).setDuration(SECOND);
        showAnimator.setInterpolator(new AccelerateInterpolator());
        showAnimator.start();
    }

    public void hide() {
        setAlpha(0f);
    }
}
