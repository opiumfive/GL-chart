package com.opiumfive.glchart.graphics.typewriter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLES31;
import android.opengl.GLUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * SpritePack
 */
public class SpritePack {
    /** Texture width limit */
    private static final int WIDTH_LIMIT = 600;

    /** SpriteId -> Bitmap */
    private Map<Integer, Bitmap> tempBitmaps = new HashMap<>();

    /** SpriteId -> Sprite */
    private Map<Integer, Sprite> sprites = new HashMap<>();

    /** textureId */
    private int textureId = -1;

    /**
     * Add sprite
     * @param bm sprite
     * @return spriteId
     */
    public int put(Bitmap bm) {
        int id = tempBitmaps.size() + 1;
        tempBitmaps.put(id, bm);
        return id;
    }

    public void build() {
        if (textureId != -1) {
            throw new RuntimeException("Allowed to call build() only once");
        }

        final Rect r = forEachBitmap(new BitmapConsumer() {
            @Override
            public void next(int textureId, Bitmap bm, int x1, int y1, int x2, int y2) {
                sprites.put(textureId, new Sprite(x1, y1, x2, y2));
            }
        });

        Sprite bigSprite = new Sprite(0, 0, r.w, r.h);
        sprites.put(-1, bigSprite);
        bigSprite.setTextureCoordinates(0, 0, 1, 1);

        Bitmap textureBitmap = Bitmap.createBitmap(r.w, r.h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(textureBitmap);

        forEachBitmap(new BitmapConsumer() {
            @Override
            public void next(int textureId, Bitmap bitmap, int x1, int y1, int x2, int y2) {
                Sprite ch = sprites.get(textureId);
                canvas.drawBitmap(bitmap, ch.x1, ch.y1, null);
                float u1 = (float) ch.x1 / r.w;
                float u2 = (float) ch.x2 / r.w;
                float v1 = (float) (ch.y1) / r.h;
                float v2 = (float) (ch.y2) / r.h;
                ch.setTextureCoordinates(u1, v1, u2, v2);
            }
        });

        textureId = generateTextures(textureBitmap);
        textureBitmap.recycle();

        for (Bitmap bm: tempBitmaps.values()) {
            bm.recycle();
        }
        tempBitmaps.clear();
    }


    /**
     * @return OpenGL TextureId
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * @param textureId textureId
     * @return TextureInfo
     */
    public Sprite get(int textureId) {
        return sprites.get(textureId);
    }

    private Rect forEachBitmap(BitmapConsumer bitmapConsumer) {
        int xOffset = 0;
        int yOffset = 0;

        int rowHeight = 0;
        int rowWidth = 0;

        for (Map.Entry<Integer, Bitmap> e : tempBitmaps.entrySet()) {
            int textureId = e.getKey();
            Bitmap bitmap = e.getValue();

            if (xOffset + bitmap.getWidth() > WIDTH_LIMIT) {
                xOffset = 0;
                yOffset += rowHeight;
                rowHeight = 0;
            }

            int x1 = xOffset;
            int y1 = yOffset;
            int x2 = xOffset + bitmap.getWidth();
            int y2 = yOffset + bitmap.getHeight();
            xOffset += bitmap.getWidth();

            bitmapConsumer.next(textureId, bitmap, x1, y1, x2, y2);
            if (rowHeight < bitmap.getHeight()) {
                rowHeight = bitmap.getHeight();
            }

            if (rowWidth < x2) {
                rowWidth = x2;
            }
        }

        return new Rect(rowWidth, rowHeight + yOffset);
    }

    private int generateTextures(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES31.glGenTextures(1, textures, 0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, textures[0]);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_REPEAT);
        GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_REPEAT);
        GLUtils.texImage2D(GLES31.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return textures[0];
    }

    private interface BitmapConsumer {
        void next(int spriteId, Bitmap bitmap, int x1, int y1, int x2, int y2);
    }

    private class Rect {
        final int w;
        final int h;

        private Rect(int w, int h) {
            this.w = w;
            this.h = h;
        }
    }
}
