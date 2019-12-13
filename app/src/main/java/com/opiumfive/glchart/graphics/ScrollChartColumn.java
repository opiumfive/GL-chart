package com.opiumfive.glchart.graphics;

import android.graphics.Color;
import android.opengl.GLES31;
import android.opengl.Matrix;
import com.opiumfive.glchart.ChartRenderer;
import com.opiumfive.glchart.ViewConstants;
import com.opiumfive.glchart.data.Column;

import java.util.Collections;
import java.util.Iterator;

final class ScrollChartColumn {
    private final int mProgram;
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";


    private final Column yColumn;
    private final int vaoId;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    private int mPositionHandle = 1;

    public ScrollChartColumn(Column xColumn, Column yColumn, int c) {
        this.yColumn = yColumn;

        color[0] = Color.red(c) / 255f;
        color[1] = Color.green(c) / 255f;
        color[2] = Color.blue(c) / 255f;

        initVertexBuffer(xColumn, yColumn);

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glBindAttribLocation(mProgram, mPositionHandle, "vPosition");
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);

        GlFloatBuffer vertexBuffer = yColumn.getVertexBuffer();
        int vboId = vertexBuffer.createVBO();
        vaoId = vertexBuffer.createVAO(vboId, mPositionHandle);
    }

    private void initVertexBuffer(Column xColumn, Column yColumn) {
        Iterator<Double> itX = xColumn.iterator();
        Iterator<Double> itY = yColumn.iterator();

        double maxY = Collections.max(yColumn.values());
        double minY = 0; //Collections.min(yColumn.values());
        double deltaY = maxY - minY;

        double maxX = Collections.max(xColumn.values());
        double minX = Collections.min(xColumn.values());
        double deltaX = maxX - minX;

        double xFactor = 1.0 / deltaX;
        double yFactor = 1.0 / deltaY;

        GlFloatBuffer vertexBuffer = new GlFloatBuffer(xColumn.size());
        while (itX.hasNext()) {
            float x = (float) ((itX.next().floatValue() - minX) * xFactor) * 2f - 1f;
            float y = (float) ((itY.next().floatValue() - minY) * yFactor);

            vertexBuffer.putVertex(x, y);
        }
        vertexBuffer.position(0);

        yColumn.setVertexBuffer(vertexBuffer);
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        GLES31.glUseProgram(mProgram);
        GLES31.glBindVertexArray(vaoId);

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
        float scaleFactor = 0.8f * (float) 2 *  ViewConstants.SCROLL_HEIGHT / height * yColumn.getAnimatedScrollYScaleFactor();

        Matrix.scaleM(identity, 0, 1f, scaleFactor, 1f);
        Matrix.translateM(identity, 0, 0, (- 1f + (0.2f * ViewConstants.SCROLL_HEIGHT / height)) / scaleFactor  , 0);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        GLES31.glLineWidth(4f);
        GLES31.glDrawArrays(GLES31.GL_LINE_STRIP, 0, yColumn.getVertexBuffer().getVertexCount());
        GLES31.glBindVertexArray(0);
    }
}
