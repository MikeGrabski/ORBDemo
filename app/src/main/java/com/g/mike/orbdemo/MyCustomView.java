package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iosuser11 on 8/1/16.
 */
public class MyCustomView extends View {

    private Canvas canvas2;
    private Bitmap backingBitmap;
    private Paint paint;

    public MyCustomView(Context context) {
        super(context);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        backingBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        canvas2 = new Canvas(backingBitmap);
        paint = new Paint();
        paint.setARGB(255, 255, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(10, 10, 10, paint);
        canvas2.drawCircle(25, 25, 25, paint);
        canvas.drawBitmap(backingBitmap, 200, 90, paint);
        Log.d("MyCustomView", "onDraw: ondraw called");
    }
}
