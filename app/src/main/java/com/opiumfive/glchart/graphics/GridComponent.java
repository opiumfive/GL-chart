package com.opiumfive.glchart.graphics;

import android.opengl.GLES31;
import android.opengl.Matrix;
import com.opiumfive.glchart.ChartRenderer;
import com.opiumfive.glchart.Model;
import com.opiumfive.glchart.ViewConstants;
import com.opiumfive.glchart.transitions.SinTransition;

import java.util.Locale;

public class GridComponent {
    private final Model model;

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform float vColor;" +
            "void main() {" +
            "  gl_FragColor.rgb = vec3(241.0 / 255.0, 241.0 / 255.0, 241.0 / 255.0);" +
            "  gl_FragColor.a = vColor;" +
            "}";


    private final SpriteRenderer spriteRenderer;
    private final GlFloatBuffer buffer;
    private int mMVPMatrixHandle;
    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;

    private SinTransition transition;
    private float opacity;

    private State state = State.HIDDEN;
    private double maxValue;

    public GridComponent(SpriteRenderer spriteRenderer, Model model) {
        this.model = model;
        this.spriteRenderer = spriteRenderer;
        this.buffer = new GlFloatBuffer(24);

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        if (state == State.HIDDEN) {
            return;
        }

        GLES31.glUseProgram(mProgram);
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);
        buffer.clear();

        float i1 = (float) maxValue / 6f;
        for (int i = 0; i < 10; i++) {
            buffer.putVertex(-1f, i1 * i);
            buffer.putVertex(+1f, i1 * i);
        }

        buffer.position(0);
        buffer.bindPointer(mPositionHandle);

        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        GLES31.glUniform1f(mColorHandle, opacity);
        mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);

        float scrollFactor = (float) ViewConstants.CHART_OFFSET / height;
        float absoluteMax = (float) model.getSmoothMaxFactor();

        float yScaleFactor = 2f / absoluteMax;
        yScaleFactor *= (1f - (float) ViewConstants.CHART_OFFSET / height);

        Matrix.scaleM(identity, 0, 1f, yScaleFactor, 1f);
        Matrix.translateM(identity, 0, 0, -1/yScaleFactor, 0);
        Matrix.translateM(identity, 0, 0, 2 * scrollFactor / yScaleFactor, 0);

        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);
        GLES31.glLineWidth(4f);
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, buffer.getVertexCount());
        GLES31.glDisableVertexAttribArray(mPositionHandle);

        drawText(height, mvpMatrix);
    }

    private void drawText(int height, float[] mvpMatrix) {
        float step = (height - ViewConstants.CHART_OFFSET) / 6;

        float yPos = 0;
        for (int i = 0; i < 20; i++) {
            int y = (int) (-10 + height - ViewConstants.CHART_OFFSET - yPos * 1 / (model.getSmoothMaxFactor() / maxValue));

            if (y < 0 || y > height) {
                break;
            }
            spriteRenderer.drawString(String.format("%.0f", i * maxValue / 6f, Locale.US), 5, y, ViewConstants.VIEW_GRAY, opacity);
            yPos += step;
        }
    }

    public void show() {
        opacity = 0.0f;
        this.transition = new SinTransition(0, 1, 20);

        state = State.FADE_IN;
    }

    public void hide() {
        opacity = 1.0f;
        this.transition = new SinTransition(1, 0, 20);

        state = State.FADE_OUT;
    }

    public boolean tick() {
        if (state == State.HIDDEN || state == State.VISIBLE) {
            return false;
        }

        if (transition.tick()) {
            opacity += transition.getDelta();
        } else {
            if (state == State.FADE_OUT) {
                state = State.HIDDEN;
            } else
            if (state == State.FADE_IN) {
                state = State.VISIBLE;
            }
        }

        return true;
    }

    public void setMaxFactor(double max) {
        this.maxValue = max;
    }

    enum State {
        VISIBLE,
        FADE_IN,
        FADE_OUT,
        HIDDEN
    }
}
