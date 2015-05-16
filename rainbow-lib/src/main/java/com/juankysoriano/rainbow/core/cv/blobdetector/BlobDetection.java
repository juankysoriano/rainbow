package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;

//==================================================
//class BlobDetection
//==================================================
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

    public BlobDetection(RainbowImage rainbowImage, int maxNumberOfBlobs) {
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
                if (hasToPaintMoreBlobs()) {
                    if (!luminanceMap.isVisited(x, y) && isBlobEdge(x, y)) {
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
            numberOfBlobsDetected++;
            onBlobDetectedCallback.onBlobDetected(newBlob);
        }
    }

    private void exploreVertex(final Blob newBlob, final int x, final int y) {
        if (luminanceMap.isVisited(x, y)) {
            return;
        }

        luminanceMap.visit(x, y);
        if (isBlobEdge(x, y)) {
            addVertexToBlob(newBlob, x, y);
            try {
                exploreNeighbours(newBlob, x, y);
            } catch (StackOverflowError e) {
                exploreNeighbours(newBlob, x, y);
            }
        }
    }

    public boolean isBlobEdge(int x, int y) {

        int neighbourInsideBlobCounter = 0;
        if (isInsideBlob(x - 1, y)) {
            neighbourInsideBlobCounter++;
        }
        if (isInsideBlob(x, y - 1)) {
            neighbourInsideBlobCounter++;
        }
        if (isInsideBlob(x, y + 1)) {
            neighbourInsideBlobCounter++;
        }
        if (isInsideBlob(x + 1, y)) {
            neighbourInsideBlobCounter++;
        }

        // We have already seen if the neighbours are inside a blob.
        // If all of them are, or none of them are, then we can guarantee that coord(x, y)
        // is not a blob edge.
        return neighbourInsideBlobCounter != 0 && neighbourInsideBlobCounter != 4;
    }

    public boolean isInsideBlob(int x, int y) {
        return luminanceMap.getLuminanceAt(x, y) < luminanceThreshold;
    }

    private void exploreNeighbours(Blob newBlob, int x, int y) {
        exploreVertex(newBlob, x - 1, y);
        exploreVertex(newBlob, x + 1, y);
        exploreVertex(newBlob, x, y - 1);
        exploreVertex(newBlob, x, y + 1);
    }

    private void addVertexToBlob(Blob newBlob, int x, int y) {
        float edgeX = x / (float) luminanceMap.getWidth();
        float edgeY = y / (float) luminanceMap.getHeight();
        newBlob.addEdgeVertex(new EdgeVertex(edgeX, edgeY));
    }

    private boolean hasToPaintMoreBlobs() {
        return numberOfBlobsDetected < maxNumberOfBlobs;
    }

    public void cancel() {
        threadGroup.interrupt();
    }

}
