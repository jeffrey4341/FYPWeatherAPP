package com.jeffrey.fypweatherapp.weather.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.jeffrey.fypweatherapp.database.WeatherDatabaseHelper;
import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer;
import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer.Type;
import com.jeffrey.fypweatherapp.util.LocationManager;
import com.jeffrey.fypweatherapp.util.PreferenceUtil;
import com.jeffrey.fypweatherapp.util.TimestampFormatter;
import com.jeffrey.fypweatherapp.util.UiUtil;
import com.jeffrey.fypweatherapp.weather.MainActivity;
import com.jeffrey.fypweatherapp.weather.WeatherApplication;
import com.jeffrey.fypweatherapp.weather.api.entity.AirQualityResponse;
import com.jeffrey.fypweatherapp.weather.api.entity.DailyForecast;
import com.jeffrey.fypweatherapp.weather.api.entity.OpenWeatherJSON;
import com.jeffrey.fypweatherapp.weather.api.entity.Weather;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ApiManager {

	private static final String ONE_CALL_URL = "https://api.openweathermap.org/data/3.0/onecall";
	private static final String AQI_CALL_URL = "https://api.openweathermap.org/data/2.5/air_pollution";
	private static final String API_KEY = "6e6a888df4e70041d6de407bd99f3f9f";
	private static final Gson GSON = new GsonBuilder().serializeNulls().create();
	static final String TAG = ApiManager.class.getSimpleName();
	private static final String KEY_SELECTED_AREA = "KEY_SELECTED_AREA";
	private static StringBuilder WeatherJSON;
	private static StringBuilder AQIJSON;

	public interface ApiListener {
		public void onReceiveWeather(Weather weather, boolean updated);

		public void onUpdateError();
	}
	public static Weather fetchWeather(double lat, double lon) {
		String urlString = ONE_CALL_URL + "?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
		String aqiurlString = AQI_CALL_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
		Weather weather = new Weather();

		try {
			UiUtil.logDebug(TAG, "theAPI->" + urlString);
			HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();

				Log.d("FUCK", "API Response: " + response.toString());
				WeatherJSON = response;

				OpenWeatherJSON parsedData = GSON.fromJson(response.toString(), OpenWeatherJSON.class);

				if (parsedData == null || !parsedData.isValid()) {
					Log.e("ApiManager", "OpenWeatherJSON is null or invalid after parsing.");
					throw new IllegalStateException("Failed to parse weather data.");
				}

				weather.OpenWeatherJSON = parsedData;

//				Log.d("ApiManager", "Parsed Weather: " + weather);
//				Log.d("ApiManager", "Parsed Weather Object: " + weather.toString());
//				Log.d("ApiManager", "Latitude: " + parsedData.lat);
//				Log.d("ApiManager", "Longitude: " + parsedData.lon);
//				Log.d("ApiManager", "Current Weather: " + parsedData.current.weather.get(0).main);
//				Log.d("ApiManager", "Hourly Weather Count: " + (parsedData.hourly != null ? parsedData.hourly.size() : "null"));
//				Log.d("ApiManager", "Daily Weather Count: " + (parsedData.daily != null ? parsedData.daily.size() : "null"));

//				return weather;
//			} else {
//				throw new IllegalStateException("API returned an error: HTTP " + responseCode);
			}

			// Fetch AQI data
			HttpURLConnection aqiConnection = (HttpURLConnection) new URL(aqiurlString).openConnection();
			aqiConnection.setRequestMethod("GET");
			aqiConnection.setConnectTimeout(5000);
			aqiConnection.setReadTimeout(5000);

			if (aqiConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader aqiReader = new BufferedReader(new InputStreamReader(aqiConnection.getInputStream()));
				StringBuilder aqiResponse = new StringBuilder();
				String aqiLine;
				while ((aqiLine = aqiReader.readLine()) != null) {
					aqiResponse.append(aqiLine);
				}
				aqiReader.close();
				AQIJSON = aqiResponse;

				AirQualityResponse aqiParsedData = GSON.fromJson(aqiResponse.toString(), AirQualityResponse.class);
				if (aqiParsedData != null) {
					weather.AirQualityResponse = aqiParsedData;
				} else {
					Log.e("ApiManager", "AirQualityResponse is null or invalid after parsing.");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed to fetch weather or AQI data: " + e.getMessage());
		}
//
//		try {
//			URL url = new URL(aqiurlString);
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setRequestMethod("GET");
//			connection.setConnectTimeout(5000);
//			connection.setReadTimeout(5000);
//
//			int responseCode = connection.getResponseCode();
//			if (responseCode == HttpURLConnection.HTTP_OK) {
//				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				StringBuilder response = new StringBuilder();
//				String line;
//				while ((line = reader.readLine()) != null) {
//					response.append(line);
//				}
//				reader.close();
//
//				Log.d("ApiManager", "AQI_API Response: " + response.toString());
//
//				return GSON.fromJson(response.toString(), (java.lang.reflect.Type) AirQualityResponse.class);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new IllegalStateException("Failed to fetch weather data: " + e.getMessage());
//		}
		return weather;
//		return null;
	}

//	public static AirQualityResponse fetchAirQuality(double lat, double lon) {
//		String aqiurlString = AQI_CALL_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
//
//		try {
//			URL url = new URL(aqiurlString);
//			UiUtil.logDebug(TAG, "theAQIAPI->" + url);
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setRequestMethod("GET");
//			connection.setConnectTimeout(5000);
//			connection.setReadTimeout(5000);
//
//			int responseCode = connection.getResponseCode();
//			if (responseCode == HttpURLConnection.HTTP_OK) {
//				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				StringBuilder response = new StringBuilder();
//				String line;
//				while ((line = reader.readLine()) != null) {
//					response.append(line);
//				}
//				reader.close();
//
//				Log.d("ApiManager", "AQI API Response: " + response.toString());
//				AQIJSON = response;
//
//				AirQualityResponse airQuality = GSON.fromJson(response.toString(), AirQualityResponse.class);
//				if (airQuality == null) {
//					throw new IllegalStateException("Failed to parse AQI data.");
//				}
//
//				return airQuality;
//			} else {
//				throw new IllegalStateException("AQI API returned an error: HTTP " + responseCode);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new IllegalStateException("Failed to fetch AQI data: " + e.getMessage());
//		}
//	}


	public static Weather loadWeather(@NonNull Context context, @NonNull String areaId) {
//		if (context == null || TextUtils.isEmpty(areaId)) {
//			return null;
//		}
//		try {
//			String json = MxxPreferenceUtil.getPrefString(context, areaId, null);
//			if (TextUtils.isEmpty(json)) {
//				return null;
//			}
//			Weather weather = GSON.fromJson(json, Weather.class);
//			return weather;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return null;
	}

//	public static ArrayList<Area> loadSelectedArea(Context context) {
//		ArrayList<Area> areas = new ArrayList<ApiManager.Area>();
//		String json = WeatherApplication.USE_SAMPLE_DATA ? GITHUB_SAMPLE_SELECTED_AREA : PreferenceUtil.getPrefString(context, KEY_SELECTED_AREA, "");
//		if (TextUtils.isEmpty(json)) {
//			return areas;
//		}
//		try {
//			Area[] aa = GSON.fromJson(json, Area[].class);
//			if (aa != null) {
//				Collections.addAll(areas, aa);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return areas;
//	}

	public static void updateWeather(@NonNull Context context, @NonNull Double latitude, @NonNull Double longitude, @NonNull ApiListener apiListener) {
		if (TextUtils.isEmpty(latitude.toString()) || TextUtils.isEmpty(longitude.toString())) {
			return;
		}
		// UiUtil.logDebug(TAG, "updateWeather->" + latitude + longitude);

	}
	/**
	 * 是否需要更新Weather数据 1小时15分钟之内的return false; 传入null或者有问题的weather也会返回true
	 *
	 * @param weather
	 * @return
	 */
	public static boolean isNeedUpdate(Weather weather) {
		if (!acceptWeather(weather)) {
			return true;
		}
		try {
			final String updateTime = TimestampFormatter.formatTimestamp(weather.getOpenWeatherJSON().current.dt, "yyyy-MM-dd HH:mm");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date updateDate = format.parse(updateTime);
			Date curDate = new Date();
			long interval = curDate.getTime() - updateDate.getTime();// 时间间隔 ms
			if ((interval >= 0) && (interval < 75 * 60 * 1000)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 是否是今天2015-11-05 04:00 合法data格式： 2015-11-05 04:00 或者2015-11-05
	 *
	 * @param date
	 * @return
	 */
	public static boolean isToday(String date) {
		if (TextUtils.isEmpty(date) || date.length() < 10) {// 2015-11-05
			// length=10
			return false;
		}
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String today = format.format(new Date());
			if (TextUtils.equals(today, date.substring(0, 10))) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		// final String[] strs = date.substring(0, 10).split("-");
		// final int year = Integer.valueOf(strs[0]);
		// final int month = Integer.valueOf(strs[1]);
		// final int day = Integer.valueOf(strs[2]);
		// Calendar c = Calendar.getInstance();
		// int curYear = c.get(Calendar.YEAR);
		// int curMonth = c.get(Calendar.MONTH) + 1;//Java月份从0月开始
		// int curDay = c.get(Calendar.DAY_OF_MONTH);
		// if(curYear == year && curMonth == month && curDay == day){
		// return true;
		// }
		// return false;
	}

	/**
	 * 是否是合法的Weather数据
	 *
	 * @param weather
	 * @return
	 */
	public static boolean acceptWeather(Weather weather) {
		if (weather == null || weather.getOpenWeatherJSON() == null || weather.getOpenWeatherJSON().current == null) {
			Log.e("ApiManager", "Weather data is invalid.");
			return false;
		}
		return true;
	}

	/**
	 * 把Weather转换为对应的BaseDrawer.Type
	 *
	 * @param weather
	 * @return
	 */
	public static BaseDrawer.Type convertWeatherType(Weather weather) {
		if (weather == null) {
			return Type.DEFAULT;
		}

		Log.d("ApiManager", "Weather Object: " + weather.OpenWeatherJSON.current.weather.get(0).id);

		final boolean isNight = isNight(weather);

		try {
//			final int conditionCode = weather.OpenWeatherJSON.current.weather.get(0).id; // OpenWeather condition code
			final int conditionCode = weather.OpenWeatherJSON.current.weather.get(0).id;

			// Group 2xx: Thunderstorm
			if (conditionCode >= 200 && conditionCode <= 232) {
				return isNight ? Type.RAIN_SNOW_N : Type.RAIN_SNOW_D;
			}

			// Group 3xx: Drizzle
			if (conditionCode >= 300 && conditionCode <= 321) {
				return isNight ? Type.HAZE_N : Type.HAZE_D;
			}

			// Group 5xx: Rain
			if (conditionCode >= 500 && conditionCode <= 531) {
				return isNight ? Type.RAIN_N : Type.RAIN_D;
			}

			// Group 6xx: Snow
			if (conditionCode >= 600 && conditionCode <= 622) {
				return isNight ? Type.SNOW_N : Type.SNOW_D;
			}

			// Group 7xx: Atmosphere (e.g., mist, smoke, haze, dust)
			if (conditionCode == 701) { // Mist
				return isNight ? Type.DEFAULT : Type.DEFAULT;
			} else if (conditionCode == 711) { // Smoke
				return isNight ? Type.FOG_N : Type.FOG_D;
			} else if (conditionCode == 721) { // Haze
				return isNight ? Type.HAZE_N : Type.HAZE_D;
			} else if (conditionCode == 731) { // Sand/dust whirls
				return isNight ? Type.SAND_N : Type.SAND_D;
			} else if (conditionCode == 741) { // Fog
				return isNight ? Type.FOG_N : Type.FOG_D;
			} else if (conditionCode == 751) { // Sand
				return isNight ? Type.SAND_N : Type.SAND_D;
			} else if (conditionCode == 761) { // Dust
				return isNight ? Type.SAND_N : Type.SAND_D;
			} else if (conditionCode == 762) { // Volcanic ash
				return isNight ? Type.HAZE_N : Type.HAZE_D;
			} else if (conditionCode == 771) { // Squalls
				return isNight ? Type.WIND_N : Type.WIND_D;
			} else if (conditionCode == 781) { // Tornado
				return isNight ? Type.WIND_N : Type.WIND_D;
			}


			// Group 800: Clear
			if (conditionCode == 800) {
				return isNight ? Type.CLEAR_N : Type.CLEAR_D;
			}

			// Group 80x: Clouds
			if (conditionCode >= 801 && conditionCode <= 804) {
				if (conditionCode == 801) { // Few clouds
					return isNight ? Type.CLOUDY_N : Type.CLOUDY_D;
				} else if (conditionCode == 802) { // Scattered clouds
					return isNight ? Type.CLOUDY_N : Type.CLOUDY_D;
				} else if (conditionCode == 803) { // Broken clouds
					return isNight ? Type.CLOUDY_N : Type.CLOUDY_D;
				} else { // Overcast clouds (804)
					return isNight ? Type.OVERCAST_N : Type.OVERCAST_D;
				}
			}

//		try {
//			final int w = Integer.parseInt(weather.get().current.weather.get(0).toString());
//			switch (w) {
//				case 100:
//					return isNight ? Type.CLEAR_N : Type.CLEAR_D;
//				case 101:// 多云
//				case 102:// 少云
//				case 103:// 晴间多云
//					return isNight ? Type.CLOUDY_N : Type.CLOUDY_D;
//				case 104:// 阴
//					return isNight ? Type.OVERCAST_N : Type.OVERCAST_D;
//				// 200 - 213是风
//				case 200:// thunderstorm with light rain
//				case 201:// thunderstorm with rain
//				case 202:// thunderstorm with heavy rain
//				case 203://
//				case 204://
//				case 205://
//				case 206://
//				case 207://
//				case 208://
//				case 209://
//				case 210:// light thunderstorm
//				case 211:// thunderstorm
//				case 212:// heavy thunderstorm
//				case 221:// ragged thunderstorm
//				case 230://	thunderstorm with light drizzle
//				case 231:// thunderstorm with drizzle
//				case 232:// thunderstorm with heavy drizzle
//					return isNight ? Type.WIND_N : Type.WIND_D;
//				case 300:// 阵雨Shower Rain
//				case 301:// 强阵雨 Heavy Shower Rain
//				case 302:// 雷阵雨 Thundershower
//				case 303:// 强雷阵雨 Heavy Thunderstorm
//				case 304:// 雷阵雨伴有冰雹 Hail
//				case 305:// 小雨 Light Rain
//				case 306:// 中雨 Moderate Rain
//				case 307:// 大雨 Heavy Rain
//				case 308:// 极端降雨 Extreme Rain
//				case 309:// 毛毛雨/细雨 Drizzle Rain
//				case 310:// 暴雨 Storm
//				case 311:// 大暴雨 Heavy Storm
//				case 312:// 特大暴雨 Severe Storm
//				case 313:// 冻雨 Freezing Rain
//					return isNight ? Type.RAIN_N : Type.RAIN_D;
//				case 400:// 小雪 Light Snow
//				case 401:// 中雪 Moderate Snow
//				case 402:// 大雪 Heavy Snow
//				case 403:// 暴雪 Snowstorm
//				case 407:// 阵雪 Snow Flurry
//					return isNight ? Type.SNOW_N : Type.SNOW_D;
//				case 404:// 雨夹雪 Sleet
//				case 405:// 雨雪天气 Rain And Snow
//				case 406:// 阵雨夹雪 Shower Snow
//					return isNight ? Type.RAIN_SNOW_N : Type.RAIN_SNOW_D;
//				case 500:// 薄雾
//				case 501:// 雾
//					return isNight ? Type.FOG_N : Type.FOG_D;
//				case 502:// 霾
//				case 504:// 浮尘
//					return isNight ? Type.HAZE_N : Type.HAZE_D;
//				case 503:// 扬沙
//				case 506:// 火山灰
//				case 507:// 沙尘暴
//				case 508:// 强沙尘暴
//					return isNight ? Type.SAND_N : Type.SAND_D;
//				default:
			return isNight ? Type.UNKNOWN_N : Type.UNKNOWN_D;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isNight ? Type.UNKNOWN_N : Type.UNKNOWN_D;
	}

	/**
	 * 转换日期2015-11-05为今天、明天、昨天，或者是星期几
	 *
	 * @param //date
	 * @return
	 */
//	public static String prettyDate(String date) {
//		try {
//			final String[] strs = date.split("-");
//			final int year = Integer.valueOf(strs[0]);
//			final int month = Integer.valueOf(strs[1]);
//			final int day = Integer.valueOf(strs[2]);
//			Calendar c = Calendar.getInstance();
//			int curYear = c.get(Calendar.YEAR);
//			int curMonth = c.get(Calendar.MONTH) + 1;// Java月份从0月开始
//			int curDay = c.get(Calendar.DAY_OF_MONTH);
//			if (curYear == year && curMonth == month) {
//				if (curDay == day) {
//					return "今天";
//				} else if ((curDay + 1) == day) {
//					return "明天";
//				} else if ((curDay - 1) == day) {
//					return "昨天";
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return date;
//	}

	public static boolean isNight(Weather weather) {
		if (weather == null) {
			Log.d("isNight", "Weather is null. Returning false.");
			return false;
		}
		// SimpleDateFormat time=new SimpleDateFormat("yyyy MM dd HH mm ss");
		try {
			final Date date = new Date();
			String todaydate = (new SimpleDateFormat("yyyy-MM-dd")).format(date);
			String todaydate1 = (new SimpleDateFormat("yyyy-M-d")).format(date);
			DailyForecast todayForecast = null;
			for (DailyForecast forecast : weather.getOpenWeatherJSON().daily) {
				String formattedTime = TimestampFormatter.formatTimestamp(forecast.dt, "yyyy-MM-dd");
				if (TextUtils.equals(todaydate, formattedTime) || TextUtils.equals(todaydate1, formattedTime)) {
					todayForecast = forecast;
					break;
				}
			}
			if (todayForecast != null) {
				final int curTime = Integer.parseInt((new SimpleDateFormat("HHmm").format(date)));
				final int srTime = Integer.parseInt(TimestampFormatter.formatTimestamp(Long.parseLong(todayForecast.sunrise), "HHmm").replaceAll(":", ""));// 日出
				final int ssTime = Integer.parseInt(TimestampFormatter.formatTimestamp(Long.parseLong(todayForecast.sunset), "HHmm").replaceAll(":", ""));// 日落
				Log.d("isNight", "Current Time: " + curTime + ", Sunrise Time: " + srTime + ", Sunset Time: " + ssTime);
				if (curTime > srTime && curTime <= ssTime) {// 是白天
					Log.d("isNight", "It is daytime. Returning false.");
					return false;
				} else {
					Log.d("isNight", "It is nighttime. Returning true.");
					return true;
				}
			} else {
				Log.d("isNight", "No matching forecast for today found. Returning false.");
			}
		} catch (Exception e) {
			Log.e("isNight", "Exception occurred: " + e.getMessage(), e);
		}
		Log.d("isNight", "Default return false.");
		return false;
	}

	public static void saveWeatherData(Context context, long lastUpdated) {
		WeatherDatabaseHelper dbHelper = new WeatherDatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// Insert new data
		ContentValues values = new ContentValues();
		values.put(WeatherDatabaseHelper.COLUMN_OPEN_WEATHER_JSON, String.valueOf(WeatherJSON));
		values.put(WeatherDatabaseHelper.COLUMN_AIR_QUALITY_JSON, String.valueOf(AQIJSON));
		values.put(WeatherDatabaseHelper.COLUMN_LAST_UPDATED, lastUpdated);

		db.insert(WeatherDatabaseHelper.TABLE_WEATHER, null, values);
		db.close();
	}

	public static String[] getLatestWeatherData(Context context) {
		WeatherDatabaseHelper dbHelper = new WeatherDatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String openWeatherJson = null;
		String airQualityJson = null;

		Cursor cursor = db.query(WeatherDatabaseHelper.TABLE_WEATHER,
				new String[]{WeatherDatabaseHelper.COLUMN_OPEN_WEATHER_JSON, WeatherDatabaseHelper.COLUMN_AIR_QUALITY_JSON},
				null, null, null, null,
				WeatherDatabaseHelper.COLUMN_LAST_UPDATED + " DESC", "1"); // Sort by timestamp and limit to 1 row

		if (cursor != null && cursor.moveToFirst()) {
			int openWeatherIndex = cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_OPEN_WEATHER_JSON);
			int airQualityIndex = cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_AIR_QUALITY_JSON);

			if (openWeatherIndex >= 0) {
				byte[] openWeatherBlob = cursor.getBlob(openWeatherIndex);
				openWeatherJson = new String(openWeatherBlob, StandardCharsets.UTF_8);
			} else {
				Log.e("Database", "COLUMN_OPEN_WEATHER_JSON not found");
			}

			if (airQualityIndex >= 0) {
				byte[] airQualityBlob = cursor.getBlob(airQualityIndex);
				airQualityJson = new String(airQualityBlob, StandardCharsets.UTF_8);
			} else {
				Log.e("Database", "COLUMN_AIR_QUALITY_JSON not found");
			}
			cursor.close();
		}
		else {
			return null;
		}
		db.close();
		return new String[]{openWeatherJson, airQualityJson};
	}



//	public static class Area implements Serializable {
//		private static final long serialVersionUID = 7646903512215148839L;
//
//		public Area() {
//			super();
//		}
//
//		@SerializedName("id")
//		@Expose
//		public String id;
//		@SerializedName("name_en")
//		@Expose
//		public String name_en;
//		@SerializedName("name_cn")
//		@Expose
//		public String name_cn;
//		@SerializedName("city")
//		@Expose
//		public String city;
//		@SerializedName("province")
//		@Expose
//		public String province;
//
//		@Override
//		public String toString() {
//			return name_cn + " [" + city + "," + province + "]";// + "," + id
//		}
//
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + ((city == null) ? 0 : city.hashCode());
//			result = prime * result + ((id == null) ? 0 : id.hashCode());
//			result = prime * result + ((name_cn == null) ? 0 : name_cn.hashCode());
//			result = prime * result + ((name_en == null) ? 0 : name_en.hashCode());
//			result = prime * result + ((province == null) ? 0 : province.hashCode());
//			return result;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			Area other = (Area) obj;
//			if (city == null) {
//				if (other.city != null)
//					return false;
//			} else if (!city.equals(other.city))
//				return false;
//			if (id == null) {
//				if (other.id != null)
//					return false;
//			} else if (!id.equals(other.id))
//				return false;
//			if (name_cn == null) {
//				if (other.name_cn != null)
//					return false;
//			} else if (!name_cn.equals(other.name_cn))
//				return false;
//			if (name_en == null) {
//				if (other.name_en != null)
//					return false;
//			} else if (!name_en.equals(other.name_en))
//				return false;
//			if (province == null) {
//				if (other.province != null)
//					return false;
//			} else if (!province.equals(other.province))
//				return false;
//			return true;
//		}
//
//	}
}