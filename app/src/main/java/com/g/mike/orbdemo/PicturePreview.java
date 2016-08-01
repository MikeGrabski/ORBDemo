package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * Created by iosuser11 on 8/1/16.
 */
public class PicturePreview extends SurfaceView {
    Paint paint;
    boolean drawingState;
    SurfaceHolder mHolder;

    public PicturePreview(Context context) {
        super(context);
        setZOrderMediaOverlay(true);
        //setColorFormat(TRANSLUCENT);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        paint = new Paint();
        paint.setARGB(255, 255, 0, 0);
        drawingState = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawingState) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            //canvas.drawCircle(10, 10, 10, paint);
            canvas.drawColor(Color.RED);
        }
    }

    void setDrawingState(boolean state) {
        drawingState = state;
    }
}
