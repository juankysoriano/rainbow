package com.juankysoriano.rainbow.demo.sketch.rainbow.detection;

import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.cv.blobdetector.Blob;
import com.juankysoriano.rainbow.core.cv.blobdetector.BlobDetection;
import com.juankysoriano.rainbow.core.cv.blobdetector.EdgeVertex;
import com.juankysoriano.rainbow.core.cv.blobdetector.OnBlobDetectedCallback;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.event.RainbowInputController;
import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.core.listeners.LoadPictureListener;
import com.juankysoriano.rainbow.demo.R;
import com.juankysoriano.rainbow.demo.sketch.rainbow.LibraryApplication;
import com.juankysoriano.rainbow.utils.RainbowMath;

public class RainbowBlobDetection extends Rainbow implements OnBlobDetectedCallback {

    private static final int RESIZE_FACTOR = 2;
    private static final float THRESHOLD_STEP = 0.1f;
    private static final int DEFAULT_ALPHA = 60;
    private static final int TOTAL_ITERATIONS = 3;
    private static final float[] MIN_DISCARD_BLOB_THRESHOLD = {400, 200, 20};
    private static final float[] MAX_DISCARD_BLOB_THRESHOLD = {1.0f, 1.0f, 1.0f};
    private int iteration;
    private RainbowImage rainbowImage;
    private BlobDetection blobDetection;
    private float detectThreshold = 0.0f;

    public RainbowBlobDetection(ViewGroup viewGroup) {
        super(viewGroup);
    }

    @Override
    public void onDrawingStart(RainbowInputController rainbowInputController) {
    }

    @Override
    public void onSketchSetup(final RainbowDrawer rainbowDrawer) {
        frameRate(30);
        rainbowDrawer.noFill();
        rainbowDrawer.loadImage(LibraryApplication.getContext(), R.drawable.rainbow2, getWidth() / RESIZE_FACTOR, getHeight() / RESIZE_FACTOR, Rainbow.LOAD_CENTER_CROP, new LoadPictureListener() {

            @Override
            public void onLoadSucceed(RainbowImage image) {
                rainbowImage = image;
                rainbowImage.loadPixels();
                BlobDetection.setConstants(300, 700);
                startNextBunchDetection();
            }

            @Override
            public void onLoadFail() {
            }
        });
    }

    private void startNextBunchDetection() {
        if(blobDetection == null) {
            blobDetection = new BlobDetection(rainbowImage.width, rainbowImage.height);
        }
        blobDetection.setThreshold(detectThreshold);
        blobDetection.computeBlobs(rainbowImage.pixels, this);
    }

    @Override
    public void onDrawingStep(RainbowDrawer rainbowDrawer, RainbowInputController rainbowInputController) {
    }

    @Override
    public void onDrawingStop(RainbowInputController rainbowInputController) {
    }

    @Override
    public void onBlobDetected(final Blob b) {
        paintBlob(b);
    }

    private void paintBlob(Blob b) {
        for (int i = 0; i < b.getEdgeNb(); i++) {
            EdgeVertex edgeA = b.getEdgeVertexA(i);
            EdgeVertex edgeB = b.getEdgeVertexB((int) RainbowMath.random(b.getEdgeNb()));
            drawLineColoredByDivisions(edgeA, edgeB, 3);
        }
    }

    @Override
    public boolean isToDiscardBlob(Blob b) {
        float blobPseudoArea = getBlobPseudoArea(b);
        return blobPseudoArea < MIN_DISCARD_BLOB_THRESHOLD[iteration];
    }

    private float getBlobPseudoArea(Blob b) {
        return RainbowMath.abs(b.xMax - b.xMin) * getWidth() * RainbowMath.abs(b.yMax - b.yMin) * getHeight();
    }

    private float getRainbowSketchArea() {
        return getWidth() * getHeight();
    }

    @Override
    public void onBlobDetectionFinish() {
        if (detectThreshold < 1.0f) {
            detectThreshold += THRESHOLD_STEP;
            startNextBunchDetection();
        } else if (iteration < TOTAL_ITERATIONS-1) {
            iteration++;
            detectThreshold = 0.0f;
            startNextBunchDetection();
        }
    }

    private void drawLineColoredByDivisions(EdgeVertex vertexA, EdgeVertex vertexB, int divisions) {
        if (divisions > 0) {
            divisions--;
            EdgeVertex vertexBetweenAB = new EdgeVertex((vertexA.x + vertexB.x) / 2, (vertexA.y + vertexB.y) / 2);
            drawLineColoredByDivisions(vertexA, vertexBetweenAB, divisions);
            drawLineColoredByDivisions(vertexBetweenAB, vertexB, divisions);
        } else {
            RainbowDrawer rainbowDrawer = getRainbowDrawer();
            int color = rainbowImage.get((int) (vertexA.x * rainbowImage.width), (int) (vertexA.y * rainbowImage.height));
            int x1 = (int) (vertexA.x * getWidth());
            int x2 = (int) (vertexB.x * getWidth());
            int y1 = (int) (vertexA.y * getHeight());
            int y2 = (int) (vertexB.y * getHeight());
            rainbowDrawer.stroke(color, DEFAULT_ALPHA);
            rainbowDrawer.line(x1, y1, x2, y2);
        }
    }
}
