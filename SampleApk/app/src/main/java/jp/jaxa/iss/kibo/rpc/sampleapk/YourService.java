package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.nfc.Tag;
import android.util.Log;

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

    // You can add your method.
    private void CamINIT(){

    }

    private void NAVCamINIT(){
        Mat CamMatrix = new Mat(3, 3, CvType.CV_32F);
        Mat DistCoeffs = new Mat(3,3,CvType.CV_32F);

        //set Matrix of Cam & coefficient
        float[] Navcam = {523.105750f, 0.0f, 635.434258f, 0.0f, 534.765913f,
                500.335102f,0.0f, 0.0f, 1.0f};
        float[] coefficients = {-0.164787f, 0.020375f, -0.001572f, -0.000369f, 0.0f};
        CamMatrix.put(0, 0,Navcam);
        DistCoeffs.put(0, 0, coefficients);
    }

    private int ARTAGDetection(Mat Source){
        Mat undistort = new Mat(),
            IDs = new Mat();
        List<Mat> corner = new ArrayList<>();
        //Set Flashlight On
        api.flashlightControlFront(0.5f);
        Mat distort = api.getMatNavCam();
        //Set Flashlight Off
        api.flashlightControlFront(0.0f);

        //undistort image
        Calib3d.undistort(distort, undistort, CamMatrix, DistCoeffs);
        //ARTAG Detection
        Dictionary dict = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_50);
        Aruco.detectMarkers(undistort, dict,corner, IDs);

        if (!IDs.empty()){
            // Get the ID of the latest detected marker
            Integer lastestID = (int) IDs.get(IDs.rows() - 1, 0)[0];
            Log.i(TAG, "Marker found IDs:" + lastestID.toString());
        }
        int x;
        return  x = 0;
    }
}
