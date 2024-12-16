package com.jeffrey.fypweatherapp.weather;

import static java.text.Normalizer.normalize;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.gson.Gson;
import com.jeffrey.fypweatherapp.R;
import com.jeffrey.fypweatherapp.dynamicweathertype.DynamicWeatherView;
import com.jeffrey.fypweatherapp.util.LocationManager;
import com.jeffrey.fypweatherapp.util.TimestampFormatter;
import com.jeffrey.fypweatherapp.util.TimingLogger;
import com.jeffrey.fypweatherapp.util.UiUtil;
import com.jeffrey.fypweatherapp.weather.api.ApiManager;
import com.jeffrey.fypweatherapp.weather.api.entity.AirQualityResponse;
import com.jeffrey.fypweatherapp.weather.api.entity.CurrentWeather;
import com.jeffrey.fypweatherapp.weather.api.entity.DailyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.HourlyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.OpenWeatherJSON;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;
import com.jeffrey.fypweatherapp.weather.prediction.WeatherPrediction;
import com.jeffrey.fypweatherapp.widget.effect.FragmentPagerAdapter;
import com.jeffrey.fypweatherapp.widget.effect.ViewPager;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends FragmentActivity {

	public static Typeface typeface;

	public static Typeface getTypeface(Context context) {
		return typeface;
	}

	private static final int LOCATION_REQUEST_CODE = 100;
	private static FusedLocationProviderClient fusedLocationClient;
	private WeatherPrediction weatherPrediction;

	private DynamicWeatherView weatherView;
	private ViewPager viewPager;

	public ViewPager getViewPager() {
		return viewPager;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//UiUtil.toastDebug(this, "onNewIntent->" + intent.toString());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TimingLogger logger = new TimingLogger("MainActivity.onCreate");
//		typeface = Typeface.createFromAsset(getAssets(), "fonts/clock text typeface.ttf");
		if (typeface == null) {
			typeface = Typeface.createFromAsset(getAssets(), "fonts/mxx_font2.ttf");
		}
		logger.addSplit("Typeface.createFromAsset");
		setContentView(R.layout.activity_main);
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
		} else {
			getLastLocation();
		}
		logger.addSplit("setContentView");
		viewPager = (ViewPager) findViewById(R.id.main_viewpager);
		if (Build.VERSION.SDK_INT >= 19) {
			viewPager.setPadding(0, UiUtil.getStatusBarHeight(), 0, 0);
		}
		AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
		alphaAnimation.setDuration(260);
		alphaAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				getWindow().setBackgroundDrawable(//getResources().getDrawable(R.drawable.window_frame_color));
						new ColorDrawable(Color.BLACK));
//				WeatherNotificationService.startServiceWithNothing(MainActivity.this);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		viewPager.setAnimation(alphaAnimation);
//		viewPager.setPageMargin(UiUtil.dp2px(this, 4));
		logger.addSplit("findViewPager");
		weatherView = findViewById(R.id.main_dynamicweatherview);
		logger.addSplit("findWeatherView");
		logger.addSplit("loadAreaToViewPager");
		logger.dumpToLog();


	}
	private float normalize(float value, float min, float max) {
		if (min == max) {
			// Handle edge case: if min == max, return a constant value (e.g., 0.5)
			return 0.5f;
		}
		return (value - min) / (max - min);
	}



	//	private float[][][] prepareInputData(OpenWeatherJSON weather) {
//		int totalHours = weather.hourly.size(); // Total hourly data points available
//		int days = Math.min(totalHours / 48, 7);    // Calculate the number of full days (24-hour blocks) to process
//		float[][][] inputData = new float[days][24][9]; // Adjust dimensions for days, hours, and features
//
//		for (int hour = 0; hour < 24; hour++) {
//			int index = totalHours - 24 + hour; // Start from the last 24 hours in the hourly array
//			HourlyForecast hourlyForecast = weather.hourly.get(index);
//			DailyForecast dailyForecast = weather.daily.get(0); // Use today's daily forecast data
//
//			// Extract features for each hour
//			inputData[0][hour][0] = normalize((float) hourlyForecast.temp, 0, 50);            // Hourly temperature
//			inputData[0][hour][1] = normalize((float) hourlyForecast.feelsLike, 0, 50);      // Feels-like temperature
//			inputData[0][hour][2] = normalize((float) hourlyForecast.pressure, 900, 1100);   // Atmospheric pressure
//			inputData[0][hour][3] = normalize((float) hourlyForecast.humidity, 0, 100);      // Humidity percentage
//			inputData[0][hour][4] = normalize((float) dailyForecast.temp.min, 0, 50);        // Daily min temperature
//			inputData[0][hour][5] = normalize((float) dailyForecast.temp.max, 0, 50);        // Daily max temperature
//			inputData[0][hour][6] = normalize((float) hourlyForecast.windSpeed, 0, 20);      // Wind speed
//			inputData[0][hour][7] = normalize((float) hourlyForecast.windDeg, 0, 360);       // Wind direction
//			inputData[0][hour][8] = normalize((float) hourlyForecast.clouds, 0, 100);        // Cloudiness percentage
//		}
//
//		return inputData;
//	}
	private float[][][] prepare7DaysPredictionInput(OpenWeatherJSON weather) {
		int timeSteps = 7; // 7 days
		int features = 9; // Number of features per day

		// Ensure there are enough daily data points
		if (weather.daily == null || weather.daily.size() < timeSteps) {
			Log.e("FUCK", "Not enough daily data for 7-day prediction. Found: " +
					(weather.daily == null ? 0 : weather.daily.size()));
			return null;
		}

		float[][][] inputData = new float[1][timeSteps][features]; // [Batch, TimeSteps, Features]

		for (int i = 0; i < 7; i++) {
			DailyForecast dailyForecast = weather.daily.get(i);
			inputData[0][i][0] = normalize((float) dailyForecast.temp.day, 0, 50);
			inputData[0][i][1] = normalize((float) dailyForecast.feelsLike.day, 0, 50);
			inputData[0][i][2] = normalize((float) dailyForecast.pressure, 900, 1100);
			inputData[0][i][3] = normalize((float) dailyForecast.humidity, 0, 100);
			inputData[0][i][4] = normalize((float) dailyForecast.temp.min, 0, 50);
			inputData[0][i][5] = normalize((float) dailyForecast.temp.max, 0, 50);
			inputData[0][i][6] = normalize((float) dailyForecast.windSpeed, 0, 20);
			inputData[0][i][7] = normalize((float) dailyForecast.windDeg, 0, 360);
			inputData[0][i][8] = normalize((float) dailyForecast.clouds, 0, 100);
		}
		Log.d("FUCK", Arrays.deepToString(inputData));
		Log.d("FUCK", "7-day input data prepared successfully.");
		return inputData;
	}

	private float[][][] prepare24HoursPredictionInput(OpenWeatherJSON weather) {
		int timeSteps = 24; // 24 hours
		int features = 9; // Number of features per hour

		// Ensure there are enough hourly data points
		if (weather.hourly == null || weather.hourly.size() < timeSteps) {
			Log.e("	FUCK", "Not enough hourly data for 24-hour prediction. Found: " +
					(weather.hourly == null ? 0 : weather.hourly.size()));
			return null;
		}

		float[][][] inputData = new float[1][timeSteps][features]; // [Batch, TimeSteps, Features]

		for (int i = 0; i < timeSteps; i++) {
			HourlyForecast hourlyForecast = weather.hourly.get(i);
			DailyForecast dailyForecast = weather.daily.get(0);

			// Extract features
			inputData[0][i][0] = normalize((float) hourlyForecast.temp, 0, 50);         // Hourly temperature
			inputData[0][i][1] = normalize((float) hourlyForecast.feelsLike, 0, 50);   // Feels-like temperature
			inputData[0][i][2] = normalize((float) hourlyForecast.pressure, 900, 1100); // Pressure
			inputData[0][i][3] = normalize((float) hourlyForecast.humidity, 0, 100);   // Humidity
			inputData[0][i][4] = normalize((float) dailyForecast.temp.min, 0, 50);        // Temp min placeholder
			inputData[0][i][5] = normalize((float) dailyForecast.temp.max, 0, 50);        // Temp max placeholder
			inputData[0][i][6] = normalize((float) hourlyForecast.windSpeed, 0, 20);   // Wind speed
			inputData[0][i][7] = normalize((float) hourlyForecast.windDeg, 0, 360);    // Wind direction
			inputData[0][i][8] = normalize((float) hourlyForecast.clouds, 0, 100);     // Clouds
		}
		Log.d("FUCK", "24-hour input data prepared successfully.");
		return inputData;
	}


	private float[][][] prepareRainPredictionInput(OpenWeatherJSON weather) {
		int timeSteps = 1; // Current weather (single time step)
		int features = 9; // Number of features

		// Ensure current weather data exists
		if (weather.current == null) {
			Log.e("WeatherPrediction", "No current weather data available.");
			return null;
		}
		CurrentWeather current = weather.current;
		DailyForecast dailyForecast = weather.daily.get(0);

		// Prepare 3D input tensor [1, 12, 9]
		float[][][] inputData = new float[1][timeSteps][features]; // [Batch, TimeSteps, Features]

		// Populate data for the 12 time steps
		inputData[0][0][0] = normalize((float) current.temp, 0, 50);             // Current temperature
		inputData[0][0][1] = normalize((float) current.feelsLike, 0, 50);       // Feels-like temperature
		inputData[0][0][2] = normalize((float) current.pressure, 900, 1100);    // Pressure
		inputData[0][0][3] = normalize((float) current.humidity, 0, 100);       // Humidity
		inputData[0][0][4] = normalize((float) dailyForecast.temp.min, 0, 50);            // Temp min (placeholder)
		inputData[0][0][5] = normalize((float) dailyForecast.temp.max, 0, 50);            // Temp max (placeholder)
		inputData[0][0][6] = normalize((float) current.windSpeed, 0, 20);       // Wind speed
		inputData[0][0][7] = normalize((float) current.windDeg, 0, 360);        // Wind direction
		inputData[0][0][8] = normalize((float) current.clouds, 0, 100);         // Cloud coverage

		Log.d("FUCK", "Rain prediction input data prepared successfully.");
		return inputData;
	}




	public void getLastLocation() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		fusedLocationClient.getLastLocation()
				.addOnCompleteListener(new OnCompleteListener<Location>() {
					@Override
					public void onComplete(@NonNull Task<Location> task) {
						if (task.isSuccessful() && task.getResult() != null) {
							Location location = task.getResult();
							double latitude = location.getLatitude();
							double longitude = location.getLongitude();
							LocationManager.getInstance().setLatitude(location.getLatitude());
							LocationManager.getInstance().setLongitude(location.getLongitude());
							Toast.makeText(MainActivity.this,
									"Lat: " + latitude + ", Lon: " + longitude,
									Toast.LENGTH_LONG).show();
							loadAreaToViewPager();
						} else {
							Toast.makeText(MainActivity.this,
									"Unable to get location. Ensure location is enabled on the device.",
									Toast.LENGTH_LONG).show();
							loadAreaToViewPager();
						}
					}
				});

	}

	public void loadAreaToViewPager(){
//		final ArrayList<ApiManager.Area> selectedAreas = ApiManager.loadSelectedArea(this);
		final BaseFragment[] fragments = new BaseFragment[2]; // Settings and one dynamic weather fragment
		// Create and load the settings fragment
		fragments[0] = SettingsFragment.makeInstance();

		// Fetch weather data dynamically and set up the weather fragment
		new Thread(() -> {
			try {
				// Assuming the location is retrieved dynamically (latitude and longitude)
				double latitude = LocationManager.getInstance().getLatitude();
				double longitude = LocationManager.getInstance().getLongitude();

				// Fetch weather data
				Weather weather = ApiManager.fetchWeather(latitude, longitude);
//				AirQualityResponse airQuality = ApiManager.fetchAirQuality(latitude, longitude);

				// Create and add the WeatherFragment on the main thread
				runOnUiThread(() -> {
					if (weather != null) {
						fragments[1] = WeatherFragment.makeInstance(weather);
						viewPager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), fragments));

						viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
							@Override
							public void onPageSelected(int position) {
								super.onPageSelected(position);
								weatherView.setDrawerType(((SimpleFragmentPagerAdapter) viewPager.getAdapter()).getItem(
										position).getDrawerType());
							}
						});
						viewPager.setCurrentItem(1, false); // Show the weather fragment by default
					} else {
						Log.e("MainActivity", "Failed to fetch weather data.");
					}
				});

				// AI Prediction Part
				// AI Predictions for 7-day, 24-hour, and rain prediction
				try {
					WeatherPrediction weatherPrediction = new WeatherPrediction(this);

					// Prepare input data
					float[][][] input7Days = prepare7DaysPredictionInput(weather.getOpenWeatherJSON());
					float[][][] input24Hours = prepare24HoursPredictionInput(weather.getOpenWeatherJSON());
					float[][][] inputRain = prepareRainPredictionInput(weather.getOpenWeatherJSON());

					// Make predictions
					String[] predictions7Days = weatherPrediction.predictWeatherDescription7Days(input7Days);
					String[] predictions24Hours = weatherPrediction.predictWeatherDescription24Hours(input24Hours);
					float willRainPercent = weatherPrediction.predictRainNextHour(inputRain);
					boolean willRain;
                    willRain = willRainPercent > 0.5;

					// Log results
					Log.d("FUCK", "7-day Predictions: " + Arrays.toString(predictions7Days));
					Log.d("FUCK", "24-hour Predictions: " + Arrays.toString(predictions24Hours));
					Log.d("FUCK", "Rain Percent Predictions: " + willRainPercent);
					Log.d("FUCK", "Rain Prediction: " + (willRain ? "Rain" : "No Rain"));


					if (predictions7Days != null) {
						for (int i = 0; i < predictions7Days.length; i++) {
							Log.d("FUCK", "Day " + (i + 1) + ": " + predictions7Days[i]);
						}
					} else {
						Log.e("FUCK", "predictions7Days is null.");
					}

					if (predictions24Hours != null) {
						for (int i = 0; i < predictions24Hours.length; i++) {
							Log.d("FUCK", "Hour " + (i + 1) + ": " + predictions24Hours[i]);
						}
					} else {
						Log.e("FUCK", "predictions24Hours is null.");
					}


//					// Display predictions
//					for (int i = 0; i < predictions7Days.length; i++) {
//						Log.d("FUCK", "Day " + (i + 1) + ": " + predictions7Days[i]);
//					}
//
//					for (int i = 0; i < predictions24Hours.length; i++) {
//						Log.d("FUCK", "Hour " + (i + 1) + ": " + predictions24Hours[i]);
//					}

					try {
						//boolean willRain = weatherPrediction.predictRainNextHour(inputRain);

						// Ensure Toast runs on the main thread
						runOnUiThread(() -> {
							Toast.makeText(
									MainActivity.this,
									willRain ? "Rain expected in the next hour." : "No rain expected in the next hour.",
									Toast.LENGTH_LONG
							).show();
						});

						Log.d("FUCK", "Rain expected in the next hour: " + willRain);
					} catch (Exception e) {
						Log.e("FUCK", "Error in weather prediction", e);
					}

					runOnUiThread(() -> {
						try {
							// 7-Day Forecast
							LinearLayout forecast7Days = findViewById(R.id.w_7day_forecast);
							forecast7Days.removeAllViews();

							for (int i = 0; i < predictions7Days.length; i++) {
								// Create a vertical layout for each day's forecast
								LinearLayout dayLayout = new LinearLayout(this);
								dayLayout.setOrientation(LinearLayout.HORIZONTAL);
								dayLayout.setPadding(8, 8, 8, 8); // Add padding
								dayLayout.setWeightSum(3);

								// Day label
								TextView dayLabel = new TextView(this);
								dayLabel.setText("Day " + (i + 1) + ": ");
								dayLabel.setTextColor(getResources().getColor(R.color.w_text_primary));
								dayLabel.setTextSize(16);
								dayLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

								// Forecast data
								TextView dayForecast = new TextView(this);
								dayForecast.setText(predictions7Days[i]);
								dayForecast.setTextColor(getResources().getColor(R.color.w_text_secondary));
								dayForecast.setTextSize(16);
								dayForecast.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

								// Add views to the horizontal layout
								dayLayout.addView(dayLabel);
								dayLayout.addView(dayForecast);

								// Add horizontal layout to the parent LinearLayout
								forecast7Days.addView(dayLayout);

								// Add a divider between days
								View divider = new View(this);
								divider.setLayoutParams(new LinearLayout.LayoutParams(
										LinearLayout.LayoutParams.MATCH_PARENT, 1));
								divider.setBackgroundColor(getResources().getColor(R.color.w_text_secondary));
								forecast7Days.addView(divider);
							}


							// 24-Hour Forecast
							LinearLayout forecast24Hours = findViewById(R.id.w_24hour_forecast);
							forecast24Hours.removeAllViews();

							for (int i = 0; i < predictions24Hours.length; i++) {
								// Create a horizontal LinearLayout for each hour's forecast
								LinearLayout hourLayout = new LinearLayout(this);
								hourLayout.setOrientation(LinearLayout.HORIZONTAL);
								hourLayout.setPadding(8, 8, 8, 8); // Add padding
								hourLayout.setWeightSum(3); // Total weight for equal distribution

								// Hour label
								TextView hourLabel = new TextView(this);
								hourLabel.setText("Hour " + (i + 1) + ": ");
								hourLabel.setTextColor(getResources().getColor(R.color.w_text_primary));
								hourLabel.setTextSize(16);
								hourLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

								// Forecast data
								TextView hourForecast = new TextView(this);
								hourForecast.setText(predictions24Hours[i]);
								hourForecast.setTextColor(getResources().getColor(R.color.w_text_secondary));
								hourForecast.setTextSize(16);
								hourForecast.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

								// Add views to the horizontal layout
								hourLayout.addView(hourLabel);
								hourLayout.addView(hourForecast);

								// Add horizontal layout to the parent LinearLayout
								forecast24Hours.addView(hourLayout);

								// Add a divider for better visibility
								View divider = new View(this);
								divider.setLayoutParams(new LinearLayout.LayoutParams(
										LinearLayout.LayoutParams.MATCH_PARENT, 1));
								divider.setBackgroundColor(getResources().getColor(R.color.w_text_secondary));
								forecast24Hours.addView(divider);
							}


							// Rain Prediction
							TextView rainPrediction = findViewById(R.id.w_rain_prediction);
							rainPrediction.setText(willRain
									? "Rain expected in the next hour.\nRain Percentage: " + willRainPercent + "%"
									: "No rain expected in the next hour.\nRain Percentage: " + willRainPercent + "%");


						} catch (Exception e) {
							Log.e("FUCK", "Error updating weather UI", e);
						}
					});

				} catch (Exception e) {
					Log.e("FUCK", "Error in weather prediction", e);
				}



//				try {
//					// Load the TFLite model
//					WeatherPrediction prediction = new WeatherPrediction(this, "model/weather_forecast_model.tflite");
////					MappedByteBuffer model = loadModelFile("model/weather_forecast_model.tflite");
////					WeatherPrediction prediction = new WeatherPrediction("model/weather_forecast_model.tflite");
//
//					// Load sample weather JSON
//					// InputStreamReader reader = new InputStreamReader(getAssets().open("sample_openweather.json"));
//					OpenWeatherJSON weatherData = weather.getOpenWeatherJSON();
//
//					if (weatherData != null && weatherData.hourly != null && !weatherData.hourly.isEmpty()) {
//						float[][][] dailyInputData = prepareInputData(weatherData);
//
//						// Categories used in training
//						String[] weatherCategories = {"Thunderstorm", "Drizzle", "Rain", "Clouds", "Clear"};
//
//						// Predict for the next 7 days
//						prediction.predictNext24Hours(dailyInputData, weatherCategories);
//					} else {
//						Log.e("FUCK", "Invalid or empty daily weather data.");
//					}
//				} catch (Exception e) {
//					Log.e("FUCK", "Error during prediction", e);
//				}



			} catch (Exception e) {
				e.printStackTrace();
				runOnUiThread(() ->
						Log.e("FUCK",  "An error occurred: " + e.getMessage())
				);
			}
		}).start();


//		viewPager.setOffscreenPageLimit(fragments.length);
//		fragments[0] = SettingsFragment.makeInstance();
//		viewPager.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager(), fragments));
//		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//			@Override
//			public void onPageScrollStateChanged(int state) {
//				super.onPageScrollStateChanged(state);
//			}
//			@Override
//			public void onPageSelected(int position) {
//				super.onPageSelected(position);
//				weatherView.setDrawerType(((SimpleFragmentPagerAdapter) viewPager.getAdapter()).getItem(
//						position).getDrawerType());
//			}
//		});
//		viewPager.setCurrentItem(1, false);


	}

	public void updateCurDrawerType() {
		weatherView.setDrawerType(((SimpleFragmentPagerAdapter) viewPager.getAdapter()).getItem(
				viewPager.getCurrentItem()).getDrawerType());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		weatherView.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		weatherView.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		weatherView.onDestroy();
	}

	public static class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

		private BaseFragment[] fragments;

		public SimpleFragmentPagerAdapter(FragmentManager fragmentManager, BaseFragment[] fragments) {
			super(fragmentManager);
			this.fragments = fragments;
		}
		

		@Override
		public BaseFragment getItem(int position) {
			BaseFragment fragment = fragments[position];
			fragment.setRetainInstance(true);
			return fragment;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return fragments[position].getTitle();
		}

		@Override
		public int getCount() {
			return fragments.length;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(((Fragment) object).getView());
			super.destroyItem(container, position, object);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
			getLastLocation();
		} else {
			Log.e("MainActivity", "Location permission denied. Using default location.");
			LocationManager.getInstance().setLatitude(0.0);
			LocationManager.getInstance().setLongitude(0.0);
			// Permission denied, inform the user and provide fallback
			Toast.makeText(this, "Location permission is required for weather updates.", Toast.LENGTH_LONG).show();
			// Optionally, re-request permissions or load a default ViewPager
			showPermissionRationaleDialog();
		}
	}

	private void showPermissionRationaleDialog() {
		new AlertDialog.Builder(this)
				.setTitle("Location Permission Needed")
				.setMessage("This app requires location permissions to provide accurate weather updates. Please allow location access.")
				.setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(
						MainActivity.this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						LOCATION_REQUEST_CODE))
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.dismiss();
					// Set default behavior if permissions are not granted
					LocationManager.getInstance().setLatitude(0.0);
					LocationManager.getInstance().setLongitude(0.0);
					loadAreaToViewPager(); // Load ViewPager with default location
				})
				.create()
				.show();
	}
}
