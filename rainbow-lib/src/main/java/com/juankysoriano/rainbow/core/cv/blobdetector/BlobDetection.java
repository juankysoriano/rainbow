package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.utils.schedulers.RainbowScheduler;
import com.juankysoriano.rainbow.utils.schedulers.RainbowSchedulers;

/**
 * “It's still magic even if you know how it's done.”
 * <p>
 * - Terry Pratchet, A Hat Full of Sky
 */
public class BlobDetection {
    private static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 10000;

    private final int maxNumberOfBlobs;
    private final LuminanceMap luminanceMap;

    private int numberOfBlobsDetected;
    private final RainbowScheduler scheduler;
    private boolean skipBlobDetection;

    public BlobDetection(RainbowImage rainbowImage) {
        this(rainbowImage, DEFAULT_MAX_NUMBER_OF_BLOBS);
    }

    private BlobDetection(RainbowImage rainbowImage, int maxNumberOfBlobs) {
        this.luminanceMap = LuminanceMap.newInstance(rainbowImage);
        this.maxNumberOfBlobs = maxNumberOfBlobs;
        scheduler = RainbowSchedulers.singleForRecursion("BlobDetection", RainbowSchedulers.Priority.NORMAL);
    }

    public void setThreshold(float value) {
        luminanceMap.setThreshold(value);
    }

    public void computeBlobs(final OnBlobDetectedCallback onBlobDetectedCallback) {
        scheduler.scheduleNow(new Runnable() {
            @Override
            public void run() {
                luminanceMap.reset();
                detectBlobs(onBlobDetectedCallback);
                onBlobDetectedCallback.onBlobDetectionFinish();
            }
        });
    }

    private void detectBlobs(OnBlobDetectedCallback onBlobDetectedCallback) {
        for (int x = 0; x < luminanceMap.getWidth(); x++) {
            for (int y = 0; y < luminanceMap.getHeight(); y++) {
                if (hasToDetectMoreBlobs()) {
                    skipBlobDetection = false;
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
        if (luminanceMap.isVisited(x, y) || !isBlobEdge(x, y)) {
            return;
        }

        Blob newBlob = new Blob();
        findVertexes(newBlob, x, y);

        if (onBlobDetectedCallback.filterBlob(newBlob)) {
            numberOfBlobsDetected++;
            onBlobDetectedCallback.onBlobDetected(newBlob);
        }
    }

    private void findVertexes(final Blob newBlob, final int x, final int y) {
        if (skipBlobDetection || luminanceMap.isVisited(x, y)) {
            return;
        }

        luminanceMap.visit(x, y);

        if (isBlobEdge(x, y)) {
            addVertexToBlob(newBlob, x, y);
            safeExploreNeighbours(newBlob, x, y);
        }
    }

    private void safeExploreNeighbours(Blob newBlob, int x, int y) {
        try {
            exploreNeighbours(newBlob, x, y);
        } catch (StackOverflowError error) {
            skipBlobDetection = true;
        }
    }

    private void addVertexToBlob(Blob newBlob, int x, int y) {
        float edgeX = x / (float) luminanceMap.getWidth();
        float edgeY = y / (float) luminanceMap.getHeight();
        newBlob.addEdgeVertex(new EdgeVertex(edgeX, edgeY));
    }

    private void exploreNeighbours(Blob newBlob, int x, int y) {
        findVertexes(newBlob, x - 1, y);
        findVertexes(newBlob, x + 1, y);
        findVertexes(newBlob, x, y - 1);
        findVertexes(newBlob, x, y + 1);
    }

    private boolean isBlobEdge(int x, int y) {
        boolean isLeftPixelInsideBlob = luminanceMap.isInsideBlob(x - 1, y);
        boolean isRightPixelInsideBlob = luminanceMap.isInsideBlob(x + 1, y);
        boolean isUpPixelInsideBlob = luminanceMap.isInsideBlob(x, y - 1);
        boolean isDownPixelInsideBlob = luminanceMap.isInsideBlob(x, y + 1);
        boolean allNeighboursInsideBlob = isLeftPixelInsideBlob && isRightPixelInsideBlob && isUpPixelInsideBlob && isDownPixelInsideBlob;
        boolean noNeighbourInsideBlob = !isLeftPixelInsideBlob && !isRightPixelInsideBlob && !isUpPixelInsideBlob && !isDownPixelInsideBlob;

        // We have already seen if the neighbours are inside a blob.
        // If all of them are, or none of them are, then we can guarantee that coord(x, y)
        // is not a blob edge.
        return !noNeighbourInsideBlob && !allNeighboursInsideBlob;
    }

    public void cancel() {
        scheduler.shutdown();
    }

}
