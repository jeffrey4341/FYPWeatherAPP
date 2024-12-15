package com.jeffrey.fypweatherapp.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TimestampFormatter {

    /**
     * Converts a Unix timestamp to a formatted string using java.time (for API level 26+).
     *
     * @param timestamp The Unix timestamp in seconds.
     * @param pattern   The desired date-time format (e.g., "yyyy-MM-dd HH:mm:ss").
     * @return The formatted date-time string.
     */
    public static String formatWithJavaTime(long timestamp, String pattern) {
        return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Converts a Unix timestamp to a formatted string using SimpleDateFormat (for lower API levels).
     *
     * @param timestamp The Unix timestamp in seconds.
     * @param pattern   The desired date-time format (e.g., "yyyy-MM-dd HH:mm:ss").
     * @return The formatted date-time string.
     */
    public static String formatWithSimpleDateFormat(long timestamp, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = new Date(timestamp * 1000); // Convert seconds to milliseconds
        return formatter.format(date);
    }

    /**
     * Automatically chooses the best formatter based on API level.
     *
     * @param timestamp The Unix timestamp in seconds.
     * @param pattern   The desired date-time format (e.g., "yyyy-MM-dd HH:mm:ss").
     * @return The formatted date-time string.
     */
    public static String formatTimestamp(long timestamp, String pattern) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return formatWithJavaTime(timestamp, pattern);
        } else {
            return formatWithSimpleDateFormat(timestamp, pattern);
        }
    }
}

