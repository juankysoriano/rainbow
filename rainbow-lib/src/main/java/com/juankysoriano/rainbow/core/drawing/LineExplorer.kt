package com.juankysoriano.rainbow.core.drawing

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.PointDetectedListener
import com.juankysoriano.rainbow.core.drawing.RainbowDrawer.Precision
import com.juankysoriano.rainbow.core.math.RainbowMath

private const val NO_PREVIOUS = -1f

internal class LineExplorer {
    private var previousDetectedX = NO_PREVIOUS
    private var previousDetectedY = NO_PREVIOUS

    fun exploreLine(px: Float, py: Float, x: Float, y: Float, precision: Precision, listener: PointDetectedListener) {
        val precisionValue = precision.value.toDouble()
        previousDetectedX = x
        previousDetectedY = y
        val dx = x - px
        val dy = y - py
        if (x == px) {
            processVerticalLine(x, y, py, precisionValue, listener)
        } else {
            val k = dy / dx
            val m = y - x * k
            if (px <= x) {
                processLineGoingLeft(x, px, k, m, precisionValue, listener)
            } else {
                processLineGoingRight(x, px, k, m, precisionValue, listener)
            }
        }
    }

    private fun doProcessOnPoint(x: Float, y: Float, listener: PointDetectedListener) {
        listener.onPointDetected(previousDetectedX, previousDetectedY, x, y)
        previousDetectedX = x
        previousDetectedY = y
    }

    private fun processVerticalLine(x: Float, y: Float, py: Float, precision: Double, listener: PointDetectedListener) {
        if (py <= y) {
            processVerticalLineGoingDown(x, y, py, precision, listener)
        } else {
            processVerticalLineGoingUp(x, y, py, listener, precision)
        }
    }

    private fun processLineGoingLeft(x: Float, px: Float, k: Float, m: Float, precision: Double, listener: PointDetectedListener) {
        var i = RainbowMath.min(x, px)
        while (i <= RainbowMath.max(x, px)) {
            doProcessOnPoint(i, k * i + m, listener)
            i += (precision / RainbowMath.max(1f, RainbowMath.abs(k))).toFloat()
        }
    }

    private fun processLineGoingRight(x: Float, px: Float, k: Float, m: Float, precision: Double, listener: PointDetectedListener) {
        var i = RainbowMath.max(x, px)
        while (i >= RainbowMath.min(x, px)) {
            doProcessOnPoint(i, k * i + m, listener)
            i -= (precision / RainbowMath.max(1f, RainbowMath.abs(k))).toFloat()
        }
    }

    private fun processVerticalLineGoingDown(x: Float, y: Float, py: Float, precision: Double, listener: PointDetectedListener) {
        var i = RainbowMath.min(y, py)
        while (i <= RainbowMath.max(y, py)) {
            doProcessOnPoint(x, i, listener)
            i += precision.toFloat()
        }
    }

    private fun processVerticalLineGoingUp(x: Float, y: Float, py: Float, listener: PointDetectedListener, precision: Double) {
        var i = RainbowMath.max(y, py)
        while (i >= RainbowMath.min(y, py)) {
            doProcessOnPoint(x, i, listener)
            i -= precision.toFloat()
        }
    }
}
