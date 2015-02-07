package com.juankysoriano.rainbow.demo.sketch.rainbow.ink;

public interface StepDrawer {

    void paintStep();

    void initDrawingAt(float x, float y);

    void disable();

    void enable();
}
