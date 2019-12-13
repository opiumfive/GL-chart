package com.opiumfive.glchart.graphics;

import com.opiumfive.glchart.graphics.typewriter.Typewriter;

public final class StringSprite {
    public final int x;
    public final int y;
    public final int color;
    public final String s;
    public final float opacity;
    public final Typewriter.FontType size;

    StringSprite(int x, int y, String s, int color, float opacity, Typewriter.FontType size) {
        this.x = x;
        this.y = y;
        this.s = s;
        this.color = color;
        this.opacity = opacity;
        this.size = size;
    }
}
