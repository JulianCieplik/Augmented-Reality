package org.opencv.samples.tutorial1;


import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.ArrayList;
import java.util.List;
import org.opencv.calib3d.Calib3d;

/**
 * Created by Mikael on 2016-04-02.
 * This class should detect,track and replace the markers with 3D contents
 */
public class AR_Engine {
    private static AR_Engine ourInstance = new AR_Engine();

    public static AR_Engine getInstance() {
        return ourInstance;
    }

    private AR_Engine() {
    }

    public void calculateHomography(){
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
        List<Point> imagePoints = new ArrayList<Point>();
        //Calib3d.projectPoints(object,rvec,tvec,cameraMatrix,distCoeffs,imagePoints);
        // drawing functions to look at is draw line and drawContours
    }

}
