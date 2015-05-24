package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.matrix.RVector;

import java.util.HashSet;
import java.util.Set;

public class ParticleWorld {

    private static final int NUMBER_PARTICLES = 500;
    private static final int NUCLEUS_DIAMETER = 40;
    private static final float HALF = 0.5f;

    private final Set<Particle> particles;
    private final Nucleus nucleus;

    ParticleWorld(Nucleus nucleus, Set<Particle> particles) {
        this.nucleus = nucleus;
        this.particles = particles;
    }

    public static ParticleWorld newInstance(int width, int height) {
        RVector nucleusCoordinates = new RVector(width * HALF, height * HALF);
        Nucleus nucleus = new Nucleus(nucleusCoordinates, NUCLEUS_DIAMETER);
        Set<Particle> particles = generateParticles(nucleus, NUMBER_PARTICLES);

        return new ParticleWorld(nucleus, particles);
    }

    private static Set<Particle> generateParticles(Nucleus nucleus, int numberOfParticles) {
        Set<Particle> particles = new HashSet<Particle>(numberOfParticles);
        for (int i = 0; i < numberOfParticles; i++) {
            Particle particle = Particle.newInstance();
            particle.resetTo(nucleus);
            particles.add(particle);
        }
        return particles;
    }

    public void displayNucleus(RainbowDrawer rainbowDrawer) {
        RVector origin = nucleus.getPosition();
        rainbowDrawer.fill(0);
        rainbowDrawer.ellipse(origin.x, origin.y, nucleus.getDiameter(), nucleus.getDiameter());
    }

    public void updateAndDisplay(RainbowDrawer rainbowDrawer) {
        float[] points = new float[particles.size()*2];
        int point = 0;
        for (Particle current : particles) {
            current.updateWith(nucleus);
            RVector location = current.getLocation();
            points[point++] = location.x;
            points[point++] = location.y;
        }

        rainbowDrawer.stroke(100,60,60, 128);
        rainbowDrawer.point(points);
    }

    public void moveNucleusTo(float x, float y) {
        this.nucleus.getPosition().set(x, y, 0);
    }
}