package com.g.mike.orbdemo;

/**
 * Created by iosuser12 on 7/22/16.
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;


public class CameraRenderer extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {
    private Context mContext;
    private SurfaceTexture mSurfaceTexture;
    private final OESTexture mCameraTexture = new OESTexture();
    private Shader defaultView = new Shader();
    private int mWidth, mHeight;
    private ByteBuffer mFullQuadVertices;
    private float[] mTransformM = new float[16];
    private float[] mOrientationM = new float[16];
    private float[] mRatio = new float[2];
    private Camera mCamera;
    private Camera.Parameters params;
    private byte[] data;
    private boolean frameread = true;
    private Triangle triangle;

    public CameraRenderer(Context context) {
        super(context);
        mContext = context;
        mCamera = Camera.open();
        mCamera.setPreviewCallback(this);
        final byte FULL_QUAD_COORDS[] = {-1, 1,
                -1, -1,
                1, 1,
                1, -1};
        mFullQuadVertices = ByteBuffer.allocateDirect(4 * 2);
        mFullQuadVertices.put(FULL_QUAD_COORDS).position(0);

        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        triangle = new Triangle();
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture){
        //updateTexture = true;
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
        Log.d("asdf", "onSurfaceChanged: ");

        mWidth = width;
        mHeight= height;
        mSurfaceTexture = new SurfaceTexture(mCameraTexture.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);

        int camera_width = 0;
        int camera_height = 0;
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
        defaultView();
        gl.glTranslatef(0.0f, 0.0f, -5.0f);
        triangle.draw(gl);
    }

    private void defaultView(){
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransformM);

        GLES20.glViewport(0, 0, mWidth, mHeight);

        defaultView.useProgram();

        int uTransformM = defaultView.getHandle("uTransformM");
        int uOrientationM = defaultView.getHandle("uOrientationM");
        int uRatioV = defaultView.getHandle("ratios");

        GLES20.glUniformMatrix4fv(uTransformM, 1, false, mTransformM, 0);
        GLES20.glUniformMatrix4fv(uOrientationM, 1, false, mOrientationM, 0);
        GLES20.glUniform2fv(uRatioV, 1, mRatio, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraTexture.getTextureId());

        renderQuad(defaultView.getHandle("aPosition"));
    }

    private void renderQuad(int aPosition){
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, mFullQuadVertices);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onResume() {
        //
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
        //method1: just pass the image data
        if(frameread == true) {
            frameread = false;
            data = bytes;
            frameread = true;
        }

//        //method2:
//        int width = mCamera.getParameters().getPreviewSize().width;
//        int height = mCamera.getParameters().getPreviewSize().height;
//        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, os);
//        byte[] jpegByteArray = os.toByteArray();
//        Bitmap original = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);
//        Bitmap resized = Bitmap.createScaledBitmap(original, original.getWidth()/100, original.getHeight()/100, true);
//        ByteArrayOutputStream blob = new ByteArrayOutputStream();
//        resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
//        data = blob.toByteArray();

//        if(frameread == true) {
//            frameread = false;
//            Bitmap original = BitmapFactory.decodeByteArray(bytes , 0, bytes.length);
//            Bitmap resized = Bitmap.createScaledBitmap(original, original.getWidth()/100, original.getHeight()/100, true);
//            ByteArrayOutputStream blob = new ByteArrayOutputStream();
//            resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
//            data = blob.toByteArray();
//            frameread = true;
//        }     //returns null bitmap as the byte array was not in jpeg format
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