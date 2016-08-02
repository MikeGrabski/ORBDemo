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

    private Paint paint;
    boolean drawingState;
    Matrix mat;

    public MyCustomView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mat = new Matrix();
        paint = new Paint();
        //paint.setAlpha(255);
//        paint.setARGB(255, 255, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawingState) {
            //canvas.drawCircle(300, 300, 10, paint);
//            Drawable d = getResources().getDrawable(R.drawable.penguin);
//            d.draw(canvas);
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.penguin);
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
