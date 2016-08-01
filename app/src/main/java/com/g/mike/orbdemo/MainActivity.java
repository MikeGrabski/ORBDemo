package com.g.mike.orbdemo;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
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

    FeatureDetector detector;
    DescriptorExtractor descriptor;

    //Total number of features detected in the captured image
    int featuresnumber;

    //Total number of mathces per 2 images
    int matchnumber = 0;

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
    private long elapsedTime = 0;
    private int frameCount= 0;

    MatOfDMatch features;
    MatOfDMatch matches;

    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();

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
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        img1 = new Mat(width, height, CvType.CV_8UC3);
        img2 = new Mat(width, height, CvType.CV_8UC3);
        descriptors1 = new Mat();
        descriptors2 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        keypoints2 = new MatOfKeyPoint();
        features = new MatOfDMatch();
        matches = new MatOfDMatch();

        setPreviewFormat();
    }

    @Override
    protected void onStart() {
        super.onStart();
        messages.setText("Please, capture a reference photo.");
        cameraView.addView(cameraPreview);
        cameraView.addView(myCustomView);
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
            byte[] data = cameraPreview.getCurrentFrame();
            img2.put(0, 0, data);
            //resize(img2, img2, img2.size(), 0, 0, 1);
            detector.detect(img2, keypoints2);
            descriptor.compute(img2, keypoints2, descriptors2);
            //matcher should include 2 different image's descriptors
            matcher.match(descriptors1, descriptors2, matches);

            int DIST_LIMIT = 25;
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
            long endTime = System.currentTimeMillis();
            elapsedTime+=(endTime-startTime);
            frameCount++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numOfMatches.setText("Number of Matches: "+matchnumber);
                    if(matchnumber > featuresnumber*0/10) {
                        messages.setText("VERY CLOSE!");
                        myCustomView.setDrawingState(true);
                    } else {
                        messages.setText("NOT CLOSE!");
                        myCustomView.setDrawingState(false);
                    }
                    myCustomView.invalidate();
                }
            });

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.onPause();
    }
}

