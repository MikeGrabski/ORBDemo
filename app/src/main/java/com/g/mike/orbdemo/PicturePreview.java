package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by iosuser11 on 7/21/16.
 */
public class PicturePreview extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener
{
    public PicturePreview(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }
}