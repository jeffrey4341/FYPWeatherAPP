package com.jeffrey.fypweatherapp.weather.prediction;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class WeatherPrediction {
    private static final String TAG = "WeatherPrediction";
    private Interpreter interpreter;

    public WeatherPrediction(Context context, String modelPath) {
        try {
            MappedByteBuffer tfliteModel = loadModelFile(context, modelPath);
            interpreter = new Interpreter(tfliteModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws Exception {
        AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void predictNext24Hours(float[][][] initial24HourData, String[] weatherCategories) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is not initialized");
            return;
        }

        // Validate the input shape
        if (initial24HourData.length != 1 || initial24HourData[0].length != 24 || initial24HourData[0][0].length != 9) {
            Log.e(TAG, "Input data must have the shape [1, 24, 9]. Provided shape: [" + initial24HourData.length + ", " + initial24HourData[0].length + ", " + initial24HourData[0][0].length + "]");
            return;
        }

        // Create a mutable array to hold the rolling input window
        float[][] rollingWindow = new float[24][9];
        for (int i = 0; i < 24; i++) {
            System.arraycopy(initial24HourData[0][i], 0, rollingWindow[i], 0, 9);
        }

        for (int hour = 0; hour < 24; hour++) {
            // Prepare the input tensor for the current prediction
            float[][][] currentInput = new float[1][24][9];
            currentInput[0] = rollingWindow; // Set the rolling window as the current input

            // Prepare output tensor for the prediction
            float[][] outputData = new float[1][weatherCategories.length];

            // Run inference for the next hour
            interpreter.run(currentInput, outputData);

            // Get the predicted category index
            int predictedIndex = argMax(outputData[0]);
            String predictedWeather = weatherCategories[predictedIndex];

            // Log the prediction for the current hour
            Log.d("FUCK", "AI Prediction - Hour " + (hour + 1) + ": " + predictedWeather);

            // Update the rolling window with the predicted hour
            // Example: Fill with dummy normalized values based on the prediction (e.g., 0.5 for simplicity)
            // You may adjust this logic to integrate more realistic next-hour values.
            float[] predictedFeatures = new float[9];
            for (int j = 0; j < 9; j++) {
                predictedFeatures[j] = 0.5f; // Placeholder normalized value
            }

            // Shift the rolling window and add the new prediction
            for (int i = 1; i < 24; i++) {
                rollingWindow[i - 1] = rollingWindow[i];
            }
            rollingWindow[23] = predictedFeatures; // Append predicted features as the last hour
        }
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
