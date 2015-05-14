package com.juankysoriano.rainbow.core.cv.blobdetector;

public interface OnBlobDetectedCallback {
    void onBlobDetected(BlobDetection.Blob b);
    boolean isToDiscardBlob(BlobDetection.Blob b);
    void onBlobDetectionFinish();
}
