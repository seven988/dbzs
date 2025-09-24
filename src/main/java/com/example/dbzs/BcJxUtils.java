package com.example.dbzs;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.List;

public class BcJxUtils {
    public static String getJx(String shift) {
        switch (shift) {
            case "早班": return "早";
            case "中班": return "中";
            case "晚班": return "晚";
            case "休息": return "休";
            default:    return "";
        }
    }

    public static SpannableString joinText(List<String> selected){
        StringBuilder str = new StringBuilder();
        String result = "";
        for (String s : selected) {
            str.append(getJx(s)).append(",");
        }
        if (str.toString().length()>1){
            result = "已选班次："+str.toString().substring(0,str.toString().length()-1);
        }else {
            result = "已选班次：";
        }
        SpannableString spannableString = new SpannableString(result);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 5, 0);
        if (result.length()>5){
            for (int i = 5; i <result.length() ; i++) {
                if (result.charAt(i) == ','){
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), i, i+1, 0);
                }
                if (result.charAt(i) == '早'){
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(ColorUtils.getShiftColorResId("早班"))), i, i+1, 0);
                }
                if (result.charAt(i) == '中'){
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(ColorUtils.getShiftColorResId("中班"))), i, i+1, 0);
                }
                if (result.charAt(i) == '晚'){
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(ColorUtils.getShiftColorResId("晚班"))), i, i+1, 0);
                }
                if (result.charAt(i) == '休'){
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(ColorUtils.getShiftColorResId("休息"))), i, i+1, 0);
                }
            }
        }
        return spannableString;
    }
}
