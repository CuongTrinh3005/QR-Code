package com.example.firstapplication;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import androidx.appcompat.app.ActionBar;

public class Helper {
    public static void setActionBarBackGroundColor(ActionBar actionBar, String colorCode) {
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor(colorCode));
        actionBar.setBackgroundDrawable(colorDrawable);
    }

    private static void setActionbarTextColor(ActionBar actionBar, int color) {
        String title = actionBar.getTitle().toString();
        Spannable spannablerTitle = new SpannableString(title);
        spannablerTitle.setSpan(new BackgroundColorSpan(color), 0, spannablerTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(spannablerTitle);
    }
}
