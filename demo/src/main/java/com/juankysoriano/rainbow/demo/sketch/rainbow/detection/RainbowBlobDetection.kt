package com.juankysoriano.rainbow.demo.sketch.rainbow.detection

import android.media.MediaPlayer
import android.view.ViewGroup
import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.core.cv.blobdetector.Blob
import com.juankysoriano.rainbow.core.cv.blobdetector.BlobDetection
import com.juankysoriano.rainbow.core.cv.blobdetector.EdgeVertex
import com.juankysoriano.rainbow.core.cv.blobdetector.OnBlobDetectedCallback
import com.juankysoriano.rainbow.core.drawing.Modes
import com.juankysoriano.rainbow.core.graphics.RainbowImage
import com.juankysoriano.rainbow.core.math.RainbowMath
import com.juankysoriano.rainbow.core.schedulers.RainbowScheduler
import com.juankysoriano.rainbow.core.schedulers.RainbowSchedulers
import com.juankysoriano.rainbow.demo.R
import java.util.*

class RainbowBlobDetection(viewGroup: ViewGroup,
                           private val blobList: MutableList<Blob> = ArrayList(),
                           private val mediaPlayer: MediaPlayer = MediaPlayer.create(viewGroup.context.applicationContext, R.raw.mozart),
                           private val scheduler: RainbowScheduler = RainbowSchedulers.multiThreaded("DrawBlobs", RainbowSchedulers.Priority.NORMAL))
    : Rainbow(viewGroup), OnBlobDetectedCallback {

    private lateinit var rainbowImage: RainbowImage
    private lateinit var blobDetection: BlobDetection
    private var detectThreshold = 0.0f
    private var iteration: Int = 0
    private var painted = 0
    private val isBackgroundPainted: Boolean
        get() = painted >= 5000
    private val area
        get() = (width * height)

    override fun onSketchSetup() {
        super.onSketchSetup()
        with(rainbowDrawer) {
            smooth()
            noFill()
            background(0f, 0f, 0f)
            loadImage(
                    R.drawable.gatito,
                    width / RESIZE_FACTOR,
                    height / RESIZE_FACTOR,
                    Modes.LoadMode.LOAD_CENTER_CROP,
                    object : RainbowImage.LoadPictureListener {
                        override fun onLoadSucceed(image: RainbowImage) {
                            rainbowImage = image
                            mediaPlayer.start()
                            blobDetection = BlobDetection(rainbowImage)
                            detectThreshold = THRESHOLD_STEP[iteration]
                            startNextBunchDetection()
                        }

                        override fun onLoadFail() {
                            //no-op
                        }
                    }
            )
        }
    }

    private fun startNextBunchDetection() {
        with(blobDetection) {
            setThreshold(1.0f - detectThreshold)
            computeBlobs(this@RainbowBlobDetection)
        }
    }

    override fun onBlobDetected(blob: Blob) {
        blobList.add(blob)
    }

    override fun onDrawingStep() {
        if (isBackgroundPainted) {
            paintNextBlob()
        } else {
            paintBackgroundLines()
        }
    }

    private fun paintNextBlob() {
        val blobs = ArrayList(blobList)
        blobList.clear()
        scheduler.scheduleNow(paintBlobTask(blobs))
    }

    private fun paintBlobTask(blobs: List<Blob>): Runnable {
        return Runnable {
            for (blob in blobs) {
                paintBlob(blob)
            }
        }
    }

    private fun paintBackgroundLines() {
        for (i in 0..9) {
            val x1 = RainbowMath.random(1f)
            val y1 = RainbowMath.random(1f)
            val x2 = RainbowMath.random(1f)
            val y2 = RainbowMath.random(1f)
            val start = EdgeVertex(x1, y1)
            val end = EdgeVertex(x2, y2)
            drawLineWithDivisions(start, end, 3)
            painted++
        }
    }

    private fun paintBlob(blob: Blob) {
        for (i in 0 until blob.edgeCount / 2) {
            val start = blob.getEdgeVertex(RainbowMath.random(blob.edgeCount))
            val end = blob.getEdgeVertex(RainbowMath.random(blob.edgeCount))
            drawLineWithDivisions(start, end, 3)
        }
    }

    override fun filterBlob(blob: Blob): Boolean {
        val blobPseudoArea = getBlobArea(blob)
        val area = area
        return blobPseudoArea >= MIN_DISCARD_BLOB_THRESHOLD[iteration] * area && blobPseudoArea <= MAX_DISCARD_BLOB_THRESHOLD[iteration] * area
    }

    private fun getBlobArea(blob: Blob): Float {
        return blob.area * width.toFloat() * height.toFloat()
    }

    override fun onBlobDetectionFinish() {
        if (detectThreshold < 1.0f) {
            updateThreshold()
            startNextBunchDetection()
        } else if (iteration < MAX_ITERATIONS - 1) {
            detectThreshold = THRESHOLD_STEP[iteration]
            iteration++
            startNextBunchDetection()
        }
    }

    private fun updateThreshold() {
        detectThreshold += THRESHOLD_STEP[iteration]
        detectThreshold = RainbowMath.min(detectThreshold, 1.0f)
    }

    private fun drawLineWithDivisions(start: EdgeVertex, end: EdgeVertex, divisions: Int) {
        var pendingDivisions = divisions
        if (pendingDivisions > 0) {
            pendingDivisions--
            val middle = EdgeVertex((start.x + end.x) / 2, (start.y + end.y) / 2)
            drawLineWithDivisions(start, middle, pendingDivisions)
            drawLineWithDivisions(middle, end, pendingDivisions)
        } else {
            paintLine(start, end)
        }
    }

    private fun paintLine(start: EdgeVertex, end: EdgeVertex) {
        val rainbowDrawer = rainbowDrawer
        val color = rainbowImage!!.get((start.x * rainbowImage!!.width).toInt(), (start.y * rainbowImage!!.height).toInt())
        val x1 = (start.x * width).toInt()
        val x2 = (end.x * width).toInt()
        val y1 = (start.y * height).toInt()
        val y2 = (end.y * height).toInt()
        rainbowDrawer.stroke(color, ALPHAS[iteration].toFloat())
        rainbowDrawer.line(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
    }

    override fun onSketchDestroy() {
        releaseMediaPlayer()
        releaseBlobDetection()
        scheduler.shutdown()
    }

    private fun releaseBlobDetection() {
        blobDetection.cancel()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    companion object {
        private const val RESIZE_FACTOR = 1
        private val ALPHAS = intArrayOf(70, 40, 20)
        private const val MAX_ITERATIONS = 3
        private val THRESHOLD_STEP = floatArrayOf(0.09f / RESIZE_FACTOR, 0.045f / RESIZE_FACTOR, 0.0225f / RESIZE_FACTOR)
        private val MIN_DISCARD_BLOB_THRESHOLD = floatArrayOf(0.125f, 0.015625f, 0.00015f)
        private val MAX_DISCARD_BLOB_THRESHOLD = floatArrayOf(0.5f, 0.125f, 0.015625f)
    }
}
