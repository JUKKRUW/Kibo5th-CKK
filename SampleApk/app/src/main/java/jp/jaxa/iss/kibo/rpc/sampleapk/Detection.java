package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.content.Context;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class Detection {
    private Interpreter tflite;

    public Detection(Context context) throws IOException {
        MappedByteBuffer tfliteModel = loadModelFile(context, "model.tflite");
        tflite = new Interpreter(tfliteModel);
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public DetectionResult runInference(Mat inputImage) {
        // Preprocess the image (e.g., resize and normalize)
        Mat resizedImage = new Mat();
        Imgproc.resize(inputImage, resizedImage, new Size(320, 320)); // Adjust size as needed
        resizedImage.convertTo(resizedImage, CvType.CV_32F);

        // Convert Mat to ByteBuffer
        ByteBuffer imgData = ByteBuffer.allocateDirect(4 * 300 * 300 * 3);
        imgData.order(ByteOrder.nativeOrder());
        resizedImage.get(0, 0, imgData.array());

        // Output arrays for the model (adjust sizes as needed)
        float[][][] boxes = new float[1][10][4]; // Example for 10 detections
        float[][] scores = new float[1][10];
        float[][] classes = new float[1][10];
        float[] numDetections = new float[1];

        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, boxes);
        outputMap.put(1, classes);
        outputMap.put(2, scores);
        outputMap.put(3, numDetections);

        tflite.runForMultipleInputsOutputs(inputArray, outputMap);

        // Post-process the output to get classes and number of detections
        int numDet = (int) numDetections[0];
        float[] detectedClasses = new float[numDet];
        System.arraycopy(classes[0], 0, detectedClasses, 0, numDet);

        return new DetectionResult(detectedClasses, numDet);
    }
}

class DetectionResult {
    public final float[] classes;
    public final int numDetections;

    public DetectionResult(float[] classes, int numDetections) {
        this.classes = classes;
        this.numDetections = numDetections;
    }
}
