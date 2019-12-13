package com.opiumfive.glchart.data;

import com.opiumfive.glchart.graphics.GlFloatBuffer;

import java.util.*;

public class Column {
    private final String label;
    private final List<Double> data;
    private final double minValue;
    private final double maxValue;
    private final double division;
    private int color;

    private float opacity = 0f;
    private boolean visible;

    private float scrollYScaleFactor = 1.0f;
    private float animatedScrollYScaleFactor = 1.0f;
    private GlFloatBuffer vertexBuffer;
    private int markerSpriteId;

    public Column(String label, List<Double> data) {
        this.label = label;
        this.data = data;
        this.maxValue = Collections.max(data);
        this.minValue = Collections.min(data);

        this.division = data.get(1) - data.get(0);
    }

    public String getLabel() {
        return label;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public Iterator<Double> iterator() {
        return data.iterator();
    }

    public int size() {
        return data.size();
    }

    public Collection<Double> values() {
        return data;
    }

    public void setVisible(boolean isChecked) {
        this.visible = isChecked;
    }

    public boolean isVisible() {
        return visible;
    }

    public void incOpacity(float delta) {
        opacity += delta;
    }

    public float getOpacity() {
        return opacity;
    }

    public float getScrollYScaleFactor() {
        return scrollYScaleFactor;
    }

    public void setScrollYScaleFactor(float factor) {
        this.scrollYScaleFactor = factor;
    }

    public float getAnimatedScrollYScaleFactor() {
        return animatedScrollYScaleFactor;
    }

    public void setAnimatedScrollYScaleFactor(float animatedScrollYScaleFactor) {
        this.animatedScrollYScaleFactor = animatedScrollYScaleFactor;
    }

    public void incAnimatedScrollScaleFactor(float delta) {
        this.animatedScrollYScaleFactor += delta;
    }

    public void setVertexBuffer(GlFloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public GlFloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }
    public double getDivision() {
        return division;
    }

    public double getMaxValue(double left, double right) {
        int ix1 = (int) Math.floor(left * (data.size() - 1));
        int ix2 = (int) Math.ceil(right * (data.size() - 1));

        double v = 0;
        for (int i = Math.max(0, ix1); i < Math.min(data.size(), ix2); i++) {
            v = Math.max(v, data.get(i));
        }

        return v;
    }


    public int nearestIndex(double offset) {
        offset = clamp(offset);

        int index = (int) Math.round(offset * (data.size() - 1));
        if (index < 0) {
            index = 0;
        }

        if (index >= data.size()) {
            index = data.size() - 1;
        }

        return index;
    }

    public double getValue(int index) {
        return data.get(index);
    }

    public List<Double> slice(double left, double right) {
        left = clamp(left);
        right = clamp(right);

        List<Double> rs = new ArrayList<>();

        int ix1 = (int) Math.floor(left * (data.size() - 1));
        int ix2 = (int) Math.ceil(right * (data.size() - 1));

        for (int i = ix1; i < ix2; i++) {
            rs.add(data.get(i));
        }

        return rs;
    }

    public List<Double> sample(int k) {
        List<Double> rs = new ArrayList<>();

        int inc = (int) Math.pow(2, k);
        for (int i = 0; i < data.size(); i += inc) {
            rs.add(data.get(i));
        }

        return rs;
    }


    private double clamp(double v) {
        if (v > 1.0) {
            return 1.0;
        }
        if (v < 0) {
            return 0;
        }
        return v;
    }

    public List<Double> sample(int k, double left, double right) {
        left = clamp(left);
        right = clamp(right);

        List<Double> rs = new ArrayList<>();

        int inc = (1 << k);

        int start = (int) (Math.floor(left * (data.size() - 1) / inc) * inc);
        int end   = (int) (Math.ceil((data.size() - 1) * right));

        for (int i = start; i < end; i += inc) {
            rs.add(data.get(i));
        }

        return rs;
    }


    public List<Double> sampleHalf(int k, double left, double right) {
        List<Double> rs = new ArrayList<>();
        int inc = 1 << (k + 1);

        int start = (int) ((1 << k) + Math.floor(left * (data.size() - 1) / inc) * inc);
        int end   = (int) (Math.ceil((data.size() - 1) * right));

        start = Math.max(0, start);
        for (int i = Math.max(0, start); i < end; i += inc) {
            rs.add(data.get(i));
        }

        return rs;
    }

    public void setMarkerSpriteId(int markerSpriteId) {
        this.markerSpriteId = markerSpriteId;
    }

    public int getMarkerSpriteId() {
        return markerSpriteId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
