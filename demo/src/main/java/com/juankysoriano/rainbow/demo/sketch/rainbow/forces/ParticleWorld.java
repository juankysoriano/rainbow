package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.matrix.RVector;

import java.util.ArrayList;
import java.util.List;

class ParticleWorld {

    private static final int NUMBER_PARTICLES = 50;
    private static final int NUCLEUS_DIAMETER = 40;
    private static final float HALF = 0.5f;

    private final List<Particle> particles;
    private final Nucleus nucleus;
    private final float[] points;

    private ParticleWorld(Nucleus nucleus, List<Particle> particles) {
        this.nucleus = nucleus;
        this.particles = particles;
        this.points = new float[NUMBER_PARTICLES * 2];
    }

    static ParticleWorld newInstance(int width, int height) {
        RVector nucleusCoordinates = new RVector(width * HALF, height * HALF);
        Nucleus nucleus = new Nucleus(nucleusCoordinates, NUCLEUS_DIAMETER);
        List<Particle> particles = generateParticles(nucleus);

        return new ParticleWorld(nucleus, particles);
    }

    private static List<Particle> generateParticles(Nucleus nucleus) {
        List<Particle> particles = new ArrayList<>(ParticleWorld.NUMBER_PARTICLES);
        for (int i = 0; i < ParticleWorld.NUMBER_PARTICLES; i++) {
            Particle particle = Particle.newInstance();
            particle.resetTo(nucleus);
            particles.add(particle);
        }
        return particles;
    }

    void updateAndDisplay(final RainbowDrawer rainbowDrawer) {
        for (int i = 0; i < NUMBER_PARTICLES; i++) {
            setPointFor(i);
            particles.get(i).updateWith(nucleus);
        }

        drawParticles(rainbowDrawer);
        drawLinesBetweenParticles(rainbowDrawer);
    }

    private void drawParticles(RainbowDrawer rainbowDrawer) {
        rainbowDrawer.strokeWeight(Particle.PARTICLE_DIAMETER);
        rainbowDrawer.stroke(200, 10, 10, 60);
        rainbowDrawer.point(points);
    }

    private void drawLinesBetweenParticles(RainbowDrawer rainbowDrawer) {
        rainbowDrawer.strokeWeight(1);
        rainbowDrawer.stroke(35, 3, 3, 60);
        rainbowDrawer.line(points);
    }

    private void setPointFor(int particleIndex) {
        RVector particleLocation = particles.get(particleIndex).getLocation();
        points[particleIndex * 2] = particleLocation.x;
        points[particleIndex * 2 + 1] = particleLocation.y;
    }

    void moveNucleusTo(float x, float y) {
        this.nucleus.getPosition().set(x, y, 0);
    }
}
