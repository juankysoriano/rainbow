package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import android.content.res.Resources;

import com.juankysoriano.rainbow.core.drawing.RainbowDrawer;
import com.juankysoriano.rainbow.core.matrix.RVector;
import com.juankysoriano.rainbow.demo.R;
import com.juankysoriano.rainbow.demo.sketch.rainbow.LibraryApplication;
import com.juankysoriano.rainbow.utils.RainbowMath;

import static com.juankysoriano.rainbow.core.matrix.RVector.dist;
import static com.juankysoriano.rainbow.core.matrix.RVector.sub;
import static com.juankysoriano.rainbow.utils.RainbowMath.*;

public class Particle {
    private static final int[] RAINBOW = {R.color.red, R.color.orange, R.color.yellow, R.color.green, R.color.blue, R.color.purple, R.color.white};
    private static final float GRAVITY_CONSTANT = 0.0075f;
    private static final float RADIUS_FACTOR = .1f;
    private static final float MAX_PARTICLE_DIAMETER = 6;
    private static final float MAX_GRAVITY_AMPLITUDE = 0.025f;

    private float diameter;
    private RVector location;
    private RVector speed;
    private RVector gravity;
    private int color;

    Particle(int color) {
        this.color = color;
        this.location = new RVector();
        this.speed = new RVector();
        this.gravity = new RVector();
    }

    public static Particle newInstance() {
        Resources resources = LibraryApplication.getContext().getResources();
        return new Particle(resources.getColor(RAINBOW[((int) RainbowMath.random(RAINBOW.length))]));
    }

    public void updateWith(Nucleus nucleus) {
        updateGravityWith(nucleus);
        updatePosition();
    }

    private void updateGravityWith(Nucleus nucleus) {
        RVector nucleusPosition = nucleus.getPosition();
        float distanceToNucleus = dist(location, nucleusPosition);
        float gravityAmplitude = (GRAVITY_CONSTANT * nucleus.getDiameter() / pow(distanceToNucleus, 2));

        sub(nucleusPosition, location, gravity);
        gravity.mult(gravityAmplitude);
        gravity.setMag(MAX_GRAVITY_AMPLITUDE);
    }

    private void updatePosition() {
        speed.add(gravity);
        location.add(speed);
    }

    public void displayWith(RainbowDrawer rainbowDrawer) {
        diameter = max(MAX_PARTICLE_DIAMETER, location.z * RADIUS_FACTOR);
        rainbowDrawer.strokeWeight(diameter);
        rainbowDrawer.stroke(color, 100);
        rainbowDrawer.point(location.x, location.y);
    }

    public void resetTo(Nucleus nucleus) {
        RVector nucleusPosition = nucleus.getPosition();
        float alpha = random(RainbowMath.TWO_PI);
        location.set(nucleusPosition.x + (cos(alpha)), nucleusPosition.y + (sin(alpha)), nucleusPosition.z + (random(-1, 1)));
        speed.set(random(-1, 1), random(-1, 1), random(-1, 1));
        diameter = location.z * RADIUS_FACTOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Particle particle = (Particle) o;

        return color == particle.color
                && location.equals(particle.location);
    }

    @Override
    public int hashCode() {
        int result = (diameter != +0.0f ? Float.floatToIntBits(diameter) : 0);
        result = 31 * result + location.hashCode();
        result = 31 * result + color;
        return result;
    }
}

