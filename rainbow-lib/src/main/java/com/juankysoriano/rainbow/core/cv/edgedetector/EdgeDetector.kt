package com.juankysoriano.rainbow.core.cv.edgedetector

import com.juankysoriano.rainbow.core.graphics.RainbowImage
import com.juankysoriano.rainbow.core.math.RVector
import com.juankysoriano.rainbow.core.math.RainbowMath

/*
 An algorithm that uses a custom implementation of a Sobel/Scharr operator to get
 the significant points of a picture.
 */

private const val SOBEL = 0
private const val SCHARR = 1

class EdgeDetector {
    private val operator = arrayOf(
            arrayOf(intArrayOf(2, 2, 0), intArrayOf(2, 0, -2), intArrayOf(0, -2, -2)), // Sobel
            arrayOf(intArrayOf(6, 10, 0), intArrayOf(10, 0, -10), intArrayOf(0, -10, -6))) // Scharr kernel
    private var op = SCHARR
    private var treshold = 140
    private var step = 4

    fun setOperator(operator: Int) {
        op = operator
    }

    fun changeTreshold(threshold: Int) {
        this.treshold = RainbowMath.constrain(threshold, 50, 2000)
    }

    fun changeStep(step: Int) {
        this.step = RainbowMath.constrain(step, 2, 20)
    }

    // This method add significant points of the given picture to a given list
    fun extractPoints(vertices: MutableList<RVector>, img: RainbowImage) {
        var col = 0
        var colSum = 0
        val w = img.width - 1
        val h = img.height - 1

        // For any pixel in the image excepting borders
        var y = 1
        while (y < h) {
            run {
                var x = 1
                while (x < w) {
                    // Convolute surrounding pixels with desired operator
                    for (y in -1..1) {
                        var x = -1
                        while (x <= 1) {
                            colSum += operator[op][x + 1][y + 1] * ((col shr 16 and 0xFF) + (col shr 8 and 0xFF) + (col and 0xFF))
                            x++
                            col = img.get(x + x, y + y)
                        }
                    }
                    // And if the resulting sum is over the treshold add pixel
                    // position to the list
                    if (RainbowMath.abs(colSum) > treshold) {
                        vertices.add(RVector(x.toFloat(), y.toFloat()))
                    }
                    x += step
                    colSum = 0
                }
            }
            y += step
        }
    }

}
