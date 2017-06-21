package net.c_kogyo.returnvisitorv5.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVRecord;

import java.util.ArrayList;

import static net.c_kogyo.returnvisitorv5.db.RVDBContract.AND;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.CLASS_NAME;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.DATA;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.DATA_ID;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.IS_DELETED;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.TABLE_NAME;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.UPDATED_AT;

/**
 * Created by SeijiShii on 2017/06/21.
 */

public class RVDBHelper {

    private static final String TAG = "RVDBHelper";

    public enum ClassName {
        Place,
        Person,
        Visit,
        Tag,
        NoteCompItem,
        Work,
        Publication
    }

    public SQLiteDatabase mDB;
    private RVDBOpenHelper mOpenHelper;

    public RVDBHelper(Context context) {
        this.mOpenHelper = new RVDBOpenHelper(context);
        initDB();
    }

    private void initDB() {
        if (mDB == null) {
            mDB = mOpenHelper.getWritableDatabase();
        }
    }

    public void SaveRecord(RVRecord record, boolean includeDeleted) {
        if (hasSameUpdatedRecord(record, includeDeleted)) {
            if (isRecordNewer(record, includeDeleted)) {
                updateRecord(record);
            }
        } else {
            insertRecord(record);
        }
    }

    public RVRecord loadRecord(String dataId, boolean loadDeleted) {

        RVRecord record = new RVRecord();
        Cursor cursor;

        if (loadDeleted) {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{},
                    DATA_ID + "= ?",
                    new String[]{dataId},
                    null, null, null, null);
        } else {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{},
                    DATA_ID + "= ?" + AND + IS_DELETED + "= 0",
                    new String[]{dataId},
                    null, null, null, null);
        }

        boolean isEOf = cursor.moveToFirst();
        while (isEOf) {
            record.setDataId(cursor.getString(0));
            record.setClassName(cursor.getString(1));
            record.setUpdatedAt(cursor.getLong(2));
            record.setData(cursor.getString(3));
            record.setDeleted(cursor.getInt(4) == 1);
        }
        cursor.close();
        return record;
    }

    private boolean hasSameUpdatedRecord(RVRecord record, boolean includesDeleted) {

        boolean result;
        Cursor cursor;

        if (includesDeleted) {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{DATA_ID, UPDATED_AT},
                    DATA_ID + "= ?" + AND + UPDATED_AT + "= ?",
                    new String[]{record.getDataId(), String.valueOf(record.getUpdatedAt())},
                    null, null, null, null);
        } else {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{DATA_ID, UPDATED_AT},
                    DATA_ID + "= ?" + AND + UPDATED_AT + "= ?" + AND + IS_DELETED + "= 1",
                    new String[]{record.getDataId(), String.valueOf(record.getUpdatedAt())},
                    null, null, null, null);
        }

        result = cursor.getCount() >= 1;
        cursor.close();

        return result;
    }

    private boolean isRecordNewer(RVRecord record, boolean includesDeleted) {

        boolean result;
        Cursor cursor;

        if (includesDeleted) {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{DATA_ID, UPDATED_AT},
                    DATA_ID + "= ?" + AND + UPDATED_AT + "< ?",
                    new String[]{record.getDataId(), String.valueOf(record.getUpdatedAt())},
                    null, null, null, null);
        } else {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{DATA_ID, UPDATED_AT},
                    DATA_ID + "= ?" + AND + UPDATED_AT + "< ?" + AND + IS_DELETED + "= 1",
                    new String[]{record.getDataId(), String.valueOf(record.getUpdatedAt())},
                    null, null, null, null);
        }

        result = cursor.getCount() >= 1;
        cursor.close();

        return result;
    }

    private int updateRecord(RVRecord record) {

        ContentValues values = new ContentValues(4);
        values.put(DATA_ID, record.getDataId());
        values.put(CLASS_NAME, record.getClassName());
        values.put(UPDATED_AT, String.valueOf(record.getUpdatedAt()));
        values.put(DATA, record.getDataJSON());
        values.put(IS_DELETED, record.isDeleted());

        return mDB.update(TABLE_NAME, values, DATA_ID + "= ?", new String[]{record.getDataId()});
    }

    private long insertRecord(RVRecord record) {

        ContentValues values = new ContentValues(4);
        values.put(DATA_ID, record.getDataId());
        values.put(CLASS_NAME, record.getClassName());
        values.put(UPDATED_AT, String.valueOf(record.getUpdatedAt()));
        values.put(DATA, record.getDataJSON());
        values.put(IS_DELETED, record.isDeleted());

        return mDB.insert(TABLE_NAME, null, values);
    }

    private int deleteRecordFromDB(RVRecord record) {
        return mDB.delete(TABLE_NAME, DATA_ID + "= ?", new String[]{record.getDataId()});
    }

    private ArrayList<RVRecord> loadRecords(ClassName className, boolean loadDeleted) {
        ArrayList<RVRecord> records = new ArrayList<>();
        Cursor cursor;

        if (loadDeleted) {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{},
                    CLASS_NAME + "= ?",
                    new String[]{className.toString()},
                    null, null, null, null);
        } else {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{},
                    CLASS_NAME + "= ?" + AND + IS_DELETED + "= 0",
                    new String[]{className.toString()},
                    null, null, null, null);
        }

        boolean isEOf = cursor.moveToFirst();
        while (isEOf) {
            RVRecord record = new RVRecord();
            record.setDataId(cursor.getString(0));
            record.setClassName(cursor.getString(1));
            record.setUpdatedAt(cursor.getLong(2));
            record.setData(cursor.getString(3));
            record.setDeleted(cursor.getInt(4) == 1);

            records.add(record);
        }
        cursor.close();
        return records;
    }

    public ArrayList<Place> loadPlaceList() {
        ArrayList<Place> placeList = new ArrayList<>();
        for (RVRecord record : loadRecords(ClassName.Place, false)) {
            placeList.add(new Gson().fromJson(record.getDataJSON(), Place.class));
        }
        return placeList;
    }

}
