package com.opiumfive.glchart.graphics;

import android.opengl.GLES31;
import com.opiumfive.glchart.ChartRenderer;
import com.opiumfive.glchart.Model;
import com.opiumfive.glchart.ViewConstants;

public class ScrollRibbonComponent {
    private final Model model;

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

    private final int mProgram;
    private final GlFloatBuffer triangles;

    public ScrollRibbonComponent(Model model) {
        this.model = model;

        this.triangles = new GlFloatBuffer(13);

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        GLES31.glUseProgram(mProgram);
        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);

        float leftX = (float) (model.getScrollLeft() * width);
        float rightX = (float) (model.getScrollRight() * width);
        int topY = height - ViewConstants.SCROLL_HEIGHT;
        int bottomY = height;

        int fw1 = ViewConstants.FRAME_WIDTH_1;
        int fw2 = ViewConstants.FRAME_WIDTH_2;

        triangles.clear();
        triangles.putVertex(0, topY - fw1);
        triangles.putVertex(leftX - fw2, topY - fw1);
        triangles.putVertex(leftX - fw2, bottomY);
        triangles.putVertex(0, topY - fw1);
        triangles.putVertex(leftX - fw2, bottomY);
        triangles.putVertex(0, bottomY);
        triangles.putVertex(rightX, topY - fw1);
        triangles.putVertex(width, topY - fw1);
        triangles.putVertex(width, bottomY);
        triangles.putVertex(rightX, topY - fw1);
        triangles.putVertex(width, bottomY);
        triangles.putVertex(rightX, bottomY);
        triangles.position(0);

        triangles.bindPointer(mPositionHandle);

        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_COLOR, 0);
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, triangles.getVertexCount());
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }
}
