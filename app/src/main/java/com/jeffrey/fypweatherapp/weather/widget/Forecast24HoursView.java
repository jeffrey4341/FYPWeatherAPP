package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jeffrey.fypweatherapp.R;

import java.util.Arrays;

public class Forecast24HoursView extends LinearLayout {

    public Forecast24HoursView(Context context) {
        super(context);
        init();
    }

    public Forecast24HoursView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Forecast24HoursView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL); // Ensure the layout is vertical by default
    }

    public void displayForecast(String[] predictions24Hours) {
        //removeAllViews(); // Clear any existing views

        for (int i = 0; i < predictions24Hours.length; i++) {
            String hourPrediction = predictions24Hours[i];
            String[] conditions = hourPrediction.split("\\s+"); // Split forecast data by space
            //Log.d("FUCK", Arrays.toString(conditions));
            // Variables to track the highest percentage and corresponding weather label
            String highestLabel = "Unknown";
            double highestPercent = 0;

            // Extract weather condition percentages
            for (int j = 1; j < conditions.length; j++) { // Assuming format: Clouds 81.63% Rain 14.61%
                try {
                    String label = conditions[j - 1]; // Weather condition (e.g., Clouds)
                    //Log.d("FUCK", label);
                    double percent = Double.parseDouble(conditions[j].replace("%", ""));
                    //Log.d("FUCK", String.valueOf(percent));

                    if (percent > highestPercent) {
                        highestPercent = percent;
                        //Log.d("FUCK", String.valueOf(highestPercent));
                        highestLabel = label;
//                        Log.d("FUCK", highestLabel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Create a horizontal LinearLayout for each hour's forecast
            LinearLayout hourLayout = new LinearLayout(getContext());
            hourLayout.setOrientation(LinearLayout.HORIZONTAL);
            hourLayout.setPadding(8, 8, 8, 8); // Add padding
            hourLayout.setWeightSum(3); // Total weight for equal distribution

            // Hour label
            TextView hourLabel = new TextView(getContext());
            hourLabel.setText("Hour " + (i + 1) + ": ");
            hourLabel.setTextColor(getResources().getColor(R.color.w_text_primary));
            hourLabel.setTextSize(16);
            hourLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

            // Forecast data with the highest weather condition
            TextView hourDetailsForecast = new TextView(getContext());
            hourDetailsForecast.setText(highestLabel + " (" + String.format("%.2f", highestPercent) + "%)");
            hourDetailsForecast.setTextColor(getResources().getColor(R.color.w_text_secondary));
            hourDetailsForecast.setTextSize(16);
            hourDetailsForecast.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

            // Forecast data details
            TextView hourForecast = new TextView(getContext());
            hourForecast.setText(predictions24Hours[i]);
            hourForecast.setTextColor(getResources().getColor(R.color.w_text_secondary));
            hourForecast.setTextSize(16);
            hourForecast.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

            // Add views to the horizontal layout
            hourLayout.addView(hourLabel);
            hourLayout.addView(hourDetailsForecast);
            hourLayout.addView(hourForecast);

            // Add horizontal layout to the parent LinearLayout
            addView(hourLayout);

            // Add a divider for better visibility
            View divider = new View(getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(getResources().getColor(R.color.w_text_secondary));
            addView(divider);
        }
    }
}
