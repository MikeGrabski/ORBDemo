package com.g.mike.orbdemo;

/**
 * Created by iosuser12 on 7/22/16.
 */
import android.app.ActionBar;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.List;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private byte[] data;
    private boolean frameread;
    private Camera.Parameters params;

    public CameraPreview(Context context) {
        super(context);
        frameread = true;

        mCamera = Camera.open();
        params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(this);

        mHolder = getHolder();
        mHolder.addCallback(this);

        this.setLayoutParams(new ActionBar.LayoutParams(mCamera.getParameters().getPreviewSize().height, mCamera.getParameters().getPreviewSize().width));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.d("CameraPreview", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        //mCamera.stopPreview();

        // make any resize, rotate or reformatting changes here


        /*try {
            mCamera.setPreviewDisplay(mHolder);
            //mCamera.startPreview();
        } catch (Exception e){
            Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
        }*/
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
//        if(frameread == true) {
//            frameread = false;
//            data = bytes;
//            frameread = true;
//        }
        data = bytes;

    }

    void startPreview() {
        mCamera.startPreview();
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