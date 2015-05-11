package com.juankysoriano.rainbow.core.cv.blobdetector;

import java.util.Arrays;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection extends EdgeDetection {
    public static final int DEFAULT_MAX_LINES_PER_BLOB = 4000;
    public static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 1000;

    private int blobNumber;
    private final boolean[] gridVisited;
    private final ThreadGroup threadGroup = new ThreadGroup("BLOB");
    private int maxNumberOfBlobs = DEFAULT_MAX_NUMBER_OF_BLOBS;
    private int maxLinesPerBlob = DEFAULT_MAX_LINES_PER_BLOB;

    // --------------------------------------------
    // Constructor
    // --------------------------------------------
    public BlobDetection(int imgWidth, int imgHeight) {
        super(imgWidth, imgHeight);

        gridVisited = new boolean[nbGridValue];
        blobNumber = 0;
    }

    public BlobDetection(int imgWidth, int imgHeight, int maxNumberOfBlobs, int maxLinesPerBlob) {
        this(imgWidth, imgHeight);
        this.maxNumberOfBlobs = maxNumberOfBlobs;
        this.maxLinesPerBlob = maxLinesPerBlob;
    }

    public int getBlobNb() {
        return blobNumber;
    }

    // --------------------------------------------
    // computeBlobs()
    // --------------------------------------------
    public void computeBlobs(int[] pixels, final OnBlobDetectedCallback onBlobDetectedCallback) {
        setImage(pixels);

        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {

                Arrays.fill(gridVisited, false);

                computeIsovalue();

                int x, y, squareIndex;
                int offset;

                nbLineToDraw = 0;
                blobNumber = 0;
                Blob newBlob = new Blob(BlobDetection.this, maxLinesPerBlob);
                for (x = 0; x < resx - 1; x++) {
                    for (y = 0; y < resy - 1; y++) {
                        offset = x + resx * y;
                        if (!gridVisited[offset]) {
                            squareIndex = getSquareIndex(x, y);

                            if (squareIndex > 0 && squareIndex < 15) {
                                if (blobNumber >= 0 && blobNumber < maxNumberOfBlobs) {
                                    findBlob(newBlob, x, y, onBlobDetectedCallback);
                                    blobNumber++;
                                } else {
                                    nbLineToDraw /= 2;
                                    onBlobDetectedCallback.onBlobDetectionFinish();
                                    return;
                                }
                            }
                        }
                    }
                }
                nbLineToDraw /= 2;
                onBlobDetectedCallback.onBlobDetectionFinish();
            }
        }, "blob", 100000);

        thread.start();
    }

    public void findBlob(Blob newBlob, int x, int y, OnBlobDetectedCallback onBlobDetectedCallback) {

        newBlob.id = blobNumber;
        newBlob.xMin = Integer.MAX_VALUE;
        newBlob.xMax = Integer.MIN_VALUE;
        newBlob.yMin = Integer.MAX_VALUE;
        newBlob.yMax = Integer.MIN_VALUE;
        newBlob.nbLine = 0;

        computeEdgeVertex(newBlob, x, y);
        newBlob.update();
        if (onBlobDetectedCallback != null) {
            if (onBlobDetectedCallback.isToDiscardBlob(newBlob)) {
                blobNumber--;
            } else {
                onBlobDetectedCallback.onBlobDetected(newBlob);
            }
        }
    }

    // --------------------------------------------
    // computeEdgeVertex()
    // --------------------------------------------
    void computeEdgeVertex(Blob newBlob, int x, int y) {
        int offset = x + resx * y;
        if (gridVisited[offset]) {
            return;
        }

        if (newBlob.nbLine < maxLinesPerBlob) {
            gridVisited[offset] = true;
            nbLineToDraw++;
            newBlob.line[newBlob.nbLine++] = ((x) * resy + (y)) * 2;

            calculateEdgeVertex(newBlob, x, y);

            int squareIndex = getSquareIndex(x, y);
            byte neighborVoxel = MetaballsTable.neightborVoxel[squareIndex];
            if (x < resx - 2 && (neighborVoxel & 1) == 1) {
                computeEdgeVertex(newBlob, x + 1, y);
            }
            if (x > 0 && (neighborVoxel & 2) == 2) {
                computeEdgeVertex(newBlob, x - 1, y);
            }
            if (y < resy - 2 && (neighborVoxel & 4) == 4) {
                computeEdgeVertex(newBlob, x, y + 1);
            }
            if (y > 0 && (neighborVoxel & 8) == 8) {
                computeEdgeVertex(newBlob, x, y - 1);
            }
        }
    }

    private void calculateEdgeVertex(Blob newBlob, int x, int y) {
        int index = (x * resy + y) * 2;
        int offset = x + resx * y;
        int squareIndex = getSquareIndex(x, y);
        int toCompute = MetaballsTable.edgeToCompute[squareIndex];
        float t;
        float value;
        if ((toCompute & 1) > 0) {
            float vx = (float) x * stepx;
            t = (isovalue - gridValue[offset]) / (gridValue[offset + 1] - gridValue[offset]);
            value = vx * (1.0f - t) + t * (vx + stepx);
            edgeVrt[index].x = value;

            newBlob.xMin = Math.min(value, newBlob.xMin);
            newBlob.xMax = Math.max(value, newBlob.xMax);
        }
        if ((toCompute & 2) > 0) {
            float vy = (float) y * stepy;
            t = (isovalue - gridValue[offset]) / (gridValue[offset + resx] - gridValue[offset]);
            value = vy * (1.0f - t) + t * (vy + stepy);
            edgeVrt[index + 1].y = value;

            newBlob.yMin = Math.min(value, newBlob.yMin);
            newBlob.yMax = Math.max(value, newBlob.yMax);
        }
    }
}
