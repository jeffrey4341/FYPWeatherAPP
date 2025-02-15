package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.jeffrey.fypweatherapp.weather.MainActivity;
import com.jeffrey.fypweatherapp.weather.api.entity.AirQualityResponse;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;

public class AqiView extends View {
	private final float density;
	private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
	private RectF rectF = new RectF();
	private int aqi = -1; // Default value if no AQI data
	private String airQuality = ""; // Default air quality description

	public AqiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		textPaint.setTextAlign(Align.CENTER);
		if (isInEditMode()) {
			return;
		}
		textPaint.setTypeface(MainActivity.getTypeface(context));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final float w = getWidth();
		final float h = getHeight();
		if (w <= 0 || h <= 0) {
			return;
		}
		final float lineSize = h / 10f; // Line height
		if (aqi < 0) { // No AQI data available
			textPaint.setStyle(Style.FILL);
			textPaint.setTextSize(lineSize * 1.25f);
			textPaint.setColor(0xaaffffff);
			canvas.drawText("No Data", w / 2f, h / 2f, textPaint);
			return;
		}

		float currAqiPercent = Math.min((float) aqi / 5f, 1f); // Calculate AQI percentage

		// Draw AQI background arc
		float aqiArcRadius = lineSize * 4f;
		textPaint.setStyle(Style.STROKE);
		textPaint.setStrokeWidth(lineSize);
		textPaint.setColor(0x55ffffff);
		rectF.set(-aqiArcRadius, -aqiArcRadius, aqiArcRadius, aqiArcRadius);
		final int saveCount = canvas.save();
		canvas.translate(w / 2f, h / 2f);

		final float startAngle = -210f;
		final float sweepAngle = 240f;
		canvas.drawArc(rectF, startAngle + sweepAngle * currAqiPercent, sweepAngle * (1f - currAqiPercent), false,
				textPaint);

		// Draw AQI data
		if (currAqiPercent > 0) {
			textPaint.setColor(0x99ffffff);
			canvas.drawArc(rectF, startAngle, sweepAngle * currAqiPercent, false, textPaint);

			// Draw center circle
			textPaint.setColor(0xffffffff);
			textPaint.setStrokeWidth(lineSize / 8f);
			canvas.drawCircle(0, 0, lineSize / 3f, textPaint);

			// Draw AQI number
			textPaint.setStyle(Style.FILL);
			textPaint.setTextSize(lineSize * 1.5f);
			textPaint.setColor(0xffffffff);
			canvas.drawText(String.valueOf(aqi), 0, lineSize * 3, textPaint);

			// Draw air quality description
			textPaint.setTextSize(lineSize);
			textPaint.setColor(0x88ffffff);
			canvas.drawText(airQuality, 0, lineSize * 4.25f, textPaint);

			// Draw AQI line indicator
			canvas.rotate(startAngle + sweepAngle * currAqiPercent - 180f);
			textPaint.setStyle(Style.STROKE);
			textPaint.setColor(0xffffffff);
			float startX = lineSize / 3f;
			canvas.drawLine(-startX, 0, -lineSize * 4.5f, 0, textPaint);
		}
		canvas.restoreToCount(saveCount);
	}

	public void setData(AirQualityResponse airQuality) {
		this.aqi = airQuality.list.get(0).main.aqi;
		if (aqi == 1) {
			this.airQuality = "Good";
		} else if (aqi == 2) {
			this.airQuality = "Fair";
		} else if (aqi == 3) {
			this.airQuality = "Moderate";
		} else if (aqi == 4) {
			this.airQuality = "Poor";
		} else if (aqi == 5) {
			this.airQuality = "Very Poor";
		} else {
			this.airQuality = "";
		}
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
