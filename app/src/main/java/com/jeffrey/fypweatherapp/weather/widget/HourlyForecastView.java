package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jeffrey.fypweatherapp.util.TimestampFormatter;
import com.jeffrey.fypweatherapp.weather.MainActivity;
import com.jeffrey.fypweatherapp.weather.api.entity.HourlyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;

import java.util.List;

/**
 * HourlyForecastView: Displays 24-hour weather forecasts with temperature trends.
 */
public class HourlyForecastView extends View {

	private int width, height;
	private final float density;
	private List<HourlyForecast> forecastList;
	private Path tmpPath = new Path();
	private Path goneTmpPath = new Path();
	private Data[] datas;
	private final int fullDataCount = 8; // Expected data points for 24-hour forecast
	private final DashPathEffect dashPathEffect;

	private final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

	public class Data {
		public float offsetPercent;
		public int temperature;
		public String date;
		public String windSpeed;
		public String precipitation;
	}

	public HourlyForecastView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		dashPathEffect = new DashPathEffect(new float[]{density * 3, density * 3}, 1);
		if (isInEditMode()) {
			return;
		}
		init(context);
	}

	private void init(Context context) {
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(1f * density);
		paint.setTextSize(12f * density);
		paint.setStyle(Style.FILL);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(MainActivity.getTypeface(context));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isInEditMode()) {
			return;
		}

		paint.setStyle(Style.FILL);
		final float textSize = this.height / 12f;
		paint.setTextSize(textSize);
		final float textOffset = getTextPaintOffset(paint);
		final float dH = textSize * 4f;
		final float dCenterY = textSize * 4f;

		if (datas == null || datas.length <= 1) {
			canvas.drawLine(0, dCenterY, this.width, dCenterY, paint);
			return;
		}

		final float dW = this.width * 1f / fullDataCount;
		tmpPath.reset();
		goneTmpPath.reset();
		final int length = datas.length;
		float[] x = new float[length];
		float[] y = new float[length];

		final float textPercent = 1f;//(percent >= 0.6f) ? ((percent - 0.6f) / 0.4f) : 0f;
		final float pathPercent = 1f;//(percent >= 0.6f) ? 1f : (percent / 0.6f);

		final float smallerHeight = 4 * textSize;
		final float smallerPercent = 1 - smallerHeight / 2f / dH;
		paint.setAlpha((int) (255 * textPercent));
		final int dataLengthOffset = Math.max(0, fullDataCount - length);

		for (int i = 0; i < length; i++) {
			final Data d = datas[i];
			final int index = i + dataLengthOffset;
			x[i] = index * dW + dW / 2f;
			y[i] = dCenterY - d.offsetPercent * dH * smallerPercent;

			canvas.drawText(d.temperature + "°", x[i], y[i] - textSize + textOffset, paint);

			if (i == 0) {
				final float i0_x = dW / 2f;
				canvas.drawText("", i0_x, textSize * 7.5f + textOffset, paint);
				canvas.drawText("", i0_x, textSize * 9f + textOffset, paint);
				canvas.drawText("", i0_x, textSize * 10.5f + textOffset, paint);
			}

			canvas.drawText(d.date.substring(11), x[i], textSize * 7.5f + textOffset, paint);
			canvas.drawText(d.precipitation + "%", x[i], textSize * 9f + textOffset, paint);
			canvas.drawText(d.windSpeed, x[i], textSize * 10.5f + textOffset, paint);
		}
		paint.setAlpha(255);
		paint.setStyle(Style.STROKE);
		final float data_x0 = dataLengthOffset * dW;

		goneTmpPath.moveTo(0,  y[0]);
		goneTmpPath.lineTo(data_x0, y[0]);
		paint.setPathEffect(dashPathEffect);
		canvas.drawPath(goneTmpPath, paint);

		for (int i = 0; i < (length - 1); i++) {
			final float midX = (x[i] + x[i + 1]) / 2f;
			final float midY = (y[i] + y[i + 1]) / 2f;
			if (i == 0) {
				tmpPath.moveTo(x[i], y[i]);
			}
			tmpPath.cubicTo(x[i], y[i], x[i], y[i], midX, midY);

			if (i == (length - 2)) {
				tmpPath.cubicTo(x[i + 1], y[i + 1], x[i + 1], y[i + 1], this.width, y[i + 1]);
			}
		}
		// draw tmp path
		final boolean needClip = pathPercent < 1f;
		if (needClip) {
			canvas.save();
			canvas.clipRect(0, 0, this.width * pathPercent, this.height);
		}

		paint.setPathEffect(null);
		canvas.drawPath(tmpPath, paint);
		if (needClip) {
			canvas.restore();
		}
	}

	public void setData(Weather weather) {
		if (weather == null) {
			return;
		}

		if (this.forecastList == weather.getOpenWeatherJSON().hourly) {
//			percent = 0f;
			invalidate();
			return;
		}

		if (weather.getOpenWeatherJSON() != null && weather.getOpenWeatherJSON().hourly != null) {
			// Take the first 24 items or fewer if the list size is less than 24
			this.forecastList = weather.getOpenWeatherJSON().hourly.size() > 24
					? weather.getOpenWeatherJSON().hourly.subList(0, 24)
					: weather.getOpenWeatherJSON().hourly;
		}

		// this.forecastList = weather.getOpenWeatherJSON().hourly;

		if (forecastList == null && forecastList.size() == 0) {
			return;
		}

		datas = new Data[forecastList.size()];

		try {
			int allMax = Integer.MIN_VALUE;
			int allMin = Integer.MAX_VALUE;
			for (int i = 0; i < forecastList.size(); i++) {
				HourlyForecast forecast = forecastList.get(i);
				int temp = (int) forecast.temp;

				allMax = Math.max(allMax, temp);
				allMin = Math.min(allMin, temp);

				final Data data = new Data();
				data.temperature = temp;
				// data.date = forecast.dt + ""; // Format the date properly
				data.date = TimestampFormatter.formatTimestamp(forecast.dt, "yyyy-MM-dd HH:mm");
				data.windSpeed = String.format("%.1f km/h", forecast.windSpeed);
				data.precipitation = String.valueOf((int) (forecast.pop * 100));
				datas[i] = data;
			}

			float allDistance = Math.abs(allMax - allMin);
			float averageDistance = (allMax + allMin) / 2f;
			for (Data d : datas) {
				d.offsetPercent = (d.temperature - averageDistance) / allDistance;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w;
		this.height = h;
	}

	public static float getTextPaintOffset(Paint paint) {
		FontMetrics fontMetrics = paint.getFontMetrics();
		return -(fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.top;
	}
}
