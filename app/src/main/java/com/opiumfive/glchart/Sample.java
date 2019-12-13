package com.opiumfive.glchart;

import com.opiumfive.glchart.graphics.typewriter.Typewriter;

import java.util.Locale;

public final class Sample {
    private final String label;
    private final double value;
    private final String stringValue;

    public Sample(String label, double value) {
        this.label = label;
        this.value = value;
        this.stringValue = String.format(Locale.US, "%.0f", value);
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }

    public String getStringValue() {
        return stringValue;
    }

    public double getWidth(Typewriter tw) {
        double labelWidth = tw.getContext(Typewriter.FontType.NORMAL_FONT).stringWidth(label);
        double valueWidth = tw.getContext(Typewriter.FontType.BIG_FONT).stringWidth(stringValue);

        return Math.max(labelWidth, valueWidth);
    }

    public double getHeight(Typewriter tw) {
        double labelHeight = tw.getContext(Typewriter.FontType.NORMAL_FONT).fontHeight;
        double valueHeight = tw.getContext(Typewriter.FontType.BIG_FONT).fontHeight;

        return labelHeight + valueHeight;
    }
}
