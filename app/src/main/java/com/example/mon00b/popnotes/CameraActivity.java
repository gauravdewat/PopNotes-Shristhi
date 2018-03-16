package com.example.mon00b.popnotes;

import android.content.Context;
import android.graphics.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

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

public class CameraActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String TAG = CameraActivity.class.getCanonicalName();

    private CameraBridgeViewBase mOpenCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private int mAbsoluteTriSize = 0;
    private Paint paint, bg, txt;

    private MatOfRect triangles;
    private Mat mRgba;
    private Mat mGray;
    private Bitmap bitmap;

    String drawTxt = "ladkhfj;had;fklgh;wekafgnj;lkaskdfjgal;jfgklsfgnksmfhgkasjdmhfglksjmfgnksjhfgksd,fgnskjdfhmgsl,fgmnksdf,jgnskjfdngksdfjhglser;jg;lsketjg", lol = "";


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
            InputStream is = getResources().openRawResource(R.raw.myhaar);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "myhaar.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeClassifier.load(mCascadeFile.getAbsolutePath());

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

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(getResources().getDisplayMetrics().density * 2);

        bg = new Paint();
        bg.setTextSize(getResources().getDisplayMetrics().density * 12);
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
        android.graphics.Rect roi;

        if (cascadeClassifier.empty()) {
            Log.d("ErrorInLoading", "Classifier not loaded");
        }

        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, triangles, 1.1, 2, 3,
                    new Size(mAbsoluteTriSize, mAbsoluteTriSize), new Size());
        }

        Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGB2RGBA);
        Rect[] trianglesArray = triangles.toArray();
        bitmap = Bitmap.createBitmap(mGray.cols(), mGray.rows(), Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(mGray, bitmap);

        for (int i = 0; i < trianglesArray.length; i++) {
            double xd1 = trianglesArray[i].tl().x;
            double yd1 = trianglesArray[i].tl().y;
            double xd2 = trianglesArray[i].br().x;
            double yd2 = trianglesArray[i].br().y;
            int ixd1 = (int) xd1;
            int iyd1 = (int) yd1;
            int ixd2 = (int) xd2;
            int iyd2 = (int) yd2;

            // Create a rectangle around it
            //Imgproc.rectangle(mGray, trianglesArray[i].tl(), trianglesArray[i].br(), new Scalar(255, 0, 0, 255), 2);
            roi = new android.graphics.Rect(ixd1, iyd1, ixd2 - ixd1, iyd2 - iyd1);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(roi, paint);
            canvas.drawRect(roi, bg);
            int k = roi.right;
            if (drawTxt.length() < roi.right)
                k = drawTxt.length();
            for (int j = 0; j < drawTxt.length(); ) {
                lol += drawTxt.substring(j, k);
                lol += "\n";
                j = k;
                if (2 * k < drawTxt.length())
                    k = 2 * k;
                else
                    k = drawTxt.length();
            }
Log.d("loltxt",lol);
            //canvas.drawText(lol, roi.right + (getResources().getDisplayMetrics().density * 2), roi.bottom + (getResources().getDisplayMetrics().density * 10), txt);
            float y = roi.bottom + (getResources().getDisplayMetrics().density * 10);
            for (String line : lol.split(";")) {
                canvas.drawText(line, roi.right + (getResources().getDisplayMetrics().density * 2), roi.bottom + (getResources().getDisplayMetrics().density * 10), txt);
                y += txt.descent() - txt.ascent();
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