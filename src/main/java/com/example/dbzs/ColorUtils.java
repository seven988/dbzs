package com.example.dbzs;

import android.content.Context;

import androidx.core.content.ContextCompat;

// ColorUtils.java
public class ColorUtils {
    public static String getShiftColorResId(String shift) {
        switch (shift) {
            case "早班": return "#4CAF50";
            case "中班": return "#FFC107";
            case "晚班": return "#F44336";
            case "休息": return "#9E9E9E";
            default:    return "#FFFFFFFF";
        }
    }

}