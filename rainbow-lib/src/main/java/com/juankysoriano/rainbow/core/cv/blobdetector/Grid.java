package com.juankysoriano.rainbow.core.cv.blobdetector;

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

    protected boolean shouldExploreLeft(int x, int y) {
        return (getNeighbourVoxel(x, y) & 2) == 2;
    }

    protected boolean shouldExploreRight(int x, int y) {
        return (getNeighbourVoxel(x, y) & 1) == 1;
    }

    protected boolean shouldExploreUp(int x, int y) {
        return (getNeighbourVoxel(x, y) & 4) == 4;
    }

    protected boolean shouldExploreDown(int x, int y) {
        return (getNeighbourVoxel(x, y) & 8) == 8;
    }

    protected boolean shouldExploreNeighbours(int x, int y) {
        return shouldExploreLeft(x, y) || shouldExploreRight(x, y) || shouldExploreUp(x, y) || shouldExploreDown(x, y);
    }

    private int getSquareIndex(int x, int y) {
        int squareIndex = 0;
        int offY = width * y;
        int nextOffY = width * (y + 1);

        if (isIndexInsideGrid(x + offY) && gridValue[x + offY] > isoValue) {
            squareIndex |= 1;
        }
        if (isIndexInsideGrid(x + 1 + offY) && gridValue[x + 1 + offY] > isoValue) {
            squareIndex |= 2;
        }
        if (isIndexInsideGrid(x + 1 + nextOffY) && gridValue[x + 1 + nextOffY] > isoValue) {
            squareIndex |= 4;
        }
        if (isIndexInsideGrid(x + nextOffY) && gridValue[x + nextOffY] > isoValue) {
            squareIndex |= 8;
        }
        return squareIndex;
    }

    private boolean isIndexInsideGrid(int offset) {
        return offset < gridValue.length;
    }

    public void setIsoValue(float isoValue) {
        this.isoValue = isoValue;
    }
}
