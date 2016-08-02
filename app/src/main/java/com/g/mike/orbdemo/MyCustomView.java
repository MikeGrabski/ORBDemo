package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iosuser11 on 8/1/16.
 */
public class MyCustomView extends View {

    private Bitmap image;
    private Paint paint;
    private Matrix mat;
    boolean drawingState;

    public MyCustomView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        image = BitmapFactory.decodeResource(getResources(), R.drawable.penguin);
        mat = new Matrix();
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawingState) {
            mat.setValues(new float[]{1,0,220,0,1,350,0,0,1});
            canvas.drawBitmap(image, mat, paint);
        }
    }

    void setDrawingState(boolean state) {
        drawingState = state;
    }

    void setTransformMatrix(Matrix matrix) {
        mat = matrix;
    }
}
