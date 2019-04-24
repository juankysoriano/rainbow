package com.juankysoriano.rainbow.demo.sketch.rainbow.forces

import com.juankysoriano.rainbow.core.Rainbow
import com.juankysoriano.rainbow.core.math.RVector

private const val NUMBER_PARTICLES = 200000
private const val NUCLEUS_DIAMETER = 40
private const val HALF = 0.5f

internal class ParticleWorld private constructor(private val nucleus: Nucleus,
                                                 private val particles: Array<Particle>,
                                                 private val points: FloatArray = FloatArray(2 * NUMBER_PARTICLES)) {

    fun updateAndDisplay(rainbow: Rainbow) {
        for (i in 0 until NUMBER_PARTICLES) {
            val (x, y) = particles[i].location
            points[i * 2] = x
            points[i * 2 + 1] = y
            particles[i].updateWith(nucleus)
        }

        rainbow.rainbowDrawer.apply {
            strokeWeight(0f)
            stroke(255, 70f)
            point(*points)
        }
    }

    fun moveNucleusTo(x: Float, y: Float) = this.nucleus.origin.set(x, y)

    companion object {
        fun create(width: Int, height: Int): ParticleWorld {
            val nucleusCoordinates = RVector(width * HALF, height * HALF)
            val nucleus = Nucleus(nucleusCoordinates, NUCLEUS_DIAMETER.toFloat())
            val particles = generateParticles(nucleus)

            return ParticleWorld(nucleus, particles)
        }

        private fun generateParticles(nucleus: Nucleus): Array<Particle> {
            val particles = ArrayList<Particle>(NUMBER_PARTICLES)
            for (i in 0 until NUMBER_PARTICLES) {
                val particle = Particle()
                particle.resetTo(nucleus)
                particles.add(particle)
            }
            return particles.toTypedArray()
        }
    }
}
