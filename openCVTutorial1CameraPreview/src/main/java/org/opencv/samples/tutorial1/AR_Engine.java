package org.opencv.samples.tutorial1;


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
}
