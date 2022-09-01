package com.example.firstapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Helper {
    public static final List<String> allowedDaysOfWeek = Arrays.asList("TUE", "THU", "SUN");

    public static String getDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(date);
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        return formatter.format(date);
    }

    public static String getTimestampFromDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateString = formatter.format(date);
        return dateString.split(" ")[1];
    }

    public static Integer getHourFromTimestamp() {
        String timestamp = getTimestampFromDate(new Date());
        return Integer.parseInt(timestamp.split(":")[0]);
    }

    public static Integer getMinuteFromTimestamp() {
        String timestamp = getTimestampFromDate(new Date());
        return Integer.parseInt(timestamp.split(":")[1]);
    }

    public static String getDayOfWeek(Date date) {
        String dateString = convertDateToString(date);
        return dateString.split(",")[0];
    }

    public static Boolean checkTueAndThuAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        Integer hour = getHourFromTimestamp();
        Integer minute = getMinuteFromTimestamp();

        if (((allowedDaysOfWeek.get(0).equalsIgnoreCase(dayOfWeek) || allowedDaysOfWeek.get(1).equalsIgnoreCase(dayOfWeek))
                && hour == 9 && minute >= 0 && minute < 30))
            return true;

        return false;
    }

    public static Boolean checkSundayEarlyAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        Integer hour = getHourFromTimestamp();
        Integer minute = getMinuteFromTimestamp();

        if ((allowedDaysOfWeek.get(2).equalsIgnoreCase(dayOfWeek) && hour == 6 && minute >= 0 && minute < 30))
            return true;

        return false;
    }

    public static Boolean checkSundayAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        Integer hour = getHourFromTimestamp();

        if ((allowedDaysOfWeek.get(2).equalsIgnoreCase(dayOfWeek) && hour >= 7 && hour <= 8))
            return true;

        return false;
    }
}
