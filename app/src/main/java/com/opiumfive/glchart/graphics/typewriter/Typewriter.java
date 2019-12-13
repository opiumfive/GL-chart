package com.opiumfive.glchart.graphics.typewriter;

import android.content.res.Resources;
import android.graphics.*;
import android.text.TextPaint;

import com.opiumfive.glchart.R;
import com.opiumfive.glchart.ViewConstants;

import java.util.HashMap;
import java.util.Map;

public class Typewriter {

    private final String alfabet =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "01234567890.,+-= ";

    private final Map<FontType, FontInfo> fontContexts;
    private final SpritePack spritePack;
    private final Resources resources;
    private int markerFillerId;

    private final int[] cornerIds = new int[8];


    public Typewriter(Resources resources) {
        this.resources = resources;
        this.spritePack = new SpritePack();

        fontContexts = new HashMap<>();
    }

    public void init() {
        addNormalFont();
        addBigFont();
        addBoldFont();
        addSprites();

        initTextures();
    }

    private void addSprites() {
        addMarker();

//        addCorners();
        addBorders();
    }

    private void addBorders() {
        cornerIds[0] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_left_top));
        cornerIds[1] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_top));
        cornerIds[2] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_right_top));
        cornerIds[3] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_right));
        cornerIds[4] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_right_bottom));
        cornerIds[5] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_bottom));
        cornerIds[6] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_left_bottom));
        cornerIds[7] = spritePack.put(BitmapFactory.decodeResource(resources, R.drawable.corner_left));
    }

    private void addCorners() {
        for (int i = 0; i < 4; i++) {
            cornerIds[i] = addCorner(i, R.drawable.corner_1);
        }
    }

    private int addCorner(int rot, int resourceId) {
        Bitmap bm = BitmapFactory.decodeResource(resources, resourceId);

        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        cv.rotate(rot * 90, (float) width / 2, (float) height/ 2);
        cv.drawBitmap(bm, 0, 0, null);

        return spritePack.put(b);
    }

    public int getCornerSideId(int rot) {
        return cornerIds[rot];
    }

    private void addMarker() {
        int d = (int) ViewConstants.MARKER_INNER_RADIUS * 2;
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        cv.drawCircle(d / 2, d / 2, ViewConstants.MARKER_INNER_RADIUS, paint);
        this.markerFillerId = spritePack.put(bm);
    }

    private void addNormalFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        fontContexts.put(FontType.NORMAL_FONT, new FontInfo(normalTextPaint, alfabet));
    }

    private void addBigFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_2);
        fontContexts.put(FontType.BIG_FONT, new FontInfo(normalTextPaint, alfabet));
    }

    private void addBoldFont() {
        TextPaint normalTextPaint = new TextPaint();
        normalTextPaint.setAntiAlias(true);
        normalTextPaint.setARGB(0xff, 0, 0, 0);
        normalTextPaint.setTextSize(ViewConstants.FONT_SIZE_1);
        normalTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        fontContexts.put(FontType.BOLD_FONT, new FontInfo(normalTextPaint, alfabet));
    }

    public int getTextureId() {
        return spritePack.getTextureId();
    }

    private void initTextures() {
        for (FontInfo ctx: fontContexts.values()) {
            for (int i = 0; i < alfabet.length(); i++) {
                char ch = alfabet.charAt(i);
                int charWidth = (int) Math.ceil(ctx.measureText(String.valueOf(ch)));
                int charHeight = (int) Math.ceil(ctx.fontHeight);

                Bitmap bitmap = Bitmap.createBitmap(charWidth, charHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(String.valueOf(ch), 0, charHeight - ctx.fontMetrics.descent, ctx.textPaint);

                int spriteId = spritePack.put(bitmap);
                ctx.put(ch, spriteId);
            }
        }

        spritePack.build();
    }

    public FontInfo getContext(FontType size) {
        return fontContexts.get(size);
    }

    public Sprite getSprite(int id) {
        return spritePack.get(id);
    }

    public int newMarker(int columnColor) {
        int d = (int) ViewConstants.MARKER_EXTERNAL_RADIUS * 2;
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();

        paint.setColor(columnColor);
        cv.drawCircle((float) d / 2, (float)d / 2, ViewConstants.MARKER_EXTERNAL_RADIUS, paint);

        paint.setColor(Color.WHITE);
        cv.drawCircle((float) d / 2, (float) d / 2, ViewConstants.MARKER_INNER_RADIUS, paint);

        return spritePack.put(bm);
    }

    public int getMarkerFillerId() {
        return markerFillerId;
    }

    public enum FontType {
        NORMAL_FONT,
        BOLD_FONT,
        BIG_FONT
    }
}
