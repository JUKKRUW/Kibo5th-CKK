package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.util.Log;

import gov.nasa.arc.astrobee.Result;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.calib3d.Calib3d;
import org.opencv.aruco.Dictionary;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {

    final String TAG = "CKK-SWPP";
    Mat IDs, CamMatrix, DistCoeffs, tVec;
    List<Mat> corner = new ArrayList<>();

    final double[][] Coordinate ={{},//astronaut
            {10.905f, -9.806f, 5.195f}, //Area 1 Coordinate
            {10.925f, -8.875f, 4.08803},//Area 2 Coordinate
            {10.804f, -7.925f, 4.867f}, //Area 3 Coordinate
            {11.15f , -6.422f, 4.967f}  //Area 4 Coordinate
    };
    final float[][] qua = {{},//Astronaut
            {0.f ,0.0f , -0.707f, 0.707f},//Area 1 Quaternion
            {-0.5f, 0.5f, 0.5f, 0.5f},//Area 2 Quaternion
            {0.0f, 0.0f, 1.0f, 0.0f},//Area 3 Quaternion
            {0f, 0f, -0.707f, 0.707f}//Area 4 Quaternion
    };

    @Override
    protected void runPlan1(){
        // The mission starts.
        api.startMission();
        moveTo(1);
        NAVCamINIT();
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

            if (loop_count == loop_max) { Log.i(TAG, "Something went wrong"); } //tell team if Astrobee can't move to coordinate
        } while(!result.hasSucceeded() && loop_count < loop_max);
        Log.i(TAG,"MOVING ENDED");
    }

    private void moveTo(int coor){
        moveTo(Coordinate[coor][0], Coordinate[coor][1], Coordinate[coor][2],
                qua[coor][0], qua[coor][1],qua[coor][2], qua[coor][3]);
    }

    private void NAVCamINIT(){
        //set Matrix of Cam & coefficient
        float[] Navcam =   {523.105750f, 0.0f, 635.434258f,
                            0.0f, 534.765913f, 500.335102f,
                            0.0f, 0.0f, 1.0f};
        float[] coefficients = {-0.164787f, 0.020375f, -0.001572f, -0.000369f, 0.0f};

        CamMatrix.put(0, 0,Navcam);
        DistCoeffs.put(0, 0, coefficients);
        Log.i(TAG, "CamINIT SUCCESSFUL");
    }

    //Corvet matrix into integer
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

    private Mat getImageTrain(){
        //Set up variable
        int ARTagLength = 5; //Centimeter
        Mat undistorted = new Mat(),
            IDs = new Mat(),
            rVec = new Mat(),
            tVec = new Mat();
        List<Mat> corners = new ArrayList<>(); //Store corner of ARTAg
        Dictionary dict = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250); //ARTAg is 5*5 pixel

        //no use
        /* DetectorParameters parameters = DetectorParameters.create(); */

        //Get an image and undistrot the image
        api.flashlightControlFront(0.05f);
        Mat distort = api. getMatNavCam();
        api.flashlightControlFront(0.0f);
        Calib3d.undistort(distort, undistorted, CamMatrix, DistCoeffs);//undistorted image

        //Detect ARTag
        Aruco.detectMarkers(undistorted, dict ,corners, IDs);
        if (corners != null){ Log.i(TAG, "Found the ARTAG!!"); }
        else {Log.i(TAG, "No ARTAg Found");}

        //get Rotation vector and translation vectors
        Aruco.estimatePoseSingleMarkers(corners, ARTagLength, CamMatrix, DistCoeffs, rVec, tVec);
        double Dist_x = tVec.get(0, 0)[0];//Distance from camara X-axis
        double Dist_y = tVec.get(1, 0)[0];//Distance from camara y-axis
        double Dist_z = tVec.get(2, 0)[0];//Distance from camara z-axis
        double rotate_x = rVec.get(0, 0)[0];//Rotation of ARTAG  x-axis
        double rotate_y = rVec.get(0, 0)[0];//Rotation of ARTAG  y-axis
        double rotate_z = rVec.get(0, 0)[0];//Rotation of ARTAG  z-axis

        return  undistorted;
    }
}