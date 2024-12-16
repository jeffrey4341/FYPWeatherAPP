package com.jeffrey.fypweatherapp.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jeffrey.fypweatherapp.R;
import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer;
import com.jeffrey.fypweatherapp.util.LocationManager;
import com.jeffrey.fypweatherapp.util.NetworkUtil;
import com.jeffrey.fypweatherapp.util.TimestampFormatter;
import com.jeffrey.fypweatherapp.util.TimingLogger;
import com.jeffrey.fypweatherapp.util.UiUtil;
import com.jeffrey.fypweatherapp.weather.api.ApiManager;
//import com.jeffrey.fypweatherapp.weather.api.ApiManager.Area;
import com.jeffrey.fypweatherapp.weather.api.entity.AirQualityResponse;
import com.jeffrey.fypweatherapp.weather.api.entity.OpenWeatherJSON;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;
import com.jeffrey.fypweatherapp.weather.api.entity.DailyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.HourlyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.CurrentWeather;
//import com.jeffrey.fypweatherapp.weather.api.entity.WeatherCondition;
import com.jeffrey.fypweatherapp.weather.widget.AqiView;
import com.jeffrey.fypweatherapp.weather.widget.AstroView;
import com.jeffrey.fypweatherapp.weather.widget.DailyForecastView;
import com.jeffrey.fypweatherapp.weather.widget.HourlyForecastView;
import com.jeffrey.fypweatherapp.weather.widget.PullRefreshLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WeatherFragment extends BaseFragment {

	private View mRootView;
	private Weather mWeather;
	private DailyForecastView mDailyForecastView;
	private PullRefreshLayout pullRefreshLayout;
	private HourlyForecastView mHourlyForecastView;
	private AqiView mAqiView;
	private AstroView mAstroView;
	//private Area mArea;
	private ScrollView mScrollView;
	private BaseDrawer.Type mDrawerType = BaseDrawer.Type.UNKNOWN_D;
	private static final String BUNDLE_EXTRA_WEATHER = "BUNDLE_EXTRA_WEATHER";
	private String airQuality = "";

	public BaseDrawer.Type getDrawerType() {
		return this.mDrawerType;
	}

	public static WeatherFragment makeInstance(Weather weather) {
		WeatherFragment fragment = new WeatherFragment();
		Bundle bundle = new Bundle();
		if (weather != null) {
			bundle.putSerializable(BUNDLE_EXTRA_WEATHER, weather);
		}
		fragment.setArguments(bundle);
		return fragment;
	}

	private void fetchArguments() {
		if (this.mWeather == null) {
			try {
                assert getArguments() != null;
                this.mWeather = (Weather) getArguments().getSerializable(BUNDLE_EXTRA_WEATHER);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.fragment_weather, null);
			mDailyForecastView = mRootView.findViewById(R.id.w_dailyForecastView);
			pullRefreshLayout = mRootView.findViewById(R.id.w_PullRefreshLayout);
			mHourlyForecastView = mRootView.findViewById(R.id.w_hourlyForecastView);
			mAqiView = mRootView.findViewById(R.id.w_aqi_view);
			mAstroView = mRootView.findViewById(R.id.w_astroView);
			mScrollView = mRootView.findViewById(R.id.w_WeatherScrollView);

			pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					double latitude = LocationManager.getInstance().getLatitude();
					double longitude = LocationManager.getInstance().getLongitude();

					if (latitude == 0.0 && longitude == 0.0) {
						UiUtil.toastDebug(getContext(), "Unable to fetch location. Please enable GPS.");
						return;
					}


					new Thread(() -> {
						try {
							Weather weather = ApiManager.fetchWeather(latitude, longitude);
							// AirQualityResponse airQuality = ApiManager.fetchAirQuality(latitude, longitude);

							// Update UI on the main thread
							requireActivity().runOnUiThread(() -> {
								if (weather != null) {
									if (weather == null) {
										Log.e("WeatherFragment", "Weather is null after fetch.");
									} else {
										Log.d("WeatherFragment", "Fetched Weather: " + weather);
									}
									WeatherFragment.this.mWeather = weather;

									updateWeatherUI(); // Your method to update the UI with weather data
								} else {
									UiUtil.toastDebug(getContext(), "Failed to fetch weather data.");
								}
								pullRefreshLayout.setRefreshing(false);
							});
						} catch (Exception e) {
							e.printStackTrace();
							requireActivity().runOnUiThread(() ->
									UiUtil.toastDebug(getContext(), "An error occurred: " + e.getMessage())
							);
							pullRefreshLayout.setRefreshing(false);
						}
					}).start();
				}
			});
			debug();
			if (mWeather != null) {
				updateWeatherUI();
			}
		} else {
			mScrollView.post(() -> mScrollView.scrollTo(0, 0));
		}
		return mRootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fetchArguments();
//		double lat = LocationManager.getInstance().getLatitude();
//		double lon = LocationManager.getInstance().getLongitude();
//
//		String cachedData = ApiManager.getWeatherFromDb(getContext(), lat, lon);
//
//		if (cachedData != null) {
//			Weather cachedWeather = new Gson().fromJson(cachedData, Weather.class);
//			this.mWeather = cachedWeather;
//			updateWeatherUI();
//		} else {
//			refreshWeather();
//			updateWeatherUI();
//		}
		UiUtil.logDebug("FUCK", "onCreate");
	}

	private void refreshWeather() {
		double lat = LocationManager.getInstance().getLatitude();
		double lon = LocationManager.getInstance().getLongitude();

		new Thread(() -> {
			try {
				Weather weather = ApiManager.fetchWeather(lat, lon);
//				ApiManager.saveWeatherToDb(getContext(), lat, lon, new Gson().toJson(weather));
				requireActivity().runOnUiThread(() -> {
					this.mWeather = weather;
					updateWeatherUI();
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void updateDrawerTypeAndNotify() {
		final BaseDrawer.Type curType = ApiManager.convertWeatherType(mWeather);
		// if(this.mDrawerType != curType){
		this.mDrawerType = curType;
		notifyActivityUpdate();
		// }

	}



	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		fetchArguments();
		UiUtil.logDebug("FUCK", "onActivityCreated");
//		if (this.mArea == null) {
//			return;
//		}
		TimingLogger logger = new TimingLogger("WeatherFragment.onActivityCreated");
//		if (this.mWeather == null) {
//			//this.mWeather = ApiManager.loadWeather(getActivity(), mArea.id);
//			logger.addSplit("loadWeather");
//			updateWeatherUI();
//			logger.addSplit("updateWeatherUI");
//		}
		logger.dumpToLog();
		if (mWeather == null) {
			postRefresh();
		}
	}

	private void debug() {
		// DEBUG///////////////
		if (WeatherApplication.DEBUG) {
			mRootView.findViewById(R.id.w_WeatherLinearLayout).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					ArrayList<String> strs = new ArrayList<String>();
					for (BaseDrawer.Type t : BaseDrawer.Type.values()) {
						strs.add(t.toString());
					}
					int index = 0;
					for (int i = 0; i < BaseDrawer.Type.values().length; i++) {
						if (BaseDrawer.Type.values()[i] == mDrawerType) {
							index = i;
							break;
						}
					}
					builder.setSingleChoiceItems(strs.toArray(new String[]{}), index,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mDrawerType = BaseDrawer.Type.values()[which];
									notifyActivityUpdate();
									dialog.dismiss();
									// Toast.makeText(getActivity(), "onClick->"
									// + which, Toast.LENGTH_SHORT).show();
								}
							});
					builder.setNegativeButton(android.R.string.cancel, null);
					builder.create().show();
				}
			});
		}
//					mDailyForecastView.setOnClickListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							ApiManager.loadAreaData(getActivity(), new ApiManager.LoadAreaDataListener() {
//								@Override
//								public void onLoaded(final AreaData areaData) {
//									AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//									builder.setTitle("AreaList");
//									builder.setAdapter(new ArrayAdapter<Area>(getActivity(),
//											android.R.layout.simple_list_item_1, areaData.list),
//											new DialogInterface.OnClickListener() {
//												@Override
//												public void onClick(DialogInterface dialog, int which) {
//													final Area a = areaData.list.get(which);
//													mArea = a;
//													postRefresh();
//												}
//											});
//									builder.setPositiveButton("ok", null);
//									builder.create().show();
//									toast("size->" + areaData.list.size());
//								}
		//
//								@Override
//								public void onError() {
		//
//								}
//							});
//						}
//					});
	}

	private void postRefresh() {
		if (pullRefreshLayout != null) {
//			UiUtil.toastDebug(getActivity(), mArea.name_cn + "postRefresh");
			Activity activity = getActivity();
			if (activity != null) {
				if (NetworkUtil.isNetworkAvailable(activity))
					pullRefreshLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							pullRefreshLayout.setRefreshing(true, true);
						}
					}, 100);
			}

		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			checkRefresh();
		}
//		else{
//			if(mDailyForecastView!=null){
//				mDailyForecastView.resetAnimation();
//			}
//		}

	}

	private void checkRefresh() {
		if (mWeather == null) {
			return;
		}
		// toast(mArea.name_cn + "checkRefresh");
		if (getUserVisibleHint()) {
			// updateDrawerTypeAndNotify();
			if (ApiManager.isNeedUpdate(mWeather)) {
				postRefresh();
			}
		}
	}

	private void updateWeatherUI() {
		Log.d("WeatherFragment", "mWeather: " + mWeather);
		if (mWeather == null) {
			UiUtil.toastDebug(getContext(), "mWeather is null.");
			return;
		}

		OpenWeatherJSON w = mWeather.getOpenWeatherJSON();
		AirQualityResponse a = mWeather.getAirQualityResponse();
		Log.d("WeatherFragment", "OpenWeatherJSON: " + w);
		Log.d("WeatherFragment", "AirQualityResponse: " + a);

		if (w == null || w.current == null) {
			UiUtil.toastDebug(getContext(), "OpenWeatherJSON or current weather data is missing.");
			return;
		}

		Log.d("WeatherFragment", "Current Weather: " + w.current);

		if (mWeather == null || !ApiManager.acceptWeather(mWeather)) {
			UiUtil.toastDebug(getContext(), "Weather data is not valid.");
			return;
		}

		try {
			//OpenWeatherJSON w = mWeather.get();
			if (w == null || w.current == null || w.current.weather == null || w.current.weather.isEmpty()) {
				UiUtil.toastDebug(getContext(), "Weather data is incomplete.");
				return;
			}

			Log.d("WeatherFragment", "Weather Object: " + mWeather);
			Log.d("WeatherFragment", "Current Temp: " + w.current.temp);

			if (w.current.weather.size() > 0) {
				setTextViewString(R.id.w_now_cond_text, w.current.weather.get(0).description);
			} else {
				setTextViewString(R.id.w_now_cond_text, "No data available");
			}

			updateDrawerTypeAndNotify();
			mDailyForecastView.setData(mWeather);
			mHourlyForecastView.setData(mWeather);
			mAqiView.setData(mWeather.AirQualityResponse);
			mAstroView.setData(mWeather);

			final int conditionCode = mWeather.OpenWeatherJSON.current.weather.get(0).id;

			// Group 2xx: Thunderstorm
			if (conditionCode >= 200 && conditionCode <= 232) {
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_thundershower));
			}

			// Group 3xx: Drizzle
			if (conditionCode >= 300 && conditionCode <= 321) {
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_icerain));
			}

			// Group 5xx: Rain
			if (conditionCode >= 500 && conditionCode <= 531) {
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_moderaterain));
			}

			// Group 6xx: Snow
			if (conditionCode >= 600 && conditionCode <= 622) {
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_lightsnow));
			}

			// Group 7xx: Atmosphere (e.g., mist, smoke, haze, dust)
			if (conditionCode == 701) { // Mist
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_na));
			} else if (conditionCode == 711) { // Smoke
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_haze));
			} else if (conditionCode == 721) { // Haze
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_haze));
			} else if (conditionCode == 731) { // Sand/dust whirls
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_sand));
			} else if (conditionCode == 741) { // Fog
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_foggy));
			} else if (conditionCode == 751) { // Sand
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_sand));
			} else if (conditionCode == 761) { // Dust
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_sand));
			} else if (conditionCode == 762) { // Volcanic ash
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_na));
			} else if (conditionCode == 771) { // Squalls
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_overcast));
			} else if (conditionCode == 781) { // Tornado
				mRootView.findViewById(R.id.w_weather_icon)
						.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_na));
			}


			// Group 800: Clear
			if (conditionCode == 800) {
				if (ApiManager.isNight(mWeather)) {
					mRootView.findViewById(R.id.w_weather_icon)
							.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_sun_night));
				} else {
					mRootView.findViewById(R.id.w_weather_icon)
							.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_sun));
				}
			}

			// Group 80x: Clouds
			if (conditionCode >= 801 && conditionCode <= 804) {
				if (conditionCode == 801) { // Few clouds
					if (ApiManager.isNight(mWeather)) {
						mRootView.findViewById(R.id.w_weather_icon)
								.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_cloudy_night));
					} else {
						mRootView.findViewById(R.id.w_weather_icon)
								.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_cloudy));
					}
				} else { // Overcast clouds (804)
					mRootView.findViewById(R.id.w_weather_icon)
							.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cond_icon_overcast));
				}
			}

			final String tmp = String.valueOf(w.current.temp);
			try {
				final int tmp_int = Integer.parseInt(tmp);
				if (tmp_int < 0) {
					setTextViewString(R.id.w_now_tmp, String.valueOf(-tmp_int));
					mRootView.findViewById(R.id.w_now_tmp_minus).setVisibility(View.VISIBLE);
				} else {
					setTextViewString(R.id.w_now_tmp, tmp);
					mRootView.findViewById(R.id.w_now_tmp_minus).setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				setTextViewString(R.id.w_now_tmp, tmp);
				mRootView.findViewById(R.id.w_now_tmp_minus).setVisibility(View.GONE);
			}


			setTextViewString(R.id.w_now_cond_text, w.daily.get(0).summary);

			if (ApiManager.isToday(TimestampFormatter.formatTimestamp(w.current.dt, "yyyy-MM-dd"))) {
				setTextViewString(R.id.w_basic_update_loc, TimestampFormatter.formatTimestamp(w.current.dt, "HH:mm:ss") + " 发布");
			} else {
				setTextViewString(R.id.w_basic_update_loc, TimestampFormatter.formatTimestamp(w.current.dt, "yyyy-MM-dd") + " 发布");
			}

			setTextViewString(R.id.w_todaydetail_bottomline, w.current.weather.get(0).description);
			setTextViewString(R.id.w_todaydetail_temp, ((int) Math.floor(w.current.temp)) + "°");

			setTextViewString(R.id.w_now_fl, w.current.feelsLike + "°");
			setTextViewString(R.id.w_now_hum, w.current.humidity + "%");// 湿度
			setTextViewString(R.id.w_now_vis, w.current.visibility / 1000 + "km");// 能见度
//			setTextViewString(R.id.w_now_wind_dir, String.valueOf(w.current.windDeg));
//			setTextViewString(R.id.w_now_wind_sc, String.valueOf(w.current.windSpeed));
//			setTextViewString(R.id.w_now_pres, String.valueOf(w.current.pressure));
			if (!(w.current.rain == null)) {
				setTextViewString(R.id.w_now_pcpn, (w.current.rain.get(0).hour) + "mm/h"); // 降雨量
			} else {
				setTextViewString(R.id.w_now_pcpn, 0 + "mm/h"); // 降雨量
			}
			setTextViewString(R.id.MxxPagerTitleStrip1, w.timezone);


//			try {
//				((ImageView)mRootView.findViewById(R.id.w_todaydetail_cond_imageview))
//					.setImageResource(getCondIconDrawableId(mWeather));
//			} catch (Exception e) {
//			}
//
//			setTextViewString(R.id.w_now_fl, w.current.feelsLike + "°");
//			setTextViewString(R.id.w_now_hum, w.current.humidity + "%");// 湿度
//			setTextViewString(R.id.w_now_vis, w.current.visibility + "km");// 能见度
//			setTextViewString(R.id.w_now_wind_dir, String.valueOf(w.current.windDeg));
//			setTextViewString(R.id.w_now_wind_sc, String.valueOf(w.current.windSpeed));
//			setTextViewString(R.id.w_now_pres, String.valueOf(w.current.pressure));
//			setTextViewString(R.id.w_now_pcpn, w.current.clouds + "mm"); // 降雨量


			int aqi = a.list.get(0).main.aqi;
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

			setTextViewString(R.id.w_aqi_text, airQuality);

//			setTextViewString(R.id.w_aqi_detail_text, a.list);
			setTextViewString(R.id.w_aqi_pm25, a.list.get(0).components.pm2_5 + "μg/m³");
			setTextViewString(R.id.w_aqi_pm10, a.list.get(0).components.pm10 + "μg/m³");
			setTextViewString(R.id.w_aqi_so2, a.list.get(0).components.so2 + "μg/m³");
			setTextViewString(R.id.w_aqi_no2, a.list.get(0).components.no2 + "μg/m³");

			if (w.alerts != null) {
				setTextViewString(R.id.w_suggestion_comf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_cw, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_drsg, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_flu, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_sport, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_tarv, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_uv, w.alerts.toString());

				setTextViewString(R.id.w_suggestion_comf_brf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_cw_brf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_drsg_brf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_flu_brf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_sport_brf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_tarv_brf, w.alerts.toString());
				setTextViewString(R.id.w_suggestion_uv_brf, w.alerts.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			UiUtil.toastDebug(getContext(), "Error updating UI: " + e.getMessage());
		}

	}

	@Override
	public void onDestroy() {
		// textviews.clear();
		super.onDestroy();
	}

	private void setTextViewString(int textViewId, String str) {
		TextView tv = mRootView.findViewById(textViewId);
		if (tv != null) {
			tv.setText(str);
		} else {
			UiUtil.logDebug("WeatherFragment", "TextView not found for ID: " + textViewId);
		}
	}

	@Override
	public String getTitle() {
		if (mWeather != null && mWeather.getOpenWeatherJSON() != null && mWeather.getOpenWeatherJSON().current != null) {
			// Assuming the weather object has a location or name to derive the title
			String cityName = mWeather.getOpenWeatherJSON().timezone; // Replace with a valid field from the weather object
			return cityName != null ? cityName : "Weather";
		}
		return "Weather";
	}

	@Override
	public void onResume() {
		super.onResume();
		checkRefresh();
	}

	@Override
	public void onSelected() {

	}
}


