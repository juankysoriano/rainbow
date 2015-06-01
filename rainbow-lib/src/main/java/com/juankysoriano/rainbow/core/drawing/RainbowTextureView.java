package com.juankysoriano.rainbow.core.drawing;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.R;
import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.event.RainbowInputController;

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
        RainbowInputController rainbowInputController = rainbow.getRainbowInputController();
        if (rainbowInputController != null) {
            splitIntoMultipleEventsAndPost(event, rainbowInputController);
        }
        return true;
    }

    private void splitIntoMultipleEventsAndPost(@NonNull MotionEvent event, RainbowInputController rainbowInputController) {
        float px = rainbowInputController.getX();
        float py = rainbowInputController.getY();
        float diffX = event.getX() - px;
        float diffY = event.getY() - py;
        int divisions = getDivisionsFor(event);

        for (int i = 0; i <= divisions; i++) {
            float newEventX = px + diffX * i / divisions;
            float newEventY = py + diffY * i / divisions;
            MotionEvent subEvent = obtainEventWithNewPosition(event, newEventX, newEventY);
            rainbowInputController.postEvent(subEvent);
        }
    }

    private int getDivisionsFor(MotionEvent event) {
        return event.getAction() != MotionEvent.ACTION_MOVE ? 1 : getContext().getResources().getInteger(R.integer.dragging_divisions);
    }

    private MotionEvent obtainEventWithNewPosition(@NonNull MotionEvent event, float newEventX, float newEventY) {
        MotionEvent motionEvent = MotionEvent.obtain(event);
        motionEvent.setLocation(newEventX, newEventY);
        return motionEvent;
    }
}
