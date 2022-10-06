package com.example.firstapplication.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.ActionBar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Helper {
    public static final List<String> allowedDaysOfWeek = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SUN");

    public static String getDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("E dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public static String getTimestampFromDate(Date date) {
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
//        String dateString = formatter.format(date);
//        return dateString.split(" ")[1];
        String dataStr = convertDateToString(date);
        String timestamp = dataStr.split(" ")[2];
        return timestamp;
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
        return dateString.split(" ")[0];
    }

    public static Boolean checkTueAndThuAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        int hour = getHourFromTimestamp(), minute = getMinuteFromTimestamp();

        if (allowedDaysOfWeek.get(1).equalsIgnoreCase(dayOfWeek) || allowedDaysOfWeek.get(3).equalsIgnoreCase(dayOfWeek)){
            if((hour == 5  && minute >= 0) || (hour == 6 && minute == 0)){
                return true;
            }
            else if((hour == 18 && minute >= 0) || (hour == 19 && minute == 0)){
                return true;
            }
            else return false;
        }
        else{
            return false;
        }
    }

    public static Boolean checkSundayEarlyAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        int hour = getHourFromTimestamp(), minute = getMinuteFromTimestamp();

        if ((allowedDaysOfWeek.get(5).equalsIgnoreCase(dayOfWeek) && hour == 6 && minute >= 0 && minute <= 15))
            return true;

        return false;
    }

    public static Boolean checkOtherDaysAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        int hour = getHourFromTimestamp(), minute = getMinuteFromTimestamp();

        if (allowedDaysOfWeek.get(0).equalsIgnoreCase(dayOfWeek) || allowedDaysOfWeek.get(2).equalsIgnoreCase(dayOfWeek)
                || allowedDaysOfWeek.get(4).equalsIgnoreCase(dayOfWeek)){
            if((hour == 5  && minute >= 0) || (hour == 6 && minute == 0)){
                return true;
            }
            else if((hour == 18 && minute >= 0) || (hour == 19 && minute == 0)){
                return true;
            }
            else return false;
        }
        else{
            return false;
        }
    }

    public static Boolean checkSundayAllowed() {
        Date date = new Date();
        String dayOfWeek = getDayOfWeek(date);
        int hour = getHourFromTimestamp(), minute = getMinuteFromTimestamp();

        if((allowedDaysOfWeek.get(5).equalsIgnoreCase(dayOfWeek) && (hour == 7 || (hour == 8 && minute <= 30))))
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

    public static String getCurrentTimeDisplay(){
        String currentDate = convertDateToString(new Date());
        String dayOfWeek = getDayOfWeek(new Date());
        String displayDate = "";
        switch (dayOfWeek){
            case "Mon":{
                displayDate = "Thứ Hai, " + currentDate.substring(3);
                break;
            }
            case "Tue":{
                displayDate = "Thứ Ba, " + currentDate.substring(3);
                break;
            }
            case "Wed":{
                displayDate = "Thứ Tư, " + currentDate.substring(3);
                break;
            }
            case "Thu":{
                displayDate = "Thứ Năm, " + currentDate.substring(3);
                break;
            }
            case "Fri":{
                displayDate = "Thứ Sáu, " + currentDate.substring(3);
                break;
            }
            case "Sat":{
                displayDate = "Thứ Bảy, " + currentDate.substring(3);
                break;
            }
            case "Sun":{
                displayDate = "Chúa Nhật, " + currentDate.substring(3);
                break;
            }
            default:
                break;
        }
        return displayDate;
    }
}
