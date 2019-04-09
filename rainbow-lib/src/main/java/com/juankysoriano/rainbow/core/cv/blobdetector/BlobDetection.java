package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

/**
 * “It's still magic even if you know how it's done.”
 *
 * - Terry Pratchet, A Hat Full of Sky
 */
public class BlobDetection {
    private static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 750;
    private static final float MAX_ISO_VALUE = 3.0f * 255.0f;

    private final ThreadGroup threadGroup = new ThreadGroup("BLOB");
    private final int maxNumberOfBlobs;
    private final LuminanceMap luminanceMap;

    private int numberOfBlobsDetected;
    private float luminanceThreshold;

    public BlobDetection(RainbowImage rainbowImage) {
        this(rainbowImage, DEFAULT_MAX_NUMBER_OF_BLOBS);
    }

    private BlobDetection(RainbowImage rainbowImage, int maxNumberOfBlobs) {
        this.luminanceMap = LuminanceMap.newInstance(rainbowImage);
        this.maxNumberOfBlobs = maxNumberOfBlobs;
    }

    public void setThreshold(float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Developer error, threshold should be a value between 0 and 1");
        }
        luminanceThreshold = value * MAX_ISO_VALUE;
    }

    public void computeBlobs(final OnBlobDetectedCallback onBlobDetectedCallback) {
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                luminanceMap.reset();
                detectBlobs(onBlobDetectedCallback);
                onBlobDetectedCallback.onBlobDetectionFinish();
            }
        }, "blobDetection", 1024 * 1024);
        thread.start();
    }

    private void detectBlobs(OnBlobDetectedCallback onBlobDetectedCallback) {

        for (int x = 0; x < luminanceMap.getWidth(); x++) {
            for (int y = 0; y < luminanceMap.getHeight(); y++) {
                if (hasToDetectMoreBlobs()) {
                    findBlobAt(x, y, onBlobDetectedCallback);
                } else {
                    return;
                }
            }
        }
    }

    private boolean hasToDetectMoreBlobs() {
        return numberOfBlobsDetected < maxNumberOfBlobs;
    }

    private void findBlobAt(int x, int y, OnBlobDetectedCallback onBlobDetectedCallback) {
        if (isVisited(x, y) || !isBlobEdge(x, y)) {
            return;
        }

        Blob newBlob = new Blob();
        findVertexes(newBlob, x, y);

        if (onBlobDetectedCallback.filterBlob(newBlob)) {
            numberOfBlobsDetected++;
            onBlobDetectedCallback.onBlobDetected(newBlob);
        }
    }

    private boolean isVisited(int x, int y) {
        return luminanceMap.isVisited(x, y);
    }

    private void maskAsVisited(int x, int y) {
        luminanceMap.visit(x, y);
    }

    private void findVertexes(final Blob newBlob, final int x, final int y) {
        if (isVisited(x, y)) {
            return;
        }

        maskAsVisited(x, y);

        if(isBlobEdge(x, y)) {
            addVertexToBlob(newBlob, x, y);
            infallibleExploreNeighbours(newBlob, x, y);
        }
    }

    private void addVertexToBlob(Blob newBlob, int x, int y) {
        float edgeX = x / (float) luminanceMap.getWidth();
        float edgeY = y / (float) luminanceMap.getHeight();
        newBlob.addEdgeVertex(new EdgeVertex(edgeX, edgeY));
    }

    private void infallibleExploreNeighbours(Blob newBlob, int x, int y) {
        try {
            exploreNeighbours(newBlob, x, y);
        } catch (StackOverflowError e) {
            exploreNeighbours(newBlob, x, y);
        }
    }

    private void exploreNeighbours(Blob newBlob, int x, int y) {
        findVertexes(newBlob, x - 1, y);
        findVertexes(newBlob, x + 1, y);
        findVertexes(newBlob, x, y - 1);
        findVertexes(newBlob, x, y + 1);
    }

    private boolean isBlobEdge(int x, int y) {
        boolean isLeftPixelInsideBlob = isInsideBlob(x - 1, y);
        boolean isRightPixelInsideBlob = isInsideBlob(x + 1, y);
        boolean isUpPixelInsideBlob = isInsideBlob(x, y - 1);
        boolean isDownPixelInsideBlob = isInsideBlob(x, y + 1);
        boolean allNeighboursInsideBlob = isLeftPixelInsideBlob && isRightPixelInsideBlob && isUpPixelInsideBlob && isDownPixelInsideBlob;
        boolean noNeighbourInsideBlob = !isLeftPixelInsideBlob && !isRightPixelInsideBlob && !isUpPixelInsideBlob && !isDownPixelInsideBlob;

        // We have already seen if the neighbours are inside a blob.
        // If all of them are, or none of them are, then we can guarantee that coord(x, y)
        // is not a blob edge.
        return !noNeighbourInsideBlob && !allNeighboursInsideBlob;
    }

    private boolean isInsideBlob(int x, int y) {
        return luminanceMap.getLuminanceAt(x, y) <= luminanceThreshold;
    }

    public void cancel() {
        threadGroup.interrupt();
    }

}
