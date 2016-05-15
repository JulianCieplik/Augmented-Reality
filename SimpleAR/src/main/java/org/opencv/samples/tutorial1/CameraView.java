package org.opencv.samples.tutorial1;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.hardware.Camera.Size;
import android.hardware.Camera.PictureCallback;
import android.util.Log;


import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;

/**
 * Created by Mikael on 2016-04-10.
 */
public class CameraView extends JavaCameraView implements PictureCallback {
    private static Size current;
    
    private static final String TAG = "CamView";
    private String mPictureFileName;
    private int i=0;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPictureFileName="calib"+i+".jpg";

    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        current = resolution;
        connectCamera(getWidth(), getHeight());
    }
    public void updateResolution() {
        disconnectCamera();
        mMaxHeight = current.height;
        mMaxWidth = current.width;
        connectCamera( mMaxWidth, mMaxHeight);
    }

    public void MdisconnectCamera(){
        disconnectCamera();
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }



    public void takePicture() {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName ="calib"+i+".jpg";
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
        i++;
    }



    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            fos.write(data);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

    }

}
