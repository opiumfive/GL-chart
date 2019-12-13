package com.opiumfive.glchart;

import android.graphics.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewConstants {
    public static final DateFormat FORMATTER_WITH_DATE = new SimpleDateFormat("EEE, MMM  d", Locale.US);;
    public static final DateFormat FORMATTER = new SimpleDateFormat("MMM  d", Locale.US);;

    public static final int VIEW_GRAY = Color.parseColor("#96A2AA");
    public static final int VIEW_GRAY_GRID = Color.parseColor("#F1F1F2");

    public static final int FRAME_WIDTH_2 = 12;
    public static final int FRAME_WIDTH_1 = 4;

    public static final int SCROLL_HEIGHT = 100;
    public static final int DATE_HEIGHT = 70;
    public static final int CHART_OFFSET = DATE_HEIGHT + SCROLL_HEIGHT;

    public static final int WIDTH_LIMIT = 300;

    public static final int DATE_LABEL_HEIGHT = 25;

    public static final float[] POPUP_BACKGROUND_COLOR = new float[]{1f,1f, 1f, 1f};
    public static final float[] SCROLL_FRAME_COLOR = new float[]{ 219f / 255f, 231f/ 255f, 240f/ 255f, 1f};
    public static final float[] SCROLL_COLOR = new float[]{ 235 / 255f, 241/ 255f, 243 / 255f, 0.5f};
    public static final float[] POPUP_COLOR = new float[]{ 198 / 255f, 198/ 255f, 198 / 255f, 1f};
    public static final float[] BLACK_COLOR = new float[]{ 0, 0, 0, 1f};
    public static final float LINE_WIDTH = 6f;
    public static final float FONT_SIZE_1 = 36f;
    public static final float FONT_SIZE_2 = 52f;

    public static final float MARKER_EXTERNAL_RADIUS = 12f;
    public static final float MARKER_INNER_RADIUS = 8f;
}

