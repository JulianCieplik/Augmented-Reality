package org.opencv.samples.tutorial1;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class TestData extends Activity {

    private static Size mPatternSize = new Size(2, 6);
    private static int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    private MatOfPoint2f mCorners = new MatOfPoint2f();
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


    private boolean findPattern(Mat grayFrame,int i) {
       switch (i){
           case 1:  return Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                   mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID+Calib3d.CALIB_CB_CLUSTERING);
           case 2: return Calib3d.findCirclesGrid(grayFrame, mPatternSize,
                   mCorners,Calib3d.CALIB_CB_SYMMETRIC_GRID+Calib3d.CALIB_CB_CLUSTERING);
           case 3: return Calib3d.findChessboardCorners(grayFrame,mPatternSize,mCorners,Calib3d.CALIB_CB_NORMALIZE_IMAGE+Calib3d.CALIB_CB_FAST_CHECK+Calib3d.CALIB_CB_ADAPTIVE_THRESH);
       }
    return false;
    }

   /* File root = Environment.getExternalStorageDirectory();
    ImageView IV = (ImageView) findViewById(R.id."image view");
    Bitmap bMap = BitmapFactory.decodeFile(root+"/images/01.jpg");
    IV.setImageBitmap(bMap);*/

    public void ImageDrawTest() {
        Resources res = getResources();
        SharedPreferences mPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        int i = Integer.valueOf(mPrefs.getString("Timg", "2"));
        int x = Integer.valueOf(mPrefs.getString("d1", "2"));
        int y = Integer.valueOf(mPrefs.getString("d2", "3"));
        //if (i>15) {
           // mPatternSize = new Size(9, 6);
        //}else{
            // mPatternSize= new Size(4,11);
        //}
        mPatternSize=new Size(x,y);
        int detection = Integer.valueOf(mPrefs.getString("sync_frequency","1"));
        Bitmap bMap = getTestImage(i,res);  // Image To Display
        int height = bMap.getHeight();
        int width = bMap.getWidth();

        // MakeCamera Image
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(AR_Engine.mCameraMatrix);
        AR_Engine.mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(AR_Engine.mDistortionCoefficients);
        CameraCalibrator cal = new CameraCalibrator(width, height, this);
        Log.i("hej:", "StartCalibrationLoad ");
         double[] values= new double[]{699.2254638671875, 0, 358.5,
         0, 699.2254638671875, 202,
         0, 0, 1};
         for (int j=0;j<9;j++){
             AR_Engine.mCameraMatrix.put(j/3,j%3,values[j]);
         }
        int doWork = Integer.valueOf(mPrefs.getString("dWork", "0"));
        if (doWork==1) {
            if (!CalibrationResult.tryLoad(this, cal.getCameraMatrix(), cal.getDistortionCoefficients())) {
                int calwidth=100;
                int calheight=100;

                for (int j = 2; j < 14; j++) {

                    Bitmap bMapx = getTestImage(i, res);  // Image To Display
                    // make a mat and draw something
                    calwidth=bMapx.getWidth();
                    calheight=bMapx.getHeight();
                    Mat m = Mat.zeros(bMapx.getWidth(),bMapx.getHeight(), CvType.CV_8UC3);
                    Mat gray = Mat.zeros(bMapx.getWidth(),bMapx.getHeight(), CvType.CV_8UC1);
                    Utils.bitmapToMat(bMapx, m);
                    Imgproc.cvtColor(m, gray, Imgproc.COLOR_BGR2GRAY);
                    cal.processFrame(gray, m);
                    cal.addCorners();
                }
                cal.calibrate();
                cal.getCameraMatrix().copyTo(AR_Engine.mCameraMatrix);
                cal.getDistortionCoefficients().copyTo(AR_Engine.mDistortionCoefficients);
                CalibrationResult.save(this, AR_Engine.mCameraMatrix, AR_Engine.mDistortionCoefficients,calwidth,calheight);
            } else {
                cal.setCalibrated();
                cal.ResChanged(width,height);
                cal.getCameraMatrix().copyTo(AR_Engine.mCameraMatrix);
                cal.getDistortionCoefficients().copyTo(AR_Engine.mDistortionCoefficients);
            }
        }
        //Assume UnDistorted
        // 3D model
        List<Point3> model = new ArrayList<Point3>();
        List<Point3> Cmodel = new ArrayList<Point3>();

        model.add(new Point3(0,0,0));
        model.add(new Point3(0,1,0));
        model.add(new Point3(1,1,0));
        model.add(new Point3(1,0,0));
        Cmodel.addAll(model.subList(0,4));
        Cmodel.add(new Point3(1, 0, 1));
        Cmodel.add(new Point3(1, 1, 1));
        Cmodel.add(new Point3(0, 1, 1));
        Cmodel.add(new Point3(0, 0, 1));


        MatOfPoint3f object = new MatOfPoint3f();
        MatOfPoint3f Cobject = new MatOfPoint3f();
        object.fromList(model);
        Cobject.fromList(Cmodel);

        Mat m = Mat.zeros(width, height, CvType.CV_8UC3);
        Mat gray = Mat.zeros(width, height, CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, m);
        Imgproc.cvtColor(m, gray, Imgproc.COLOR_BGR2GRAY);
        boolean contains = findPattern(gray,detection);
        //Image Is Turn by WarpProjective After findHomography
         i = Integer.valueOf(mPrefs.getString("Timg", "2"));
        Bitmap xMap =getTestImage(0,res);  // OverLay 2-D Image
        int heightx = xMap.getHeight();
        int widthx = xMap.getWidth();
        // make a mat and draw something

        Mat over = Mat.zeros(widthx, heightx, CvType.CV_8UC3);
        Utils.bitmapToMat(xMap, over);
        if (contains) {
            Imgproc.putText(m, "Pattern Detected", new Point(30, 80), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200, 200, 0), 2);
            //Now The Corners Of Figure is in mCorner
            Point[] points = mCorners.toArray();
            Point[] outerCorners = new Point[]{points[0],points[(int)mPatternSize.width-1],points[(int)(mPatternSize.width*(mPatternSize.height)-1)],points[points.length-(int)mPatternSize.width]};
            Point[] outerCornerA = new Point[]{points[0],points[(int)mPatternSize.width-1],points[(int)(mPatternSize.width*(mPatternSize.height)-1)],points[points.length-(int)mPatternSize.width],null,null};
            Point directionA = new Point(outerCorners[1].x-outerCorners[0].x,outerCorners[1].y-outerCorners[0].y);
            Point directionB = new Point(outerCorners[2].x-outerCorners[0].x,outerCorners[2].y-outerCorners[0].y);
            outerCornerA[4]=new Point(points[0].x+0.5*directionA.x,points[0].y+0.5*directionA.y);
            outerCornerA[5]=new Point(points[0].x+0.5*directionB.x,points[0].y+0.5*directionB.y);
            //2D points
            List<Point> imagePoints = new ArrayList<Point>();
            imagePoints.add(new Point(0,0));
            imagePoints.add(new Point(0, 67));
            imagePoints.add(new Point(74, 67));
            imagePoints.add(new Point(74, 0));
            //
            MatOfPoint m1 = new MatOfPoint();
            m1.fromArray(outerCorners);
            MatOfPoint mx1 = new MatOfPoint();
            mx1.fromArray(points);
            Log.i("hej:", "m1:"+m1.dump());
            List<MatOfPoint> contor = new ArrayList<>();
            MatOfPoint2f ma1 = new MatOfPoint2f();
            ma1.fromArray(outerCorners);
            MatOfPoint2f mx = new MatOfPoint2f();
            mx.fromArray(outerCornerA);
            MatOfPoint2f ma2 = new MatOfPoint2f();
            ma2.fromList(imagePoints);
            contor.add(m1);
            Mat H = Calib3d.findHomography(ma2, ma1, Calib3d.RANSAC, 3);
            Log.i("hej:", "Homography:"+m1.dump());
            String resultMessage =H.dump();
            (Toast.makeText(TestData.this, resultMessage, Toast.LENGTH_LONG)).show();

            Mat overRot = Mat.zeros(width,height,CvType.CV_8UC3);
            Imgproc.warpPerspective(over, overRot, H, new Size(width, height));
               Core.addWeighted(m, 0.8, overRot, 1, 0, m);
            // Draw Solid Overlay overRot.copyTo(m.colRange((int) points[0].x, (int) points[0].x + 74).rowRange((int) points[0].y, (int) points[0].y + 67));

            Imgproc.drawContours(m, contor, 0, new Scalar(180, 180, 70),10);

            int eWork = i= Integer.valueOf(mPrefs.getString("eWork", "0"));
            if (eWork==1){
                //Do 3D work!
                Log.i("hej:", "Assign1");

                AR_Engine.solvePnP(object,ma1);
                MatOfPoint2f respoints = AR_Engine.projectPoints(AR_Engine.SquareBox());
                MatOfPoint p = new MatOfPoint();
                p.convertTo(p, CvType.CV_32S);
                List<MatOfPoint> a = new ArrayList<>();
                Log.i("hej:", "AxisNext1");
                AR_Engine.drawAxis(m);

                Log.i("hej:", "AtdrawingPhase" + respoints.dump());
                p.fromList(respoints.toList());
                a.add(0, p);
                //p.fromList(respoints.toList().subList(5,9));
                //a.add(1,p);
                Imgproc.drawContours(m, a, 0, new Scalar(200, 50, 50), 10);
                ;Log.i("hej:", "PolyDone ");
                AR_Engine.DrawMultiColoredBox(m);
                resultMessage = respoints.dump();
                (Toast.makeText(TestData.this, resultMessage, Toast.LENGTH_LONG)).show();
            }
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
            case 0:return BitmapFactory.decodeResource(res, R.drawable.overlay);
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
            case 11:return BitmapFactory.decodeResource(res, R.drawable.i15);
            case 12:return BitmapFactory.decodeResource(res, R.drawable.i12);
            case 13:return BitmapFactory.decodeResource(res, R.drawable.i13);
            case 14:return BitmapFactory.decodeResource(res, R.drawable.i14);
            case 15:return BitmapFactory.decodeResource(res, R.drawable.i11);
            case 16:return BitmapFactory.decodeResource(res, R.drawable.i9x6);
            case 17:return BitmapFactory.decodeResource(res, R.drawable.xchessboard);
            case 18:return BitmapFactory.decodeResource(res,R.drawable.circle_pattern32);
        }
        return null;

    }
}

