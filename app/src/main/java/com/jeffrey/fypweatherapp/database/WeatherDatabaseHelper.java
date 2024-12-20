package com.jeffrey.fypweatherapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//public class WeatherDatabaseHelper extends SQLiteOpenHelper {
//
//    private static final String DATABASE_NAME = "weather.db";
//    private static final int DATABASE_VERSION = 5 ; // Increment version for schema change
//
//    public static final String TABLE_WEATHER = "weather";
//    public static final String COLUMN_OPEN_WEATHER_JSON = "open_weather_json"; // OpenWeatherJSON data as BLOB
//    public static final String COLUMN_AIR_QUALITY_JSON = "air_quality_json";  // AirQualityResponse data as BLOB
//    public static final String COLUMN_LAST_UPDATED = "last_updated";          // Timestamp of last update
//
//    private static final String TABLE_CREATE =
//            "CREATE TABLE " + TABLE_WEATHER + " (" +
//                    COLUMN_OPEN_WEATHER_JSON + " BLOB, " +
//                    COLUMN_AIR_QUALITY_JSON + " BLOB, " +
//                    COLUMN_LAST_UPDATED + " INTEGER);";
//
//    public WeatherDatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(TABLE_CREATE);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER);
//        onCreate(db);
//    }
//}
