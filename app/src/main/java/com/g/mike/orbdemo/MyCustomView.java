package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iosuser11 on 8/1/16.
 */
public class MyCustomView extends View {

    private Paint paint;
    boolean drawingState;

    public MyCustomView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        paint = new Paint();
        paint.setARGB(255, 255, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawingState) {
            canvas.drawCircle(300, 300, 10, paint);
        }
    }

    void setDrawingState(boolean state) {
        drawingState = state;
    }
}
