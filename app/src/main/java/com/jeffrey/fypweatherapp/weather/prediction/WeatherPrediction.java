package com.jeffrey.fypweatherapp.weather.prediction;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeatherPrediction {
    private static final String TAG = "WeatherPrediction";

    private Interpreter interpreter7Days;
    private Interpreter interpreter24Hours;
    private Interpreter interpreterRain;

    public WeatherPrediction(Context context) throws IOException {
        interpreter7Days = new Interpreter(loadModelFile(context, "model/weather_7days_forecast.tflite"));
        interpreter24Hours = new Interpreter(loadModelFile(context, "model/weather_24hours_forecast.tflite"));
        interpreterRain = new Interpreter(loadModelFile(context, "model/rain_percentage_forecast.tflite"));
    }

    private MappedByteBuffer loadModelFile(Context context, String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public String[] predictWeatherDescription7Days(float[][][] inputData) {
        Log.w("FUCK", "Input shape: " + inputData.length + "," + inputData[0].length + "," + inputData[0][0].length);

        // Validate inputData shape
        if (inputData.length != 1 || inputData[0].length != 7 || inputData[0][0].length != 9) {
            throw new IllegalArgumentException("Invalid inputData shape. Expected [1][7][9]");
        }

        // Prepare output array to store predictions
        String[] weatherDescriptions = new String[7]; // 7 days

        // Loop through each day's data
        for (int day = 0; day < 7; day++) {
            // Extract single-day input
            float[][][] singleDayInput = new float[1][1][9]; // Shape [1][1][9]
            for (int feature = 0; feature < 9; feature++) {
                singleDayInput[0][0][feature] = inputData[0][day][feature];
            }

            // Prepare output buffer
            float[][] outputData = new float[1][5]; // Assuming 5 possible classes for weather_main

            // Run the model
            interpreter7Days.run(singleDayInput, outputData);

            // Decode and store the prediction for this day
            Log.d("FUCK", "7day" + Arrays.deepToString(outputData));
            weatherDescriptions[day] = decodeWeatherDescription(outputData[0]);
        }

        return weatherDescriptions;
    }




    // Helper method to combine results into a single array or string
    private String[] combineDecodedDescriptions(String[][] decodedDescriptions) {
        List<String> combined = new ArrayList<>();
        for (String[] descriptions : decodedDescriptions) {
            Collections.addAll(combined, descriptions);
        }
        return combined.toArray(new String[0]);
    }


    public String[] predictWeatherDescription24Hours(float[][][] inputData) {
        Log.w("FUCK", "Input shape: " + inputData.length + "," + inputData[0].length + "," + inputData[0][0].length);

        // Validate inputData shape
        if (inputData.length != 1 || inputData[0].length != 24 || inputData[0][0].length != 9) {
            throw new IllegalArgumentException("Invalid inputData shape. Expected [1][24][9]");
        }

        // Prepare output array to store predictions
        String[] weatherDescriptions = new String[24]; // 24hours

        // Loop through each day's data
        for (int hour = 0; hour < 24; hour++) {
            // Extract single-day input
            float[][][] singleDayInput = new float[1][1][9]; // Shape [1][1][9]
            for (int feature = 0; feature < 9; feature++) {
                singleDayInput[0][0][feature] = inputData[0][hour][feature];
            }

            // Prepare output buffer
            float[][] outputData = new float[1][5]; // Assuming 5 possible classes for weather_main

            // Run the model
            interpreter7Days.run(singleDayInput, outputData);

            // Decode and store the prediction for this day
            Log.d("FUCK", Arrays.deepToString(outputData));
            weatherDescriptions[hour] = decodeWeatherDescription(outputData[0]);
        }

        return weatherDescriptions;
    }

    public float predictRainNextHour(float[][][] inputData) {
        float[][] outputData = new float[1][1];
        interpreterRain.run(inputData, outputData);
        return outputData[0][0]; // Threshold for binary classification
    }

    private String decodeWeatherDescription(float[] logits) {
        Log.e("FUCK", "Logits: " + Arrays.toString(logits));

        // Map index to weather descriptions
        String[] weatherMain = {"Clouds", "\nThunderstorm", "\nRain", "\nClear", "\nMist"};

        // Build a result string with percentages
        StringBuilder result = new StringBuilder();
        float total = 0;

        // Calculate total for percentage calculation (optional if logits are normalized)
        for (float logit : logits) {
            total += logit;
        }

        for (int i = 0; i < logits.length; i++) {
            // Calculate percentage
            float percentage = (logits[i] / total) * 100;

            // Append to the result string
            result.append(String.format("%s %.2f%%", weatherMain[i], percentage));

            if (i < logits.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
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

    private String getWeatherDescription(int index) {
        // Map index to a weather description (use your label encoder's mapping)
        String[] descriptions = {
                "Clouds",
                "Thunderstorm",
                "Rain",
                "Clear",
                "Mist"
        };

        // Validate the index to avoid ArrayIndexOutOfBoundsException
        if (index < 0 || index >= descriptions.length) {
            return "Unknown";
        }

        return descriptions[index];
    }


}
