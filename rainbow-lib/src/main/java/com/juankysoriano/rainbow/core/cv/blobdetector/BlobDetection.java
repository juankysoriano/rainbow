package com.juankysoriano.rainbow.core.cv.blobdetector;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection extends EdgeDetection {
    public static int MAX_NBLINE = 4000;
    public static int MAX_NB = 1000;
    // Temp
    public int blobNumber;
    public Blob[] blob;
    public boolean[] gridVisited;
    public ThreadGroup threadGroup = new ThreadGroup("BLOB");

    public int blobWidthMin, blobHeightMin;
    // Temp
    Object parent;

    // --------------------------------------------
    // Constructor
    // --------------------------------------------
    public BlobDetection(int imgWidth, int imgHeight) {
        super(imgWidth, imgHeight);

        gridVisited = new boolean[nbGridValue];
        blob = new Blob[MAX_NB];
        blobNumber = 0;
        for (int i = 0; i < MAX_NB; i++)
            blob[i] = new Blob(this);

        blobWidthMin = 0;
        blobHeightMin = 0;
    }

    // --------------------------------------------
    // setBlobDimensionMin()
    // --------------------------------------------
    /*
     * public void setBlobDimensionMin(int w, int h) { if (w<0) w=0; if (h<0)
	 * h=0; if (w>imgWidth) w=imgWidth; if (h>imgHeight) h=imgHeight;
	 * 
	 * blobWidthMin = w; blobHeightMin = h; }
	 */

    public static void setConstants(int maxBlobNb, int maxBlobNLine) {
        MAX_NB = maxBlobNb;
        MAX_NBLINE = maxBlobNLine;
    }

    // --------------------------------------------
    // getBlob()
    // --------------------------------------------
    public Blob getBlob(int n) {
        Blob b = null;
        if (n < blobNumber) {
            return blob[n];
        }
        return b;
    }

    // --------------------------------------------
    // getBlobNb()
    // --------------------------------------------
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
                // Image

                // Clear gridVisited
                for (int i = 0; i < nbGridValue; i++)
                    gridVisited[i] = false;

                // Compute Isovalue
                computeIsovalue();

                // Get Lines indices
                int x, y, squareIndex, n;
                int offset;

                nbLineToDraw = 0;
                blobNumber = 0;
                for (x = 0; x < resx - 1; x++) {
                    for (y = 0; y < resy - 1; y++) {
                        // > offset in the grid
                        offset = x + resx * y;

                        // > if we were already there, just go the next square!
                        if (gridVisited[offset] == true) {
                            continue;
                        }

                        // > squareIndex
                        squareIndex = getSquareIndex(x, y);

                        // >Found something
                        if (squareIndex > 0 && squareIndex < 15) {
                            if (blobNumber >= 0 && blobNumber < MAX_NB) {
                                findBlob(blobNumber, x, y, onBlobDetectedCallback);
                                blobNumber++;
                            }
                        }
                    } // for y
                } // for x
                nbLineToDraw /= 2;
                onBlobDetectedCallback.onBlobDetectionFinish();
            }
        }, "blob", 100000);

        thread.start();
        // blobNumber+=1;
    }

    // --------------------------------------------
    // findBlob()
    // --------------------------------------------
    public void findBlob(int iBlob, int x, int y, OnBlobDetectedCallback onBlobDetectedCallback) {
        // Reset Blob values

        blob[iBlob].id = iBlob;
        blob[iBlob].xMin = 1000.0f;
        blob[iBlob].xMax = -1000.0f;
        blob[iBlob].yMin = 1000.0f;
        blob[iBlob].yMax = -1000.0f;
        blob[iBlob].nbLine = 0;

        // Find it !!
        computeEdgeVertex(iBlob, x, y);
        {

            // > This is just a temp patch (somtimes 'big' blobs are detected on
            // the grid edges)

            if (blob[iBlob].xMin >= 1000.0f || blob[iBlob].xMax <= -1000.0f || blob[iBlob].yMin >= 1000.0f || blob[iBlob].yMax <= -1000.0f) {
                blobNumber--;
            } else {
                blob[iBlob].update();
                // User Filter
                if (onBlobDetectedCallback != null) {
                    if (onBlobDetectedCallback.isToDiscardBlob(blob[iBlob])) {
                        blobNumber--;
                    } else {
                        onBlobDetectedCallback.onBlobDetected(blob[iBlob]);
                    }
                }
            }

        }
    }

    // --------------------------------------------
    // computeEdgeVertex()
    // --------------------------------------------
    void computeEdgeVertex(int iBlob, int x, int y) {
        // offset
        int offset = x + resx * y;
        int index = (x * resy + y) * 2;

        // Mark voxel as visited
        if (gridVisited[offset] == true) {
            return;
        }
        gridVisited[offset] = true;

        //
        int iEdge, offx, offy, offAB;
        int[] edgeOffsetInfo;
        int squareIndex = getSquareIndex(x, y);
        float vx = (float) x * stepx;
        float vy = (float) y * stepy;

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

        int toCompute = MetaballsTable.edgeToCompute[squareIndex];
        float t = 0.0f;
        float value = 0.0f;
        if (toCompute > 0) {
            if ((toCompute & 1) > 0) // Edge 0
            {
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

        } // toCompute

        // Propagate to neightbors : use of Metaballs.neighborsTable
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
};
