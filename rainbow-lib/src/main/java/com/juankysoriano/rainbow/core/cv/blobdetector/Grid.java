package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

import java.util.Arrays;

public class Grid {
    private final int[] gridValue;
    private final boolean[] gridVisited;
    private int width;
    private int height;
    private float isoValue;

    public Grid(RainbowImage rainbowImage) {
        width = rainbowImage.getWidth();
        height = rainbowImage.getHeight();

        gridValue = new int[width * height];
        gridVisited = new boolean[width * height];

        computeIsoValues(rainbowImage);
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
        return gridVisited[x + width * y];
    }

    public boolean isBlobEdge(int x, int y) {
        int squareIndex = getSquareIndex(x, y);
        return squareIndex > 0 && squareIndex < 15;
    }

    public byte getNeighbourVoxel(int x, int y) {
        return MetaballsTable.neightborVoxel[getSquareIndex(x, y)];
    }

    public int getEdgesToCompute(int x, int y) {
        return MetaballsTable.edgeToCompute[getSquareIndex(x, y)];
    }

    public int[] getGridValues() {
        return gridValue;
    }

    private int getSquareIndex(int x, int y) {
        int squareIndex = 0;
        int offY = width * y;
        int nextOffY = width * (y + 1);

        if (gridValue[x + offY] > isoValue) {
            squareIndex |= 1;
        }
        if (gridValue[x + 1 + offY] > isoValue) {
            squareIndex |= 2;
        }
        if (gridValue[x + 1 + nextOffY] > isoValue) {
            squareIndex |= 4;
        }
        if (gridValue[x + nextOffY] > isoValue) {
            squareIndex |= 8;
        }
        return squareIndex;
    }

    private void computeIsoValues(RainbowImage rainbowImage) {
        int color, r, g, b;

        for (int i = 0; i < rainbowImage.getWidth(); i++) {
            for (int j = 0; j < rainbowImage.getHeight(); j++) {
                color = rainbowImage.get(i, j);
                r = (color & 0x00FF0000) >> 16;
                g = (color & 0x0000FF00) >> 8;
                b = (color & 0x000000FF);
                int index = i + rainbowImage.getWidth() * j;
                gridValue[index] = r + g + b;
            }
        }
    }

    public void setIsoValue(float isoValue) {
        this.isoValue = isoValue;
    }
}
