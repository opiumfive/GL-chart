package com.opiumfive.glchart.graphics;

import android.graphics.Color;
import android.opengl.GLES31;
import android.opengl.Matrix;
import com.opiumfive.glchart.ChartRenderer;
import com.opiumfive.glchart.Model;
import com.opiumfive.glchart.ViewConstants;
import com.opiumfive.glchart.data.Column;

public class ChartComponent {
    private final int mProgram;
    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition; " +
        "  gl_PointSize = 10.0;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final Column yColumn;

    private Model model;

    float color[] = { 0, 0, 0, 1.0f };

    public ChartComponent(Model model, Column xColumn, Column yColumn, int c) {
        this.model = model;
        this.yColumn = yColumn;

        color[0] = Color.red(c) / 255f;
        color[1] = Color.green(c) / 255f;
        color[2] = Color.blue(c) / 255f;

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        GLES31.glUseProgram(mProgram);
        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");

        GLES31.glEnableVertexAttribArray(mPositionHandle);
        yColumn.getVertexBuffer().bindPointer(mPositionHandle);

        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        color[3] = yColumn.getOpacity();

        GLES31.glUniform4fv(mColorHandle, 1, color, 0);
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");
        float[] identity = new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);

        float scrollFactor = (float) ViewConstants.CHART_OFFSET / height;

        double absoluteMax = model.getSmoothMaxFactor();
        double maxFactor = yColumn.getMaxValue() / absoluteMax;

        float yScaleFactor = 2f;
        yScaleFactor *= (1f - 1 * (float) ViewConstants.CHART_OFFSET / height);
        yScaleFactor *= maxFactor;

        float xScaleFactor = 1f / (float) (model.getScrollRight() - model.getScrollLeft());
        Matrix.scaleM(identity, 0, xScaleFactor, yScaleFactor, 1f);
        Matrix.translateM(identity, 0, - (float) model.getScrollLeft() + (float) (1 - model.getScrollRight()), 0, 0);
        Matrix.translateM(identity, 0, 0, -1/yScaleFactor, 0);
        Matrix.translateM(identity, 0, 0, 2 * scrollFactor / yScaleFactor, 0);

        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);
        GLES31.glLineWidth(ViewConstants.LINE_WIDTH);
        GLES31.glDrawArrays(GLES31.GL_LINE_STRIP, 0, yColumn.getVertexBuffer().getVertexCount());
    }
}
