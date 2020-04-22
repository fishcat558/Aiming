package com.example.opencvtest;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import static android.os.Environment.getExternalStoragePublicDirectory;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final int ImageGalleryReq = 20;
    public static final int CAMERA_REQUEST = 4;
    private CameraBridgeViewBase mOpenCvCameraView;
    String pathToFile;
    TextToSpeech t1;
    JavaCameraView javaCameraView;
    Scalar low, high;
    Mat input, output,proc,circles,lines;
    boolean aligned = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaCameraView = (JavaCameraView) findViewById(R.id.cameraView);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableView();
        if (OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCV Loaded Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "opencv load FAIL", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        javaCameraView.enableView();
    }

    @Override
    protected void onDestroy() {
        javaCameraView.disableView();
        super.onDestroy();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        input = new Mat(width, height, CvType.CV_16UC4);
        output = new Mat(width, height, CvType.CV_16UC4);
        proc = new Mat(width,height,CvType.CV_16UC4);
        circles = new Mat(width,height,CvType.CV_16UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Imgproc.cvtColor(inputFrame.rgba(), input, Imgproc.COLOR_BGR2HSV);
        Core.inRange(input, low, high, output);
        /* convert bitmap to mat */
        Imgproc.cvtColor(input, proc, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(proc, proc, new Size(9, 9), 0);
        Imgproc.adaptiveThreshold(proc, proc, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);
        double dp = 1d;
        double minDist = 500;
        int minRadius = 100, maxRadius = 800;
        double param1 = 70, param2 = 72;
        Imgproc.HoughCircles(proc, circles,Imgproc.CV_HOUGH_GRADIENT, dp, minDist, param1,param2, minRadius, maxRadius);
        Imgproc.HoughLines(proc, lines,param1,param1);
                int numberOfCircles = (circles.rows() == 0) ? 0 : circles.cols();
        for (int i = 0; i < numberOfCircles; i++) {
            double[] circleCoordinates = circles.get(0, i);
            double[] lineCoordinates = lines.get(0, i);
            int x_circ = (int) circleCoordinates[0], y_circ = (int) circleCoordinates[1];
            int x_lin = (int) lineCoordinates[0], y_lin = (int) lineCoordinates[1];
            Point c_centre = new Point(x_circ, y_circ);
            Point l_centre = new Point(x_lin, y_lin);
            int radius = (int) circleCoordinates[2];
            Imgproc.circle(circles, c_centre, radius, new Scalar(0,255, 0),10);
            Imgproc.rectangle(lines, new Point(x_lin - 5, y_lin - 5),
                    new Point(x_lin + 5, y_lin + 5),
                    new Scalar(0, 128, 255), 5);
            if (c_centre.x == l_centre.x && c_centre.y == l_centre.y){
                String toSpeak = "Shot aimed";
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
            }
        }
        return output;
    }
}









