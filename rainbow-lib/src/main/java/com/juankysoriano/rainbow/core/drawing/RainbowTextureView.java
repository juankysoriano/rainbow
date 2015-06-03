package com.juankysoriano.rainbow.core.drawing;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;

public class RainbowTextureView extends TextureView {
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
}
