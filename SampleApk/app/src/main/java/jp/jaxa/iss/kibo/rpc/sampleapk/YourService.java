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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
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

    final double[][] Coordinate = {{11.143, -6.337, 4.964},//astronaut
            {10.905, -9.806, 5.195}, //Area 1 Coordinate
            {10.67, -9.45, 4.77},//Area 2 Coordinate
            {10.804, -7.925, 4.534}, //Area 3 Coordinate
            {10.554, -6.66, 4.73}  //Area 4 Coordinate
    };
    final float[][] qua = {{0f, 0f, 0.707f, -0.707f},//Astronaut
            {0.f, 0.0f, -0.707f, 0.707f},//Area 1 Quaternion
            {-0.5f, 0.5f, 0.5f, 0.5f},//Area 2 Quaternion
            {-0.5f, 0.5f, 0.5f, 0.5f},//Area 3 Quaternion
            {0f, 0.707f, 0.707f, 0f}//Area 4 Quaternion
    };

    @Override
    protected void runPlan1() {
        // The mission starts.
        api.startMission();
        moveTo(1);
        getImageTrain();

    }

    @Override
    protected void runPlan2() {
        // write your plan 2 here.
    }

    @Override
    protected void runPlan3() {
        // write your plan 3 here.
    }

    private void moveTo(double px, double py, double pz,
                        float qx, float qy, float qz, float qw) {

        Point point = new Point(px, py, pz);
        Quaternion quaternion = new Quaternion(qx, qy, qz, qw);
        Result result;

        //Check the Astrobee arrive location or not
        int loop_count = 0, loop_max = 3;
        do {
            result = api.moveTo(point, quaternion, true);
            loop_count++;
            Log.i(TAG, "Trying move to " + px + "," + py + "," + pz + "|" + qx + "," + qy + "," + qz + "," + qw);

            if (loop_count == loop_max) {
                Log.i(TAG, "Something went wrong");
            } //tell team if Astrobee can't move to coordinate
        } while (!result.hasSucceeded() && loop_count < loop_max);
        Log.i(TAG, "MOVING ENDED");
    }

    private void moveTo(int coor) {
        moveTo(Coordinate[coor][0], Coordinate[coor][1], Coordinate[coor][2],
                qua[coor][0], qua[coor][1], qua[coor][2], qua[coor][3]);
    }

    private void NAVCamINIT() {
        Mat CamMatrix = new Mat(3, 3, CvType.CV_32F);
        Mat DistCoeffs = new Mat(5, 1, CvType.CV_32F);
        //set Matrix of Cam & coefficient
        float[] Navcam = {523.105750f, 0.0f, 635.434258f,
                0.0f, 534.765913f, 500.335102f,
                0.0f, 0.0f, 1.0f};
        float[] coefficients = {-0.164787f, 0.020375f, -0.001572f, -0.000369f, 0.0f};

        CamMatrix.put(0, 0, Navcam);
        DistCoeffs.put(0, 0, coefficients);
        Log.i(TAG, "CamINIT SUCCESSFUL");
    }

    //Corvet matrix into integer
    private List<Integer> MatConvertInt(Mat mat) {
        List<Integer> Convert = new ArrayList<>();

        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                double[] getDouble = mat.get(i, j);
                int getInt = (int) getDouble[0];
                Convert.add(getInt);
            }
        }
        return Convert;
    }

    private void getImageTrain() {
        //Set up variable
        Mat threshold = new Mat(),
            hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        //get  an image from NAVcam Astrobee and processing image
        api.flashlightControlFront(0.05f);
        Mat image = api.getMatNavCam();
        api.flashlightControlFront(0.0f);
        api.saveMatImage(image, "Original_image" + num + ".png");


        /*//Converting aninput image to black-white image
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        Log.i(TAG, "Converting image to gray image (Image" + num + ")");
        api.saveMatImage(image, "Gray_image_" + num + ".png"); */

        //Threshold an image
        Imgproc.threshold(image, threshold, 238, 255, Imgproc.THRESH_BINARY);
        Log.i(TAG, "Converting image to threshold image (Image" + num + ")");
        api.saveMatImage(threshold, "Threshold_image_" + num + ".png");

        //Find  contour
        Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Find the rectrangular shape
        for (MatOfPoint cnt : contours) {

            //set up an value for finding rectrangular shape
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Mat warped = new Mat();

            //calculate to find any contour
            double epsilon = 0.005 * Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            Imgproc.approxPolyDP(new MatOfPoint2f(cnt.toArray()), approxCurve, epsilon, true);
            double area = Imgproc.contourArea(cnt);                                                   //use this func to cal area of contour

            if (approxCurve.total() == 4 && area >= 100) {
                // Ensure it's a rectangle by checking aspect ratio
                Rect rect = Imgproc.boundingRect(cnt);
                double aspectRatio = (double) rect.width / rect.height;

                if (0.9 <= aspectRatio && aspectRatio <= 1.1) { // Allowing for some tolerance in aspect ratio
                    // Get the corner points
                    org.opencv.core.Point[] points = approxCurve.toArray();

                    // Destination coordinates for perspective transform
                    MatOfPoint2f src = new MatOfPoint2f(points);
                    MatOfPoint2f dst = new MatOfPoint2f(new org.opencv.core.Point(0, 0),
                            new org.opencv.core.Point(320, 0),
                            new org.opencv.core.Point(320, 320),
                            new org.opencv.core.Point(0, 320));

                    // Get the perspective transform matrix and use
                    Mat M = Imgproc.getPerspectiveTransform(src, dst);
                    Imgproc.warpPerspective(image, warped, M, new Size(320, 320));
                    api.saveMatImage(warped, "warped_image" + num + ".png");
     /*--------------------------------------------------------------------------------------*/
    num += 1;

                }
            }
        }
    }
}