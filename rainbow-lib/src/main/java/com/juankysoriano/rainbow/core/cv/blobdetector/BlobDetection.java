package com.juankysoriano.rainbow.core.cv.blobdetector;

import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.core.matrix.RVector;
import com.juankysoriano.rainbow.utils.RainbowMath;

//==================================================
//class BlobDetection
//==================================================
public class BlobDetection {
    private static final int DEFAULT_MAX_LINES_PER_BLOB = 4000;
    private static final int DEFAULT_MAX_NUMBER_OF_BLOBS = 1000;
    private static final float MAX_ISO_VALUE = 3.0f * 255.0f;

    private final ThreadGroup threadGroup = new ThreadGroup("BLOB");

    private final EdgeVertex[] edges;
    private final int maxNumberOfBlobs;
    private final int maxLinesPerBlob;
    private final float stepX, stepY;
    private final Grid grid;
    private float isoValue;
    private int blobNumber;

    public BlobDetection(RainbowImage rainbowImage) {
        this(rainbowImage, DEFAULT_MAX_NUMBER_OF_BLOBS, DEFAULT_MAX_LINES_PER_BLOB);
    }

    public BlobDetection(RainbowImage rainbowImage, int maxNumberOfBlobs, int maxLinesPerBlob) {
        int width = rainbowImage.getWidth();
        int height = rainbowImage.getHeight();
        this.grid = new Grid(rainbowImage);
        this.stepX = 1.0f / ((float) (width - 1));
        this.stepY = 1.0f / ((float) (height - 1));
        this.maxNumberOfBlobs = maxNumberOfBlobs;
        this.maxLinesPerBlob = maxLinesPerBlob;
        this.edges = initEdges();
    }

    private EdgeVertex[] initEdges() {
        EdgeVertex[] edgeVrt = new EdgeVertex[2 * grid.getWidth() * grid.getHeight()];
        int n = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                int index = 2 * n;
                edgeVrt[index] = new EdgeVertex(x * stepX, y * stepY);
                edgeVrt[index + 1] = new EdgeVertex(x * stepX, y * stepY);
                n++;
            }
        }

        return edgeVrt;
    }

    public void setThreshold(float value) {
        setIsoValue(RainbowMath.constrain(value, 0.0f, 1.0f) * MAX_ISO_VALUE);
    }

    private void setIsoValue(float iso) {
        isoValue = iso;
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
        for (int x = 0; x < grid.getWidth() - 1; x++) {
            for (int y = 0; y < grid.getHeight() - 1; y++) {
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
        computeEdgeVertex(newBlob, x, y);
        newBlob.update();

        return newBlob;
    }

    private void computeEdgeVertex(final Blob newBlob, final int x, final int y) {
        if (grid.isVisited(x, y)) {
            return;
        }

        grid.visit(x, y);
        if (newBlob.getEdgeCount() < maxLinesPerBlob) {
            calculateEdgeVertex(newBlob, x, y);
            newBlob.nbLine++;
            try {
                exploreNeighbours(newBlob, x, y);
            } catch (StackOverflowError error) {
                exploreNeighbours(newBlob, x, y);
            }
        }
    }

    private void exploreNeighbours(Blob newBlob, int x, int y) {
        byte neighborVoxel = grid.getNeighbourVoxel(x, y);
        if (x < grid.getWidth() - 2 && (neighborVoxel & 1) == 1) {
            computeEdgeVertex(newBlob, x + 1, y);
        }
        if (x > 0 && (neighborVoxel & 2) == 2) {
            computeEdgeVertex(newBlob, x - 1, y);
        }
        if (y < grid.getHeight() - 2 && (neighborVoxel & 4) == 4) {
            computeEdgeVertex(newBlob, x, y + 1);
        }
        if (y > 0 && (neighborVoxel & 8) == 8) {
            computeEdgeVertex(newBlob, x, y - 1);
        }
    }

    private void calculateEdgeVertex(Blob newBlob, int x, int y) {
        int index = (x * grid.getHeight() + y) * 2;
        int offset = x + grid.getWidth() * y;
        int toCompute = grid.getEdgesToCompute(x, y);
        float t;
        float value;
        int[] gridValues = grid.getGridValues();

        newBlob.line[newBlob.nbLine] = index;

        if ((toCompute & 1) > 0) {
            float vx = (float) x * stepX;
            t = (isoValue - gridValues[offset]) / (float) (gridValues[offset + 1] - gridValues[offset]);
            value = vx * (1.0f - t) + t * (vx + stepX);
            edges[index].x = value;
            newBlob.xMin = Math.min(value, newBlob.xMin);
            newBlob.xMax = Math.max(value, newBlob.xMax);
        }

        if ((toCompute & 2) > 0) {
            float vy = (float) y * stepY;
            t = (isoValue - gridValues[offset]) / (float) (gridValues[offset + grid.getWidth()] - gridValues[offset]);
            value = vy * (1.0f - t) + t * (vy + stepY);
            edges[index + 1].y = value;

            newBlob.yMin = Math.min(value, newBlob.yMin);
            newBlob.yMax = Math.max(value, newBlob.yMax);
        }
    }

    private boolean hasToPaintMoreBlobs() {
        return blobNumber < maxNumberOfBlobs;
    }

    public void cancel() {
        threadGroup.interrupt();
    }

    public class Blob {
        private float x, y;
        private float w, h;
        private float xMin, xMax, yMin, yMax;
        private int nbLine;
        protected int[] line;

        public Blob(float x, float y) {
            this.x = x;
            this.y = y;
            line = new int[maxLinesPerBlob];
            xMin = Integer.MAX_VALUE;
            xMax = Integer.MIN_VALUE;
            yMin = Integer.MAX_VALUE;
            yMax = Integer.MIN_VALUE;
        }

        public EdgeVertex getEdgeVertexA(int iEdge) {
            return edges[line[iEdge * 2]];
        }

        public EdgeVertex getEdgeVertexB(int iEdge) {
            return edges[line[iEdge * 2 + 1]];
        }

        public int getEdgeCount() {
            return nbLine;
        }

        private void update() {
            w = (xMax - xMin);
            h = (yMax - yMin);
            x = 0.5f * (xMax + xMin);
            y = 0.5f * (yMax + yMin);
            nbLine /= 2;
        }

        public float getArea() {
            return w * h;
        }

        public RVector getCenter() {
            return new RVector(x, y);
        }
    }
}
