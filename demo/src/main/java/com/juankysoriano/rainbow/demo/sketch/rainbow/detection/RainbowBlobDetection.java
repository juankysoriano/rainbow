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

    private static final int RESIZE_FACTOR = 1;
    private static final int DEFAULT_ALPHA = 70;
    private static final int MAX_ITERATIONS = 3;
    private static final float[] THRESHOLD_STEP = {0.09f, 0.045f, 0.0225f};
    private static final float[] MIN_DISCARD_BLOB_THRESHOLD = {0.125f, 0.015625f, 0.0005f};
    private static final float[] MAX_DISCARD_BLOB_THRESHOLD = {1.0f, 0.125f, 0.015625f};
    private float detectThreshold = 0.0f;
    private int iteration;
    private int painted = 0;
    private RainbowImage rainbowImage;
    private BlobDetection blobDetection;
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
        frameRate(200);
        getRainbowDrawer().noFill();
        getRainbowDrawer().background(0, 0, 0);
        getRainbowDrawer().loadImage(R.drawable.silvares,
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
                paintNextBlob();
            }
        }
    }

    private void paintNextBlob() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!blobList.isEmpty()) {
                    paintBlob(blobList.remove(0));
                }
            }
        });
    }

    private void paintBlob(Blob blob) {
        for (int i = 0; i < blob.getLineCount(); i++) {
            EdgeVertex start = blob.getEdgeVertexA(i);
            EdgeVertex end = blob.getEdgeVertexB(RainbowMath.random(blob.getLineCount()));
            drawLineWithDivisions(start, end, 2);
        }
    }

    private void paintBackgroundLines() {
        for (int i = 0; i < 10; i++) {
            float x1 = RainbowMath.random(1f);
            float y1 = RainbowMath.random(1f);
            float x2 = RainbowMath.random(1f);
            float y2 = RainbowMath.random(1f);
            EdgeVertex start = new EdgeVertex(x1, y1);
            EdgeVertex end = new EdgeVertex(x2, y2);
            drawLineWithDivisions(start, end, 3);
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
    public void onBlobDetectionFinish() {
        if (detectThreshold < 1.0f) {
            updateThreshold();
            startNextBunchDetection();
        } else if (iteration < MAX_ITERATIONS - 1) {
            resetThreshold();
            nextIteration();
            startNextBunchDetection();
        }
    }

    @Override
    public void onBlobDetected(final Blob b) {
        blobList.add(b);
    }

    @Override
    public boolean filterBlob(Blob blob) {
        float blobPseudoArea = getBlobArea(blob);
        float area = getArea();
        return blobPseudoArea >= MIN_DISCARD_BLOB_THRESHOLD[iteration] * area
                && blobPseudoArea <= MAX_DISCARD_BLOB_THRESHOLD[iteration] * area;
    }

    private float getBlobArea(Blob blob) {
        return blob.getArea() * getWidth() * getHeight();
    }

    private float getArea() {
        return Math.abs(getWidth() * getHeight());
    }

    private void nextIteration() {
        iteration++;
    }

    private void resetThreshold() {
        detectThreshold = 0.0f;
    }

    private void updateThreshold() {
        detectThreshold += THRESHOLD_STEP[iteration];
        detectThreshold = RainbowMath.min(detectThreshold, 1.0f);
    }

    private void drawLineWithDivisions(EdgeVertex start, EdgeVertex end, int divisions) {
        if (divisions > 0) {
            divisions--;
            EdgeVertex middle = new EdgeVertex((start.x + end.x) / 2, (start.y + end.y) / 2);
            drawLineWithDivisions(start, middle, divisions);
            drawLineWithDivisions(middle, end, divisions);
        } else {
            paintLine(start, end);
        }
    }

    private void paintLine(EdgeVertex start, EdgeVertex end) {
        RainbowDrawer rainbowDrawer = getRainbowDrawer();
        int color = rainbowImage.get((int) (start.x * rainbowImage.width), (int) (start.y * rainbowImage.height));
        int x1 = (int) (start.x * getWidth());
        int x2 = (int) (end.x * getWidth());
        int y1 = (int) (start.y * getHeight());
        int y2 = (int) (end.y * getHeight());
        rainbowDrawer.stroke(color, DEFAULT_ALPHA);
        rainbowDrawer.line(x1, y1, x2, y2);
    }

    @Override
    public void onSketchDestroy() {
        releaseMediaPlayer();
        releaseBlobDetection();
        executor.shutdown();
    }

    private void releaseBlobDetection() {
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
