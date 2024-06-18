package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
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

    private Interpreter tflite;
    private List<String> labels;
    private ObjectDetector objectDetector;


    public DetectionHelper(Context context) throws IOException {

        tflite = new Interpreter(FileUtil.loadMappedFile(context, "detect.tflite"));
        labels = FileUtil.loadLabels(context, "labelmap.txt");
        ObjectDetectorOptions options = ObjectDetectorOptions.builder()
                .setMaxResults(10) // Maximum number of detected objects
                .setScoreThreshold(0.6f)
                .build();
        objectDetector = ObjectDetector.createFromFileAndOptions(context, "detect.tflite", options);

    }

    public List<Detection> detect(Bitmap bitmap) {
        // Convert bitmap to TensorImage
        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(bitmap);

        // Run object detection on the input image
        return objectDetector.detect(tensorImage);
    }

    public List<String> getLabels() {
        return labels;
    }

}
