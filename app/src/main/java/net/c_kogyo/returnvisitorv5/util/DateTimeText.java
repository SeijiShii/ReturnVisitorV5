package net.c_kogyo.returnvisitorv5.util;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2016/08/14.
 */

public class DateTimeText {

    static public String getDurationString(long duration, boolean showSec) {

        final int secMil = 1000;
        final int minMil = secMil * 60;
        final int hourMil = minMil * 60;

        int hour = (int) duration / hourMil;
        duration = duration - hour * hourMil;

        int min = (int) duration / minMil;
        duration = duration - min * minMil;

        int sec = (int) duration / secMil;

//        if (hour > 0) {
//
//            return String.valueOf(hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
//
//        } else {
//
//            return String.valueOf(min) + ":" + String.format("%02d", sec);
//
//        }

        if (showSec) {
            return String.valueOf(hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
        } else {
            return String.valueOf(hour) + ":" + String.format("%02d", min);
        }
    }

    static public String getTimeText(Calendar calendar, boolean showSec) {

        SimpleDateFormat format = new SimpleDateFormat("kk:mm");
        if (showSec) format = new SimpleDateFormat("kk:mm:ss");
        return format.format(calendar.getTime());

    }

    static public String getMonthText(Calendar month) {
        return android.text.format.DateFormat.format("yyyy, MMM", month).toString();
    }



    static public String getDateTimeText(Calendar calendar, Context context) {
        DateFormat format = android.text.format.DateFormat.getMediumDateFormat(context);
        String dateString = format.format(calendar.getTime());

        return dateString + " " + getTimeText(calendar, false);
    }
}
