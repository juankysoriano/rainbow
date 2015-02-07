package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowEvent;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public class RainbowParticleSystem extends Rainbow implements RainbowInputController.RainbowInteractionListener {
    private ParticleWorld particleWorld;

    public RainbowParticleSystem(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup() {
        getRainbowDrawer().smooth();
        getRainbowDrawer().noFill();

        frameRate(200);
        particleWorld = ParticleWorld.newInstance(getRainbowDrawer().getWidth(), getRainbowDrawer().getHeight());
        particleWorld.displayNucleus(getRainbowDrawer());
    }

    @Override
    public void onDrawingStart() {
        getRainbowInputController().setRainbowInteractionListener(this);
    }

    @Override
    public void onDrawingStep() {
        particleWorld.updateAndDisplay(getRainbowDrawer());
    }

    @Override
    public void onDrawingStop() {
        getRainbowInputController().removeSketchInteractionListener();
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
