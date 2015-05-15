package com.juankysoriano.rainbow.demo.sketch.rainbow.detection;

import android.media.MediaPlayer;
import android.view.ViewGroup;

import com.juankysoriano.rainbow.core.Rainbow;
import com.juankysoriano.rainbow.core.cv.blobdetector.Blob;
import com.juankysoriano.rainbow.core.cv.blobdetector.BlobDetection;
import com.juankysoriano.rainbow.core.cv.blobdetector.EdgeVertex;
import com.juankysoriano.rainbow.core.cv.blobdetector.OnBlobDetectedCallback;
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.graphics.RainbowImage;
import com.juankysoriano.rainbow.demo.R;
import com.juankysoriano.rainbow.demo.sketch.rainbow.LibraryApplication;
import com.juankysoriano.rainbow.utils.RainbowMath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RainbowBlobDetection extends Rainbow implements OnBlobDetectedCallback {

    private static final int RESIZE_FACTOR = 2;
    private static final float[] THRESHOLD_STEP = {0.04f, 0.03f, 0.02f};
    private static final int DEFAULT_ALPHA = 70;
    private static final int TOTAL_ITERATIONS = 3;
    private static final float[] MIN_DISCARD_BLOB_THRESHOLD = {0.125f, 0.015625f, 0.0005f};
    private static final float[] MAX_DISCARD_BLOB_THRESHOLD = {1.0f, 0.125f, 0.015625f};
    private int iteration;
    private RainbowImage rainbowImage;
    private BlobDetection blobDetection;
    private float detectThreshold = 0.0f;
    private int painted = 0;
    private MediaPlayer mediaPlayer;
    private final List<Blob> blobList;
    private final ExecutorService executor;

    public RainbowBlobDetection(ViewGroup viewGroup) {
        super(viewGroup);
        blobList = new ArrayList<>();
        mediaPlayer = MediaPlayer.create(LibraryApplication.getContext(), R.raw.mozart);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onSketchSetup() {
        super.onSketchSetup();
        getRainbowDrawer().noFill();
        getRainbowDrawer().background(0, 0, 0);
        getRainbowDrawer().loadImage(R.drawable.gatito,
                getWidth() / RESIZE_FACTOR,
                getHeight() / RESIZE_FACTOR,
                RainbowImage.LOAD_CENTER_CROP, new RainbowImage.LoadPictureListener() {

                    @Override
                    public void onLoadSucceed(RainbowImage image) {
                        rainbowImage = image;
                        mediaPlayer.start();
                        blobDetection = new BlobDetection(rainbowImage);
                        startNextBunchDetection();
                    }

                    @Override
                    public void onLoadFail() {
                        //no-op
                    }
                });

    }

    @Override
    public void onDrawingStep() {
        if (rainbowImage != null) {
            if (painted < 5000) {
                paintBackgroundLines();
            } else {
                paintBlob();
            }
        }
    }

    private void paintBlob() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(!blobList.isEmpty()) {
                    paintBlob(blobList.remove(0));
                }
            }
        });
    }

    private void paintBackgroundLines() {
        for (int i = 0; i < 10; i++) {
            float x1 = RainbowMath.random(1f);
            float y1 = RainbowMath.random(1f);
            float x2 = RainbowMath.random(1f);
            float y2 = RainbowMath.random(1f);
            EdgeVertex edgeA = new EdgeVertex(x1, y1);
            EdgeVertex edgeB = new EdgeVertex(x2, y2);
            drawLineColoredByDivisions(edgeA, edgeB, 3);
            painted++;
        }
    }

    private void startNextBunchDetection() {
        if (blobDetection != null) {
            blobDetection.setThreshold(1.0f - detectThreshold);
            blobDetection.computeBlobs(this);
        }
    }

    @Override
    public void onBlobDetected(final Blob b) {
        blobList.add(b);
    }

    private void paintBlob(Blob b) {
        for (int i = 0; i < b.getLineCount(); i++) {
            EdgeVertex edgeA = b.getEdgeVertexA(i);
            EdgeVertex edgeB = b.getEdgeVertexB((int) RainbowMath.random(b.getLineCount()));
            drawLineColoredByDivisions(edgeA, edgeB, 2);
        }
    }

    @Override
    public boolean isToDiscardBlob(Blob b) {
        float blobPseudoArea = getBlobArea(b);
        float area = getArea();
        return blobPseudoArea < MIN_DISCARD_BLOB_THRESHOLD[iteration] * area
                || blobPseudoArea > MAX_DISCARD_BLOB_THRESHOLD[iteration] * area;
    }

    private float getBlobArea(Blob blob) {
        return blob.getArea() * getWidth() * getHeight();
    }

    private float getArea() {
        return Math.abs(getWidth() * getHeight());
    }

    @Override
    public void onBlobDetectionFinish() {
        if (detectThreshold < 1.0f) {
            detectThreshold += THRESHOLD_STEP[iteration];
            startNextBunchDetection();
        } else if (iteration < TOTAL_ITERATIONS - 1) {
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
            drawLineColorBetween(vertexA, vertexB);
        }
    }

    private synchronized void drawLineColorBetween(EdgeVertex vertexA, EdgeVertex vertexB) {
        RainbowDrawer rainbowDrawer = getRainbowDrawer();
        int color = rainbowImage.get((int) (vertexA.x * rainbowImage.width), (int) (vertexA.y * rainbowImage.height));
        int x1 = (int) (vertexA.x * getWidth());
        int x2 = (int) (vertexB.x * getWidth());
        int y1 = (int) (vertexA.y * getHeight());
        int y2 = (int) (vertexB.y * getHeight());
        rainbowDrawer.stroke(color, DEFAULT_ALPHA);
        rainbowDrawer.line(x1, y1, x2, y2);
    }

    @Override
    public void onSketchDestroy() {
        releaseMediaPlayer();
        releaseBlobDetectionIfAvailable();
        executor.shutdown();
    }

    private void releaseBlobDetectionIfAvailable() {
        if (blobDetection != null) {
            blobDetection.cancel();
            blobDetection = null;
        }
    }

    private void releaseMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
