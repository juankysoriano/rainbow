package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import android.view.MotionEvent;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

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
        particleWorld = ParticleWorld.newInstance(getRainbowDrawer().getWidth(), getRainbowDrawer().getHeight());
    }

    @Override
    public void onDrawingStart() {
        super.onDrawingStart();
        getRainbowInputController().attach(this);
    }

    @Override
    public void onDrawingStep() {
        particleWorld.update();
        particleWorld.display(getRainbowDrawer());
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
    public void onFingerDragged(MotionEvent event) {
        onSketchTouched(event);
    }

    @Override
    public void onMotionEvent(MotionEvent event) {
        //no-op
    }

    @Override
    public void onSketchTouched(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float px = getRainbowInputController().getPreviousX();
        float py = getRainbowInputController().getPreviousY();
        if (x != px || y != py) {
            particleWorld.moveNucleusTo(x, y);
        }
    }

    @Override
    public void onSketchReleased(MotionEvent event) {
        //no-op
    }
}
