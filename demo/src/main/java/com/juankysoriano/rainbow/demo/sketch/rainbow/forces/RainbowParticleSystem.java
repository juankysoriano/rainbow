package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowEvent;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.listeners.RainbowInteractionListener;

public class RainbowParticleSystem extends Rainbow implements RainbowInteractionListener {
    public static final int DEFAULT_FRAMERATE = 120;
    private ParticleWorld particleWorld;

    public RainbowParticleSystem(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup(RainbowDrawer rainbowDrawer) {
        rainbowDrawer.noSmooth();
        rainbowDrawer.noFill();
        frameRate(DEFAULT_FRAMERATE);

        particleWorld = ParticleWorld.newInstance(rainbowDrawer.getWidth(), rainbowDrawer.getHeight());
        particleWorld.displayNucleus(rainbowDrawer);
    }

    @Override
    public void onDrawingStart(RainbowInputController rainbowInputController) {
        rainbowInputController.setRainbowInteractionListener(this);
    }

    @Override
    public void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
        particleWorld.updateAndDisplay(rainbowDrawer);
    }

    @Override
    public void onDrawingStop(RainbowInputController rainbowInputController) {
        rainbowInputController.removeSketchInteractionListener();
    }

    @Override
    public void onSketchDestroy() {
        particleWorld = null;
    }

    @Override
    public void onFingerDragged(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        onSketchReleased(event, rainbowDrawer);
        onSketchTouched(event, rainbowDrawer);
    }

    @Override
    public void onSketchReleased(RainbowEvent event, RainbowDrawer rainbowDrawer) {
    }

    @Override
    public void onSketchTouched(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        float x = event.getX();
        float y = event.getY();
        float px = event.getPreviousX();
        float py = event.getPreviousY();
        if (x != px || y != py) {
            particleWorld.moveNucleusTo(x, y);
        }
    }

    @Override
    public void onMotionEvent(RainbowEvent event, RainbowDrawer rainbowDrawer) {
        // no op;
    }
}
