package com.opiumfive.glchart.graphics;

import android.graphics.Color;
import android.opengl.GLES31;
import com.opiumfive.glchart.ChartRenderer;
import com.opiumfive.glchart.Model;
import com.opiumfive.glchart.data.Column;
import com.opiumfive.glchart.graphics.typewriter.FontInfo;
import com.opiumfive.glchart.graphics.typewriter.Sprite;
import com.opiumfive.glchart.graphics.typewriter.Typewriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpriteRenderer {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 inputTextureCoordinate;" +
            "attribute vec4 color;" +
            "varying vec2 textureCoordinate;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  vColor = color;" +
            "  textureCoordinate = inputTextureCoordinate.xy;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;"+
            "uniform sampler2D videoFrame;" +
            "uniform bvec2 vFlags;" +
            "varying vec4 vColor;" +
            "varying vec2 textureCoordinate;" +
            "void main() {" +
            "  vec4 color = texture2D(videoFrame, textureCoordinate);" +
            "  if (vFlags[0]) color.rgb = vColor.rgb;" +
            "  if (vFlags[1]) color.a *= vColor.a;" +
            "  gl_FragColor = color;" +
            "}";

    private final Typewriter tw;

    private final int mPositionHandle;
    private final int mColor;
    private final int mInputTextureCoordinate;
    private final int mProgram;
    private GlFloatBuffer textureBuffer;
    private GlFloatBuffer squareBuffer;
    private GlFloatBuffer colorBuffer;

    private final List<TextureSprite> coloredSprites = new ArrayList<>();
    private final List<TextureSprite> sprites = new ArrayList<>();

    public SpriteRenderer(Model model) {
        this.tw = model.getTypewriter();

        for (Column column: model.getChart().getYColumns()) {
            int columnColor = model.getChart().getColor(column.getLabel());
            column.setMarkerSpriteId(tw.newMarker(columnColor));
        }

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mPositionHandle = 1;
        mColor = 2;
        mInputTextureCoordinate = 3;

        mProgram = GLES31.glCreateProgram();
        GLES31.glBindAttribLocation(mProgram, mPositionHandle, "vPosition");
        GLES31.glBindAttribLocation(mProgram, mColor, "color");
        GLES31.glBindAttribLocation(mProgram, mInputTextureCoordinate, "inputTextureCoordinate");

        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        GLES31.glUseProgram(mProgram);

        draw(mMVPMatrix, sprites, false);
        draw(mMVPMatrix, coloredSprites, true);
    }

    private void draw(float[] mMVPMatrix, List<TextureSprite> sprites, boolean colored) {
        prepareBuffers(sprites);
        fillBuffers(sprites);
        drawBuffer(mMVPMatrix, colored);
        sprites.clear();
    }

    public void drawSprite(int id, float x, float y, int color, float opacity) {
        coloredSprites.add(new TextureSprite(tw.getSprite(id), x, y, color, opacity));
    }

    public void drawSprite(int id, float x, float y) {
        sprites.add(new TextureSprite(tw.getSprite(id), x, y, Color.BLACK, 1f));
    }

    public void drawSprite(int id, float x, float y, float sx, float sy) {
        sprites.add(new TextureSprite(tw.getSprite(id), x, y, Color.BLACK, 1f, sx, sy));
    }

    public void drawString(String string, int x, int y, int color, float opacity) {
        drawString(string, x, y, color, opacity, Typewriter.FontType.NORMAL_FONT);
    }

    public void drawString(String string, int x, int y, int color, float opacity, Typewriter.FontType type) {
        float offset = x;
        FontInfo context = tw.getContext(type);
        for (int j = 0; j < string.length(); j++) {
            Sprite ch = tw.getSprite(context.get(string.charAt(j)));
            float width = ch.width;

            float x1 = offset, y1 = y - ch.height, x2 = offset + width, y2 = y;
            coloredSprites.add(new TextureSprite(ch, x1, y1, color, opacity));
            offset += width;
        }
    }

    private void prepareBuffers(List<TextureSprite> sprites) {
        int totalSprites = sprites.size();

        squareBuffer = new GlFloatBuffer(6 * totalSprites);
        textureBuffer = new GlFloatBuffer(6 * totalSprites);
        colorBuffer   = new GlFloatBuffer(4, 6 * totalSprites);
    }

    private void fillBuffers(List<TextureSprite> sprites) {
        for (TextureSprite tx: sprites) {
            Sprite ch = tx.character;
            int w = ch.width;
            int h = ch.height;
            float x1 = tx.x, y1 = tx.y, x2 = tx.x + w * tx.sx, y2 = tx.y + h * tx.sy;

            squareBuffer.putVertex(x1, y1);
            squareBuffer.putVertex(x2, y1);
            squareBuffer.putVertex(x1, y2);
            squareBuffer.putVertex(x2, y1);
            squareBuffer.putVertex(x2, y2);
            squareBuffer.putVertex(x1, y2);

            textureBuffer.putVertex(ch.getU1(), ch.getV1());
            textureBuffer.putVertex(ch.getU2(), ch.getV1());
            textureBuffer.putVertex(ch.getU1(), ch.getV2());
            textureBuffer.putVertex(ch.getU2(), ch.getV1());
            textureBuffer.putVertex(ch.getU2(), ch.getV2());
            textureBuffer.putVertex(ch.getU1(), ch.getV2());

            for (int k = 0; k < 6; k++) {
                float r = Color.red(tx.color) / 255.0f;
                float g = Color.green(tx.color) / 255.0f;
                float b = Color.blue(tx.color) / 255.0f;
                colorBuffer.putVertex(r, g, b, tx.opacity);
            }
        }
    }

    private void drawBuffer(float[] mMVPMatrix, boolean colored) {
        GLES31.glEnableVertexAttribArray(mPositionHandle);
        GLES31.glEnableVertexAttribArray(mColor);
        GLES31.glEnableVertexAttribArray(mInputTextureCoordinate);

        squareBuffer.position(0);
        textureBuffer.position(0);
        colorBuffer.position(0);

        squareBuffer.bindPointer(mPositionHandle);
        textureBuffer.bindPointer(mInputTextureCoordinate);
        colorBuffer.bindPointer(mColor);

        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");
        int mParams = GLES31.glGetUniformLocation(mProgram, "vFlags");

        float[] identity = Arrays.copyOf(mMVPMatrix, 16);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);
        GLES31.glUniform2ui(mParams, colored ? 1 : 0, colored ? 1 : 0);

        int mTexture = GLES31.glGetUniformLocation(mProgram, "videoFrame");
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, tw.getTextureId());
        GLES31.glUniform1i(mTexture, 0);

        if (!colored) {
            GLES31.glBlendFunc(GLES31.GL_DST_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);
        }

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, squareBuffer.getVertexCount());

        if (!colored) {
            GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);
        }

        GLES31.glDisableVertexAttribArray(mPositionHandle);
        GLES31.glDisableVertexAttribArray(mColor);
        GLES31.glDisableVertexAttribArray(mInputTextureCoordinate);
    }

    public Typewriter getTypewriter() {
        return tw;
    }

    private class TextureSprite {
        private final Sprite character;
        public final float x;
        public final float y;
        public final float sx;
        public final float sy;
        public final float opacity;
        public final int color;

        public TextureSprite(Sprite character,
                             float x,
                             float y,
                             int color,
                             float opacity,
                             float sx,
                             float sy) {
            this.color = color;
            this.character = character;
            this.x = x;
            this.y = y;
            this.opacity = opacity;
            this.sx = sx;
            this.sy = sy;
        }

        public TextureSprite(Sprite character,
                             float x,
                             float y,
                             int color,
                             float opacity) {

            this(character, x, y, color, opacity, 1f, 1f);
        }
    }

    private enum SpriteMode {
        COLOR_OPAQUE,
        COLOR,
        OPAQUE,
        NONE
    }
}

