package com.juankysoriano.rainbow.core.cv.blobdetector;

import android.graphics.Color;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

import java.util.Arrays;

public class Grid {
    private final int[] gridValue;
    private final boolean[] gridVisited;
    private final int width;
    private final int height;
    private float isoValue;

    public static Grid newInstance(RainbowImage rainbowImage) {
        int width = rainbowImage.getWidth();
        int height = rainbowImage.getHeight();
        int[] gridValue = new int[width * height];
        boolean[] gridVisited = new boolean[width * height];
        computeIsoValues(gridValue, rainbowImage);
        return new Grid(width, height, gridValue, gridVisited);
    }

    private static void computeIsoValues(int[] gridValue, RainbowImage rainbowImage) {
        for (int x = 0; x < rainbowImage.getWidth(); x++) {
            for (int y = 0; y < rainbowImage.getHeight(); y++) {
                int color = rainbowImage.get(x, y);
                int index = x + rainbowImage.getWidth() * y;
                gridValue[index] = Color.red(color) + Color.green(color) + Color.blue(color);
            }
        }
    }

    protected Grid(int width, int height, int[] gridValue, boolean[] gridVisited) {
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
        return gridVisited[x + width * y];
    }

    public boolean isBlobEdge(int x, int y) {
        int squareIndex = getSquareIndex(x, y);
        return squareIndex > 0 && squareIndex < 15;
    }

    private byte getNeighbourVoxel(int x, int y) {
        return MetaballsTable.neightbourVoxel[getSquareIndex(x, y)];
    }

    protected boolean shouldExploreRight(int x, int y) {
        return (getNeighbourVoxel(x, y) & 1) > 0;
    }

    protected boolean shouldExploreLeft(int x, int y) {
        return (getNeighbourVoxel(x, y) & 2) > 0;
    }

    protected boolean shouldExploreUp(int x, int y) {
        return (getNeighbourVoxel(x, y) & 4) > 0;
    }

    protected boolean shouldExploreDown(int x, int y) {
        return (getNeighbourVoxel(x, y) & 8) > 0;
    }

    protected boolean shouldExploreNeighbours(int x, int y) {
        return shouldExploreLeft(x, y) || shouldExploreRight(x, y) || shouldExploreUp(x, y) || shouldExploreDown(x, y);
    }

    private int getSquareIndex(int x, int y) {
        int squareIndex = 0;

        if (isIndexInsideGrid(x, y) && getGridValue(x, y) < isoValue) {
            squareIndex |= 1;
        }
        if (isIndexInsideGrid(x + 1, y) && getGridValue(x + 1, y) < isoValue) {
            squareIndex |= 2;
        }
        if (isIndexInsideGrid(x + 1, y + 1) && getGridValue(x + 1, y + 1) < isoValue) {
            squareIndex |= 4;
        }
        if (isIndexInsideGrid(x, y + 1) && getGridValue(x, y + 1) < isoValue) {
            squareIndex |= 8;
        }
        return squareIndex;
    }

    private int getGridValue(int x, int y) {
        return gridValue[x + width * y];
    }

    private boolean isIndexInsideGrid(int x, int y) {
        return x + width * y < gridValue.length;
    }

    public void setIsoValue(float isoValue) {
        this.isoValue = isoValue;
    }
}
