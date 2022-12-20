package com.example.firstapplication.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class Helper {
    public static String getDateTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public static String convertDateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("E dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public static String getTimestampFromDate(Date date) {
        String dataStr = convertDateToString(date);
        String timestamp = dataStr.split(" ")[2];
        return timestamp;
    }

    public static Integer getDateOfWeek(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static Boolean checkTueAndThuAllowed(Date date, LocalTime localTime) {
        Integer dayOfWeek = getDateOfWeek(date);
        if (dayOfWeek == Calendar.TUESDAY || dayOfWeek == Calendar.THURSDAY){
            int hour = localTime.getHour(), minute = localTime.getMinute();
            if((hour == 5  && minute >= 0) || (hour == 6 && minute == 0)){
                return true;
            }
            else return (hour == 18 && minute >= 0) || (hour == 19 && minute == 0);
        }
        else{
            return false;
        }
    }

    public static Boolean checkSundayEarlyAllowed(Date date, LocalTime localTime) {
        Integer dayOfWeek = getDateOfWeek(date);
        if (dayOfWeek == Calendar.SUNDAY){
            int hour = localTime.getHour(), minute = localTime.getMinute();
            return hour == 6 && minute >= 0 && minute <= 15;
        }
        return false;
    }

    public static Boolean checkOtherDaysAllowed(Date date, LocalTime localTime) {
        Integer dayOfWeek = getDateOfWeek(date);

        boolean isMonday = dayOfWeek==Calendar.MONDAY;
        boolean isWednesday = dayOfWeek==Calendar.WEDNESDAY;
        boolean isFriday = dayOfWeek==Calendar.FRIDAY;
        boolean isSaturday = dayOfWeek==Calendar.SATURDAY;

        if (isMonday || isWednesday || isFriday || isSaturday){
            int hour = localTime.getHour();
            int minute = localTime.getMinute();
            if((hour == 5  && minute >= 0) || (hour == 6 && minute == 0)){
                return true;
            }
            else return (!isSaturday && (hour == 18 && minute >= 0) || (hour == 19 && minute == 0));
        }
        else{
            return false;
        }
    }

    public static Boolean checkSundayAllowed(Date date, LocalTime localTime) {
        Integer dayOfWeek = getDateOfWeek(date);
        if(dayOfWeek == Calendar.SUNDAY){
            int hour = localTime.getHour(), minute = localTime.getMinute();
            if(hour == 7 || (hour == 8 && minute <= 30)){
                return true;
            }
            else if((hour == 15 && minute >= 30) || (hour == 16 && minute == 0)){
                return true;
            } else return (hour == 17 && minute >= 0) || (hour == 18 && minute == 0);
        }
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

    public static String getStringResources(AppCompatActivity activity, int option){
        return activity.getResources().getString(option);
    }

    public static void prepareDataToTesting(DatabaseHandler handler){
        handler.addAttendance(new Attendance(1, "02123_Giuse_Nguyen_Van_A", "KHAC", "01-10-2022", false));
        handler.addAttendance(new Attendance(2, "04002_Daminh_Vo_Nhat", "KHAC", "01-10-2022", false));
        handler.addAttendance(new Attendance(3, "05089_Anna_Le_Thi_Truc", "KHAC", "06-10-2022", false));
        handler.addAttendance(new Attendance(4, "01002_Matheu_Do_Minh", "KHAC", "08-10-2022", false));
        handler.addAttendance(new Attendance(5, "00123_Monica_Vu_Thi_Trang", "KHAC", "06-10-2022", false));
        handler.addAttendance(new Attendance(6, "02123_Giuse_Nguyen_Van_A", "KHAC", "06-10-2022", false));

        for(int i=0; i<300; i++){
            handler.addAttendance(new Attendance(6, "03004_Anna_Vo_Le_Thuy_Duyen", "KHAC", "07-10-2022", false));
        }
    }
}
