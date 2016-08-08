package com.g.mike.orbdemo;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import static org.opencv.calib3d.Calib3d.RANSAC;
public class MainActivity extends Activity {
    //Views
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
    //Needed for GRV
    TextView statusTextView;
    //Needed for matching
    byte[] currentPhoto;
    int width;
    int height;
    DescriptorMatcher matcher;
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    //Total number of features detected in the captured image
    int featuresnumber;
    //Total number of mathces per 2 images
    int matchnumber = 0;
    //image descriptors and keyfeatures
    Mat img1, img2;
    Mat descriptors1, descriptors2;
    MatOfKeyPoint keypoints1, keypoints2;
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

    //for GRV 
    private float[] grvCapturedValues = new float[3];
    private float[] grvChangingValues = new float[3];
    private GRVCoordinates grvCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            cameraPermissionGranted = true;
        }
        else if(permissionCheck == PackageManager.PERMISSION_DENIED){
            cameraPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        //everything that does not depend on camera preview size is initialized here once
        //ui stuff
        capture = (Button)findViewById(R.id.captureFrame);
        startTracking  = (Button)findViewById(R.id.startTracking);
        stopTracking = (Button)findViewById(R.id.stopTracking);
        cameraView = (RelativeLayout)findViewById(R.id.cameraView);
        messages = (TextView)findViewById(R.id.messages);
        numOfFeatures = (TextView)findViewById(R.id.numOfFeatures);
        numOfMatches = (TextView)findViewById(R.id.numOfMatches);
        averageTimePerFrameTextView = (TextView)findViewById(R.id.averageTimePerFrame);
        statusTextView = (TextView)findViewById(R.id.statusTextView);
        messages.setText("Capture a wall.");
        myCustomView = new MyCustomView(getApplicationContext());
        cameraView.addView(myCustomView);
        //myCustomView.setVisibility(View.GONE);
        //detector/descriptor/matcher stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        img1 = new Mat(height+height/2, width, CvType.CV_8UC1);
        img2 = new Mat(height+height/2, width, CvType.CV_8UC1);
        descriptors1 = new Mat();
        descriptors2 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        keypoints2 = new MatOfKeyPoint();
        features = new MatOfDMatch();
        matches = new MatOfDMatch();
        finalh = new float[][]{ new float[]{1, 0, 0}, new float[]{0, 1, 0}, new float[]{0, 0, 1}};
        //setting listeners
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

    private void init(){
        cameraPreview = new CameraPreview(getApplicationContext());
        cameraView.addView(cameraPreview);
        width = cameraPreview.getPreviewWidth();
        height = cameraPreview.getPreviewHeight();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startTracking() {
        //myCustomView.setVisibility(View.VISIBLE);
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
        //myCustomView.setVisibility(View.GONE);
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

        Mat pre = new Mat(height+height/2, width, CvType.CV_8UC1);
        pre.put(0, 0, currentPhoto);
        Imgproc.cvtColor(pre,img1,Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(img1,img1);
        Core.flip(img1,img1,1);
//        Mat mask = Mat.zeros(img1.size(),CvType.CV_8UC1);
//        Mat roi  = new Mat(mask,myCustomView.getRect());
//        roi.setTo(new Scalar(255,255,255));
//        detector.detect(img1,keypoints1,mask);
        detector.detect(img1,keypoints1);

        descriptor.compute(img1, keypoints1, descriptors1);
        Log.d("", "capture: featureNumber: " +keypoints1.toList().size());
        //counting the number of features in the original captured photo
        numOfFeatures.setText("Number of features: " + keypoints1.toList().size());
        Mat outputImage = new Mat(img1.size(),img1.type());
        Features2d.drawKeypoints(img1,keypoints1,outputImage);

        //take GRV coordinates
        grvCapturedValues = grvCoordinates.getValues();
        myCustomView.invalidate();
    }

    public void matchImages(){
        while(true) {
            if(stopTrackingNow) {
                stopTrackingNow = false;
                break;
            }
            long startTime = System.currentTimeMillis();
            byte[] data = cameraPreview.getCurrentFrame();
            Mat pre = new Mat(height+height/2, width, CvType.CV_8UC1);
            pre.put(0, 0, data);
            Imgproc.cvtColor(pre,img2,Imgproc.COLOR_YUV2GRAY_NV21);
            Core.transpose(img2,img2);
            Core.flip(img2,img2,1);
            //resize(img2, img2, img2.size(), 0, 0, 1);
            detector.detect(img2, keypoints2);
            descriptor.compute(img2, keypoints2, descriptors2);
            //matcher should include 2 different image's descriptors
            matcher.match(descriptors1, descriptors2, matches);
            int DIST_LIMIT = 50;
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final= new ArrayList<DMatch>();
            PriorityQueue<DMatch> orderedmatches = new PriorityQueue<>();
            count = 0;
            for(int i = 0; i < matchesList.size(); i++) {
//                orderedmatches.add(matchesList.get(i));
            }
//            for(int i = 0; i<4; i++) {
//                matches_final.add(orderedmatches.poll());
//            }
            for(int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= DIST_LIMIT) {
                    matches_final.add(matches.toList().get(i));
                    count++;
                }
            }
            Log.d("number of matches", "matchImages: "+matches_final.size());
            myCustomView.setMatches(matches_final, keypoints2);
            matchnumber = count;
            //compute the transformation based on the matched features
            if(true) {
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
                if(homography!=null) {
                    float h[][] = new float[3][3];
                    for(int i = 0; i<3; i++) {
                        for(int j = 0; j<3; j++) {
                            if(homography.get(i, j) != null)
                                h[i][j] = (float)homography.get(i, j)[0];
                        }
                    }
                    finalh = h;
                    myCustomView.setTransformMatrix(finalh);
                }
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
                    Log.d("", "run: runonui thread");
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
            grvChangingValues = grvCoordinates.getValues();
            matchCoordinates();
        }
    }

    private void matchCoordinates() {
        final String status;
        if(Math.abs((grvChangingValues[1] + 0.7) - (grvCapturedValues[1] + 0.7)) > 0.00
                && Math.abs((grvChangingValues[1] + 0.7) - (grvCapturedValues[1] + 0.7)) <= 0.04) {
            status = "Here";
        }
        else if(Math.abs((grvChangingValues[1] + 0.7) - (grvCapturedValues[1] + 0.7)) > 0.04
                && Math.abs((grvChangingValues[1] + 0.7) - (grvCapturedValues[1] + 0.7)) <= 0.09) {
            status = "Almost here";
        }
        else {
            status = "Turn Around";
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(status);
            }
        });

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        init();
        grvCoordinates = new GRVCoordinates(this);
        myCustomView.bringToFront();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.onPause();
        cameraView.removeView(cameraPreview);
    }
}