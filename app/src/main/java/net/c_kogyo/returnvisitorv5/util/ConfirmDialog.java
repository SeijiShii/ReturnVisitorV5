package net.c_kogyo.returnvisitorv5.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;

/**
 * Created by SeijiShii on 2017/03/16.
 */

public class ConfirmDialog {

    public static void confirmAndDeleteVisit(Context context,
                                             DialogInterface.OnClickListener listener,
                                             Visit visit) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_visit_title);
        String message = context.getString(R.string.delete_visit_message, DateTimeText.getDateTimeText(visit.getDatetime(), context));
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }

    public static void confirmAndDeletePlace(Context context,
                                             DialogInterface.OnClickListener listener,
                                             Place place) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_place_title);
        String message = context.getString(R.string.delete_place_message, place.toString());
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }

    public static void confirmAndDeletePerson(Context context,
                                              DialogInterface.OnClickListener listener,
                                              Person person) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_person_title);
        String message = context.getString(R.string.delete_person_message, person.toString(context));
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }

    public static void confirmAndDeleteWork(Context context,
                                            DialogInterface.OnClickListener listener,
                                            Work work) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_work_title);
        String message = context.getString(
                R.string.delete_work_message,
                DateTimeText.getDateTimeText(work.getStart(), context),
                DateTimeText.getTimeText(work.getEnd(), false));
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }
}
