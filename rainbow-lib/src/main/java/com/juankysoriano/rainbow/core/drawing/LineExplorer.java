package com.juankysoriano.rainbow.core.drawing;

public class LineExplorer {
    private int detections;

    public void exploreLine(final float x1,
                            final float y1,
                            final float x2,
                            final float y2,
                            final RainbowDrawer rainbowDrawer,
                            final RainbowDrawer.PointDetectedListener listener) {

        float cx, cy, ix, iy, dx, dy;
        float ddx = x2 - x1;
        float ddy = y2 - y1;

        if (x1 == x2 && y1 == y2) {
            doProcess(x1, y1, rainbowDrawer, listener);

        }
        if (ddx == 0) { //vertical line special case
            if (ddy > 0) {
                cy = y1;
                do {
                    doProcess(x1, cy++, rainbowDrawer, listener);
                }
                while (cy <= y2);
                return;
            } else {
                cy = y2;
                do {
                    doProcess(x1, cy++, rainbowDrawer, listener);
                }
                while (cy <= y1);
                return;
            }
        }
        if (ddy == 0) { //horizontal line special case
            if (ddx > 0) {
                cx = x1;
                do {
                    doProcess(cx, y1, rainbowDrawer, listener);
                }
                while (++cx <= x2);
                return;
            } else {
                cx = x2;
                do {
                    doProcess(cx, y1, rainbowDrawer, listener);
                }
                while (++cx <= x1);
                return;
            }
        }
        if (ddy < 0) {
            iy = -1;
            ddy = -ddy;
        }//pointing up
        else {
            iy = 1;
        }
        if (ddx < 0) {
            ix = -1;
            ddx = -ddx;
        }//pointing left
        else {
            ix = 1;
        }
        dx = dy = ddx * ddy;
        cy = y1;
        cx = x1;
        if (ddx < ddy) { // < 45 degrees, a tall line
            do {
                dx -= ddy;
                do {
                    doProcess(cx, cy, rainbowDrawer, listener);
                    cy += iy;
                    dy -= ddx;
                } while (dy >= dx);
                cx += ix;
            } while (dx > 0);
        } else { // >= 45 degrees, a wide line
            do {
                dy -= ddx;
                do {
                    doProcess(cx, cy, rainbowDrawer, listener);
                    cx += ix;
                    dx -= ddy;
                } while (dx >= dy);
                cy += iy;
            } while (dy > 0);
        }
    }

    private void doProcess(float x, float y, RainbowDrawer rainbowDrawer, RainbowDrawer.PointDetectedListener listener) {
        if (detections % 2 == 0) {
            listener.onPointDetected(x, y, rainbowDrawer);
        }
        detections++;
    }
}
