package com.juankysoriano.rainbow.core.drawing;

import com.juankysoriano.rainbow.utils.RainbowMath;

class LineExplorer {
    private static final float NO_PREVIOUS = -1;
    private final int precision;
    private final RainbowDrawer rainbowDrawer;
    private final RainbowDrawer.PointDetectedListener listener;
    private float previousDetectedX = NO_PREVIOUS;
    private float previousDetectedY = NO_PREVIOUS;

    LineExplorer(RainbowDrawer.Precision precision, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener) {
        this.precision = precision.getValue();
        this.rainbowDrawer = rainbowDrawer;
        this.listener = listener;
    }

    void exploreLine(float px,
                     float py,
                     float x,
                     float y) {
        previousDetectedX = x;
        previousDetectedY = y;
        float dx = x - px;
        float dy = y - py;
        if (isVerticalLine(x, px)) {
            processVerticalLine(x, y, py, rainbowDrawer, listener);
        } else {
            float k = dy / dx;
            float m = y - x * k;
            if (isLineGoingLeft(x, px)) {
                processLineGoingLeft(x, px, rainbowDrawer, listener, k, m);
            } else {
                processLineGoingRight(x, px, rainbowDrawer, listener, k, m);
            }
        }
    }

    private void doProcessOnPoint(float x, float y, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener) {
        listener.onPointDetected(previousDetectedX, previousDetectedY, x, y, rainbowDrawer);
        previousDetectedX = x;
        previousDetectedY = y;
    }

    private boolean isVerticalLine(float x, float px) {
        return x == px;
    }

    private void processVerticalLine(float x, float y, float py, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener) {
        if (isLineGoingDown(y, py)) {
            processVerticalLineGoingDown(x, y, py, rainbowDrawer, listener);
        } else {
            processVerticalLineGoingUp(x, y, py, rainbowDrawer, listener);
        }
    }

    private boolean isLineGoingLeft(float x, float px) {
        return RainbowMath.min(x, px) == px;
    }

    private void processLineGoingLeft(float x, float px, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener, float k, float m) {
        for (float i = RainbowMath.min(x, px); i <= RainbowMath.max(x, px); i += precision / RainbowMath.max(1, RainbowMath.abs(k))) {
            doProcessOnPoint(i, k * i + m, rainbowDrawer, listener);
        }
    }

    private void processLineGoingRight(float x, float px, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener, float k, float m) {
        for (float i = RainbowMath.max(x, px); i >= RainbowMath.min(x, px); i -= precision / RainbowMath.max(1, RainbowMath.abs(k))) {
            doProcessOnPoint(i, k * i + m, rainbowDrawer, listener);
        }
    }

    private boolean isLineGoingDown(float y, float py) {
        return RainbowMath.min(y, py) == py;
    }

    private void processVerticalLineGoingDown(float x, float y, float py, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener) {
        for (float i = RainbowMath.min(y, py); i <= RainbowMath.max(y, py); i += precision) {
            doProcessOnPoint(x, i, rainbowDrawer, listener);
        }
    }

    private void processVerticalLineGoingUp(float x, float y, float py, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener) {
        for (float i = RainbowMath.max(y, py); i >= RainbowMath.min(y, py); i -= precision) {
            doProcessOnPoint(x, i, rainbowDrawer, listener);
        }
    }
}
