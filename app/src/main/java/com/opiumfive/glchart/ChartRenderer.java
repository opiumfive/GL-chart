package com.opiumfive.glchart;

import android.content.res.Resources;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import com.opiumfive.glchart.graphics.*;

import javax.microedition.khronos.opengles.GL10;

public class ChartRenderer implements GLSurfaceView.Renderer {
    private final Model model;
    private final Resources resources;

    private ScrollChartComponent scrollChartComponent;
    private ScrollRibbonComponent scrollRibbonComponent;
    private ScrollComponent mScrollComponent;
    private DateRibbonComponent mDateComponent;
    private PopupComponent mPopupComponent;
    private VBOComponent vboComponent;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private SpriteRenderer spriteRenderer;

    public ChartRenderer(Model model, Resources resources) {
        this.model = model;
        this.resources = resources;
    }

    public void onDrawFrame(GL10 unused) {
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, +6, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.translateM(mRotationMatrix, 0, 0, 0, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        boolean dirty = false;
        for (GridComponent gc: model.getGridComponents()) {
            gc.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            dirty |= gc.tick();
        }

        synchronized (Model.class) {
            mScrollComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            scrollChartComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            scrollRibbonComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            mDateComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            mPopupComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            spriteRenderer.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
//            vboComponent.draw(model.getWidth(), model.getWidth(), mViewMatrix);

            dirty |= model.tick();
            dirty |= mDateComponent.tick();
        }

        model.updateDirty(dirty);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES31.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        GLES31.glEnable(GLES31.GL_BLEND);
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);

        spriteRenderer = new SpriteRenderer(model);
        mScrollComponent = new ScrollComponent(model);
        mDateComponent = new DateRibbonComponent(spriteRenderer, model);
        scrollChartComponent = new ScrollChartComponent(model, spriteRenderer);
        scrollRibbonComponent = new ScrollRibbonComponent(model);
        mPopupComponent = new PopupComponent(model, spriteRenderer);
        vboComponent = new VBOComponent();

        model.getGridComponents().add(new GridComponent(spriteRenderer, model));
        model.getGridComponents().add(new GridComponent(spriteRenderer, model));

        model.getTypewriter().init();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, +width, height, 0, 3, 7);

        model.setWidth(width);
        model.setHeight(height);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES31.glCreateShader(type);

        GLES31.glShaderSource(shader, shaderCode);
        GLES31.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES31.glDeleteShader(shader);
            throw new RuntimeException("Could not compile program: "
                    + GLES31.glGetShaderInfoLog(shader) + " | " + shaderCode);
        }

        return shader;
    }
}