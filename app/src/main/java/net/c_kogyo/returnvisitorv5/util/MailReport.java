package net.c_kogyo.returnvisitorv5.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.data.AggregationOfMonth;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by SeijiShii on 2016/09/26.
 */

public class MailReport {

    public static void exportToMail(Context context, Calendar month) {

        RVDBHelper helper = new RVDBHelper(context);

        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        String name = prefs.getString(Constants.SharedPrefTags.PUBLISHER_NAME, "");

        String subject = context.getString(R.string.service_report) + ": " + DateTimeText.getMonthText(month, context);

        String message = name + "\n"
                + context.getString(R.string.month)             + ": " + DateTimeText.getMonthText(month, context) + "\n"
                + context.getString(R.string.placement)         + ": " + AggregationOfMonth.placementCount(month, helper) + "\n"
                + context.getString(R.string.show_video_count)  + ": " + AggregationOfMonth.showVideoCount(month, helper) + "\n"
                + context.getString(R.string.time)              + ": " + AggregationOfMonth.hour(month, helper) + "\n"
                + context.getString(R.string.return_visit)      + ": " + AggregationOfMonth.rvCount(month, helper) + "\n"
                + context.getString(R.string.study_count)       + ": " + AggregationOfMonth.bsCount(month, helper) + "\n"
                + context.getString(R.string.comment)           + ": " ;

        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setData(Uri.parse("mailto:"));
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        mailIntent.putExtra(Intent.EXTRA_TEXT, message);

        context.startActivity(mailIntent);
    }
}
