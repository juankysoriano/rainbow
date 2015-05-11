package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.utils.RainbowMath;

import java.util.Arrays;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection extends EdgeDetection {
    public static final int DEFAULT_MAX_LINES_PER_BLOB = 4000;
    public static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 1000;

    private final ThreadGroup threadGroup = new ThreadGroup("BLOB");
    private int maxNumberOfBlobs = DEFAULT_MAX_NUMBER_OF_BLOBS;
    private int maxLinesPerBlob = DEFAULT_MAX_LINES_PER_BLOB;
    private int blobNumber;
    private boolean[] gridVisited;
    private float isoValue;
    private int resX, resY;
    private float stepX, stepY;
    private int[] gridValue;
    private EdgeVertex[] edgeVrt;
    protected int nbLineToDraw;

    // --------------------------------------------
    // Constructor
    // --------------------------------------------
    public BlobDetection(int imgWidth, int imgHeight) {
        init(imgWidth, imgHeight);
    }

    public BlobDetection(int imgWidth, int imgHeight, int maxNumberOfBlobs, int maxLinesPerBlob) {
        this(imgWidth, imgHeight);
        this.maxNumberOfBlobs = maxNumberOfBlobs;
        this.maxLinesPerBlob = maxLinesPerBlob;
    }

    protected void init(int resX, int resY) {
        this.resX = resX;
        this.resY = resY;

        this.stepX = 1.0f / ((float) (resX - 1));
        this.stepY = 1.0f / ((float) (resY - 1));

        int gridSize = resX * resY;
        gridValue = new int[gridSize];
        gridVisited = new boolean[gridSize];
        edgeVrt = new EdgeVertex[2 * gridSize];
        nbLineToDraw = 0;

        int n = 0;
        for (int x = 0; x < resX; x++) {
            for (int y = 0; y < resY; y++) {
                int index = 2 * n;
                edgeVrt[index] = new EdgeVertex(x * stepX, y * stepY);
                edgeVrt[index + 1] = new EdgeVertex(x * stepX, y * stepY);
                n++;
            }
        }

        blobNumber = 0;

    }

    public float m_coeff = 3.0f * 255.0f;

    // --------------------------------------------
    // setThreshold()
    // --------------------------------------------
    public void setThreshold(float value) {
        setIsoValue(RainbowMath.constrain(value, 0.0f, 1.0f) * m_coeff);
    }

    public void computeIsoValue(int[] pixels) {
        int color, r, g, b;

        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            r = (color & 0x00FF0000) >> 16;
            g = (color & 0x0000FF00) >> 8;
            b = (color & 0x000000FF);

            gridValue[i] = r + g + b;
        }
    }

    // --------------------------------------------
    // getSquareIndex()
    // --------------------------------------------
    protected int getSquareIndex(int x, int y) {
        int squareIndex = 0;
        int offY = resX * y;
        int nextOffY = resX * (y + 1);

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

    public EdgeVertex getEdgeVertex(int index) {
        return edgeVrt[index];
    }

    public void setIsoValue(float iso) {
        this.isoValue = iso;
    }

    public int getBlobNb() {
        return blobNumber;
    }

    // --------------------------------------------
    // computeBlobs()
    // --------------------------------------------
    public void computeBlobs(final int[] pixels, final OnBlobDetectedCallback onBlobDetectedCallback) {
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                Arrays.fill(gridVisited, false);
                computeIsoValue(pixels);

                int x, y, squareIndex;
                int offset;

                nbLineToDraw = 0;
                blobNumber = 0;
                Blob newBlob = new Blob(BlobDetection.this, maxLinesPerBlob);
                for (x = 0; x < resX - 1; x++) {
                    for (y = 0; y < resY - 1; y++) {
                        offset = x + resX * y;
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
        }, "blobDetection", 100000);
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
    void computeEdgeVertex(final Blob newBlob, final int x, final int y) {
        final int offset = x + resX * y;
        if (gridVisited[offset]) {
            return;
        }

        if (newBlob.nbLine < maxLinesPerBlob) {
            gridVisited[offset] = true;
            nbLineToDraw++;
            newBlob.line[newBlob.nbLine++] = ((x) * resY + (y)) * 2;

            calculateEdgeVertex(newBlob, x, y);

            final int squareIndex = getSquareIndex(x, y);
            try {
                computeEdgeVertexInNeighbours(newBlob, x, y, MetaballsTable.neightborVoxel[squareIndex]);
            } catch (StackOverflowError error) {
                computeEdgeVertexInNeighbours(newBlob, x, y, MetaballsTable.neightborVoxel[squareIndex]);
            }
        }
    }

    private void computeEdgeVertexInNeighbours(Blob newBlob, int x, int y, byte neighborVoxel) {
        if (x < resX - 2 && (neighborVoxel & 1) == 1) {
            computeEdgeVertex(newBlob, x + 1, y);
        }
        if (x > 0 && (neighborVoxel & 2) == 2) {
            computeEdgeVertex(newBlob, x - 1, y);
        }
        if (y < resY - 2 && (neighborVoxel & 4) == 4) {
            computeEdgeVertex(newBlob, x, y + 1);
        }
        if (y > 0 && (neighborVoxel & 8) == 8) {
            computeEdgeVertex(newBlob, x, y - 1);
        }
    }

    private void calculateEdgeVertex(Blob newBlob, int x, int y) {
        int index = (x * resY + y) * 2;
        int offset = x + resX * y;
        int squareIndex = getSquareIndex(x, y);
        int toCompute = MetaballsTable.edgeToCompute[squareIndex];
        float t;
        float value;
        if ((toCompute & 1) > 0) {
            float vx = (float) x * stepX;
            t = (isoValue - gridValue[offset]) / (float) (gridValue[offset + 1] - gridValue[offset]);
            value = vx * (1.0f - t) + t * (vx + stepX);
            edgeVrt[index].x = value;

            newBlob.xMin = Math.min(value, newBlob.xMin);
            newBlob.xMax = Math.max(value, newBlob.xMax);
        }
        if ((toCompute & 2) > 0) {
            float vy = (float) y * stepY;
            t = (isoValue - gridValue[offset]) / (float) (gridValue[offset + resX] - gridValue[offset]);
            value = vy * (1.0f - t) + t * (vy + stepY);
            edgeVrt[index + 1].y = value;

            newBlob.yMin = Math.min(value, newBlob.yMin);
            newBlob.yMax = Math.max(value, newBlob.yMax);
        }
    }
}
