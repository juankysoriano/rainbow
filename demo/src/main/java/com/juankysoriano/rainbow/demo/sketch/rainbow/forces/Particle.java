package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import com.juankysoriano.rainbow.core.matrix.RVector;
import com.juankysoriano.rainbow.utils.RainbowMath;

import static com.juankysoriano.rainbow.core.matrix.RVector.dist;
import static com.juankysoriano.rainbow.core.matrix.RVector.sub;
import static com.juankysoriano.rainbow.utils.RainbowMath.*;

class Particle {
    private static final float GRAVITY_CONSTANT = 0.035f;
    private static final float MAX_GRAVITY_AMPLITUDE = 3f;

    private RVector location;
    private RVector speed;
    private RVector gravity;

    Particle() {
        this.location = new RVector();
        this.speed = new RVector();
        this.gravity = new RVector();
    }

    void updateWith(Nucleus nucleus) {
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
        speed.add(gravity);;
        location.add(speed);
    }

    void resetTo(Nucleus nucleus) {
        RVector nucleusPosition = nucleus.getPosition();
        float alpha = random(RainbowMath.TWO_PI);
        location.set(nucleusPosition.x + (cos(alpha)), nucleusPosition.y + (sin(alpha)), nucleusPosition.z + (random(-1, 1)));
        speed.set(random(-1, 1), random(-1, 1), random(-1, 1));
    }

    RVector getLocation() {
        return location;
    }
}

