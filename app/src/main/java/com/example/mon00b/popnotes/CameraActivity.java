package com.example.mon00b.popnotes;

import android.content.Context;
import android.graphics.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.*;
import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String TAG = CameraActivity.class.getCanonicalName();

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private CameraBridgeViewBase mOpenCvCameraView;
    private CascadeClassifier circleClassifier;
    private CascadeClassifier triangleClassifier;
    private CascadeClassifier heartClassifier;
    private int mAbsoluteTriSize = 0;
    private Paint paint, bg, txt;

    private MatOfRect triangles;
    private MatOfRect circles;
    private MatOfRect hearts;
    private Mat mRgba;
    private Mat mGray;
    private Bitmap bitmap;

    String drawTxt = "Hello World:Hello World:Hello World", lol = "";


    List<MatOfPoint> contours;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    initializeOpenCVDependencies();
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream ts = getResources().openRawResource(R.raw.trianglesclassifier);
            File trianglesDir = getDir("cascade", Context.MODE_PRIVATE);
            File mtrianglesFile = new File(trianglesDir, "trianglesclassifier.xml");
            FileOutputStream tos = new FileOutputStream(mtrianglesFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = ts.read(buffer)) != -1) {
                tos.write(buffer, 0, bytesRead);
            }
            ts.close();
            tos.close();

            // Load the cascade classifier
            triangleClassifier = new CascadeClassifier(mtrianglesFile.getAbsolutePath());
            triangleClassifier.load(mtrianglesFile.getAbsolutePath());


            InputStream hs = getResources().openRawResource(R.raw.heartclassifier);
            File heartDir = getDir("cascade", Context.MODE_PRIVATE);
            File mheartsFile = new File(heartDir, "heartclassifier.xml");
            FileOutputStream hos = new FileOutputStream(mheartsFile);

            byte[] buffer1 = new byte[4096];
            int bytesRead1;
            while ((bytesRead1 = hs.read(buffer1)) != -1) {
                hos.write(buffer1, 0, bytesRead1);
            }
            hs.close();
            hos.close();

            // Load the cascade classifier
            heartClassifier = new CascadeClassifier(mheartsFile.getAbsolutePath());
            heartClassifier.load(mheartsFile.getAbsolutePath());

            InputStream cs = getResources().openRawResource(R.raw.circlesclassifier);
            File circleDir = getDir("cascade", Context.MODE_PRIVATE);
            File mcircleFile = new File(circleDir, "circlesclassifier.xml");
            FileOutputStream cos = new FileOutputStream(mcircleFile);

            byte[] buffer2 = new byte[4096];
            int bytesRead2;
            while ((bytesRead2 = cs.read(buffer2)) != -1) {
                cos.write(buffer2, 0, bytesRead2);
            }
            cs.close();
            cos.close();

            // Load the cascade classifier
            circleClassifier = new CascadeClassifier(mheartsFile.getAbsolutePath());
            circleClassifier.load(mcircleFile.getAbsolutePath());

        } catch (Exception e) {
            Log.d("CameraActivity", "Error loading cascade", e);
        }

        // Initialise the camera view
        mOpenCvCameraView.enableView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        /*databaseReference = FirebaseDatabase.getInstance().getReference().child("Notes");
        Query query = databaseReference.orderByChild("userid").equalTo(mAuth.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Model model = new Model();
                    model = snapshot.getValue(Model.class);
                    if (Objects.equals(model.getKey(), "triangle")) {

                        final String triangle = model.getNotes();
                    } else if (Objects.equals(model.getKey(), "circle")) {

                        final String circle = model.getNotes();
                    } else if (Objects.equals(model.getKey(), "rectangle")) {

                        final String rectangle = model.getNotes();
                    }

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(getResources().getDisplayMetrics().density * 2);

        bg = new Paint();
        bg.setColor(Color.WHITE);

        txt = new Paint();
        txt.setTextSize(getResources().getDisplayMetrics().density * 10);
        txt.setColor(Color.BLACK);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mAbsoluteTriSize = (int) (height * 0.1);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2RGB);
        triangles = new MatOfRect();
        circles = new MatOfRect();
        hearts = new MatOfRect();
        android.graphics.Rect troi;
        android.graphics.Rect croi;
        android.graphics.Rect hroi;

        if (triangleClassifier != null) {
            triangleClassifier.detectMultiScale(mGray, triangles, 1.1, 2, 3,
                    new Size(mAbsoluteTriSize, mAbsoluteTriSize), new Size());
        }

        if (heartClassifier != null) {
            heartClassifier.detectMultiScale(mGray, hearts, 1.1, 2, 3,
                    new Size(mAbsoluteTriSize, mAbsoluteTriSize), new Size());
        }

        if (circleClassifier != null) {
            circleClassifier.detectMultiScale(mGray, circles, 1.1, 1, 3,
                    new Size(mAbsoluteTriSize, mAbsoluteTriSize), new Size());
        }


        Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGB2RGBA);

        Rect[] trianglesArray = triangles.toArray();
        Rect[] circlesArray = circles.toArray();
        Rect[] heartsArray = hearts.toArray();

        bitmap = Bitmap.createBitmap(mGray.cols(), mGray.rows(), Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(mGray, bitmap);

        for (int i = 0; i < trianglesArray.length; i++) {
            double txd1 = trianglesArray[i].tl().x;
            double tyd1 = trianglesArray[i].tl().y;
            double txd2 = trianglesArray[i].br().x;
            double tyd2 = trianglesArray[i].br().y;
            int tixd1 = (int) txd1;
            int tiyd1 = (int) tyd1;
            int tixd2 = (int) txd2;
            int tiyd2 = (int) tyd2;
            Log.d("triangles","Detected");

            // Create a rectangle around it
            //Imgproc.rectangle(mGray, trianglesArray[i].tl(), trianglesArray[i].br(), new Scalar(255, 0, 0, 255), 2);
            troi = new android.graphics.Rect(tixd1, tiyd1, tixd2 - tixd1, tiyd2 - tiyd1);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(troi, paint);
            canvas.drawRect(troi, bg);
            int k = troi.right;
     /*       if (drawTxt.length() < roi.right)
                k = drawTxt.length();
            for (int j = 0; j < drawTxt.length(); ) {
                lol += drawTxt.substring(j, k);
                lol += "\n";
                j = k;
                if (2 * k < drawTxt.length())
                    k = 2 * k;
                else
                    k = drawTxt.length();
            }*/
            Log.d("loltxt", lol);
            //canvas.drawText(lol, roi.right + (getResources().getDisplayMetrics().density * 2), roi.bottom + (getResources().getDisplayMetrics().density * 10), txt);


            int y = 0;
            for (String line : drawTxt.split(":")) {
                canvas.drawText(line, troi.right + (getResources().getDisplayMetrics().density * 2), troi.bottom + (getResources().getDisplayMetrics().density * 10), txt);
                y += (getResources().getDisplayMetrics().density * 8);
            }

            Log.d("Success", "Conversions Successful");

        }

        for (int i = 0; i < circlesArray.length; i++) {
            double cxd1 = circlesArray[i].tl().x;
            double cyd1 = circlesArray[i].tl().y;
            double cxd2 = circlesArray[i].br().x;
            double cyd2 = circlesArray[i].br().y;
            int cixd1 = (int) cxd1;
            int ciyd1 = (int) cyd1;
            int cixd2 = (int) cxd2;
            int ciyd2 = (int) cyd2;

            // Create a rectangle around it
            //Imgproc.rectangle(mGray, trianglesArray[i].tl(), trianglesArray[i].br(), new Scalar(255, 0, 0, 255), 2);
            croi = new android.graphics.Rect(cixd1, ciyd1, cixd2 - cixd1, ciyd2 - ciyd1);
            Log.d("triangles","Detected");


            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(croi, paint);
            canvas.drawRect(croi, bg);
            int k = croi.right;
     /*       if (drawTxt.length() < roi.right)
                k = drawTxt.length();
            for (int j = 0; j < drawTxt.length(); ) {
                lol += drawTxt.substring(j, k);
                lol += "\n";
                j = k;
                if (2 * k < drawTxt.length())
                    k = 2 * k;
                else
                    k = drawTxt.length();
            }*/
            Log.d("loltxt", lol);
            //canvas.drawText(lol, roi.right + (getResources().getDisplayMetrics().density * 2), roi.bottom + (getResources().getDisplayMetrics().density * 10), txt);

            int y = 0;
            for (String line : drawTxt.split(":")) {
                canvas.drawText(line, croi.right + (getResources().getDisplayMetrics().density * 2), croi.bottom + (getResources().getDisplayMetrics().density * 10), txt);
                y += (getResources().getDisplayMetrics().density * 8);
            }

            Log.d("Success", "Conversions Successful");

        }

        for (int i = 0; i < heartsArray.length; i++) {
            double hxd1 = heartsArray[i].tl().x;
            double hyd1 = heartsArray[i].tl().y;
            double hxd2 = heartsArray[i].br().x;
            double hyd2 = heartsArray[i].br().y;
            int hixd1 = (int) hxd1;
            int hiyd1 = (int) hyd1;
            int hixd2 = (int) hxd2;
            int hiyd2 = (int) hyd2;

            // Create a rectangle around it
            //Imgproc.rectangle(mGray, trianglesArray[i].tl(), trianglesArray[i].br(), new Scalar(255, 0, 0, 255), 2);
            hroi = new android.graphics.Rect(hixd1, hiyd1, hixd2 - hixd1, hiyd2 - hiyd1);
            Log.d("triangles","Detected");


            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(hroi, paint);
            canvas.drawRect(hroi, bg);
            int k = hroi.right;
     /*       if (drawTxt.length() < roi.right)
                k = drawTxt.length();
            for (int j = 0; j < drawTxt.length(); ) {
                lol += drawTxt.substring(j, k);
                lol += "\n";
                j = k;
                if (2 * k < drawTxt.length())
                    k = 2 * k;
                else
                    k = drawTxt.length();
            }*/
            Log.d("loltxt", lol);
            //canvas.drawText(lol, roi.right + (getResources().getDisplayMetrics().density * 2), roi.bottom + (getResources().getDisplayMetrics().density * 10), txt);


            int y = 0;
            for (String line : drawTxt.split(":")) {
                canvas.drawText(line, hroi.right + (getResources().getDisplayMetrics().density * 2), hroi.bottom + (getResources().getDisplayMetrics().density * 10), txt);
                y += (getResources().getDisplayMetrics().density * 8);
            }

            Log.d("Success", "Conversions Successful");

        }


        Utils.bitmapToMat(bitmap, mGray);
        return mGray;

        /*Core.flip(mRgba.t(),mRgbaT,2);
        Imgproc.resize(mRgbaT,mRgbaT,mRgba.size());
        contours = new ArrayList<MatOfPoint>();
        hierarchy = new Mat();

        Imgproc.erode(mRgbaT,mRgbaT,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
        Imgproc.dilate(mRgbaT,mRgbaT,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1)));
        Imgproc.Canny(mRgbaT, mIntermediateMat, 255/3, 255);
        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));*/
    /* Mat drawing = Mat.zeros( mIntermediateMat.size(), CvType.CV_8UC3 );
     for( int i = 0; i< contours.size(); i++ )
     {
    Scalar color =new Scalar(Math.random()*255, Math.random()*255, Math.random()*255);
     Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point() );
     }*/
        //hierarchy.release();
       /* Imgproc.drawContours(mRgba, contours, -1, new Scalar(0, 255, 0));//, 2, 8, hierarchy, 0, new Point());
        Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        return mRgba;*/

        //For drawing rectangles
       /*for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            // Minimum size allowed for consideration
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            //Convert back to MatOfPoint
            MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

           // Get bounding rect of contour
            Rect rect = Imgproc.boundingRect(points);

            if(rect.width>40 && rect.height>40 && rect.height<500 && rect.width<500){
                Imgproc.rectangle(mRgbaT, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 2);

            }*/
    }

}