package com.juankysoriano.rainbow.core.cv.blobdetector

import android.graphics.Color
import com.juankysoriano.rainbow.core.graphics.RainbowImage
import java.util.*

private const val MAX_ISO_VALUE = 3.0f * 255.0f
private const val BORDER_OFFSET = 5

internal class LuminanceMap private constructor(private val pixels: IntArray,
                                                val width: Int,
                                                val height: Int,
                                                private val insideBlobPixels: BooleanArray,
                                                private val visitedPixels: BooleanArray) {
    private var luminanceThreshold: Float = 0f

    fun reset() {
        Arrays.fill(visitedPixels, false)
        calculateInsideBlobPixels()
    }

    private fun calculateInsideBlobPixels() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val index = x + width * y
                val color = pixels[index]
                // http://www.songho.ca/dsp/luminance/luminance.html
                val luminance = (3 * (2 * Color.red(color) + 5 * Color.green(color) + Color.blue(color)) shr 3).toFloat()

                insideBlobPixels[index] = luminance <= luminanceThreshold
            }
        }
    }

    fun visit(x: Int, y: Int) {
        val offset = x + width * y
        visitedPixels[offset] = true
    }

    fun isVisited(x: Int, y: Int): Boolean {
        return isIndexOutsideGrid(x, y) || visitedPixels[x + width * y]
    }

    fun isInsideBlob(x: Int, y: Int): Boolean {
        return insideBlobPixels[x + width * y]
    }

    private fun isIndexOutsideGrid(x: Int, y: Int): Boolean {
        return x < BORDER_OFFSET || x >= width - BORDER_OFFSET || y < BORDER_OFFSET || y >= height - BORDER_OFFSET
    }

    fun setThreshold(value: Float) {
        if (value < 0 || value > 1) {
            throw IllegalArgumentException("Developer error, threshold should be a value between 0 and 1")
        }
        luminanceThreshold = value * MAX_ISO_VALUE
    }

    companion object {
        fun from(rainbowImage: RainbowImage): LuminanceMap {
            val width = rainbowImage.getWidth()
            val height = rainbowImage.getHeight()
            rainbowImage.loadPixels()
            val pixels = rainbowImage.pixels
            val luminanceValues = BooleanArray(width * height)
            val visitedPixels = BooleanArray(width * height)
            return LuminanceMap(pixels, width, height, luminanceValues, visitedPixels)
        }
    }
}
