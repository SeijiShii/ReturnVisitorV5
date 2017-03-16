package net.c_kogyo.returnvisitorv5.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Visit;

/**
 * Created by SeijiShii on 2017/03/16.
 */

public class ConfirmDialog {

    public static void confirmAndDeleteVisit(Context context,
                                             DialogInterface.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_visit_title);
        builder.setMessage(R.string.delete_visit_message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }

    public static void confirmAndDeletePlace(Context context,
                                             DialogInterface.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_place_title);
        builder.setMessage(R.string.delete_place_message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }

    public static void confirmAndDeletePerson(Context context,
                                             DialogInterface.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_person_title);
        builder.setMessage(R.string.delete_person_message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, listener);
        builder.create().show();
    }
}
