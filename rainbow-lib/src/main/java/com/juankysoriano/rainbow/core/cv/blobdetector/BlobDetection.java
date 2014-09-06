package com.juankysoriano.rainbow.core.cv.blobdetector;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection extends EdgeDetection {
    public static final float DEFAULT_BIG_BLOB_VALUE = 1000.0f;
    private static final String THREAD_GROUP = "Blob";
    private ThreadGroup mGroup = new ThreadGroup(THREAD_GROUP);
    public static int MAX_NBLINE = 4000;
    public static int MAX_NB = 1000;
    private int blobNumber;
    private Blob[] blob;
    private boolean[] gridVisited;

    public BlobDetection(int imgWidth, int imgHeight) {
        super(imgWidth, imgHeight);

        gridVisited = new boolean[nbGridValue];
        blob = new Blob[MAX_NB];
        blobNumber = 0;
        for (int i = 0; i < MAX_NB; i++) {
            blob[i] = new Blob(this);
        }
    }

    public static void setConstants(int maxBlobNb, int maxBlobNLine) {
        MAX_NB = maxBlobNb;
        MAX_NBLINE = maxBlobNLine;
    }

    public Blob getBlob(int n) {
        Blob b = null;
        if (n < blobNumber) {
            return blob[n];
        }
        return b;
    }

    public int getBlobNb() {
        return blobNumber;
    }

    public void computeBlobs(int[] pixels, final OnBlobDetectedCallback onBlobDetectedCallback) {
        setImage(pixels);
        clearGridVisited();
        computeIsovalue();
        nbLineToDraw = 0;
        blobNumber = 0;

        Thread t = new Thread(mGroup, new Runnable() {
            @Override
            public void run() {

                int x, y;
                int offset;
                for (x = 0; x < resx - 1; x++) {
                    for (y = 0; y < resy - 1; y++) {
                        offset = x + resx * y;
                        if (gridVisited[offset] == true) {
                            continue;
                        }
                        computeBlob(onBlobDetectedCallback, x, y);
                    }
                }
                nbLineToDraw /= 2;
                onBlobDetectedCallback.blobDetectionHasFinished();
            }
        }, "thread", 1000000);
        t.start();
    }

    private void computeBlob(OnBlobDetectedCallback onBlobDetectedCallback, int x, int y) {
        int squareIndex = getSquareIndex(x, y);

        if (squareIndex > 0 && squareIndex < 15) {
            if (blobNumber >= 0 && blobNumber < MAX_NB) {
                findBlob(blobNumber, x, y, onBlobDetectedCallback);
                blobNumber++;
            }
        }
    }

    private void clearGridVisited() {
        for (int i = 0; i < nbGridValue; i++) {
            gridVisited[i] = false;
        }
    }

    public void findBlob(int iBlob, int x, int y, OnBlobDetectedCallback onBlobDetectedCallback) {

        resetBlobValues(iBlob);

        computeEdgeVertex(iBlob, x, y);

        if (blobIsVeryBig(iBlob)) {
            blobNumber--;
        } else {
            blob[iBlob].update();

            if (onBlobDetectedCallback != null) {
                boolean discardBlob = onBlobDetectedCallback.isToDiscardBlob(blob[iBlob]);
                if (discardBlob) {
                    blobNumber--;
                } else {
                    onBlobDetectedCallback.onBlobDetected(blob[iBlob]);
                }
            }
        }

    }

    private boolean blobIsVeryBig(int iBlob) {
        return blob[iBlob].xMin >= DEFAULT_BIG_BLOB_VALUE
                || blob[iBlob].xMax <= -DEFAULT_BIG_BLOB_VALUE
                || blob[iBlob].yMin >= DEFAULT_BIG_BLOB_VALUE
                || blob[iBlob].yMax <= -DEFAULT_BIG_BLOB_VALUE;
    }

    private void resetBlobValues(int iBlob) {
        blob[iBlob].id = iBlob;
        blob[iBlob].xMin = 1000.0f;
        blob[iBlob].xMax = -1000.0f;
        blob[iBlob].yMin = 1000.0f;
        blob[iBlob].yMax = -1000.0f;
        blob[iBlob].nbLine = 0;
    }

    // --------------------------------------------
    // computeEdgeVertex()
    // --------------------------------------------
    void computeEdgeVertex(int iBlob, int x, int y) {
        int offset = x + resx * y;

        if (gridVisited[offset] == true) {
            return;
        }

        gridVisited[offset] = true;

        int iEdge, offx, offy, offAB;
        int[] edgeOffsetInfo;
        int squareIndex = getSquareIndex(x, y);

        int n = 0;
        while ((iEdge = MetaballsTable.edgeCut[squareIndex][n++]) != -1) {
            edgeOffsetInfo = MetaballsTable.edgeOffsetInfo[iEdge];
            offx = edgeOffsetInfo[0];
            offy = edgeOffsetInfo[1];
            offAB = edgeOffsetInfo[2];

            if (blob[iBlob].nbLine < MAX_NBLINE) {
                nbLineToDraw++;
                blob[iBlob].line[blob[iBlob].nbLine++] = ((x + offx) * resy + (y + offy)) * 2 + offAB;
            } else {
                return;
            }
        }

        detectEdges(iBlob, x, y);

        propagateToNeighbours(iBlob, x, y, squareIndex);

    }

    private void detectEdges(int iBlob, int x, int y) {
        int offset = x + resx * y;
        int squareIndex = getSquareIndex(x, y);
        int toCompute = MetaballsTable.edgeToCompute[squareIndex];
        float t;
        float value;
        int index = (x * resy + y) * 2;

        if (toCompute > 0) {
            if ((toCompute & 1) > 0) // Edge 0
            {
                float vx = (float) x * stepx;
                t = (isovalue - gridValue[offset]) / (gridValue[offset + 1] - gridValue[offset]);
                value = vx * (1.0f - t) + t * (vx + stepx);
                edgeVrt[index].x = value;

                if (value < blob[iBlob].xMin) {
                    blob[iBlob].xMin = value;
                }
                if (value > blob[iBlob].xMax) {
                    blob[iBlob].xMax = value;
                }
            }
            if ((toCompute & 2) > 0) // Edge 3
            {
                float vy = (float) y * stepy;
                t = (isovalue - gridValue[offset]) / (gridValue[offset + resx] - gridValue[offset]);
                value = vy * (1.0f - t) + t * (vy + stepy);
                edgeVrt[index + 1].y = value;

                if (value < blob[iBlob].yMin) {
                    blob[iBlob].yMin = value;
                }
                if (value > blob[iBlob].yMax) {
                    blob[iBlob].yMax = value;
                }

            }
        }
    }

    private void propagateToNeighbours(int iBlob, int x, int y, int squareIndex) {
        byte neighborVoxel = MetaballsTable.neightborVoxel[squareIndex];
        if (x < resx - 2 && (neighborVoxel & (1 << 0)) > 0) {
            computeEdgeVertex(iBlob, x + 1, y);
        }
        if (x > 0 && (neighborVoxel & (1 << 1)) > 0) {
            computeEdgeVertex(iBlob, x - 1, y);
        }
        if (y < resy - 2 && (neighborVoxel & (1 << 2)) > 0) {
            computeEdgeVertex(iBlob, x, y + 1);
        }
        if (y > 0 && (neighborVoxel & (1 << 3)) > 0) {
            computeEdgeVertex(iBlob, x, y - 1);
        }
    }
}
