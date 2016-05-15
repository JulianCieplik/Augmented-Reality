package org.opencv.samples.tutorial1;

import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;

public class CameraCalibrator {
    private static final String TAG = "OCVSample::CameraCalibrator";
    private SharedPreferences mPrefs;
    private static Size mPatternSize = new Size(4, 11);
    private static int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    public static int calw=1;
    public static int calh=1;
    public int patternType;
    private boolean mPatternWasFound = false;
    public MatOfPoint2f mCorners = new MatOfPoint2f();
    private List<Mat> mCornersBuffer = new ArrayList<Mat>();
    private boolean mIsCalibrated = false;

    private int mFlags;
    private double mRms;
    private double mSquareSize = 0.0181;
    private Size mImageSize;

    public CameraCalibrator(int width, int height,Activity a) {
        mPrefs=PreferenceManager.getDefaultSharedPreferences(a);
        int i = Integer.valueOf(mPrefs.getString("d1","4"));
        int j = Integer.valueOf(mPrefs.getString("d2","11"));
        int k = Integer.valueOf(mPrefs.getString("sync_frequency","1"));
        mPatternSize = new Size(i,j);
        patternType= k;
        mCornersSize= (int)(mPatternSize.width * mPatternSize.height);
        mImageSize = new Size(width, height);
        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                 Calib3d.CALIB_ZERO_TANGENT_DIST +
                 Calib3d.CALIB_FIX_ASPECT_RATIO +
                 Calib3d.CALIB_FIX_K4 +
                 Calib3d.CALIB_FIX_K5;
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(AR_Engine.mCameraMatrix);
        AR_Engine.mCameraMatrix.put(0, 0, 1.0);
        //AR_Engine.mCameraMatrix.copyTo(msCameraMatrix);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(AR_Engine.mDistortionCoefficients);
        //Log.i(TAG, "started " + this.getClass());
        SharedPreferences sharedPref = a.getSharedPreferences("Store", Context.MODE_APPEND);
        calw=sharedPref.getInt("width",width);
        calh=sharedPref.getInt("height",height);
    }

    public void ReloadSettings(Activity a){
        mPrefs=PreferenceManager.getDefaultSharedPreferences(a);
        int i = Integer.valueOf(mPrefs.getString("d1","4"));
        int j = Integer.valueOf(mPrefs.getString("d2","11"));
        int k = Integer.valueOf(mPrefs.getString("sync_frequency","1"));
        mPatternSize = new Size(i,j);
        patternType= k;
        ResChanged((int)mImageSize.width,(int)mImageSize.height);
    }

    public void processFrame(Mat grayFrame, Mat rgbaFrame) {
        findPattern(grayFrame);
        renderFrame(rgbaFrame);
    }


    public void ResChanged(int width,int height){

    //    mImageSize =  new Size(width,height);
    //    double[] d=new double[]{width/calw,1,width/calw,1,height/calh,height/calh,1,1,1};
    //    Mat mScale= new Mat(3,3,CvType.CV_64FC1);
    //    mScale.put(0,0,d);
    //    AR_Engine.mCameraMatrix.copyTo(msCameraMatrix);
    //    msCameraMatrix.mul(mScale);
    }

    public void calibrate() {

        ArrayList<Mat> rvecs = new ArrayList<>(); // latest series of Configuration
        ArrayList<Mat> tvecs = new ArrayList<>(); // latest series of Configuration

        Mat reprojectionErrors = new Mat();
        ArrayList<Mat> objectPoints = new ArrayList<>();
        objectPoints.add(Mat.zeros(mCornersSize, 1, CvType.CV_32FC3));
        calcBoardCornerPositions(objectPoints.get(0));
        for (int i = 1; i < mCornersBuffer.size(); i++) {
            objectPoints.add(objectPoints.get(0));
        }

        Calib3d.calibrateCamera(objectPoints, mCornersBuffer, mImageSize,
                AR_Engine.mCameraMatrix, AR_Engine.mDistortionCoefficients, rvecs, tvecs, mFlags);
      //  AR_Engine.mCameraMatrix.copyTo(msCameraMatrix);
        mIsCalibrated = Core.checkRange(AR_Engine.mCameraMatrix)
                && Core.checkRange(AR_Engine.mDistortionCoefficients);
        calh=(int)mImageSize.height;
        calw=(int)mImageSize.width;
        mRms = computeReprojectionErrors(objectPoints, rvecs, tvecs, reprojectionErrors);
        //% Log.i(TAG, String.format("Average re-proj error: %f", mRms));
       // Log.i(TAG, "Camera matrix: " + AR_Engine.mCameraMatrix.dump());
        // Log.i(TAG, "Distortion coefficients: " + AR_Engine.mDistortionCoefficients.dump());
    }

    public void clearCorners() {
        mCornersBuffer.clear();
    }

    private void calcBoardCornerPositions(Mat corners) {
        final int cn = 3;
        float positions[] = new float[mCornersSize * cn];
        for (int i = 0; i < mPatternSize.height; i++) {
            for (int j = 0; j < mPatternSize.width * cn; j += cn) {
                positions[(int) (i * mPatternSize.width * cn + j + 0)] =
                        (2 * (j / cn) + i % 2) * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 1)] =
                        i * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 2)] = 0;
        }
        }
        corners.create(mCornersSize, 1, CvType.CV_32FC3);
        corners.put(0, 0, positions);
      //  Log.i(TAG, "Distortion coefficients: " + AR_Engine.mDistortionCoefficients.dump());
    }

    private double computeReprojectionErrors(List<Mat> objectPoints,
            List<Mat> rvecs, List<Mat> tvecs, Mat perViewErrors) {
        MatOfPoint2f cornersProjected = new MatOfPoint2f();
        double totalError = 0;
        double error;
        float viewErrors[] = new float[objectPoints.size()];

        MatOfDouble distortionCoefficients = new MatOfDouble(AR_Engine.mDistortionCoefficients);
        int totalPoints = 0;
        for (int i = 0; i < objectPoints.size(); i++) {
            MatOfPoint3f points = new MatOfPoint3f(objectPoints.get(i));
            Calib3d.projectPoints(points, rvecs.get(i), tvecs.get(i),
                    AR_Engine.mCameraMatrix, distortionCoefficients, cornersProjected);
            error = Core.norm(mCornersBuffer.get(i), cornersProjected, Core.NORM_L2);

            int n = objectPoints.get(i).rows();
            viewErrors[i] = (float) Math.sqrt(error * error / n);
            totalError  += error * error;
            totalPoints += n;
        }
        perViewErrors.create(objectPoints.size(), 1, CvType.CV_32FC1);
        perViewErrors.put(0, 0, viewErrors);

        return Math.sqrt(totalError / totalPoints);
    }

    public void findPattern(Mat grayFrame) {
        switch (patternType){
            case 1: mPatternWasFound= Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                    mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID+Calib3d.CALIB_CB_CLUSTERING); break;
            case 2: mPatternWasFound= Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                    mCorners,Calib3d.CALIB_CB_SYMMETRIC_GRID+Calib3d.CALIB_CB_CLUSTERING); break;
            case 3: mPatternWasFound= Calib3d.findChessboardCorners(grayFrame,mPatternSize,mCorners,Calib3d.CALIB_CB_NORMALIZE_IMAGE+Calib3d.CALIB_CB_FAST_CHECK+Calib3d.CALIB_CB_ADAPTIVE_THRESH);
                    break;
            case 4:  mPatternWasFound= Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                    mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID);
        }
    }

    public void addCorners() {
        if (mPatternWasFound) {
            mCornersBuffer.add(mCorners.clone());
        }
    }

    public boolean patternfound(){
        return mPatternWasFound;
    }
    public Point[] getouterCorners(){
        Point[] points = mCorners.toArray();
        Point[] outerCorners = new Point[]{points[0],points[(int)mPatternSize.width-1],points[(int)(mPatternSize.width*(mPatternSize.height)-1)],points[points.length-(int)mPatternSize.width]};
        return outerCorners;
    }
    private void drawPoints(Mat rgbaFrame) {

        Calib3d.drawChessboardCorners(rgbaFrame, mPatternSize, mCorners, mPatternWasFound);
    }

    private void renderFrame(Mat rgbaFrame) {
        drawPoints(rgbaFrame);

        Imgproc.putText(rgbaFrame, "Captured: " + mCornersBuffer.size(), new Point(rgbaFrame.cols() / 3 * 2, rgbaFrame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
    }

    public Mat getCameraMatrix() {
        return AR_Engine.mCameraMatrix;
    }

    public Mat getDistortionCoefficients() {
        return AR_Engine.mDistortionCoefficients;
    }

    public int getCornersBufferSize() {
        return mCornersBuffer.size();
    }

    public double getAvgReprojectionError() {
        return mRms;
    }

    public boolean isCalibrated() {
        return mIsCalibrated;
    }

    public void setCalibrated() {
        mIsCalibrated = true;
    }

}
