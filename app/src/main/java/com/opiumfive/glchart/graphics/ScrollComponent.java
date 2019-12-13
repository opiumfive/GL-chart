package com.opiumfive.glchart.graphics;

import android.opengl.GLES31;
import com.opiumfive.glchart.ChartRenderer;
import com.opiumfive.glchart.Model;
import com.opiumfive.glchart.ViewConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ScrollComponent {
    private final Model model;

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;

    static final int COORDS_PER_VERTEX = 3;

    static float triangleCoords[] = new float[3 * 24];

    public ScrollComponent(Model model) {
        this.model = model;

        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        GLES31.glUseProgram(mProgram);
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.clear();

        float leftX = (float) (model.getScrollLeft() * width);
        float rightX = (float) (model.getScrollRight() * width);
        int topY = height - ViewConstants.SCROLL_HEIGHT;
        int bottomY = height;

        //-------------------- TOP
        triangleCoords[0] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[1] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[3] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[4] = topY;

        triangleCoords[6] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[7] = topY;

        triangleCoords[9] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[10] = topY;

        triangleCoords[12] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[13] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[15] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[16] = topY - ViewConstants.FRAME_WIDTH_1;

//        //------------------- RIGHT
        triangleCoords[18] = rightX;
        triangleCoords[19] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[21] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[22] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[24] = rightX;
        triangleCoords[25] = bottomY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[27] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[28] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[30] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[31] = bottomY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[33] = rightX;
        triangleCoords[34] = bottomY - ViewConstants.FRAME_WIDTH_1;
//
//        // ---------------------- BOTTOM
        triangleCoords[36] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[37] = bottomY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[39] = leftX - ViewConstants.FRAME_WIDTH_2;;
        triangleCoords[40] = bottomY;

        triangleCoords[42] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[43] = bottomY;

        triangleCoords[45] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[46] = bottomY;

        triangleCoords[48] = rightX + ViewConstants.FRAME_WIDTH_2;
        triangleCoords[49] = bottomY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[51] = leftX;
        triangleCoords[52] = bottomY - ViewConstants.FRAME_WIDTH_1;

        //-------------------------- RIGHT
        triangleCoords[54] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[55] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[57] = leftX;
        triangleCoords[58] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[60] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[61] = bottomY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[63] = leftX;
        triangleCoords[64] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[66] = leftX;
        triangleCoords[67] = bottomY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[69] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[70] = bottomY - ViewConstants.FRAME_WIDTH_1;

        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_FRAME_COLOR, 0);
        mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount);
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }
}
