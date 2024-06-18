package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionHelper {

    private Interpreter tflite;
    private List<String> labels;
    private int[] inputShape;
    private DataType inputDataType;
    private int[] outputShape;
    private DataType outputDataType;

    public DetectionHelper(Context context) throws IOException {
        // Initialize TFLite interpreter
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "detect.tflite");
        Interpreter.Options tfliteOptions = new Interpreter.Options();
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        // Load labels
        labels = FileUtil.loadLabels(context, "labelmap.txt");

        // Get input and output tensor details
        inputShape = tflite.getInputTensor(0).shape();
        inputDataType = tflite.getInputTensor(0).dataType();
        outputShape = tflite.getOutputTensor(0).shape();
        outputDataType = tflite.getOutputTensor(0).dataType();
    }

    public Map<String, Float> detectObjects(Bitmap bitmap) {
        // Load image
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 320, 320, true);
        TensorImage inputImage = new TensorImage(inputDataType);

        // Load the bitmap into TensorImage
        inputImage.load(resizedBitmap);

        // Image normalization (assuming model requires [0, 1] normalization)
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new NormalizeOp(0.0f, 1.0f))  // or use (127.5f, 127.5f) for [-1, 1] normalization
                .build();
        inputImage = imageProcessor.process(inputImage);

        // Prepare output tensor
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        // Run inference
        tflite.run(inputImage.getBuffer(), outputBuffer.getBuffer().rewind());

        // Post-processing: Get labels and confidence scores
        TensorLabel tensorLabel = new TensorLabel(labels, outputBuffer);
        Map<String, Float> floatMap = tensorLabel.getMapWithFloatValue();

        return floatMap;

    }

    // Utility function to close interpreter
    public void close() {
        tflite.close();
    }
}
