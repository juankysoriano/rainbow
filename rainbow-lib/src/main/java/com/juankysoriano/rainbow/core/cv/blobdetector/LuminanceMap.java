package com.juankysoriano.rainbow.core.cv.blobdetector;

import android.graphics.Color;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

import java.util.Arrays;

public class LuminanceMap {
    private static final int BORDER_OFFSET = 5;
    private final float[] gridValue;
    private final boolean[] gridVisited;
    private final int width;
    private final int height;

    public static LuminanceMap newInstance(RainbowImage rainbowImage) {
        int width = rainbowImage.getWidth();
        int height = rainbowImage.getHeight();
        float[] gridValue = new float[width * height];
        boolean[] gridVisited = new boolean[width * height];
        computeIsoValues(gridValue, rainbowImage);
        return new LuminanceMap(width, height, gridValue, gridVisited);
    }

    private static void computeIsoValues(float[] luminances, RainbowImage rainbowImage) {
        for (int x = 0; x < rainbowImage.getWidth(); x++) {
            for (int y = 0; y < rainbowImage.getHeight(); y++) {
                int color = rainbowImage.get(x, y);
                int index = x + rainbowImage.getWidth() * y;
                // http://www.songho.ca/dsp/luminance/luminance.html
                luminances[index] = 3 * (2 * Color.red(color) + 5 * Color.green(color) + Color.blue(color)) / 8;
            }
        }
    }

    protected LuminanceMap(int width, int height, float[] gridValue, boolean[] gridVisited) {
        this.width = width;
        this.height = height;
        this.gridValue = gridValue;
        this.gridVisited = gridVisited;
    }

    public void reset() {
        Arrays.fill(gridVisited, false);
    }

    public void visit(int x, int y) {
        int offset = x + width * y;
        gridVisited[offset] = true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVisited(int x, int y) {
        return isIndexOutsideGrid(x, y) || gridVisited[x + width * y];
    }

    public float getLuminanceAt(int x, int y) {
        return gridValue[x + width * y];
    }

    private boolean isIndexOutsideGrid(int x, int y) {
        return x < BORDER_OFFSET || x >= width - BORDER_OFFSET || y < BORDER_OFFSET || y >= height - BORDER_OFFSET;
    }
}
