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
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {

    final String TAG = "CKK-SWPP";
    Mat CamMatrix, DistCoeffs;
    List<Mat> corner = new ArrayList<>();
    int num = 1;

    final double[][] Coordinate ={  {11.143, -6.337, 4.964},//astronaut
                                    {10.905f, -9.806f, 5.195f}, //Area 1 Coordinate
                                    {10.67, -9.45, 4.77},//Area 2 Coordinate
                                    {10.804, -7.925, 4.534}, //Area 3 Coordinate
                                    {10.554,-6.66,4.73}  //Area 4 Coordinate
    };
    final float[][] qua = {{0f, 0f, 0.707f, -0.707f},//Astronaut
                            {0.f ,0.0f , -0.707f, 0.707f},//Area 1 Quaternion
                            {-0.5f, 0.5f, 0.5f, 0.5f},//Area 2 Quaternion
                                {-0.5f, 0.5f, 0.5f, 0.5f},//Area 3 Quaternion
                            {0f, 0.707f, 0.707f, 0f}//Area 4 Quaternion
    };

    @Override
    protected void runPlan1(){
        // The mission starts.
        api.startMission();
        moveTo(1);
        Mat image1 = getImageTrain();
        api.saveMatImage(image1, "Image1.png");
        moveTo(2);
        moveTo(10.925, -8.875, 4.534, -0.5f, 0.5f, 0.5f, 0.5f );
        Mat image2 = getImageTrain();
        api.saveMatImage(image2, "image2.png");
        moveTo((3));
        Mat image3 = getImageTrain();
        api.saveMatImage(image3, "image3.png");
        moveTo((4));
        Mat image4 = getImageTrain();
        api.saveMatImage(image4, "image4.png");

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

    /*private void NAVCamINIT(){
        Mat CamMatrix = new Mat(3, 3, CvType.CV_32F);
        Mat DistCoeffs = new Mat(5, 1, CvType.CV_32F);
        //set Matrix of Cam & coefficient
        float[] Navcam =   {523.105750f, 0.0f, 635.434258f,
                            0.0f, 534.765913f, 500.335102f,
                            0.0f, 0.0f, 1.0f};
        float[] coefficients = {-0.164787f, 0.020375f, -0.001572f, -0.000369f, 0.0f};

        CamMatrix.put(0, 0,Navcam);
        DistCoeffs.put(0, 0, coefficients);
        Log.i(TAG, "CamINIT SUCCESSFUL");
    }*/

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

    private Mat getImageTrain()
    {
        //Set up variable
        int ARTagLength = 5; //Centimetre
        Mat undistorted = new Mat(),
            IDs = new Mat();
        List<Mat> corners = new ArrayList<>();  //Store corner of ARTAg
        Dictionary dict = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);    //ARTAg is 5*5 pixel

        //Get an image
        api.flashlightControlFront(0.05f);
        Mat distort = api. getMatNavCam();
        api.flashlightControlFront(0.0f);


        //Detect ARTag
        Aruco.detectMarkers(distort, dict ,corners, IDs);
        if (IDs != null){ Log.i(TAG, "Found the ARTAG!!"); }
        else {Log.i(TAG, "No ARTAg Found");}


        //Cropping image
        Mat mat_corner = corners.get(0);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        for (int i = 0; i < mat_corner.rows(); i++) {
            double[] c = mat_corner.get(i, 0);
            minX = Math.min(minX, c[0]);
            minY = Math.min(minY, c[1]);
        }
        int x = (int) Math.max(0, minX - 170);
        int y = (int) Math.max(0, minY - 195);
        Rect map = new Rect(x, y, 400, 400);
        Mat cropped_image = new Mat(distort, map);

        //Save original to compare with cropped image
        api.saveMatImage(distort,"Original_Image" + num + ".png");
        num +=1;
        return  cropped_image;
    }
}