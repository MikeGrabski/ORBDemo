package com.g.mike.orbdemo;

import android.graphics.SurfaceTexture;

/**
 * Created by iosuser12 on 7/22/16.
 */
public interface iCamera {
    void setupCamera(int height, int width, SurfaceTexture surfaceTexture);
    void kill();
    void releaseCamera();
    void swapCam();
    void setParameters();
    void capture(int orientation);
    void toggleFlash();
    boolean getCameraId();
    int getWidth();
    int getHeight();
    void startPreview();
    int getCameraVersion();


}
