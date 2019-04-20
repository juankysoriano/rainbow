package com.juankysoriano.rainbow.core.cv.blobdetector;

import android.graphics.Color;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

import java.util.Arrays;

class LuminanceMap {
    private static final float MAX_ISO_VALUE = 3.0f * 255.0f;
    private static final int BORDER_OFFSET = 5;
    private final boolean[] insideBlobPixels;
    private final boolean[] visitedPixels;
    private final int[] pixels;
    private final int width;
    private final int height;
    private float luminanceThreshold;

    static LuminanceMap newInstance(RainbowImage rainbowImage) {
        int width = rainbowImage.getWidth();
        int height = rainbowImage.getHeight();
        rainbowImage.loadPixels();
        int[] pixels = rainbowImage.pixels;
        boolean[] luminanceValues = new boolean[width * height];
        boolean[] visitedPixels = new boolean[width * height];
        return new LuminanceMap(pixels, width, height, luminanceValues, visitedPixels);
    }

    private LuminanceMap(int[] pixels, int width, int height, boolean[] insideBlobPixels, boolean[] visitedPixels) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.insideBlobPixels = insideBlobPixels;
        this.visitedPixels = visitedPixels;
    }

    void reset() {
        Arrays.fill(visitedPixels, false);
        calculateInsideBlobPixels();
    }

    private void calculateInsideBlobPixels() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x + width * y;
                int color = pixels[index];
                // http://www.songho.ca/dsp/luminance/luminance.html
                float luminance = 3 * (2 * Color.red(color) + 5 * Color.green(color) + Color.blue(color)) >> 3;

                insideBlobPixels[index] = luminance <= luminanceThreshold;
            }
        }
    }

    void visit(int x, int y) {
        int offset = x + width * y;
        visitedPixels[offset] = true;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    boolean isVisited(int x, int y) {
        return isIndexOutsideGrid(x, y) || visitedPixels[x + width * y];
    }

    boolean isInsideBlob(int x, int y) {
        return insideBlobPixels[x + width * y];
    }

    private boolean isIndexOutsideGrid(int x, int y) {
        return x < BORDER_OFFSET || x >= width - BORDER_OFFSET || y < BORDER_OFFSET || y >= height - BORDER_OFFSET;
    }

    void setThreshold(float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Developer error, threshold should be a value between 0 and 1");
        }
        luminanceThreshold = value * MAX_ISO_VALUE;
    }
}
