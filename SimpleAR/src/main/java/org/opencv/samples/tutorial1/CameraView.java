package org.opencv.samples.tutorial1;

import android.content.Context;
import android.util.AttributeSet;
import android.hardware.Camera.Size;

import org.opencv.android.JavaCameraView;

import java.util.List;

/**
 * Created by Mikael on 2016-04-10.
 */
public class CameraView extends JavaCameraView {
    private static Size current;
    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

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
        //mCamera.unlock();
        disconnectCamera();
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
}
