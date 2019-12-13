package com.opiumfive.glchart.graphics;

import android.opengl.GLES31;
import com.opiumfive.glchart.ChartRenderer;

public class VBOComponent {
    private final int mProgram;
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
                    "}";

    private final GlFloatBuffer vertexBuffer = new GlFloatBuffer(3);
    private final int vaoId;

    public VBOComponent() {
        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);

        vertexBuffer.putVertex(-1, -1);
        vertexBuffer.putVertex(0, 1);
        vertexBuffer.putVertex(+1, -1);
        vertexBuffer.position(0);

        int vboId = vertexBuffer.createVBO();
        vaoId = vertexBuffer.createVAO(vboId, GLES31.glGetAttribLocation(mProgram, "vPosition"));
    }

    public void draw(int width, int height, float[] mViewMatrix) {
        GLES31.glUseProgram(mProgram);
        GLES31.glBindVertexArray(vaoId);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexBuffer.getVertexCount());
        GLES31.glBindVertexArray(0);
    }
}
