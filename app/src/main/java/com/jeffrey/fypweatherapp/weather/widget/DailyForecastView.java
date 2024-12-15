package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import androidx.core.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.jeffrey.fypweatherapp.util.TimestampFormatter;
import com.jeffrey.fypweatherapp.weather.MainActivity;
import com.jeffrey.fypweatherapp.weather.api.ApiManager;
import com.jeffrey.fypweatherapp.weather.api.entity.DailyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * DailyForecastView: Displays a weekly weather forecast with temperature trends.
 */
public class DailyForecastView extends View {

	private int width, height;
	private float percent = 0f;
	private final float density;
	private List<DailyForecast> forecastList;
	private Path tmpMaxPath = new Path();
	private Path tmpMinPath = new Path();
	private Data[] datas;

	private final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

	public class Data {
		public float minOffsetPercent, maxOffsetPercent;
		public int tmpMax, tmpMin;
		public String date;
		public String windSpeed;
		public String condition;
	}

	public DailyForecastView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		if (isInEditMode()) {
			return;
		}
		init(context);
	}

	public void resetAnimation() {
		percent = 0f;
		invalidate();
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
		final float textSize = this.height / 18f;
		paint.setTextSize(textSize);
		final float textOffset = getTextPaintOffset(paint);
		final float dH = textSize * 8f;
		final float dCenterY = textSize * 6f;

		if (datas == null || datas.length <= 1) {
			canvas.drawLine(0, dCenterY, this.width, dCenterY, paint);
			return;
		}

		final float dW = this.width * 1f / datas.length;
		tmpMaxPath.reset();
		tmpMinPath.reset();

		final int length = datas.length;
		float[] x = new float[length];
		float[] yMax = new float[length];
		float[] yMin = new float[length];

		final float textPercent = (percent >= 0.6f) ? ((percent - 0.6f) / 0.4f) : 0f;
		final float pathPercent = (percent >= 0.6f) ? 1f : (percent / 0.6f);

		paint.setAlpha((int) (255 * textPercent));
		for (int i = 0; i < length; i++) {
			final Data d = datas[i];
			x[i] = i * dW + dW / 2f;
			yMax[i] = dCenterY - d.maxOffsetPercent * dH;
			yMin[i] = dCenterY - d.minOffsetPercent * dH;

			canvas.drawText(d.tmpMax + "°", x[i], yMax[i] - textSize + textOffset, paint);
			canvas.drawText(d.tmpMin + "°", x[i], yMin[i] + textSize + textOffset, paint);
			canvas.drawText(d.date, x[i], textSize * 13.5f + textOffset, paint);
			canvas.drawText(d.condition, x[i], textSize * 15f + textOffset, paint);
			canvas.drawText(d.windSpeed, x[i], textSize * 16.5f + textOffset, paint);
		}
		paint.setAlpha(255);

		for (int i = 0; i < (length - 1); i++) {
			final float midX = (x[i] + x[i + 1]) / 2f;
			final float midYMax = (yMax[i] + yMax[i + 1]) / 2f;
			final float midYMin = (yMin[i] + yMin[i + 1]) / 2f;

			if (i == 0) {
				tmpMaxPath.moveTo(0, yMax[i]);
				tmpMinPath.moveTo(0, yMin[i]);
			}
			tmpMaxPath.cubicTo(x[i] - 1, yMax[i], x[i], yMax[i], midX, midYMax);
			tmpMinPath.cubicTo(x[i] - 1, yMin[i], x[i], yMin[i], midX, midYMin);

			if (i == (length - 2)) {
				tmpMaxPath.cubicTo(x[i + 1] - 1, yMax[i + 1], x[i + 1], yMax[i + 1], this.width, yMax[i + 1]);
				tmpMinPath.cubicTo(x[i + 1] - 1, yMin[i + 1], x[i + 1], yMin[i + 1], this.width, yMin[i + 1]);
			}
		}

		paint.setStyle(Style.STROKE);
		final boolean needClip = pathPercent < 1f;
		if (needClip) {
			canvas.save();
			canvas.clipRect(0, 0, this.width * pathPercent, this.height);
		}
		canvas.drawPath(tmpMaxPath, paint);
		canvas.drawPath(tmpMinPath, paint);
		if (needClip) {
			canvas.restore();
		}

		if (percent < 1) {
			percent += 0.025f;
			percent = Math.min(percent, 1f);
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	public void setData(Weather weather) {
		if (weather == null) {
			return;
		}

		if(this.forecastList == weather.getOpenWeatherJSON().daily){
			percent = 0f;
			invalidate();
			return ;
		}

		this.forecastList = weather.getOpenWeatherJSON().daily;

		if (forecastList == null && forecastList.size() == 0) {
			return;
		}

		datas = new Data[forecastList.size()];

		try {
			int allMax = Integer.MIN_VALUE;
			int allMin = Integer.MAX_VALUE;
			for (int i = 0; i < forecastList.size(); i++) {
				DailyForecast forecast = forecastList.get(i);
				int max = (int) forecast.temp.max;
				int min = (int) forecast.temp.min;

				allMax = Math.max(allMax, max);
				allMin = Math.min(allMin, min);

				final Data data = new Data();
				data.tmpMax = max;
				data.tmpMin = min;
				//data.date = forecast.dt + ""; // Add proper date formatting
				data.date = TimestampFormatter.formatTimestamp(forecast.dt, "EEEE");
				data.windSpeed = String.format("%.1f km/h", forecast.windSpeed);
				data.condition = forecast.weather.get(0).main;
				datas[i] = data;
			}

			float allDistance = Math.abs(allMax - allMin);
			float averageDistance = (allMax + allMin) / 2f;
			for (Data d : datas) {
				d.maxOffsetPercent = (d.tmpMax - averageDistance) / allDistance;
				d.minOffsetPercent = (d.tmpMin - averageDistance) / allDistance;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		percent = 0f;
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
