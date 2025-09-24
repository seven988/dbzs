package com.example.dbzs;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Objects;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;

public class DayItem implements Parcelable {
    private final int year;
    private final int month;
    private final int day;

    private String lunarDate;

    public String getLunarDate() {
        return lunarDate;
    }

    public DayItem(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.lunarDate = getLunarDateStr(year,month,day);
    }

    public Calendar getCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal;
    }

    public static String getLunarDateStr(int year, int month, int day){
        try {
            Solar solar = new Solar(year, month+1, day);
            Lunar lunar = solar.getLunar();
            if (day==1){
                if (month==0){
                    return "元旦";
                }
                if (month==4){
                    return "劳动节";
                }
                if (month==9){
                    return "国庆节";
                }
            }
            return !lunar.getFestivals().isEmpty() ?lunar.getFestivals().get(0):(lunar.getDayInChinese().equals("初一")?(lunar.getMonthInChinese()+"月"):lunar.getDayInChinese());
        }catch (Exception e){
            return "";
        }
    }

    // region Getters
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    // endregion

    // region equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayItem dayItem = (DayItem) o;
        return year == dayItem.year &&
                month == dayItem.month &&
                day == dayItem.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day);
    }
    // endregion

    // region Parcelable implementation
    protected DayItem(Parcel in) {
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
    }

    public static final Creator<DayItem> CREATOR = new Creator<DayItem>() {
        @Override
        public DayItem createFromParcel(Parcel in) {
            return new DayItem(in);
        }

        @Override
        public DayItem[] newArray(int size) {
            return new DayItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
    }
    // endregion

    /*public String getWeekdayName(Calendar calendar) {
        calendar.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("EE", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }*/

    public String getWeekdayName(int year,int month,int day,Context context) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        int weekday = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 7=Saturday
        String[] weekdays = context.getResources().getStringArray(R.array.weekday_names);
        return weekdays[weekday - 1];
    }
}