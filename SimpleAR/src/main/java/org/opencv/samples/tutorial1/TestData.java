package org.opencv.samples.tutorial1;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageView;

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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TestData extends Activity {

    private static Size mPatternSize = new Size(4, 11);
    private static int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    private MatOfPoint2f mCorners = new MatOfPoint2f();
    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();
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
                    mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID);

        //    mPatternWasFound = Calib3d.findChessboardCorners(grayFrame,mPatternSize,mCorners,Calib3d.CALIB_CB_FAST_CHECK);
    }

   /* File root = Environment.getExternalStorageDirectory();
    ImageView IV = (ImageView) findViewById(R.id."image view");
    Bitmap bMap = BitmapFactory.decodeFile(root+"/images/01.jpg");
    IV.setImageBitmap(bMap);*/

    public void ImageDrawTest() {
        // MakeCamera Image
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
        //Assume UnDistorted

        // 3D model
        List<Point3> model = new ArrayList<Point3>();
        model.add(new Point3(0,0,0));
        model.add(new Point3(3,0,0));
        model.add(new Point3(0,3,0));
        model.add(new Point3(3,3,0));
        model.add(new Point3(0,0,3));
        model.add(new Point3(3,0,3));
        model.add(new Point3(0, 3, 3));
        model.add(new Point3(3, 3, 3));
        MatOfPoint3f object = new MatOfPoint3f();
        object.fromList(model);
        //2D points
        List<Point> imagePoints = new ArrayList<Point>();
        imagePoints.add(new Point(0,0));
        imagePoints.add(new Point(0,100));
        imagePoints.add(new Point(100,100));
        imagePoints.add(new Point(100,0));

        //Image Is Turn by WarpProjective After findHomography

        // make a mat and draw something
        Mat m = Mat.zeros(645, 501, CvType.CV_8UC3);
        Mat gray = Mat.zeros(645, 501, CvType.CV_8UC1);

        Resources res = getResources();
        Bitmap bMap = BitmapFactory.decodeResource(res, R.drawable.i01);  // Image To Display
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
            Point[] outerCorners = new Point[]{points[0],points[(int)mPatternSize.width-1],points[(int)(mPatternSize.width*(mPatternSize.height)-1)],points[points.length-(int)mPatternSize.width]};
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
            Mat H = Calib3d.findHomography(ma2, ma1, 0, 8);
            Mat overRot = Mat.zeros(645, 501, CvType.CV_8UC3);

            Imgproc.warpPerspective(over,overRot,H,new Size(645, 501));
            Core.addWeighted(m,0.8,overRot,1,0,m);
            //overRot.copyTo(m.colRange((int) points[0].x, (int) points[0].x + 74).rowRange((int) points[0].y, (int) points[0].y + 67));
            Imgproc.drawContours(m, contor, 0, new Scalar(100,100,50));
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
}

