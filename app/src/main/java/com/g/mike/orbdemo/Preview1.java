package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by iosuser11 on 7/26/16.
 */
public class Preview1 extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener, GLSurfaceView.Renderer, Camera.PreviewCallback {

    Camera mCamera;
    SurfaceTexture mSurfaceTexture;
    private int mTextureHandle;
    byte[] data;

    public Preview1(Context context) {
        super(context);
        mCamera = Camera.open();
        mCamera.setPreviewCallback(this);
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int[] mTextureHandles = new int[1];
        GLES20.glGenTextures(1, mTextureHandles, 0);
        mTextureHandle = mTextureHandles[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureHandles[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        data = bytes;
        mSurfaceTexture.updateTexImage();
    }

    void startPreview() {
        mCamera.startPreview();
    }

    byte[] getCurrentFrame() {
        return data;
    }

    int getPreviewHeight() { return mCamera.getParameters().getPreviewSize().height; }

    int getPreviewWidth() {
        return mCamera.getParameters().getPreviewSize().width;
    }

    List<Integer> getSupportedPreiewFormats(){
        return mCamera.getParameters().getSupportedPreviewFormats();
    }

    void setPreviewFormat(int previewFormat){
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFormat(previewFormat);
        mCamera.setParameters(params);
    }
}
