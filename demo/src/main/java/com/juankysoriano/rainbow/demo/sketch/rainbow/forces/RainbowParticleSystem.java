package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import android.view.MotionEvent;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.touch.RainbowInputController;

public class RainbowParticleSystem extends Rainbow implements RainbowInputController.RainbowInteractionListener {
    private ParticleWorld particleWorld;

    public RainbowParticleSystem(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup() {
        super.onSketchSetup();
        getRainbowDrawer().noSmooth();
        getRainbowDrawer().noFill();
        getRainbowDrawer().background(0);
        getRainbowDrawer().invalidate();
        stepRate(60);
        frameRate(60);
        particleWorld = ParticleWorld.newInstance(getRainbowDrawer().getWidth(), getRainbowDrawer().getHeight());
    }

    @Override
    public void onDrawingStart() {
        super.onDrawingStart();
        getRainbowInputController().attach(this);
    }

    @Override
    public void onDrawingStep() {
        super.onDrawingStep();
        getRainbowDrawer().background(0);
        if (particleWorld != null) {
            particleWorld.updateAndDisplay(this);
        }
    }

    @Override
    public void onDrawingStop() {
        getRainbowInputController().detach();
        super.onDrawingStop();
    }

    @Override
    public void onSketchDestroy() {
        particleWorld = null;
        super.onSketchDestroy();
    }

    @Override
    public void onFingerDragged(MotionEvent event, RainbowDrawer rainbowDrawer) {
        onSketchTouched(event, rainbowDrawer);
    }

    @Override
    public void onMotionEvent(MotionEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }

    @Override
    public void onSketchTouched(MotionEvent event, RainbowDrawer rainbowDrawer) {
        float x = event.getX();
        float y = event.getY();
        float px = getRainbowInputController().getPreviousX();
        float py = getRainbowInputController().getPreviousY();
        if (x != px || y != py) {
            particleWorld.moveNucleusTo(x, y);
        }
    }

    @Override
    public void onSketchReleased(MotionEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }
}
