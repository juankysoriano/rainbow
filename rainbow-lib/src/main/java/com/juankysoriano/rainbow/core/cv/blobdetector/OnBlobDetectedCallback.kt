package com.juankysoriano.rainbow.core.cv.blobdetector

interface OnBlobDetectedCallback {
    fun onBlobDetected(blob: Blob)
    fun filterBlob(blob: Blob): Boolean
    fun onBlobDetectionFinish()
}
