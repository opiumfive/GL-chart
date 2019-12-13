package com.opiumfive.glchart.graphics.typewriter;

import android.graphics.Paint;
import android.text.TextPaint;

import java.util.HashMap;
import java.util.Map;

public final class FontInfo {
    public final TextPaint textPaint;
    public final Paint.FontMetrics fontMetrics;
    public final float fontHeight;
    public final float fontWidth;
    public final float descent;

    /** Character -> spriteId */
    private Map<Character, Integer> characters = new HashMap<>();

    public FontInfo(TextPaint textPaint, String alfabet) {
        this.textPaint = textPaint;
        fontMetrics = textPaint.getFontMetrics();
        this.descent = fontMetrics.descent;
        fontHeight = fontMetrics.descent - fontMetrics.top;
        fontWidth = textPaint.measureText(alfabet);
    }

    public float measureText(String ch) {
        return textPaint.measureText(ch);
    }

    public void put(char ch, int spriteId) {
        characters.put(ch, spriteId);
    }

    public int get(char ch) {
        if (!characters.containsKey(ch)) {
            throw new RuntimeException("unknown character: " + ch);
        }
        return characters.get(ch);
    }

    public double stringWidth(String label) {
        return textPaint.measureText(label);
    }
}
