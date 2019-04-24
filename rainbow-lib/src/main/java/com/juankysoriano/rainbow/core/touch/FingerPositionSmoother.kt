package com.juankysoriano.rainbow.core.touch

import com.juankysoriano.rainbow.core.math.RainbowMath

private const val SPRING = 0.1f
private const val DAMP = 0.6f
internal class FingerPositionSmoother {
    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var oldX: Float = 0f
        private set
    var oldY: Float = 0f
        private set
    private var xVel: Float = 0f
    private var yVel: Float = 0f

    val fingerVelocity: Float
        get() = RainbowMath.dist(0f, 0f, xVel, yVel)

    fun moveTo(x: Float, y: Float) {
        oldX = this.x
        oldY = this.y
        updateVelocity(x, y)
        updatePosition()
    }

    private fun updateVelocity(newX: Float, newY: Float) {
        val xDistance = (newX - x) * SPRING
        val yDistance = (newY - y) * SPRING

        xVel = (xVel + xDistance) * DAMP
        yVel = (yVel + yDistance) * DAMP
    }

    private fun updatePosition() {
        x += xVel
        y += yVel
    }

    fun resetTo(x: Float, y: Float) {
        this.x = x
        this.y = y
        this.oldX = x
        this.oldY = y
    }
}
