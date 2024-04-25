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
        Point point = new Point(10.9d, -9.92284d, 5.195d);
        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(point, quaternion, false);

        // Get a camera image.
        Mat image = api.getMatNavCam();

        /* *********************************************************************** */
        /* Write your code to recognize type and number of items in the each area! */
        /* *********************************************************************** */

        // When you recognize items, let’s set the type and number.
        api.setAreaInfo(1, "item_name", 1);

        /* **************************************************** */
        /* Let's move to the each area and recognize the items. */
        /* **************************************************** */

        // When you move to the front of the astronaut, report the rounding completion.
        api.reportRoundingCompletion();

        /* ********************************************************** */
        /* Write your code to recognize which item the astronaut has. */
        /* ********************************************************** */

        // Let's notify the astronaut when you recognize it.
        api.notifyRecognitionItem();

        /* ******************************************************************************************************* */
        /* Write your code to move Astrobee to the location of the target item (what the astronaut is looking for) */
        /* ******************************************************************************************************* */

        // Take a snapshot of the target item.
        api.takeTargetItemSnapshot();
    }

    @Override
    protected void runPlan2(){
        // write your plan 2 here.
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here.
    }

    private void MoveTo(double px, double py, double pz,
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
