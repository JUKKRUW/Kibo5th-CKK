package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.util.Log;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.aruco.Aruco;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.calib3d.Calib3d;
import org.opencv.aruco.Dictionary;

import java.util.ArrayList;
import java.util.List;


/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {

    final String TAG = "CKK-SWPP";
    Mat CamMatrix, DistCoeffs;
    final double[][] Coor ={{}};//location
    final float[][] Qua_Coor ={{}};//quaternion

    @Override
    protected void runPlan1(){
        // The mission starts.
        api.startMission();

        // Move to a point.
        moveTo(10.804, -9.806, 5.2372, 0f, 0f, -0.707f, 0.707f);
        // Get a camera image.
        Mat image = api.getMatNavCam();
        api.saveMatImage(image, "image1");
        api.setAreaInfo(1, "item_name", 1);
        //1
        moveTo(10.67, -9.45, 4.77, -0.5f, 0.5f, 0.5f, 0.5f);
        moveTo(10.925, -8.875, 4.534, -0.5f, 0.5f, 0.5f, 0.5f);
        Mat image1 = api.getMatNavCam();
        api.saveMatImage(image1, "image2");
        api.setAreaInfo(2, "item_name", 1);
        //2
        moveTo(10.804, -7.925, 4.534, -0.5f, 0.5f, 0.5f, 0.5f);
        Mat image2 = api.getMatNavCam();
        api.saveMatImage(image2, "image3");
        api.setAreaInfo(3, "item_name", 1);
        //3
        moveTo(10.804, -7.925, 4.867, 0f, 0f, 1f, 0f);
        Mat image3 = api.getMatNavCam();
        api.saveMatImage(image3, "image4");
        api.setAreaInfo(4, "item_name", 1);
        //4
        moveTo(11.15, -6.422, 4.967, 0f, 0f, -0.707f, 0.707f);
        Mat image4 = api.getMatNavCam();
        api.saveMatImage(image4, "image5");
        api.reportRoundingCompletion();
        api.notifyRecognitionItem();
        api.takeTargetItemSnapshot();
    }
    }

    @Override
    protected void runPlan2(){
        // write your plan 2 here.
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here.
    }

    private void moveTo(double px, double py, double pz,
                        float qx, float qy, float qz, float qw){

        Point point = new Point(px, py, pz);
        Quaternion quaternion = new Quaternion(qx, qy, qz, qw);
        Result result;

        //Check the Astrobee arrive location or not
        int loop_count = 0, loop_max =3;
        do {
            result = api.moveTo(point, quaternion, true);
            loop_count++;
            Log.i(TAG, "Trying move to " + px + "," + py + "," + pz + "|" + qx + "," + qy + "," + qz + "," + qw);

            if (loop_count == loop_max) { Log.i(TAG, "Somethin went wrong"); } //tell team if Astrobee can't move to coordinate
        } while(!result.hasSucceeded() && loop_count < loop_max);
        Log.i(TAG,"MOVING ENDED");
    }

    private void NAVCamINIT(){
        Mat CamMatrix = new Mat(3, 3, CvType.CV_32F);//New Camera Matrix
        Mat DistCoeffs = new Mat(3,3,CvType.CV_32F);//New DistCoeffs
        //set Matrix of Cam & coefficient
        float[] Navcam = {523.105750f, 0.0f, 635.434258f,
                0.0f, 534.765913f, 500.335102f,
                0.0f, 0.0f, 1.0f};
        float[] coefficients = {-0.164787f, 0.020375f, -0.001572f, -0.000369f, 0.0f};
        CamMatrix.put(0, 0,Navcam);
        DistCoeffs.put(0, 0, coefficients);
        Log.i(TAG, "CamINIT SUCCESSFUL");
    }

    private void ARTAGDetection(Mat id){
        Mat undistort = new Mat();
        List<Mat> corner = new ArrayList<>();
        api.flashlightControlFront(0.5f); //Set Flashlight On
        Mat distort = api.getMatNavCam();//get Mat NavCam
        api.flashlightControlFront(0.0f);//Set Flashlight Off

        //undistort image
        Calib3d.undistort(distort, undistort, CamMatrix, DistCoeffs);
        Log.i(TAG, "Undistorted image succeed");

        //ARTAG Detection
        Dictionary dict = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_50);
        Aruco.detectMarkers(undistort, dict,corner, id);
    }

    //Corvet matrix int into integer
    private List<Integer> MatConvertInt(Mat mat){
        List<Integer> Convert = new ArrayList<>();
        for(int i = 0; i < mat.rows(); i++){
            for(int j = 0; j < mat.cols(); j++){
                double[] getDouble =  mat.get(i, j);
                int getInt = (int) getDouble[0];
                Convert.add(getInt);
            }
        }
        return Convert;
    }
}
