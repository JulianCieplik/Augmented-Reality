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

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import static android.widget.Toast.makeText;

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
    private boolean started = false;


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
        if (mCalibrator!=null) {
            if (!mCalibrator.isCalibrated() && CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
                mCalibrator.setCalibrated();
                mCalibrator.ReloadSettings(this);
                mCalibrator.ResChanged(mOpenCvCameraView.getWidth(), mOpenCvCameraView.getHeight());
                mOnCameraFrameRender = new MyRender(mCalibrator, this);
            }
            mCalibrator.ReloadSettings(this);
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
        if (mCalibrator.patternfound()){
            mOpenCvCameraView.takePicture();
            String resultMessage = "ImageTakenDone!";
            (makeText(null, resultMessage, Toast.LENGTH_LONG)).show();
        }
        started=true;
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
                   // mCalibrator.ResChanged(resolution.width,resolution.height);
                    mOnCameraFrameRender.resChanged();
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
        if (mCalibrator.isCalibrated() && started) {
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
        private MatOfPoint2f ma2 = new MatOfPoint2f();
        private MatOfPoint2f maz = new MatOfPoint2f();
        private Mat over;
        private boolean showHomog;
        public int show3Dwire= 1;
        public int solid=1;
        private boolean wireframe=true;
        public void resChanged(){
        }

        public MyRender(CameraCalibrator calibrator, Activity a) {
            mCalibrator = calibrator;
            //Assume UnDistorted
            // 3D model
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(a);
            int showHomo = Integer.valueOf(mPrefs.getString("zWork","1"));  // Display The Homography
            show3Dwire = Integer.valueOf(mPrefs.getString("3Dframewire","1"));  // Display The Homography
            showHomog = showHomo==1;
            wireframe=Integer.valueOf(mPrefs.getString("figure","1"))==1;
            solid = Integer.valueOf(mPrefs.getString("Solid","1"));
            float homoScale = Float.valueOf(mPrefs.getString("homoScale","1.0")); //Scale of Homography
            AR_Engine.solid = Integer.valueOf(mPrefs.getString("Solid","1"));
            AR_Engine.height =  Float.valueOf(mPrefs.getString("wfh","1.0"));
            AR_Engine.w =  Float.valueOf(mPrefs.getString("wscale","1.0"));
            AR_Engine.h =  Float.valueOf(mPrefs.getString("height","1.0"));
            AR_Engine.scale = Float.valueOf(mPrefs.getString("bscale","1.0"));
            AR_Engine.wscale = Float.valueOf(mPrefs.getString("wiscale","1.0"));
            //2D points (1/2 scaled)
            List<Point> imagePoints = new ArrayList<Point>();
            imagePoints.add(new Point(0,0));
            imagePoints.add(new Point(0, 67*homoScale));
            imagePoints.add(new Point(74*homoScale, 67*homoScale));
            imagePoints.add(new Point(74*homoScale, 0));
            ma2.fromList(imagePoints);
            List<Point> imagePointsB = new ArrayList<Point>();
            imagePointsB.add(new Point(0, 0));
            imagePointsB.add(new Point(0, 100*homoScale));
            imagePointsB.add(new Point(100*homoScale, 100*homoScale));
            imagePointsB.add(new Point(100*homoScale, 0));
            MatOfPoint2f maz = new MatOfPoint2f();
            maz.fromList(imagePointsB);
            Cobject=AR_Engine.Get3Dfigure(show3Dwire);
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
                MatOfPoint2f ma1 = new MatOfPoint2f();
                ma1.fromArray(outerCorners);
                MatOfPoint m1 = new MatOfPoint();
                m1.fromArray(outerCorners);
                List<MatOfPoint> contor = new ArrayList<>();
                contor.add(m1);
                Imgproc.drawContours(rgbaFrame, contor, 0, new Scalar(100, 100, 50));
                int width = rgbaFrame.width();
                int height = rgbaFrame.height();
                if (showHomog) {
                    Mat H = Calib3d.findHomography(ma2, ma1, 0, 3);
                    Mat overRot = Mat.zeros(width, height, CvType.CV_8UC3);
                    Imgproc.warpPerspective(over, overRot, H, new org.opencv.core.Size(width, height));
                    Core.addWeighted(rgbaFrame, 0.8, overRot, 1, 0, rgbaFrame);
                }

                AR_Engine.solvePnP(ma1);
                AR_Engine.drawAxis(rgbaFrame);
                if (wireframe) {
                    MatOfPoint2f respoints = AR_Engine.projectPoints(Cobject);
                    MatOfPoint p = new MatOfPoint();
                    p.convertTo(p, CvType.CV_32S);
                    List<MatOfPoint> a = new ArrayList<>();
                    p.fromList(respoints.toList());
                    a.add(0, p);
                    Imgproc.drawContours(rgbaFrame, a, 0, new Scalar(200, 50, 50), 10);
                }
                AR_Engine.DrawSolid(rgbaFrame);

            }

            return rgbaFrame;
        }
    }
}
