package com.g.mike.orbdemo;

/**
 * Created by iosuser12 on 7/22/16.
 */
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.List;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private byte[] data;
    private boolean trueLocation = false;
    private Camera.Parameters params;

    public CameraPreview(Context context) {
        super(context);

        mCamera = Camera.open();
        params = mCamera.getParameters();

        //get the dimensions of the device screen
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);

        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            //if (size.width * size.height < params.getPreviewSize().width * params.getPreviewSize().height && size.width > displaysize.x/10 && size.height > displaysize.y/10)
            //if (size.width * size.height < params.getPreviewSize().width * params.getPreviewSize().height && Math.abs((double)size.width / (double)size.height - (double)displaysize.x / (double)displaysize.y) < 0.1)
            if (size.width * size.height < params.getPreviewSize().width * params.getPreviewSize().height) {
                params.setPreviewSize(size.width, size.height);
            }
        }
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(this);

        mHolder = getHolder();
        mHolder.addCallback(this);

        //this.setLayoutParams(new ActionBar.LayoutParams(mCamera.getParameters().getPreviewSize().height, mCamera.getParameters().getPreviewSize().width));
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
//        mCamera.stopPreview();
//
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//        } catch (Exception e){
//            Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
//        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        data = bytes;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        assert getLayoutParams() != null;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
//        lp.height = mCamera.getParameters().getPreviewSize().width;
//        lp.width = mCamera.getParameters().getPreviewSize().height;

        setLayoutParams(lp);
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

    void setTrueLocation (boolean truelocation) {
        trueLocation = truelocation;
    }

    void onPause() {
        mCamera.stopPreview();
        mCamera.release();
    }

    void onResume() {
        startPreview();
    }
}