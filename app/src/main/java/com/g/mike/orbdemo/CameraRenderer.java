package com.g.mike.orbdemo;

/**
 * Created by iosuser12 on 7/22/16.
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


public class CameraRenderer extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {
    private Context mContext;
    private SurfaceTexture mSurfaceTexture;
    private final OESTexture mCameraTexture = new OESTexture();
    private Shader defaultView = new Shader();
    private int mWidth, mHeight;
    private boolean updateTexture = false;
    private ByteBuffer mFullQuadVertices;
    private float[] mTransformM = new float[16];
    private float[] mOrientationM = new float[16];
    private float[] mRatio = new float[2];
    private Camera mCamera;
    private Camera.Parameters params;
    private byte[] data;
    private boolean frameread = true;

    public CameraRenderer(Context context) {
        super(context);
        mContext = context;
        mCamera = Camera.open();
        mCamera.setPreviewCallback(this);
        final byte FULL_QUAD_COORDS[] = {-1, 1,
                -1, -1,
                1, 1,
                1, -1};
        mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);//why bytebuffor not byte[]
        mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);

        setPreserveEGLContextOnPause(true); // preserves the context when  onPause called
        setEGLContextClientVersion(2);//sets to OpenGL 2
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture){
        updateTexture = true;
        requestRender();
    }


    @Override
    public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            defaultView.setProgram(R.raw.vshader,  R.raw.fshader, mContext);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCameraTexture.init();
    }

    @Override
    public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight= height;
        SurfaceTexture oldSurfaceTexture = mSurfaceTexture;
        mSurfaceTexture = new SurfaceTexture(mCameraTexture.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        if(oldSurfaceTexture != null){
            oldSurfaceTexture.release();
        }

        int camera_width =0;
        int camera_height =0;
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(0);

        camera_width = params.getPreviewSize().width;
        camera_height = params.getPreviewSize().height;
        Matrix.setRotateM(mOrientationM, 0, 90.0f, 0f, 0f, 1f);
        mRatio[1] = camera_width*1.0f/height;
        mRatio[0] = camera_height*1.0f/width;
        requestRender();
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if(updateTexture){
            defaultView();
        }
    }

    private void defaultView(){
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformM);

        updateTexture = false;

        GLES20.glViewport(0, 0, mWidth, mHeight);

        defaultView.useProgram();

        int uTransformM = defaultView.getHandle("uTransformM");
        int uOrientationM = defaultView.getHandle("uOrientationM");
        int uRatioV = defaultView.getHandle("ratios");

        GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
        GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
        GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCameraTexture.getTextureId());

        renderQuad(defaultView.getHandle("aPosition"));
    }

    private void renderQuad(int aPosition){
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, mFullQuadVertices);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if(frameread == true) {
            frameread = false;
            data = bytes;
            frameread = true;
        }
        data = bytes;

    }

    void startPreview() {
        mCamera.startPreview();
    }

    void stopCamera() {
        mCamera.release();
    }

    byte[] getCurrentFrame() {
        return data;
    }

    int getPreviewFormat() {
        return mCamera.getParameters().getPreviewFormat();
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