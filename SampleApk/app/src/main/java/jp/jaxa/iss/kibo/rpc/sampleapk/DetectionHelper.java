package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionHelper {

    private ObjectDetector objectDetector;

    public DetectionHelper()

    {
        ObjectDetectorOptions options = ObjectDetectorOptions.builder()
                .setMaxResults(10) // Maximum number of detected objects
                .setScoreThreshold(0.6f)
                .build();
        try {
            Context context = null;
            objectDetector = ObjectDetector.createFromFileAndOptions(
                    context,
                    "detect.tflite",
                    options);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<Detection> detectObjects(Mat InputImage) {
        Bitmap bitmap = null;
        Utils.matToBitmap(InputImage, bitmap);
        return objectDetector.detect(TensorImage.fromBitmap(bitmap));
    }

    public List<String> NameOfDetection(Mat InputImage){

        Map<String, Integer> detectionCounts = new HashMap<>();
        List<String> AllNames = new ArrayList<>();
        List<Detection> results = detectObjects(InputImage);
        for (Detection detection : results){
            String detectedclass = detection.getCategories().get(0).getLabel();
            AllNames.add(detectedclass);
        }

        return AllNames;

    }

}
