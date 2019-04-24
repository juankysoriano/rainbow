package com.juankysoriano.rainbow.demo.sketch.rainbow.forces

import com.juankysoriano.rainbow.core.math.RVector
import com.juankysoriano.rainbow.core.math.RVector.Companion.dist
import com.juankysoriano.rainbow.core.math.RVector.Companion.sub
import com.juankysoriano.rainbow.core.math.RainbowMath.*

private const val GRAVITY_CONSTANT = 0.035f
private const val MAX_GRAVITY_AMPLITUDE = 3f

internal data class Particle(
        val location: RVector = RVector(0f, 0f),
        private val speed: RVector = RVector(0f, 0f),
        private val gravity: RVector = RVector(0f, 0f)
) {

    fun updateWith(nucleus: Nucleus) {
        updateGravityWith(nucleus)
        updatePosition()
    }

    private fun updateGravityWith(nucleus: Nucleus) {
        val nucleusPosition = nucleus.origin
        val distanceToNucleus = dist(location, nucleusPosition)
        val gravityAmplitude = GRAVITY_CONSTANT * nucleus.diameter / pow(distanceToNucleus, 2f)

        sub(nucleusPosition, location, gravity)
        gravity.mult(gravityAmplitude)
        gravity.setMag(MAX_GRAVITY_AMPLITUDE)
    }

    private fun updatePosition() {
        speed.add(gravity)
        location.add(speed)
    }

    fun resetTo(nucleus: Nucleus) {
        val (x, y, z) = nucleus.origin
        val alpha = random(TWO_PI)
        location[x + cos(alpha), y + sin(alpha)] = z + random(-1f, 1f)
        speed[random(-1f, 1f), random(-1f, 1f)] = random(-1f, 1f)
    }
}

