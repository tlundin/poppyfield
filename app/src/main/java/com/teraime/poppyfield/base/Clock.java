package com.teraime.poppyfield.base;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Clock {

    public static String getTimeStamp() {
        return getYear()+getMonth()+getDayOfMonth()+"_"+getHour()+"_"+getMinute();
    }

    public static String getYear() {
        return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }

    public static String getWeekNumber() {
        return Integer.toString(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
    }

    public static String getMonth() {
        return Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
    }

    public static String getDayOfMonth() {
        return Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    public static String getHour() {
        return Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    public static String getMinute() {
        return Integer.toString(Calendar.getInstance().get(Calendar.MINUTE));
    }
    public static String getSecond() {
        return Integer.toString(Calendar.getInstance().get(Calendar.SECOND));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getSweDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
}
