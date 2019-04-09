package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import android.view.MotionEvent;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

public class RainbowParticleSystem extends Rainbow implements RainbowInputController.RainbowInteractionListener {
    private ParticleWorld particleWorld;

    public RainbowParticleSystem(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onSketchSetup() {
        super.onSketchSetup();
        frameRate(500);
        getRainbowDrawer().smooth();
        getRainbowDrawer().noFill();
        getRainbowDrawer().background(255);
        particleWorld = ParticleWorld.newInstance(getRainbowDrawer().getWidth(), getRainbowDrawer().getHeight());
    }

    @Override
    public void onDrawingStart() {
        super.onDrawingStart();
        getRainbowInputController().setRainbowInteractionListener(this);
    }

    @Override
    public void onDrawingStep() {
        super.onDrawingStep();
        particleWorld.updateAndDisplay(getRainbowDrawer());
    }

    @Override
    public void onDrawingStop() {
        getRainbowInputController().removeSketchInteractionListener();
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
        if (event.getEventTime() % 6 == 0) {
            float x = event.getX();
            float y = event.getY();
            float px = getRainbowInputController().getPreviousX();
            float py = getRainbowInputController().getPreviousY();
            if (x != px || y != py) {
                particleWorld.moveNucleusTo(x, y);
            }
        }

    }

    @Override
    public void onSketchReleased(MotionEvent event, RainbowDrawer rainbowDrawer) {
        //no-op
    }
}
