package com.juankysoriano.rainbow.core.cv.blobdetector

interface OnBlobDetectedCallback {
    fun onBlobDetected(b: Blob)
    fun filterBlob(b: Blob): Boolean
    fun onBlobDetectionFinish()
}
