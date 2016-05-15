package org.opencv.samples.tutorial1;


import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import android.util.Log;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Mikael on 2016-04-02.
 * This class should detect,track and replace the markers with 3D contents
 * Store All possible Models That could be inserted
 */
public class AR_Engine {
    private static AR_Engine ourInstance = new AR_Engine();

    public static AR_Engine getInstance() {
        return ourInstance;
    }
    public static MatOfDouble mCameraMatrix = new MatOfDouble();
    public static MatOfDouble mDistortionCoefficients = new MatOfDouble();
    public static Mat rvec = new Mat();
    public static Mat tvec = new Mat();
    public static double scale = 0.5;
    public static double wscale = 1;
    public static double w = 1.5;
    public static double h = 1;
    public static double height = 1; // heightFactor!

    private AR_Engine() {
    }

    public static void solvePnP(MatOfPoint2f ma1){
        MatOfPoint3f object = new MatOfPoint3f();
        object.fromArray(new Point3[]{new Point3(0,0,0),new Point3(0,h,0),new Point3(w,h,0),new Point3(w,0,0)});
        Calib3d.solvePnP(object, ma1, mCameraMatrix, mDistortionCoefficients, rvec, tvec);
    }

    public static MatOfPoint2f projectPoints(MatOfPoint3f Cobject){
        MatOfPoint2f respoints = new MatOfPoint2f();
        Calib3d.projectPoints(Cobject, rvec, tvec, mCameraMatrix, mDistortionCoefficients, respoints);
        return respoints;
    }

    public static void DrawMultiColoredBox(Mat rgbaFrame){
        List<Point3> Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,0,0));
        Face1.add(new Point3(0,h*scale,0));
        Face1.add(new Point3(w*scale,h*scale,0));
        Face1.add(new Point3(w*scale,0,0));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Face1);
        MatOfPoint a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Log.i("typeI","V:"+a.type());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 0, 255), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,0,0));
        Face1.add(new Point3(0,h*scale,0));
        Face1.add(new Point3(0,h*scale,1*scale*height));
        Face1.add(new Point3(0,0,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 255, 0), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,0,0));
        Face1.add(new Point3(w*scale,0,0));
        Face1.add(new Point3(w*scale,0,1*scale*height));
        Face1.add(new Point3(0,0,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();

        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(128, 128, 128), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(w*scale,h*scale,0));
        Face1.add(new Point3(w*scale,0,0));
        Face1.add(new Point3(w*scale,0,1*scale*height));
        Face1.add(new Point3(w*scale,h*scale,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(255, 0, 0), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,h*scale,0));
        Face1.add(new Point3(w*scale,h*scale,0));
        Face1.add(new Point3(w*scale,h*scale,1*scale*height));
        Face1.add(new Point3(0,h*scale,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(255, 255, 0), 8,0);
    }

    public static void DrawOneColoredBox(Mat rgbaFrame){
        List<Point3> Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,0,0));
        Face1.add(new Point3(0,h*scale,0));
        Face1.add(new Point3(w*scale,h*scale,0));
        Face1.add(new Point3(w*scale,0,0));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Face1);
        MatOfPoint a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Log.i("typeI","V:"+a.type());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 0, 0), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,0,0));
        Face1.add(new Point3(0,h*scale,0));
        Face1.add(new Point3(0,h*scale,1*scale*height));
        Face1.add(new Point3(0,0,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 0, 0), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,0,0));
        Face1.add(new Point3(w*scale,0,0));
        Face1.add(new Point3(w*scale,0,1*scale*height));
        Face1.add(new Point3(0,0,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();

        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 0, 0), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(w*scale,h*scale,0));
        Face1.add(new Point3(w*scale,0,0));
        Face1.add(new Point3(w*scale,0,1*scale*height));
        Face1.add(new Point3(w*scale,h*scale,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 0, 0), 8,0);
        Face1 = new ArrayList<Point3>();
        Face1.add(new Point3(0,h*scale,0));
        Face1.add(new Point3(w*scale,h*scale,0));
        Face1.add(new Point3(w*scale,h*scale,1*scale*height));
        Face1.add(new Point3(0,h*scale,1*scale*height));
        res= new MatOfPoint3f();
        res.fromList(Face1);
        a = new MatOfPoint();
        a.fromArray(projectPoints(res).toArray());
        Imgproc.fillConvexPoly(rgbaFrame, a, new Scalar(0, 0, 0), 8,0);
    }

    public static MatOfPoint3f Get3Dfigure(int type){
        switch(type){
            case 1: return Triangle();
            case 2: return SquareBox();
            case 3: return SquareBox();
            case 4: return SphereRoid();
            default: return Nothing();
        }
    }
    private static MatOfPoint3f Nothing(){
        List<Point3> Xmodel = new ArrayList<Point3>();
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(0,0.01,0));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Xmodel);
        return res;
    }

    public static void drawAxis(Mat rgbaFrame){
        Point[] axises=AR_Engine.projectPoints(AR_Engine.D3Axis()).toArray();
        Imgproc.line(rgbaFrame,axises[0],axises[1],new Scalar(0,255,0),5);
        Imgproc.line(rgbaFrame,axises[0],axises[2],new Scalar(0,0,255),5);
        Imgproc.line(rgbaFrame,axises[0],axises[3],new Scalar(255,0,0),5);
    }

    // WireFrames HardCoded (Static Method)
    public static MatOfPoint3f D3Axis(){
        List<Point3> Xmodel = new ArrayList<Point3>();
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(w,0,0));
        Xmodel.add(new Point3(0,h,0));
        Xmodel.add(new Point3(0,0,1));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Xmodel);
        return res;
    }

    public static MatOfPoint3f Triangle(){
        List<Point3> Xmodel = new ArrayList<Point3>();
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(0,h*wscale,0));
        Xmodel.add(new Point3(0.5*wscale,0.5*wscale,1*wscale*height));
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(w*wscale,0,0));
        Xmodel.add(new Point3(0.5*wscale,0.5*wscale,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,0,0));
        Xmodel.add(new Point3(w*wscale,h*wscale,0));
        Xmodel.add(new Point3(0.5*wscale,0.5*wscale,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,h*wscale,0));
        Xmodel.add(new Point3(0,h*wscale,0));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Xmodel);
        return res;
    }
    public static MatOfPoint3f TrianglePart(){
        List<Point3> Xmodel = new ArrayList<Point3>();
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(0,h*wscale,0));
        Xmodel.add(new Point3(0.5*wscale,0.5*wscale,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,h*wscale,0));
        Xmodel.add(new Point3(w*wscale,0,0));
        Xmodel.add(new Point3(0.5,0.5*wscale,1*wscale*height));
        Xmodel.add(new Point3(0,0,0));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Xmodel);
        return res;
    }

    /**
     * Closed SquareBox : All Corners
     * @return
     */
    public static MatOfPoint3f SquareBox(){
        List<Point3> Xmodel = new ArrayList<Point3>();
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(0,h*wscale,0));
        Xmodel.add(new Point3(w*wscale,h*wscale,0));
        Xmodel.add(new Point3(w*wscale,0,0));
        Xmodel.add(new Point3(0,0,0));
        Xmodel.add(new Point3(0,0,1*wscale*height));
        Xmodel.add(new Point3(0,h*wscale,1*wscale*height));
        Xmodel.add(new Point3(0,h*wscale,0));
        Xmodel.add(new Point3(0,h*wscale,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,h*wscale,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,h*wscale,0));
        Xmodel.add(new Point3(w*wscale,h*wscale,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,0,1*wscale*height));
        Xmodel.add(new Point3(w*wscale,0,0));
        Xmodel.add(new Point3(w*wscale,0,1*wscale*height));
        Xmodel.add(new Point3(0,0,1*wscale*height));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Xmodel);
        return res;
    }

    public static MatOfPoint3f SphereRoid(){
        List<Point3> Xmodel = new ArrayList<Point3>();
        Xmodel.add(new Point3(1,0.5,0.5));
        Xmodel.add(new Point3(0.5,0.5,1));
        Xmodel.add(new Point3(0,0.5,0.5));
        Xmodel.add(new Point3(0.5,0.5,0));
        Xmodel.add(new Point3(1,0.5,0.5));
        Xmodel.add(new Point3(0.5,0,0.5));
        Xmodel.add(new Point3(0,0.5,0.5));
        Xmodel.add(new Point3(0.5,1,0.5));
        Xmodel.add(new Point3(1,0.5,0.5));
        MatOfPoint3f res= new MatOfPoint3f();
        res.fromList(Xmodel);
        return res;
    }

}
