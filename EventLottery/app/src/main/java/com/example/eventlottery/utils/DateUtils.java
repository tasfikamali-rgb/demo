package com.example.eventlottery.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_SHORT =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.getDefault());

    public static String formatDate(long millis) {
        return DATE_FORMAT.format(new Date(millis));
    }

    public static String formatDateShort(long millis) {
        return DATE_SHORT.format(new Date(millis));
    }

    public static String formatDateTime(long millis) {
        return DATE_TIME_FORMAT.format(new Date(millis));
    }

    public static String formatTime(long millis) {
        return TIME_FORMAT.format(new Date(millis));
    }

    public static String formatRelative(long millis) {
        long diff = System.currentTimeMillis() - millis;
        long minutes = diff / 60000;
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }

    public static long daysUntil(long millis) {
        long diff = millis - System.currentTimeMillis();
        if (diff < 0) return 0;
        return diff / (1000 * 60 * 60 * 24);
    }
}
