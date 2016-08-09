package com.g.mike.orbdemo;
/**
 * Created by iosuser12 on 7/22/16.
 */
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import java.io.IOException;
import java.util.List;
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private byte[] data;
    private Camera.Parameters params;
    private Context mContext;
    public CameraPreview(Context context) {
        super(context);
        mContext = context;
        if(mCamera == null)
            mCamera = Camera.open();
        params = mCamera.getParameters();
        //get the dimensions of the device screen to find the optimal size for camera preview
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);

//        for (Camera.Size size : params.getSupportedPreviewSizes()) {
//            if (size.width * size.height < params.getPreviewSize().width * params.getPreviewSize().height) {
//                params.setPreviewSize(size.width, size.height);
//            }
//        }

        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(this);
        this.setLayoutParams(new ActionBar.LayoutParams(mCamera.getParameters().getPreviewSize().height, mCamera.getParameters().getPreviewSize().width));
        mHolder = getHolder();
        mHolder.addCallback(this);
        startPreview();
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

    void onPause() {
        mCamera.stopPreview();
        mCamera.release();
    }
}