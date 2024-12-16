package com.jeffrey.fypweatherapp.weather.prediction;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
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
        interpreter7Days = new Interpreter(loadModelFile(context, "model/weather_description_7days.tflite"));
        interpreter24Hours = new Interpreter(loadModelFile(context, "model/weather_description_24hours.tflite"));
        interpreterRain = new Interpreter(loadModelFile(context, "model/rain_prediction_1hour.tflite"));
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
        Log.w("FUCK", String.valueOf(inputData.length + "," + inputData[0].length + "," + inputData[0][0].length));

        // Validate inputData shape
        if (inputData.length != 1 || inputData[0].length != 7 || inputData[0][0].length != 9) {
            throw new IllegalArgumentException("Invalid inputData shape. Expected [1][7][8]");
        }

        // Ensure input matches the required shape
        float[][][] reshapedInput = new float[1][7][9];
        for (int j = 0; j < 7; j++) {
            System.arraycopy(inputData[0][j], 0, reshapedInput[0][j], 0, 8);
        }

        // Run the interpreter
        float[][] outputData = new float[1][16];
        interpreter7Days.run(reshapedInput, outputData);

        // Decode and return the weather descriptions
        return decodeWeatherDescription(outputData[0]);
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
        float[][] outputData = new float[1][16];
        interpreter24Hours.run(inputData, outputData);
        return decodeWeatherDescription(outputData[0]);
    }

    public int predictRainNextHour(float[][][] inputData) {
        float[][] outputData = new float[1][1];
        interpreterRain.run(inputData, outputData);
        return (int) outputData[0][0]; // Threshold for binary classification
    }

    private String[] decodeWeatherDescription(float[] predictions) {
        String[] descriptions = new String[predictions.length];
        for (int i = 0; i < predictions.length; i++) {
            int maxIndex = argMax(predictions); // Find the class with the highest probability
            descriptions[i] = getWeatherDescription(maxIndex); // Map the class index to a description
        }
        return descriptions;
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
                "Thunderstorm with light rain",
                "Thunderstorm with rain",
                "Thunderstorm with heavy rain",
                "Light thunderstorm",
                "Thunderstorm",
                "Heavy thunderstorm",
                "Ragged thunderstorm",
                "Thunderstorm with light drizzle",
                "Thunderstorm with drizzle",
                "Thunderstorm with heavy drizzle",
                "Light intensity drizzle",
                "Drizzle",
                "Heavy intensity drizzle",
                "Light intensity drizzle rain",
                "Drizzle rain",
                "Heavy intensity drizzle rain",
                "Shower rain and drizzle",
                "Heavy shower rain and drizzle",
                "Shower drizzle",
                "Light rain",
                "Moderate rain",
                "Heavy intensity rain",
                "Very heavy rain",
                "Extreme rain",
                "Freezing rain",
                "Light intensity shower rain",
                "Shower rain",
                "Heavy intensity shower rain",
                "Ragged shower rain",
                "Light snow",
                "Snow",
                "Heavy snow",
                "Sleet",
                "Light shower sleet",
                "Shower sleet",
                "Light rain and snow",
                "Rain and snow",
                "Light shower snow",
                "Shower snow",
                "Heavy shower snow",
                "Mist",
                "Smoke",
                "Haze",
                "Sand/ dust whirls",
                "Fog",
                "Sand",
                "Dust",
                "Volcanic ash",
                "Squalls",
                "Tornado",
                "Clear sky",
                "Few clouds",
                "Scattered clouds",
                "Broken clouds",
                "Overcast clouds"
        };

        // Validate the index to avoid ArrayIndexOutOfBoundsException
        if (index < 0 || index >= descriptions.length) {
            return "Unknown";
        }

        return descriptions[index];
    }


}
