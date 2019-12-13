package com.opiumfive.glchart.transitions;

public interface Transition {
    boolean tick();

    float getDelta();
}
