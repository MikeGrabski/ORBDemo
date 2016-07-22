package com.g.mike.orbdemo;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    //UI stuff
    Button capture;
    Button startTracking;
    Button stopTracking;
    FrameLayout cameraView;
    TextView numOfMatches;
    TextView averageTimePerFrameTextView;

    //CameraPreview
    CameraRenderer cameraPreview;

    //Needed for matching
    byte[] currentPhoto;
    int width;
    int height;
    private int previewFormat;

    FeatureDetector detector;
    DescriptorExtractor descriptor;

    //Total number of mathces per 2 images
    int matchnumber;

    //1st image descriptors and keyfeatures
    DescriptorMatcher matcher;
    Mat img1;
    Mat descriptors1;
    MatOfKeyPoint keypoints1;

    //2nd image descriptors and keyfeautures
    Mat img2 ;
    Mat descriptors2 ;
    MatOfKeyPoint keypoints2 ;

    //needed for time measuring
    private boolean stopTrackingNow = false;
    private long elapsedTime=0;
    private int frameCount= 0;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!OpenCVLoader.initDebug()){
                Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        matchnumber = 0;
        capture = (Button)findViewById(R.id.captureFrame);
        startTracking  = (Button)findViewById(R.id.startTracking);
        stopTracking = (Button)findViewById(R.id.stopTracking);
        cameraView = (FrameLayout)findViewById(R.id.cameraView);
        numOfMatches = (TextView)findViewById(R.id.numOfMatches);
        averageTimePerFrameTextView = (TextView)findViewById(R.id.averageTimePerFrame);
        cameraPreview = new CameraRenderer(getApplicationContext());
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
        setPreviewFormat();
        cameraView.addView(cameraPreview);
        cameraPreview.startPreview();
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
        if(currentPhoto==null)
        {
            Toast.makeText(getApplicationContext(),"Please Capture First!",Toast.LENGTH_SHORT).show();
            return;
        }
        startTracking.setVisibility(Button.GONE);
        stopTracking.setVisibility(Button.VISIBLE);
        MatchImages matchImages = new MatchImages();
        matchImages.execute();
    }
    private void stopTracking() {

        stopTrackingNow = true;
        averageTimePerFrameTextView.setText("Avg Time per Frame: "+(double)elapsedTime / (double)frameCount + " ms");
        stopTracking.setVisibility(Button.GONE);

        capture.setVisibility(Button.VISIBLE);
        elapsedTime = 0;
        frameCount = 0;
    }

    private void capture() {

        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

        width = cameraPreview.getPreviewWidth();
        height = cameraPreview.getPreviewHeight();
        //first image
        currentPhoto = cameraPreview.getCurrentFrame();

        img1 = new Mat(width, height, CvType.CV_8UC3);
        img1.put(0,0,currentPhoto);
        descriptors1 = new Mat();

        img2 = new Mat(width, height, CvType.CV_8UC3);
        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();


        keypoints1 = new MatOfKeyPoint();

        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);
        startTracking.setVisibility(Button.VISIBLE);
        capture.setVisibility(Button.GONE);
    }
    //change
    private class MatchImages extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... voids) {

            matchImages();
            return  null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            numOfMatches.setText("NumOfMatches: " + matchnumber);
        }
    }
    public void matchImages(){
        while(true) {
            if(stopTrackingNow) {
                stopTrackingNow = false;
                break;
            }
            long startTime = System.currentTimeMillis();
            byte[] data = cameraPreview.getCurrentFrame();
            img2.put(0, 0, data);
            detector.detect(img2, keypoints2);
            descriptor.compute(img2, keypoints2, descriptors2);
            //matcher should include 2 different image's descriptors
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors1, descriptors2, matches);


            int DIST_LIMIT = 60;
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final= new ArrayList<DMatch>();
            int count = 0;
            for(int i=0; i<matchesList.size(); i++) {
                if (matchesList.get(i).distance <= DIST_LIMIT) {
                    matches_final.add(matches.toList().get(i));
                    count++;
                }
            }
            matchnumber = count;
            long endTime = System.currentTimeMillis();
            elapsedTime+=(endTime-startTime);
            frameCount++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numOfMatches.setText("Number of Matches: "+matchnumber);

                }
            });


            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

