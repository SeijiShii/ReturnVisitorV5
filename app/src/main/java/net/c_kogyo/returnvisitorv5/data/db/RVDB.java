package net.c_kogyo.returnvisitorv5.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.c_kogyo.returnvisitorv5.data.Record;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/02/21.
 */
public class RVDB {

    private RVDBOpenHelper mHelper;
    public RVDB(Context context) {
        mHelper = new RVDBOpenHelper(context);
    }

    public void saveData(Context context) {



    }

    public void loadAllPlaces(Context context, QueryDataListener listener) {

        SQLiteDatabase db = mHelper.getReadableDatabase();

    }

    public interface QueryDataListener {
        void onDataLoaded(ArrayList<Record> records);
    }




}
