package org.opencv.samples.tutorial1;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.List;

public class TestData extends Activity {

    private static Size mPatternSize = new Size(4, 11);
    private static int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    private MatOfPoint2f mCorners = new MatOfPoint2f();
    private MatOfDouble mCameraMatrix = new MatOfDouble();
    private MatOfDouble mDistortionCoefficients = new MatOfDouble();
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_data);

    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS ) {
                // now we can call opencv code !
                ImageDrawTest();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
        // you may be tempted, to do something here, but it's *async*, and may take some time,
        // so any opencv call here will lead to unresolved native errors.
    }

  /*  public void StartCameraCalibrator(){
        if (mCalibrator == null)
            mCalibrator = new CameraCalibrator(width, height, this);
        if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
            mCalibrator.setCalibrated();
        }
    }*/

    private boolean findPattern(Mat grayFrame) {
            return Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                    mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID+Calib3d.CALIB_CB_CLUSTERING);

        //    mPatternWasFound = Calib3d.findChessboardCorners(grayFrame,mPatternSize,mCorners,Calib3d.CALIB_CB_FAST_CHECK);
    }

   /* File root = Environment.getExternalStorageDirectory();
    ImageView IV = (ImageView) findViewById(R.id."image view");
    Bitmap bMap = BitmapFactory.decodeFile(root+"/images/01.jpg");
    IV.setImageBitmap(bMap);*/

    public void ImageDrawTest() {
        // MakeCamera Image
       int mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5;
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
        //Assume UnDistorted
        // 3D model
        List<Point3> model = new ArrayList<Point3>();
        List<Point3> Cmodel = new ArrayList<Point3>();
        model.add(new Point3(0,0,0));
        model.add(new Point3(0,3,0));
        model.add(new Point3(3,3,0));
        model.add(new Point3(3,0,0));
        model.add(new Point3(0,1.5,0));
        Cmodel.addAll(model);
        Cmodel.add(new Point3(0, 0, 3));
        Cmodel.add(new Point3(3, 0, 3));
        Cmodel.add(new Point3(0, 3, 3));
        Cmodel.add(new Point3(3, 3, 3));
        MatOfPoint3f object = new MatOfPoint3f();
        MatOfPoint3f Cobject = new MatOfPoint3f();
        object.fromList(model);
        Cobject.fromList(Cmodel);


        //Image Is Turn by WarpProjective After findHomography



        Resources res = getResources();
        SharedPreferences mPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        int i = Integer.valueOf(mPrefs.getString("Timg", "1"));
        Bitmap bMap = getTestImage(i,res);  // Image To Display
        int height = bMap.getHeight();
        int width = bMap.getWidth();
        // make a mat and draw something
        Mat m = Mat.zeros(width, height, CvType.CV_8UC3);
        Mat gray = Mat.zeros(width, height, CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, m);
        Imgproc.cvtColor(m, gray, Imgproc.COLOR_BGR2GRAY);
        boolean contains = findPattern(gray);
        Mat over = Mat.zeros(74, 67, CvType.CV_8UC3);
        Bitmap xMap = BitmapFactory.decodeResource(res, R.drawable.overlay);  // OverLay 2-D Image
        Utils.bitmapToMat(xMap, over);
        if (contains) {
            Imgproc.putText(m, "Pattern Detected", new Point(30, 80), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200, 200, 0), 2);
            //Now The Corners Of Figure is in mCorner
            Point[] points = mCorners.toArray();
            Point[] outerCorners = new Point[]{points[0],points[(int)mPatternSize.width-1],points[(int)(mPatternSize.width*(mPatternSize.height)-1)],points[points.length-(int)mPatternSize.width],null};
            Point directionA = new Point(outerCorners[1].x-outerCorners[0].x,outerCorners[1].y-outerCorners[0].y);
            Point directionB = new Point(outerCorners[2].x-outerCorners[0].x,outerCorners[2].y-outerCorners[0].y);
            outerCorners[4]=new Point(outerCorners[0].x+directionA.x*0.5,outerCorners[0].y+directionA.y*0.5);
            //2D points
            List<Point> imagePoints = new ArrayList<Point>();
            imagePoints.add(new Point(0,0));
            imagePoints.add(new Point(0, 67));
            imagePoints.add(new Point(74, 67));
            imagePoints.add(new Point(74, 0));
            imagePoints.add(new Point(0, 33.5));
            //
            MatOfPoint m1 = new MatOfPoint();
            m1.fromArray(outerCorners);
            List<MatOfPoint> contor = new ArrayList<>();
            contor.add(m1);
            MatOfPoint m2 = new MatOfPoint();

            m2.fromList(imagePoints);

            MatOfPoint2f ma1 = new MatOfPoint2f();
            ma1.fromArray(outerCorners);
            MatOfPoint2f ma2 = new MatOfPoint2f();
            ma2.fromList(imagePoints);
            Mat H = Calib3d.findHomography(ma2, ma1, 0, 3);
            String resultMessage =H.dump();
            (Toast.makeText(TestData.this, resultMessage, Toast.LENGTH_LONG)).show();

            Mat overRot = Mat.zeros(width,height,CvType.CV_8UC3);
            Imgproc.warpPerspective(over, overRot, H, new Size(width, height));
            Core.addWeighted(m, 0.8, overRot, 1, 0, m);
            //overRot.copyTo(m.colRange((int) points[0].x, (int) points[0].x + 74).rowRange((int) points[0].y, (int) points[0].y + 67));

            Imgproc.drawContours(m, contor, 0, new Scalar(100, 100, 50));
            //Do 3D work!
            ArrayList<Mat> rvecs = new ArrayList<>();
            ArrayList<Mat> tvecs = new ArrayList<>();
            Mat rvec = new Mat();
            Mat tvec = new Mat();

            Calib3d.solvePnP(object, ma1, mCameraMatrix, mDistortionCoefficients, rvec, tvec);
            MatOfPoint2f respoints = new MatOfPoint2f();
           // respoints.fromList(imagePoints);
            MatOfPoint p = new MatOfPoint();
            p.convertTo(p, CvType.CV_32S);
           List<MatOfPoint> a = new ArrayList<>();
            Calib3d.projectPoints(Cobject, rvec, tvec, mCameraMatrix, mDistortionCoefficients, respoints);
            p.fromList(respoints.toList());
            a.add(0, p);
            Imgproc.fillPoly(m, a, new Scalar(100, 100, 50));
            resultMessage =respoints.dump();
            (Toast.makeText(TestData.this, resultMessage, Toast.LENGTH_LONG)).show();
        }else{
            Imgproc.putText(m, "Pattern Undetected", new Point(30, 80), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200, 200, 0), 2);
            over.copyTo(m.colRange(80, 80 + 74).rowRange(100, 100 + 67));
        }
        Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bm);
        // find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(R.id.iView1);
        iv.setImageBitmap(bm);
    }

    Bitmap getTestImage(int i,Resources res){
        switch (i){
            case 1:return BitmapFactory.decodeResource(res, R.drawable.i01);
            case 2:return BitmapFactory.decodeResource(res, R.drawable.i02);
            case 3:return BitmapFactory.decodeResource(res, R.drawable.i03);
            case 4:return BitmapFactory.decodeResource(res, R.drawable.i04);
            case 5:return BitmapFactory.decodeResource(res, R.drawable.i05);
            case 6:return BitmapFactory.decodeResource(res, R.drawable.i06);
            case 7:return BitmapFactory.decodeResource(res, R.drawable.i07);
            case 8:return BitmapFactory.decodeResource(res, R.drawable.i08);
            case 9:return BitmapFactory.decodeResource(res, R.drawable.i09);
            case 10:return BitmapFactory.decodeResource(res, R.drawable.i10);
            case 11:return BitmapFactory.decodeResource(res, R.drawable.i11);
            case 12:return BitmapFactory.decodeResource(res, R.drawable.i12);
            case 13:return BitmapFactory.decodeResource(res, R.drawable.i13);
            case 14:return BitmapFactory.decodeResource(res, R.drawable.i14);
            case 15:return BitmapFactory.decodeResource(res, R.drawable.i15);
        }
        return null;
    }
}

