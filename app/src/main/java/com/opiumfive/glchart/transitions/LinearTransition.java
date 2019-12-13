package com.opiumfive.glchart.transitions;

public class LinearTransition implements Transition {
    private final float delta;
    private int tick;
    private final int ticks;

    public LinearTransition(float from, float to, int ticks) {
        this.ticks = ticks;
        delta = (to - from) / ticks;

        tick = 0;
    }

    @Override
    public boolean tick() {
        ++tick;
        return tick < ticks;
    }

    @Override
    public float getDelta() {
        return delta;
    }
}
