package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.core.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jeffrey.fypweatherapp.util.TimestampFormatter;
import com.jeffrey.fypweatherapp.util.UiUtil;
import com.jeffrey.fypweatherapp.weather.MainActivity;
import com.jeffrey.fypweatherapp.weather.api.ApiManager;
import com.jeffrey.fypweatherapp.weather.api.entity.CurrentWeather;
import com.jeffrey.fypweatherapp.weather.api.entity.DailyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;

import java.util.Calendar;

/**
 * AstroView: Displays sun arc and wind speed details.
 */
public class AstroView extends View {

	private int width, height;
	private final float density;
	private final DashPathEffect dashPathEffect;
	private Path sunPath = new Path();
	private RectF sunRectF = new RectF();
	private Path fanPath = new Path();
	private Path fanPillarPath = new Path();
	private float fanPillerHeight;
	private final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
	private float paintTextOffset;
	final float offsetDegree = 15f;
	private float curRotate;
	private CurrentWeather currentWeather;
	private float sunArcHeight, sunArcRadius;

	private Rect visibleRect = new Rect();

	public void setData(Weather weather) {
		try {
			if (ApiManager.acceptWeather(weather)) {
				this.currentWeather = weather.getOpenWeatherJSON().current;
				final DailyForecast forecast = weather.getTodayDailyForecast();
				if (this.currentWeather != null) {
					invalidate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public AstroView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		dashPathEffect = new DashPathEffect(new float[]{density * 3, density * 3}, 1);
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(density);
		paint.setTextAlign(Align.CENTER);
		if (isInEditMode()) {
			return;
		}
		paint.setTypeface(MainActivity.getTypeface(context));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (this.currentWeather == null) {
			// UiUtil.logDebug("WTFisthis", "current->" + currentWeather);
			return;
		}

		paint.setColor(Color.WHITE);
		float textSize = paint.getTextSize();
		try {
			paint.setStrokeWidth(density);
			paint.setStyle(Style.STROKE);

			// Draw sun path
			paint.setColor(0x55ffffff);
			paint.setPathEffect(dashPathEffect);
			canvas.drawPath(sunPath, paint);
			paint.setPathEffect(null);
			paint.setColor(Color.WHITE);
			int saveCount = canvas.save();
			canvas.translate(width / 2f - fanPillerHeight, textSize + sunArcHeight - fanPillerHeight);

			// Draw wind details
			paint.setStyle(Style.FILL);
			paint.setTextAlign(Align.LEFT);
			final float fanHeight = textSize * 2f;
			canvas.drawText("Wind Speed", fanHeight + textSize, -textSize, paint);
			canvas.drawText(currentWeather.windSpeed + "km/h " + currentWeather.windDeg + "°", fanHeight + textSize, 0, paint);

			// Draw fan and fanPillar
			paint.setStyle(Style.STROKE);
			canvas.drawPath(fanPillarPath, paint);
			canvas.rotate(curRotate * 360f);
			float speed = (float) Math.max(currentWeather.windSpeed, 0.75f);
			curRotate += 0.001f * speed;
			if (curRotate > 1f) {
				curRotate = 0f;
			}
			paint.setStyle(Style.FILL);
			canvas.drawPath(fanPath, paint);
			canvas.rotate(120f);
			canvas.drawPath(fanPath, paint);
			canvas.rotate(120f);
			canvas.drawPath(fanPath, paint);
			canvas.restoreToCount(saveCount);

			// Draw bottom line
			paint.setStyle(Style.STROKE);
			final float lineLeft = width / 2f - sunArcRadius;
			canvas.drawLine(lineLeft, sunArcHeight + textSize, width - lineLeft, sunArcHeight + textSize, paint);

			// Draw pressure info
			paint.setStyle(Style.FILL);
			paint.setTextAlign(Align.RIGHT);
			final float pressureTextRight = width / 2f + sunArcRadius - textSize * 2.5f;
			canvas.drawText("Pressure: " + currentWeather.pressure + " hPa", pressureTextRight, sunArcHeight + paintTextOffset, paint);

			// Draw sunrise and sunset info
			final float textLeft = width / 2f - sunArcRadius;
			paint.setTextAlign(Align.CENTER);
			canvas.drawText("Sunrise: " + TimestampFormatter.formatTimestamp(Long.parseLong(String.valueOf(currentWeather.sunrise)), "HH:mm"), textLeft, textSize * 10.5f + paintTextOffset, paint);
			canvas.drawText("Sunset: " + TimestampFormatter.formatTimestamp(Long.parseLong(String.valueOf(currentWeather.sunset)), "HH:mm"), width - textLeft, textSize * 10.5f + paintTextOffset, paint);

			// draw the sun
			String srTimeFormatted = TimestampFormatter.formatTimestamp(Long.parseLong(String.valueOf(currentWeather.sunrise)), "HH:mm:ss"); // Sunrise
			String ssTimeFormatted = TimestampFormatter.formatTimestamp(Long.parseLong(String.valueOf(currentWeather.sunset)), "HH:mm:ss"); // Sunset
			String[] srTimeParts = srTimeFormatted.split(":");
			int srTime = Integer.valueOf(srTimeParts[0]) * 60 * 60
					+ Integer.valueOf(srTimeParts[1]) * 60
					+ Integer.valueOf(srTimeParts[2]);

			String[] ssTimeParts = ssTimeFormatted.split(":");
			int ssTime = Integer.valueOf(ssTimeParts[0]) * 60 * 60
					+ Integer.valueOf(ssTimeParts[1]) * 60
					+ Integer.valueOf(ssTimeParts[2]);
			Calendar c = Calendar.getInstance();
			int curTime = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.MINUTE);
			if (curTime >= srTime && curTime <= ssTime) {// 说明是在白天，需要画太阳
				canvas.save();
				canvas.translate(width / 2f, sunArcRadius + textSize);// 先平移到圆心
				float percent = (curTime - srTime) / ((float) (ssTime - srTime));
				float degree = 15f + 150f * percent;
				final float sunRadius = density * 4f;
				canvas.rotate(degree);// 旋转到太阳的角度

				paint.setStyle(Style.FILL);
				// canvas.drawLine(-1000, 0, 1000, 0, paint);//测试
				paint.setStrokeWidth(density * 1.333f);// 宽度是2对应半径是6
				canvas.translate(-sunArcRadius, 0);// 平移到太阳应该在的位置
				canvas.rotate(-degree);// 转正方向。。。
				// 尝试清除太阳周围的一圈，但是没有成功
				// final float clearRadius =sunRadius * 1.2f;
				// int layer = canvas.saveLayer(-clearRadius, -clearRadius,
				// clearRadius, clearRadius, paint,Canvas.ALL_SAVE_FLAG);
				// paint.setColor(Color.TRANSPARENT);
				// paint.setXfermode(clearfXfermode);
				// canvas.drawCircle(0, 0, sunRadius * 1.2f, paint);
				// canvas.restoreToCount(layer);
				// paint.setColor(Color.WHITE);
				// paint.setXfermode(null);
				canvas.drawCircle(0, 0, sunRadius, paint);
				// paint.setStrokeJoin(Paint.Join.ROUND);
				// paint.setStrokeCap(Paint.Cap.ROUND);
				paint.setStyle(Style.STROKE);
				final int light_count = 8;
				for (int i = 0; i < light_count; i++) {// 画刻度
					double radians = Math.toRadians(i * (360 / light_count));
					float x1 = (float) (Math.cos(radians) * sunRadius * 1.6f);
					float y1 = (float) (Math.sin(radians) * sunRadius * 1.6f);
					float x2 = x1 * (1f + 0.4f * 1f);
					float y2 = y1 * (1f + 0.4f * 1f);
					canvas.drawLine(0 + x1, y1, 0 + x2, y2, paint);
				}
				canvas.restore();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		getGlobalVisibleRect(visibleRect);
		if (!visibleRect.isEmpty()) {
			ViewCompat.postInvalidateOnAnimation(this);
			 Log.d(AstroView.class.getSimpleName(),
			 "postInvalidateOnAnimation");
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 间距1 图8.5行 间距0.5 字1行 间距1 = 12;
		// 9.5 10 11 12
		this.width = w;
		this.height = h;

		try {
			final float textSize = height / 12f;
			paint.setTextSize(textSize);
			paintTextOffset = UiUtil.getTextPaintOffset(paint);

			sunPath.reset();

			sunArcHeight = textSize * 8.5f;
			sunArcRadius = (float) (sunArcHeight / (1f - Math.sin(Math.toRadians(offsetDegree))));
			final float sunArcLeft = width / 2f - sunArcRadius;
			sunRectF.left = sunArcLeft;
			sunRectF.top = textSize;
			sunRectF.right = width - sunArcLeft;
			sunRectF.bottom = sunArcRadius * 2f + textSize;
			sunPath.addArc(sunRectF, -165, +150);// 圆形的最右端点为0，顺时针sweepAngle

			// fanPath和fanPillarPath的中心点在扇叶圆形的中间
			fanPath.reset();
			final float fanSize = textSize * 0.2f;// 风扇底部半圆的半径
			final float fanHeight = textSize * 2f;
			final float fanCenterOffsetY = fanSize * 1.6f;
			// fanPath.moveTo(fanSize, -fanCenterOffsetY);
			// 也可以用arcTo
			// 从右边到底部到左边了的弧
			fanPath.addArc(new RectF(-fanSize, -fanSize - fanCenterOffsetY, fanSize, fanSize - fanCenterOffsetY), 0,
					180);
			// fanPath.lineTo(0, -fanHeight - fanCenterOffsetY);
			fanPath.quadTo(-fanSize * 1f, -fanHeight * 0.5f - fanCenterOffsetY, 0, -fanHeight - fanCenterOffsetY);
			fanPath.quadTo(fanSize * 1f, -fanHeight * 0.5f - fanCenterOffsetY, fanSize, -fanCenterOffsetY);
			fanPath.close();

			fanPillarPath.reset();
			final float fanPillarSize = textSize * 0.25f;// 柱子的宽度
			fanPillarPath.moveTo(0, 0);
			fanPillerHeight = textSize * 4f;// 柱子的宽度
			fanPillarPath.lineTo(fanPillarSize, fanPillerHeight);
			fanPillarPath.lineTo(-fanPillarSize, fanPillerHeight);
			fanPillarPath.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
