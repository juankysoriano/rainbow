package com.juankysoriano.rainbow.core.drawing;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.PointDetectedListener;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.Precision;
import com.juankysoriano.rainbow.core.math.RainbowMath;

class LineExplorer {
    private static final float NO_PREVIOUS = -1;
    private float previousDetectedX = NO_PREVIOUS;
    private float previousDetectedY = NO_PREVIOUS;

    void exploreLine(float px, float py, float x, float y, Precision precision, PointDetectedListener listener) {
        double precisionValue = precision.getValue();
        previousDetectedX = x;
        previousDetectedY = y;
        float dx = x - px;
        float dy = y - py;
        if (x == px) {
            processVerticalLine(x, y, py, precisionValue, listener);
        } else {
            float k = dy / dx;
            float m = y - x * k;
            if (px <= x) {
                processLineGoingLeft(x, px, k, m, precisionValue, listener);
            } else {
                processLineGoingRight(x, px, k, m, precisionValue, listener);
            }
        }
    }

    private void doProcessOnPoint(float x, float y, PointDetectedListener listener) {
        listener.onPointDetected(previousDetectedX, previousDetectedY, x, y);
        previousDetectedX = x;
        previousDetectedY = y;
    }

    private void processVerticalLine(float x, float y, float py, double precision, PointDetectedListener listener) {
        if (py <= y) {
            processVerticalLineGoingDown(x, y, py, precision, listener);
        } else {
            processVerticalLineGoingUp(x, y, py, listener, precision);
        }
    }

    private void processLineGoingLeft(float x, float px, float k, float m, double precision, PointDetectedListener listener) {
        for (float i = RainbowMath.min(x, px); i <= RainbowMath.max(x, px); i += precision / RainbowMath.max(1, RainbowMath.abs(k))) {
            doProcessOnPoint(i, k * i + m, listener);
        }
    }

    private void processLineGoingRight(float x, float px, float k, float m, double precision, PointDetectedListener listener) {
        for (float i = RainbowMath.max(x, px); i >= RainbowMath.min(x, px); i -= precision / RainbowMath.max(1, RainbowMath.abs(k))) {
            doProcessOnPoint(i, k * i + m, listener);
        }
    }

    private void processVerticalLineGoingDown(float x, float y, float py, double precision, PointDetectedListener listener) {
        for (float i = RainbowMath.min(y, py); i <= RainbowMath.max(y, py); i += precision) {
            doProcessOnPoint(x, i, listener);
        }
    }

    private void processVerticalLineGoingUp(float x, float y, float py, PointDetectedListener listener, double precision) {
        for (float i = RainbowMath.max(y, py); i >= RainbowMath.min(y, py); i -= precision) {
            doProcessOnPoint(x, i, listener);
        }
    }
}
