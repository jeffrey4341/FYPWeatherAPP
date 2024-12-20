package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jeffrey.fypweatherapp.R;

public class Forecast7DaysView extends LinearLayout {

    public Forecast7DaysView(Context context) {
        super(context);
        init();
    }

    public Forecast7DaysView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Forecast7DaysView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL); // Ensure the layout is vertical by default
    }

    public void displayForecast(String[] predictions7Days) {
        //removeAllViews(); // Clear any existing views

        for (int i = 0; i < predictions7Days.length; i++) {
            String dayPrediction = predictions7Days[i];
            String[] conditions = dayPrediction.split("\\s+"); // Split forecast data by space

            // Variables to track the highest percentage and corresponding weather label
            String highestLabel = "Unknown";
            double highestPercent = 0;

            // Extract weather condition percentages
            for (int j = 1; j < conditions.length; j += 5) { // Assuming format: Clouds 81.63% Rain 14.61%
                try {
                    String label = conditions[j - 1]; // Weather condition (e.g., Clouds)
                    double percent = Double.parseDouble(conditions[j].replace("%", ""));

                    if (percent > highestPercent) {
                        highestPercent = percent;
                        highestLabel = label;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Create a vertical layout for each day's forecast
            LinearLayout dayLayout = new LinearLayout(getContext());
            dayLayout.setOrientation(LinearLayout.HORIZONTAL);
            dayLayout.setPadding(8, 8, 8, 8); // Add padding
            dayLayout.setWeightSum(3);

            // Day label
            TextView dayLabel = new TextView(getContext());
            dayLabel.setText("Day " + (i + 1) + ": ");
            dayLabel.setTextColor(getResources().getColor(R.color.w_text_primary));
            dayLabel.setTextSize(16);
            dayLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

            // Forecast data with the highest weather condition
            TextView dayForecast = new TextView(getContext());
            dayForecast.setText(highestLabel + " (" + String.format("%.2f", highestPercent) + "%)");
            dayForecast.setTextColor(getResources().getColor(R.color.w_text_secondary));
            dayForecast.setTextSize(16);
            dayForecast.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

            // Forecast data details
            TextView dayDetailsForecast = new TextView(getContext());
            dayDetailsForecast.setText(predictions7Days[i]);
            dayDetailsForecast.setTextColor(getResources().getColor(R.color.w_text_secondary));
            dayDetailsForecast.setTextSize(16);
            dayDetailsForecast.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

            // Add views to the horizontal layout
            dayLayout.addView(dayLabel);
            dayLayout.addView(dayForecast);
            dayLayout.addView(dayDetailsForecast);

            // Add horizontal layout to the parent LinearLayout
            addView(dayLayout);

            // Add a divider between days
            View divider = new View(getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(getResources().getColor(R.color.w_text_secondary));
            addView(divider);
        }
    }
}
