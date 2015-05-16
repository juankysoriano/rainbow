package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.utils.RainbowMath;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection {
    private static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 750;
    private static final float MAX_ISO_VALUE = 3.0f * 255.0f;
    private static final int BORDER_OFFSET = 20;

    private final ThreadGroup threadGroup = new ThreadGroup("BLOB");

    private final int maxNumberOfBlobs;
    private final int maxX;
    private final int maxY;
    private final int minX;
    private final int minY;
    private final Grid grid;
    private int blobNumber;

    public BlobDetection(RainbowImage rainbowImage) {
        this(rainbowImage, DEFAULT_MAX_NUMBER_OF_BLOBS);
    }

    public BlobDetection(RainbowImage rainbowImage, int maxNumberOfBlobs) {
        this.grid = Grid.newInstance(rainbowImage);
        this.minX = BORDER_OFFSET;
        this.minY = BORDER_OFFSET;
        this.maxX = grid.getWidth() - BORDER_OFFSET;
        this.maxY = grid.getHeight() - BORDER_OFFSET;
        this.maxNumberOfBlobs = maxNumberOfBlobs;
    }

    public void setThreshold(float value) {
        setIsoValue(RainbowMath.constrain(value, 0.0f, 1.0f) * MAX_ISO_VALUE);
    }

    private void setIsoValue(float iso) {
        grid.setIsoValue(iso);
    }

    public int getNumberOfBlobs() {
        return blobNumber;
    }

    public void computeBlobs(final OnBlobDetectedCallback onBlobDetectedCallback) {
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                grid.reset();
                detectBlobs(onBlobDetectedCallback);
                onBlobDetectedCallback.onBlobDetectionFinish();
            }
        }, "blobDetection", 100000);
        thread.start();
    }

    private void detectBlobs(OnBlobDetectedCallback onBlobDetectedCallback) {
        int diffX = maxX - minX;
        int diffY = maxY - minY;
        int offsetX = RainbowMath.random((diffX) / 2);
        int offsetY = RainbowMath.random((diffY) / 2);

        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                int x = (i + offsetX) % diffX + minX;
                int y = (j + offsetY) % diffY + minY;
                if (hasToPaintMoreBlobs()) {
                    if (grid.isBlobEdge(x, y) && !grid.isVisited(x, y)) {
                        findBlob(x, y, onBlobDetectedCallback);
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void findBlob(int x, int y, OnBlobDetectedCallback onBlobDetectedCallback) {
        Blob newBlob = new Blob(x, y);
        exploreVertex(newBlob, x, y);

        if (onBlobDetectedCallback.filterBlob(newBlob)) {
            blobNumber++;
            onBlobDetectedCallback.onBlobDetected(newBlob);
        }
    }

    private void exploreVertex(final Blob newBlob, final int x, final int y) {
        if (grid.isVisited(x, y)) {
            return;
        }

        grid.visit(x, y);
        if (/*newBlob.getEdgeCount() < maxEdgesPerBlob &&*/
                grid.shouldExploreNeighbours(x, y)) {
            calculateEdgeVertex(newBlob, x, y);
            try {
                exploreNeighbours(newBlob, x, y);
            } catch (StackOverflowError error) {
                exploreNeighbours(newBlob, x, y);
            }
        }
    }

    private void exploreNeighbours(Blob newBlob, int x, int y) {
        if (areCoordinatesInsideGrid(x, y)) {
            if (grid.shouldExploreRight(x, y)) {
                exploreVertex(newBlob, x + 1, y);
            }
            if (grid.shouldExploreLeft(x, y)) {
                exploreVertex(newBlob, x - 1, y);
            }
            if (grid.shouldExploreUp(x, y)) {
                exploreVertex(newBlob, x, y + 1);
            }
            if (grid.shouldExploreDown(x, y)) {
                exploreVertex(newBlob, x, y - 1);
            }
        }
    }

    private boolean areCoordinatesInsideGrid(int x, int y) {
        return x > 0 && x < grid.getWidth()
                && y > 0 && y < grid.getHeight();
    }

    private void calculateEdgeVertex(Blob newBlob, int x, int y) {
        float edgeX = x / (float) grid.getWidth();
        float edgeY = y / (float) grid.getHeight();
        newBlob.addEdgeVertex(new EdgeVertex(edgeX, edgeY));
    }

    private boolean hasToPaintMoreBlobs() {
        return blobNumber < maxNumberOfBlobs;
    }

    public void cancel() {
        threadGroup.interrupt();
    }

}
