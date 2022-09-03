package com.example.firstapplication.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import androidx.appcompat.app.ActionBar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static androidx.core.content.ContextCompat.getSystemService;

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
                && hour == 6 && minute >= 0 && minute < 30))
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

    public static void setActionBarBackGroundColor(ActionBar actionBar, String colorCode) {
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor(colorCode));
        actionBar.setBackgroundDrawable(colorDrawable);
    }
    public static Boolean validateQrCode(String content){
        return Pattern.matches("[0-9]{5}_.+", content);
    }

    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
