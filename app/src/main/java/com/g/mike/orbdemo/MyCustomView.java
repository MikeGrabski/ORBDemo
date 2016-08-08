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
    int viewHeight;
    int viewWidth;
    int dx;
    int dy;
    org.opencv.core.Rect rectmask;
    public MyCustomView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        windowHeight = displaysize.y;
        windowWidth = displaysize.x;
        viewHeight = getHeight();
        viewWidth = getWidth();
        dx = (windowWidth - viewWidth)/2;
        dy = (windowHeight - viewHeight)/2;
        mat = new Matrix();
        paint = new Paint();
        matches_final= new ArrayList<DMatch>();
        drawingState = true;
        mat.setValues(new float[]{1,0,0,0,1,0,0,0,1});
        Paint paint = new Paint();
        rectWidth = 100;
        rectHeight = 100;
        rectmask = new org.opencv.core.Rect(windowWidth/2 - rectWidth/2, windowHeight/2 - rectHeight/2, rectWidth, rectHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("", "onDraw: called");
        canvas.setMatrix(mat);
//        image = BitmapFactory.decodeResource(getResources(), R.drawable.square);
//        if(drawingState) {
//            canvas.drawBitmap(image, mat, paint);
//        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(255,0,255,0));
        if(keypoints!=null) {
            for(int i = 0; i<matches_final.size()/10; i++) {
                canvas.drawCircle((float)keypoints.toList().get((matches_final.get(i)).queryIdx).pt.x, (float)keypoints.toList().get((matches_final.get(i)).queryIdx).pt.y, 5, new Paint(Color.GREEN));
                //canvas.drawCircle((float)keypoints.toList().get(i).pt.x+dx, (float)keypoints.toList().get(i).pt.y+dy, 5, paint);
            }
        }
        android.graphics.Rect rect = new android.graphics.Rect(windowWidth/2 - rectWidth/2, windowHeight/2 - rectHeight/2, windowWidth/2 + rectWidth/2, windowHeight/2 + rectHeight/2);
        //android.graphics.Rect rect = new android.graphics.Rect(0, 0, 100, 100);
        canvas.drawRect(rect, paint);
        //canvas.drawColor(Color.RED);
    }

    void setDrawingState(boolean state) {
        drawingState = state;
    }

    void setTransformMatrix(float[][] homography) {
        //decompose the homography
        mat.setValues(new float[]{1,0,0,0,1,0,0,0,1});
        //mat.postTranslate(homography[0][2], homography[1][2]);
        float[][] temp = new float[3][3];
        float[][] ident = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        multiply(ident, homography, temp);
        mat.setValues(new float[]{temp[0][0],temp[0][1],temp[0][2],temp[1][0],temp[1][1],temp[1][2],temp[2][0],temp[2][1],temp[2][2]});
        //tempmat to mat
    }

    void setMatches (List<DMatch> matches_final, MatOfKeyPoint keypoints2) {
        this.matches_final = matches_final;
        this.keypoints = keypoints2;
    }

    org.opencv.core.Rect getRect() {
        return rectmask;
    }

    public static void multiply(float[][] m1, float[][] m2, float[][] result) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j] + m1[i][2] * m2[2][j];
            }
        }
    }
}