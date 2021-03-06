package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.matrix.RVector;

class ParticleWorld {

    private static final int NUMBER_PARTICLES = 100000;
    private static final int NUCLEUS_DIAMETER = 40;
    private static final float HALF = 0.5f;

    private final Particle[] particles;
    private final Nucleus nucleus;
    private final float[] points;

    static ParticleWorld newInstance(int width, int height) {
        RVector nucleusCoordinates = new RVector(width * HALF, height * HALF);
        Nucleus nucleus = new Nucleus(nucleusCoordinates, NUCLEUS_DIAMETER);
        Particle[] particles = generateParticles(nucleus);

        return new ParticleWorld(nucleus, particles);
    }

    private ParticleWorld(Nucleus nucleus, Particle[] particles) {
        this.nucleus = nucleus;
        this.particles = particles;
        this.points = new float[NUMBER_PARTICLES * 2];
    }

    private static Particle[] generateParticles(Nucleus nucleus) {
        Particle[] particles = new Particle[ParticleWorld.NUMBER_PARTICLES];
        for (int i = 0; i < ParticleWorld.NUMBER_PARTICLES; i++) {
            Particle particle = new Particle();
            particle.resetTo(nucleus);
            particles[i] = particle;
        }
        return particles;
    }

    void update() {
        for (int i = 0; i < NUMBER_PARTICLES; i++) {
            setPointFor(i);
            particles[i].updateWith(nucleus);
        }
    }

    void display(RainbowDrawer rainbowDrawer) {
        rainbowDrawer.background(0);
        rainbowDrawer.strokeWeight(0);
        rainbowDrawer.stroke(255, 70);
        rainbowDrawer.point(points);
    }

    private void setPointFor(int particleIndex) {
        RVector particleLocation = particles[particleIndex].getLocation();
        points[particleIndex * 2] = particleLocation.x;
        points[particleIndex * 2 + 1] = particleLocation.y;
    }

    void moveNucleusTo(float x, float y) {
        this.nucleus.getPosition().set(x, y, 0);
    }
}
