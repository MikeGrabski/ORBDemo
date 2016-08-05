package com.g.mike.orbdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iosuser11 on 8/1/16.
 */
public class MyCustomView extends View {

    private Bitmap image;
    private Paint paint;
    private Matrix mat;
    boolean drawingState;
    List<DMatch> matches_final;
    MatOfKeyPoint keypoints;
    int windowWidth;
    int windowHeight;
    int rectWidth;
    int rectHeight;
    Mat mask;
    org.opencv.core.Rect rectmask;

    public MyCustomView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        windowHeight = display.getHeight();
        windowWidth = display.getWidth();
        mat = new Matrix();
        paint = new Paint();
        matches_final= new ArrayList<DMatch>();
        drawingState = false;
        mat.setValues(new float[]{1,0,0,0,1,0,0,0,1});
        Paint paint =new Paint(Color.argb(100, 255, 0, 0));
        rectWidth = 100;
        rectHeight = 80;
        rectmask = new org.opencv.core.Rect(windowWidth/2 - rectWidth/2, windowHeight/2 - rectHeight/2, rectWidth, rectHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setMatrix(mat);
//        image = BitmapFactory.decodeResource(getResources(), R.drawable.square);
//        if(drawingState) {
//            canvas.drawBitmap(image, mat, paint);
//        }
//        for(int i = 0; i<matches_final.size(); i++) {
//            canvas.drawCircle((float)keypoints.toList().get((matches_final.get(i)).queryIdx).pt.x, (float)keypoints.toList().get((matches_final.get(i)).queryIdx).pt.y, 5, new Paint(Color.GREEN));
//        }
        paint.setStyle(Paint.Style.STROKE);
        android.graphics.Rect rect = new android.graphics.Rect(windowWidth/2 - rectWidth/2, windowHeight/2 - rectHeight/2, windowWidth/2 + rectWidth/2, windowHeight/2 + rectHeight/2);
        canvas.drawRect(rect, paint);
    }

    void setDrawingState(boolean state) {
        drawingState = state;
    }

    void setTransformMatrix(float[][] f) {
        mat.postTranslate(f[0][2], f[1][2]);
    }

    void setMatches (List<DMatch> matches_final, MatOfKeyPoint keypoints2) {
        this.matches_final = matches_final;
        this.keypoints = keypoints2;
    }

    org.opencv.core.Rect getRect() {
        return rectmask;
    }
}