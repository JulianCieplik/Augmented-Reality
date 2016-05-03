package org.opencv.samples.tutorial1;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "OCVSample::Activity";
    private CameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    public static CameraCalibrator mCalibrator;
    private MyRender mOnCameraFrameRender;
    private List<Size> mResolutionList;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;


    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Tutorial1Activity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);


        mOpenCvCameraView = (CameraView) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        if (mCalibrator!=null && !mCalibrator.isCalibrated())
        if ( CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
            mCalibrator.setCalibrated();
            mOnCameraFrameRender= new MyRender(mCalibrator,this);
        }
        mOpenCvCameraView.enableView();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mCalibrator.isCalibrated()) {
            mOpenCvCameraView.MdisconnectCamera();
            mOpenCvCameraView.disableView();
            Intent intent = new Intent(this, CameraCalibrationActivity.class);
            startActivity(intent);
        }
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        if (mCalibrator == null) {
            mCalibrator = new CameraCalibrator(width, height, this);
            mOpenCvCameraView.setResolution(mOpenCvCameraView.getResolution());
            mOnCameraFrameRender=new MyRender(mCalibrator,this);
        }
        if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
            mCalibrator.setCalibrated();
            mOnCameraFrameRender=new MyRender(mCalibrator,this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu1, menu);

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionMenu.setGroupCheckable(1,true,true);
        SubMenu mResolutionMenu2 = menu.addSubMenu("Calibrate");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];
        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while (resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(1, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case 1:
                    int id = item.getItemId();
                    Size resolution = mResolutionList.get(id);
                    mOpenCvCameraView.setResolution(resolution);
                    mCalibrator.ResChanged(resolution.width,resolution.height);
                    resolution = mOpenCvCameraView.getResolution();
                    String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
                    Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
                return true;

        default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void toMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat imageFrame = inputFrame.rgba();
        if (mCalibrator.isCalibrated()) {
           // Imgproc.putText(imageFrame, "Calibrated", new Point(50, 50),
            //        Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
            mOnCameraFrameRender.render(imageFrame,inputFrame.gray());
        } else {
            Imgproc.putText(imageFrame, "UnCalibrated", new Point(50, 50),
                    Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
        }
        //if (mOnCameraFrameRender !=null)
           // return mOnCameraFrameRender.render(imageFrame,inputFrame.gray());
        return imageFrame;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Tutorial1 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.opencv.samples.tutorial1/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Tutorial1 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://org.opencv.samples.tutorial1/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    static class MyRender{
        private MatOfPoint3f object;
        private CameraCalibrator mCalibrator;
        private MatOfPoint3f Cobject;
        private List<Point> imagePoints = new ArrayList<Point>();
        List<Point3> Xmodel = new ArrayList<Point3>();
        private MatOfPoint2f ma2 = new MatOfPoint2f();
        private MatOfDouble mCameraMatrix = new MatOfDouble();
        private MatOfDouble mDistortionCoefficients = new MatOfDouble();
        private Mat over;
        public MyRender(CameraCalibrator calibrator, Activity a) {
            mCalibrator = calibrator;
            //Assume UnDistorted
            // 3D model
            mCalibrator.getCameraMatrix().copyTo(mCameraMatrix);
            mCalibrator.getDistortionCoefficients().copyTo(mDistortionCoefficients);
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
            //2D points
            imagePoints.add(new Point(0,0));
            imagePoints.add(new Point(0, 67));
            imagePoints.add(new Point(74, 67));
            imagePoints.add(new Point(74, 0));

            Xmodel.add(new Point3(0,0,0));
            Xmodel.add(new Point3(0,1,0));
            Xmodel.add(new Point3(0.5,0.5,1));
            Xmodel.add(new Point3(1,1,0));
            Xmodel.add(new Point3(1,0,0));
            Xmodel.add(new Point3(0.5,0.5,1));
            Xmodel.add(new Point3(0,0,0));
            MatOfPoint3f Xobject = new MatOfPoint3f();
            Xobject.fromList(Xmodel);
            ma2.fromList(imagePoints);
            object = new MatOfPoint3f();
            Cobject = new MatOfPoint3f();
            object.fromList(model);
            Cobject.fromList(Xmodel);
            Log.i("hej:", "ModelsDone");
            over = Mat.zeros(74, 67, CvType.CV_8UC3);
            Resources res = a.getResources();
            Bitmap xMap = BitmapFactory.decodeResource(res, R.drawable.overlay);  // OverLay 2-D Image
            Utils.bitmapToMat(xMap, over);
        }

        public Mat render(Mat inputFrame,Mat gray) {
            Mat rgbaFrame = inputFrame;
            Mat grayFrame = gray;
            mCalibrator.findPattern(grayFrame);
            if (mCalibrator.patternfound()) {
                Point[] outerCorners = mCalibrator.getouterCorners();
                MatOfPoint m1 = new MatOfPoint();
                m1.fromArray(outerCorners);
                List<MatOfPoint> contor = new ArrayList<>();
                contor.add(m1);
                int width = rgbaFrame.width();
                int height = rgbaFrame.height();
                MatOfPoint2f ma1 = new MatOfPoint2f();
                ma1.fromArray(outerCorners);
                Mat H = Calib3d.findHomography(ma2, ma1, 0, 2);
                Mat overRot = Mat.zeros(width,height,CvType.CV_8UC3);
                Imgproc.warpPerspective(over, overRot, H, new org.opencv.core.Size(width, height));
                Core.addWeighted(rgbaFrame, 0.8, overRot, 1, 0, rgbaFrame);
                Imgproc.drawContours(rgbaFrame, contor, 0, new Scalar(100, 100, 50));

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
                Log.i("hej:", "AtdrawingPhase" + respoints.dump());
                p.fromList(respoints.toList());
                a.add(0, p);
                //p.fromList(respoints.toList().subList(5,9));
                //a.add(1,p);
                Imgproc.drawContours(rgbaFrame, a, 0, new Scalar(200, 50, 50), 10);
            }

            return rgbaFrame;
        }
    }
}
