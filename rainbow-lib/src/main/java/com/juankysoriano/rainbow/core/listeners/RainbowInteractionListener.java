package com.juankysoriano.rainbow.core.listeners;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowEvent;

public interface RainbowInteractionListener {
    void onSketchTouched(final RainbowEvent event, final RainbowDrawer rainbowDrawer);

    void onSketchReleased(final RainbowEvent event, final RainbowDrawer rainbowDrawer);

    void onFingerDragged(final RainbowEvent event, final RainbowDrawer rainbowDrawer);

    void onMotionEvent(final RainbowEvent event, final RainbowDrawer rainbowDrawer);
}
