package com.g.mike.orbdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.calib3d.Calib3d.RANSAC;
import static org.opencv.calib3d.Calib3d.decomposeHomographyMat;
import static org.opencv.calib3d.Calib3d.findHomography;
import static org.opencv.imgproc.Imgproc.getAffineTransform;
import static org.opencv.imgproc.Imgproc.resize;

public class MainActivity extends Activity {
    //CameraPreview
    CameraPreview cameraPreview;
    MyCustomView myCustomView;

    //UI stuff
    Button capture;
    Button startTracking;
    Button stopTracking;
    RelativeLayout cameraView;
    TextView numOfMatches;
    TextView averageTimePerFrameTextView;
    TextView numOfFeatures;
    TextView messages;

    //Needed for matching
    byte[] currentPhoto;
    int width;
    int height;
    private int previewFormat;
    DescriptorMatcher matcher;
    FeatureDetector detector;
    DescriptorExtractor descriptor;

    //Total number of features detected in the captured image
    int featuresnumber;

    //Total number of mathces per 2 images
    int matchnumber = 0;

    //1st image descriptors and keyfeatures
    Mat img1;
    Mat descriptors1;
    MatOfKeyPoint keypoints1;

    //2nd image descriptors and keyfeautures
    Mat img2 ;
    Mat descriptors2 ;
    MatOfKeyPoint keypoints2 ;

    //needed for time measuring
    private boolean stopTrackingNow = false;
    private long elapsedTime = 0;
    private int frameCount= 0;

    MatOfDMatch features;
    MatOfDMatch matches;

    int count;

    boolean cameraPermissionGranted = false;

    Mat cameraCalib;
    private List<Mat> normals;
    private List<Mat> translations;

    float finalh[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        OpenCVLoader.initDebug();

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            cameraPermissionGranted = true;
            init();
        }
        else if(permissionCheck == PackageManager.PERMISSION_DENIED){
            cameraPermissionGranted = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }

    }
    private void init(){
        capture = (Button)findViewById(R.id.captureFrame);
        startTracking  = (Button)findViewById(R.id.startTracking);
        stopTracking = (Button)findViewById(R.id.stopTracking);
        cameraView = (RelativeLayout)findViewById(R.id.cameraView);
        cameraPreview = new CameraPreview(getApplicationContext());
        myCustomView = new MyCustomView(getApplicationContext());
        width = cameraPreview.getPreviewWidth();
        height = cameraPreview.getPreviewHeight();

        messages = (TextView)findViewById(R.id.messages);
        numOfFeatures = (TextView)findViewById(R.id.numOfFeatures);
        numOfMatches = (TextView)findViewById(R.id.numOfMatches);
        averageTimePerFrameTextView = (TextView)findViewById(R.id.averageTimePerFrame);

        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

        img1 = new Mat(width, height, CvType.CV_8UC3);
        img2 = new Mat(width, height, CvType.CV_8UC3);
        descriptors1 = new Mat();
        descriptors2 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        keypoints2 = new MatOfKeyPoint();
        features = new MatOfDMatch();
        matches = new MatOfDMatch();

        setPreviewFormat();


        messages.setText("Capture a wall.");
        cameraView.addView(cameraPreview);
        cameraPreview.startPreview();
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });
        startTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTracking();
            }
        });
        stopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTracking();
            }
        });


//        finalHomography.setValues(new float[]{1f,0f,0f,0f,1f,0f,0f,0f,1f});
//        float trainingData[][] = new float[][]{ new float[]{1, 0, 0}, new float[]{0, 1, 0}, new float[]{0, 0, 1}};
//        finalHomography = new Mat(3, 3, 6);//HxW 4x2
//        for (int i=0;i<3;i++)
//            finalHomography.put(i,0, trainingData[i]);
        finalh = new float[][]{ new float[]{1, 0, 0}, new float[]{0, 1, 0}, new float[]{0, 0, 1}};;

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setPreviewFormat(){
        List<Integer> format = cameraPreview.getSupportedPreiewFormats();
        for (int i = 0; i < format.size(); i++){
            if(format.get(i)== ImageFormat.NV21) {
                previewFormat = ImageFormat.NV21;
            }
        }
        if(previewFormat != ImageFormat.NV21)
            Toast.makeText(getApplicationContext(),"Your phone not supported yet", Toast.LENGTH_LONG).show();
        cameraPreview.setPreviewFormat(previewFormat);
    }

    private void startTracking() {
        if(averageTimePerFrameTextView.getVisibility() == Button.VISIBLE) {
            averageTimePerFrameTextView.setVisibility(Button.GONE);
        }
        numOfMatches.setVisibility(Button.VISIBLE);
        startTracking.setVisibility(Button.GONE);
        stopTracking.setVisibility(Button.VISIBLE);
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected Integer doInBackground(Void... voids) {
                matchImages();
                return  null;
            }
        }.execute();
        cameraView.addView(myCustomView);
    }
    private void stopTracking() {

        messages.setText("Tracking stopped!");

        stopTrackingNow = true;
        numOfMatches.setVisibility(Button.GONE);
        numOfFeatures.setVisibility(Button.GONE);
        stopTracking.setVisibility(Button.GONE);
        capture.setVisibility(Button.VISIBLE);
        averageTimePerFrameTextView.setVisibility(Button.VISIBLE);
        averageTimePerFrameTextView.setText("Avg Time per Frame: "+(double)elapsedTime / (double)frameCount + " ms");
        elapsedTime = 0;
        frameCount = 0;
        cameraView.removeView(myCustomView);
    }

    private void capture() {

        messages.setText("Start tracking!");

        capture.setVisibility(Button.GONE);
        averageTimePerFrameTextView.setVisibility(Button.GONE);
        startTracking.setVisibility(Button.VISIBLE);
        numOfFeatures.setVisibility(Button.VISIBLE);

        currentPhoto = cameraPreview.getCurrentFrame();

        img1.put(0,0,currentPhoto);
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

        //counting the number of features in the original captured photo
        matcher.match(descriptors1, descriptors1, features);
        List<DMatch> featuresList = features.toList();
        List<DMatch> features_final= new ArrayList<DMatch>();
        count = 0;
        for(int i=0; i<featuresList.size(); i++) {
            features_final.add(features.toList().get(i));
            count++;
        }
        featuresnumber = count;
        numOfFeatures.setText("Number of features: " + featuresnumber);
    }

    public void matchImages(){
        while(true) {
            if(stopTrackingNow) {
                stopTrackingNow = false;
                break;
            }
            long startTime = System.currentTimeMillis();
//            img1 = img2;
//            keypoints1 = keypoints2;
//            descriptors1 = descriptors2;
            byte[] data = cameraPreview.getCurrentFrame();
            img2.put(0, 0, data);
            //resize(img2, img2, img2.size(), 0, 0, 1);
            detector.detect(img2, keypoints2);
            descriptor.compute(img2, keypoints2, descriptors2);
            //matcher should include 2 different image's descriptors
            matcher.match(descriptors1, descriptors2, matches);

            int DIST_LIMIT = 50;
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final= new ArrayList<DMatch>();
            count = 0;
            for(int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= DIST_LIMIT) {
                    matches_final.add(matches.toList().get(i));
                    count++;
                }
            }
            matchnumber = count;



            //compute the transformation based on the matched features

            if(matchnumber>4) {
                List<Point> objpoints = new ArrayList<Point>();
                List<Point> scenepoints = new ArrayList<Point>();
                List<KeyPoint> keys1 = keypoints1.toList();
                List<KeyPoint> keys2 = keypoints2.toList();
                for(int i=0; i < matches_final.size(); i++) {
                    objpoints.add(keys1.get((matches_final.get(i)).queryIdx).pt);
                    scenepoints.add(keys2.get((matches_final.get(i)).trainIdx).pt);
                }
                MatOfPoint2f obj = new MatOfPoint2f();
                obj.fromList(objpoints);
                MatOfPoint2f scene = new MatOfPoint2f();
                scene.fromList(scenepoints);
                Mat homography = Calib3d.findHomography(obj, scene, RANSAC, 0.1);
                Log.d("INT", "matchImages: " +homography.type());
//                Core.gemm(homography,finalHomography, 1.0, finalHomography, 0.0, null, 0);
//                Matrix h = new Matrix();
//                h.setValues(new float[]{(float) homography.get(0,0)[0],(float) homography.get(0,1)[0],
//(float) homography.get(0,2)[0], (float) homography.get(1,0)[0], (float) homography.get(1,1)[0],
//(float) homography.get(1,2)[0], (float) homography.get(2,0)[0], (float) homography.get(2,1)[0], (float) homography.get(2,2)[0]});

                float h[][] = new float[3][3];
                for(int i = 0; i<3; i++) {
                    for(int j = 0; j<3; j++) {
                        h[i][j] = (float)homography.get(i, j)[0];
                    }
                }
                multiply(finalh, h, finalh);
                myCustomView.setTransformMatrix(finalh);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();
            elapsedTime+=(endTime-startTime);
            frameCount++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numOfMatches.setText("Number of Matches: " + matchnumber);
                    if(matchnumber > featuresnumber * 0/10) {
                        messages.setText("VERY CLOSE!");
                        myCustomView.setDrawingState(true);
                    } else {
                        messages.setText("NOT CLOSE!");
                        myCustomView.setDrawingState(false);
                    }
                    myCustomView.invalidate();
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionGranted = true;
                    init();

                } else {
                    cameraPermissionGranted = false;
                    Toast.makeText(getApplicationContext(),"This app can't work without camera, BYE!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(cameraPermissionGranted)
            cameraPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraPermissionGranted)
            cameraPreview.onPause();
    }

    public static void multiply(float[][] m1, float[][] m2, float[][] result) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j] + m1[i][2] * m2[2][j];
            }
        }
    }


}