package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.utils.RainbowMath;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection {
    private static final int DEFAULT_MAX_EDGES_PER_BLOB = 2000;
    private static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 1000;
    private static final float MAX_ISO_VALUE = 3.0f * 255.0f;

    private final ThreadGroup threadGroup = new ThreadGroup("BLOB");

    private final int maxNumberOfBlobs;
    private final int maxEdgesPerBlob;
    private final Grid grid;
    private int blobNumber;

    public BlobDetection(RainbowImage rainbowImage) {
        this(rainbowImage, DEFAULT_MAX_NUMBER_OF_BLOBS, DEFAULT_MAX_EDGES_PER_BLOB);
    }

    public BlobDetection(RainbowImage rainbowImage, int maxNumberOfBlobs, int maxEdgesPerBlob) {
        this.grid = Grid.newInstance(rainbowImage);
        this.maxNumberOfBlobs = maxNumberOfBlobs;
        this.maxEdgesPerBlob = maxEdgesPerBlob;
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
        for (int x = 2; x < grid.getWidth() - 2; x++) {
            for (int y = 2; y < grid.getHeight() - 2; y++) {
                if (hasToPaintMoreBlobs()) {
                    if (grid.isBlobEdge(x, y) && !grid.isVisited(x, y)) {
                        Blob newBlob = findBlob(x, y);
                        if (!onBlobDetectedCallback.isToDiscardBlob(newBlob)) {
                            onBlobDetectedCallback.onBlobDetected(newBlob);
                            blobNumber++;
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    private Blob findBlob(int x, int y) {
        Blob newBlob = new Blob(x, y);
        exploreVertex(newBlob, x, y);

        return newBlob;
    }

    private void exploreVertex(final Blob newBlob, final int x, final int y) {
        if (grid.isVisited(x, y)) {
            return;
        }

        grid.visit(x, y);
        if (newBlob.getEdgeCount() < maxEdgesPerBlob) {
            if (grid.shouldExploreNeighbours(x, y)) {
                calculateEdgeVertex(newBlob, x, y);
                try {
                    exploreNeighbours(newBlob, x, y);
                } catch (StackOverflowError error) {
                    exploreNeighbours(newBlob, x, y);
                }
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
        return x > 2 && x < grid.getWidth() - 2
                && y > 2 && y < grid.getHeight() - 2;
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
