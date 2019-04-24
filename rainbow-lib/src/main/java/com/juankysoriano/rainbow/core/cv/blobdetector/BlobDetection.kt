package com.juankysoriano.rainbow.core.cv.blobdetector

import com.juankysoriano.rainbow.core.graphics.RainbowImage
import com.juankysoriano.rainbow.core.schedulers.RainbowScheduler
import com.juankysoriano.rainbow.core.schedulers.RainbowSchedulers

/**
 * “It's still magic even if you know how it's done.”
 *
 *
 * - Terry Pratchet, A Hat Full of Sky
 */
private const val DEFAULT_MAX_NUMBER_OF_BLOBS = 10000

class BlobDetection private constructor(rainbowImage: RainbowImage,
                                        private val luminanceMap: LuminanceMap = LuminanceMap.from(rainbowImage),
                                        private val scheduler: RainbowScheduler =
                                                RainbowSchedulers.singleForRecursion("BlobDetection", RainbowSchedulers.Priority.NORMAL),
                                        private val maxNumberOfBlobs: Int = DEFAULT_MAX_NUMBER_OF_BLOBS) {

    private var numberOfBlobsDetected = 0
    private var skipBlobDetection: Boolean = false

    fun setThreshold(value: Float) = luminanceMap.setThreshold(value)

    fun computeBlobs(onBlobDetectedCallback: OnBlobDetectedCallback) {
        scheduler.scheduleNow(Runnable {
            luminanceMap.reset()
            detectBlobs(onBlobDetectedCallback)
            onBlobDetectedCallback.onBlobDetectionFinish()
        })
    }

    private fun detectBlobs(onBlobDetectedCallback: OnBlobDetectedCallback) {
        for (x in 0 until luminanceMap.width) {
            for (y in 0 until luminanceMap.height) {
                if (hasToDetectMoreBlobs()) {
                    skipBlobDetection = false
                    findBlobAt(x, y, onBlobDetectedCallback)
                } else {
                    return
                }
            }
        }
    }

    private fun hasToDetectMoreBlobs(): Boolean {
        return numberOfBlobsDetected < maxNumberOfBlobs
    }

    private fun findBlobAt(x: Int, y: Int, onBlobDetectedCallback: OnBlobDetectedCallback) {
        if (luminanceMap.isVisited(x, y) || !isBlobEdge(x, y)) {
            return
        }

        val newBlob = Blob()
        findVertexes(newBlob, x, y)

        if (onBlobDetectedCallback.filterBlob(newBlob)) {
            numberOfBlobsDetected++
            onBlobDetectedCallback.onBlobDetected(newBlob)
        }
    }

    private fun findVertexes(newBlob: Blob, x: Int, y: Int) {
        if (skipBlobDetection || luminanceMap.isVisited(x, y)) {
            return
        }

        luminanceMap.visit(x, y)

        if (isBlobEdge(x, y)) {
            addVertexToBlob(newBlob, x, y)
            safeExploreNeighbours(newBlob, x, y)
        }
    }

    private fun safeExploreNeighbours(newBlob: Blob, x: Int, y: Int) {
        try {
            exploreNeighbours(newBlob, x, y)
        } catch (error: StackOverflowError) {
            skipBlobDetection = true
        }

    }

    private fun addVertexToBlob(newBlob: Blob, x: Int, y: Int) {
        val edgeX = x / luminanceMap.width.toFloat()
        val edgeY = y / luminanceMap.height.toFloat()
        newBlob.addEdgeVertex(EdgeVertex(edgeX, edgeY))
    }

    private fun exploreNeighbours(newBlob: Blob, x: Int, y: Int) {
        findVertexes(newBlob, x - 1, y)
        findVertexes(newBlob, x + 1, y)
        findVertexes(newBlob, x, y - 1)
        findVertexes(newBlob, x, y + 1)
    }

    private fun isBlobEdge(x: Int, y: Int): Boolean {
        val isLeftPixelInsideBlob = luminanceMap.isInsideBlob(x - 1, y)
        val isRightPixelInsideBlob = luminanceMap.isInsideBlob(x + 1, y)
        val isUpPixelInsideBlob = luminanceMap.isInsideBlob(x, y - 1)
        val isDownPixelInsideBlob = luminanceMap.isInsideBlob(x, y + 1)
        val allNeighboursInsideBlob = isLeftPixelInsideBlob && isRightPixelInsideBlob && isUpPixelInsideBlob && isDownPixelInsideBlob
        val noNeighbourInsideBlob = !isLeftPixelInsideBlob && !isRightPixelInsideBlob && !isUpPixelInsideBlob && !isDownPixelInsideBlob

        // We have already seen if the neighbours are inside a blob.
        // If all of them are, or none of them are, then we can guarantee that coord(x, y)
        // is not a blob edge.
        return !noNeighbourInsideBlob && !allNeighboursInsideBlob
    }

    fun cancel() {
        scheduler.shutdown()
    }
}
