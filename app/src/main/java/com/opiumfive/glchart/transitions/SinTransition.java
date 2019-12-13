package com.opiumfive.glchart.transitions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SinTransition implements Transition {
    private Iterator<Float> it;
    private float val;
    private List<Float> ts = new ArrayList<>();
    public SinTransition(float from, float to, int ticks) {
        float v = from;
        for (int i = 1; i <= ticks; i++) {
            float r = (float) (from + (-Math.cos(i * Math.PI / ticks) + 1) / 2 * (to - from));

            ts.add(r - v);
            v = r;
        }
    }

    @Override
    public float getDelta() {
        return val;
    }

    @Override
    public boolean tick() {
        if (it == null) {
            it = ts.iterator();
        }
        boolean b = it.hasNext();
        if (b) {
            val = it.next();
        }

        return b;
    }
}
