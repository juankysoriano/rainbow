package com.juankysoriano.rainbow.demo.sketch.rainbow.forces;

import com.juankysoriano.rainbow.core.matrix.RVector;

public class Nucleus {
    RVector origin;
    float diameter;

    public Nucleus(RVector origin, float diameter) {
        this.origin = origin;
        this.diameter = diameter;
    }

    public RVector getPosition() {
        return origin;
    }

    public float getDiameter() {
        return diameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Nucleus nucleus = (Nucleus) o;

        return Float.compare(nucleus.diameter, diameter) == 0 && origin.equals(nucleus.origin);

    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + (diameter != +0.0f ? Float.floatToIntBits(diameter) : 0);
        return result;
    }
}
