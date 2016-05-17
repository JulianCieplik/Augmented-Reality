package org.opencv.samples.tutorial1;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class TestData extends Activity {

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

   /* File root = Environment.getExternalStorageDirectory();
    ImageView IV = (ImageView) findViewById(R.id."image view");
    Bitmap bMap = BitmapFactory.decodeFile(root+"/images/01.jpg");
    IV.setImageBitmap(bMap);*/
   // mPatternSize = new Size(9, 6);
   // mPatternSize= new Size(4,11);

    public void Calibrations(CameraCalibrator cal, int width, int height){
        Resources res = getResources();
        SharedPreferences mPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        // MakeCamera Image
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(AR_Engine.mCameraMatrix);
        AR_Engine.mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(AR_Engine.mDistortionCoefficients);

        Log.i("hej:", "StartCalibrationLoad ");
        int doWork = Integer.valueOf(mPrefs.getString("dWork", "0"));
        if (doWork==1 || !CalibrationResult.tryLoad(this, cal.getCameraMatrix(), cal.getDistortionCoefficients())) {
            int calwidth=100;
            int calheight=100;

            for (int j = 2; j < 15; j++) {

                Bitmap bMapx = getTestImage(j, res);  // Image To Display
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

    public void ImageDrawTest() {
        //Imgproc.circle();
        Resources res = getResources();
        SharedPreferences mPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        int i = Integer.valueOf(mPrefs.getString("Timg", "2"));
        int x = Integer.valueOf(mPrefs.getString("pat_rows", "2"));
        int y = Integer.valueOf(mPrefs.getString("pat_cols", "3"));
        int showHomog = Integer.valueOf(mPrefs.getString("zWork","1"));  // Display The Homography
        AR_Engine.solid = Integer.valueOf(mPrefs.getString("Solid","1"));
        float homoScale = Float.valueOf(mPrefs.getString("homoScale","1.0"));
        AR_Engine.height =  Float.valueOf(mPrefs.getString("wfh","1.0"));
        AR_Engine.w =  Float.valueOf(mPrefs.getString("wscale","1.0"));
        AR_Engine.h =  Float.valueOf(mPrefs.getString("height","1.0"));
        AR_Engine.scale = Float.valueOf(mPrefs.getString("bscale","1.0"));
        AR_Engine.wscale = Float.valueOf(mPrefs.getString("wiscale","1.0"));
        int detection = Integer.valueOf(mPrefs.getString("sync_frequency","1")); //Detection Method

        Bitmap bMap = getTestImage(i,res);  // Image To Display
        int height = bMap.getHeight();
        int width = bMap.getWidth();
        CameraCalibrator cal = new CameraCalibrator(width, height, this);
        Calibrations(cal,width,height);
        Mat m = Mat.zeros(width, height, CvType.CV_8UC3);
        Mat gray = Mat.zeros(width, height, CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, m);
        Imgproc.cvtColor(m, gray, Imgproc.COLOR_BGR2GRAY);
        cal.patternType=detection;
        cal.ReloadSettings(this);
        cal.findPattern(gray);
        //Image Is Turn by WarpProjective After findHomography
         i = Integer.valueOf(mPrefs.getString("Timg", "2"));
        Bitmap xMap =getTestImage(0,res);  // OverLay 2-D Image
        int heightx = xMap.getHeight();
        int widthx = xMap.getWidth();
        // make a mat and draw something

        Mat over = Mat.zeros(widthx, heightx, CvType.CV_8UC3);
        Utils.bitmapToMat(xMap, over);


        if (cal.patternfound()) {

            //Now The Corners Of Figure is in mCorner
            Point[] points = mCorners.toArray();
            Point[] outerCorners = cal.getouterCorners();
            MatOfPoint m1 = new MatOfPoint();
            m1.fromArray(outerCorners);
            List<MatOfPoint> contor = new ArrayList<>();
            contor.add(m1);
            Imgproc.drawContours(m, contor, 0, new Scalar(180, 180, 70),10);
            MatOfPoint2f ma1 = new MatOfPoint2f();
            ma1.fromArray(outerCorners);
            AR_Engine.solvePnP(ma1);
            //2D points
            if (showHomog==1) {
                List<Point> imagePoints = new ArrayList<Point>();
                imagePoints.add(new Point(0, 0));
                imagePoints.add(new Point(0, 67*homoScale));
                imagePoints.add(new Point(74*homoScale, 67*homoScale));
                imagePoints.add(new Point(74*homoScale, 0));
                MatOfPoint2f ma2 = new MatOfPoint2f();
                ma2.fromList(imagePoints);
                List<Point> imagePointsB = new ArrayList<Point>();
                imagePointsB.add(new Point(0, 0));
                imagePointsB.add(new Point(0, 100*homoScale));
                imagePointsB.add(new Point(100*homoScale, 100*homoScale));
                imagePointsB.add(new Point(100*homoScale, 0));
                MatOfPoint2f maz = new MatOfPoint2f();
                maz.fromList(imagePointsB);
                Mat H = Calib3d.findHomography(ma2, ma1, Calib3d.RANSAC, 3);
                Mat overRot = Mat.zeros(width, height, CvType.CV_8UC3);
                Imgproc.warpPerspective(over, overRot, H, new Size(width, height));
                Core.addWeighted(m, 0.8, overRot, 1, 0, m);
                AR_Engine.HomographyCompletefigure(m,getRubicSides(res),maz);
            }

            MatOfPoint mx1 = new MatOfPoint();
            mx1.fromArray(points);
            Log.i("hej:", "m1:"+m1.dump());

            int eWork = i= Integer.valueOf(mPrefs.getString("eWork", "0"));
            if (eWork==1){
                //Do 3D work!

                AR_Engine.drawAxis(m);
                if (Integer.valueOf(mPrefs.getString("figure","1"))==1) {
                    int show3Dwire = Integer.valueOf(mPrefs.getString("3Dframewire", "1"));  // Display The Homography
                    MatOfPoint2f respoints = AR_Engine.projectPoints(AR_Engine.Get3Dfigure(show3Dwire));
                    MatOfPoint p = new MatOfPoint();
                    p.convertTo(p, CvType.CV_32S);
                    List<MatOfPoint> a = new ArrayList<>();

                    Log.i("hej:", "AtdrawingPhase" + respoints.dump());
                    p.fromList(respoints.toList());
                    a.add(0, p);
                    AR_Engine.DrawDots(m,respoints.toArray());
                    Imgproc.drawContours(m, a, 0, new Scalar(200, 50, 50), 5);
                }
                AR_Engine.DrawSolid(m);


                ;Log.i("hej:", "PolyDone ");
                //String resultMessage = respoints.dump();
                //(Toast.makeText(TestData.this, resultMessage, Toast.LENGTH_LONG)).show();
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

    List<Mat> getRubicSides(Resources res){
        ArrayList<Mat> Sides = new ArrayList<>();
        Mat imag=new Mat();
        Bitmap b = BitmapFactory.decodeResource(res, R.drawable.green);
        Utils.bitmapToMat(b,imag);
        Sides.add(imag);
       imag=new Mat();
        b = BitmapFactory.decodeResource(res, R.drawable.blue);
        Utils.bitmapToMat(b,imag);
        Sides.add(imag);
         imag=new Mat();
        b = BitmapFactory.decodeResource(res, R.drawable.orange);
        Utils.bitmapToMat(b,imag);
        Sides.add(imag);
         imag=new Mat();
         b = BitmapFactory.decodeResource(res, R.drawable.yellow);
        Utils.bitmapToMat(b,imag);
        Sides.add(imag);
        imag=new Mat();
        b = BitmapFactory.decodeResource(res, R.drawable.white);
        Utils.bitmapToMat(b,imag);
        Sides.add(imag);
        imag=new Mat();
        b = BitmapFactory.decodeResource(res, R.drawable.red);
        Utils.bitmapToMat(b,imag);
        Sides.add(imag);
        return Sides;
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
            case 11:return BitmapFactory.decodeResource(res, R.drawable.i15b);
            case 12:return BitmapFactory.decodeResource(res, R.drawable.i12);
            case 13:return BitmapFactory.decodeResource(res, R.drawable.i13);
            case 14:return BitmapFactory.decodeResource(res, R.drawable.i14);
            case 15:return BitmapFactory.decodeResource(res, R.drawable.i11);
            case 16:return BitmapFactory.decodeResource(res, R.drawable.i15);
            case 17:return BitmapFactory.decodeResource(res, R.drawable.w1);
            case 18:return BitmapFactory.decodeResource(res, R.drawable.w2);
            case 19:return BitmapFactory.decodeResource(res, R.drawable.w3);
            case 20:return BitmapFactory.decodeResource(res, R.drawable.w4);
            case 21:return BitmapFactory.decodeResource(res, R.drawable.w5);
          //  case 28:return BitmapFactory.decodeResource(res, R.drawable.w12);
            case 29:return BitmapFactory.decodeResource(res, R.drawable.i9x6);
            case 30:return BitmapFactory.decodeResource(res, R.drawable.xchessboard);
            case 31:return BitmapFactory.decodeResource(res,R.drawable.circle_pattern32);
        }
        return null;

    }
}

