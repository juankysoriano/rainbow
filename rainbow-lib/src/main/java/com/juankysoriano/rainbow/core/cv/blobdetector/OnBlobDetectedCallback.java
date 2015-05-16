package com.juankysoriano.rainbow.core.cv.blobdetector;

public interface OnBlobDetectedCallback {
    void onBlobDetected(Blob b);
    boolean filterBlob(Blob b);
    void onBlobDetectionFinish();
}
