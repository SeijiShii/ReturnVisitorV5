package net.c_kogyo.returnvisitorv5.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.data.NoteCompItem;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public static final String TAG = "RVDBHelper";

    private SQLiteDatabase mDB;
    private RVDBOpenHelper mOpenHelper;
    private Gson mGson;

    public RVDBHelper(Context context) {
        this.mOpenHelper = new RVDBOpenHelper(context);
        this.mGson = new Gson();
        initDB();
    }

    private void initDB() {
        if (mDB == null) {
            mDB = mOpenHelper.getWritableDatabase();
        }
    }

    private void saveRecord(RVRecord record, boolean includeDeleted) {
        if (hasSameUpdatedRecord(record, includeDeleted)) {
            if (isRecordNewer(record, includeDeleted)) {
                updateRecord(record);
            }
        } else {
            insertRecord(record);
        }
    }

    private void saveRecord(RVRecord record) {
        saveRecord(record, false);
    }

    @Nullable
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

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
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

    public ArrayList<RVRecord> loadRecordLaterThanTime(long lastSyncTime) {

        Cursor cursor = mDB.query(false,
                TABLE_NAME,
                null,
                UPDATED_AT + " > ?",
                new String[]{String.valueOf(lastSyncTime)},
                null, null, null, null);

        ArrayList<RVRecord> records = new ArrayList<>();

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

    public boolean containsRecordWithId(String dataId, boolean includesDeleted) {

        String whereClause;
        if (includesDeleted) {
            whereClause = DATA_ID + "= ?";
        } else {
            whereClause = DATA_ID + "= ?" + AND + IS_DELETED + "= 1";
        }
        Cursor cursor = mDB.query(false,
                TABLE_NAME,
                new String[]{DATA_ID},
                whereClause,
                new String[]{dataId},
                null, null, null, null );
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public boolean containsRecordWithId(String dataId) {
        return containsRecordWithId(dataId, false);
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

    private   <T extends DataItem> ArrayList<RVRecord> loadRecords(Class<T> className, boolean loadDeleted) {
        ArrayList<RVRecord> records = new ArrayList<>();
        Cursor cursor;

        if (loadDeleted) {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{},
                    CLASS_NAME + "= ?",
                    new String[]{className.getSimpleName()},
                    null, null, null, null);
        } else {
            cursor = mDB.query(false,
                    TABLE_NAME,
                    new String[]{},
                    CLASS_NAME + "= ?" + AND + IS_DELETED + "= 0",
                    new String[]{className.getSimpleName()},
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

    public   <T extends DataItem> ArrayList<RVRecord> loadRecords(Class<T> className) {
        return loadRecords(className, false);
    }

    public  <T extends DataItem> ArrayList<T> loadList(Class<T> className, boolean loadDeleted) {

        ArrayList<T> list = new ArrayList<>();
        for (RVRecord record : loadRecords(className, loadDeleted)) {
            list.add(mGson.fromJson(record.getDataJSON(), className));
        }
        return list;
    }

    public ArrayList<RVRecord> loadRecordsByIds(ArrayList<String> ids, boolean deleted) {
        ArrayList<RVRecord> records = new ArrayList<>();

        String whereClause;
        if (deleted) {
            whereClause = DATA_ID + " = ?";
        } else {
            whereClause = DATA_ID + " = ?" + AND + IS_DELETED + " = 0";
        }

        for (String id : ids) {

            Cursor cursor = mDB.query(false,
                    TABLE_NAME,
                    null,
                    whereClause,
                    new String[]{id},
                    null, null, null, null);
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
        }
        return records;
    }

    private ArrayList<RVRecord> loadRecordsByIds(ArrayList<String> ids) {
        return loadRecordsByIds(ids, false);
    }

    public <T extends DataItem> ArrayList<T> loadListByIds(Class<T> className, ArrayList<String> ids) {
        ArrayList<T> list = new ArrayList<>();
        for (RVRecord record : loadRecordsByIds(ids)) {
            list.add(mGson.fromJson(record.getDataJSON(), className));
        }
        return list;
    }

    public <T extends DataItem> void save(T item) {
        saveRecord(new RVRecord(item));
    }

    public <T extends DataItem> void saveList(ArrayList<T> list) {
        for (T item : list) {
            save(item);
        }
    }

    public void saveRecords(ArrayList<RVRecord> records) {
        for (RVRecord record : records) {
            saveRecord(record);
        }
    }

    public <T extends DataItem> void  saveAsDeletedRecord(T item) {
        saveRecord(new RVRecord(item, true));
    }

    public <T extends DataItem> void saveAsDeletedRecords(ArrayList<T> list) {
        for (T item : list) {
            saveAsDeletedRecord(item);
        }
    }

    public <T extends DataItem> ArrayList<T> getSearchedItems(Class<T> className, String searchWord, Context context) {

        String[] words = searchWord.split(" ");

        ArrayList<T> searchResultItems = new ArrayList<>();
        for (RVRecord record : loadRecords(className, false)) {
            searchResultItems.add(mGson.fromJson(record.getDataJSON(), className));
        }

        for (String word : words) {
            List<T> listToRemove = new ArrayList<>();

            for (T t: searchResultItems) {
                if (!t.toStringForSearch(context).contains(word)){
                    listToRemove.add(t);
                }
            }
            searchResultItems.removeAll(listToRemove);
        }
        return searchResultItems;
    }

    public <T extends DataItem> boolean containsDataWithName(Class<T> tClass, String name) {
        for (T item : loadList(tClass, false)) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public <T extends DataItem >boolean addIfNoSameName(Class<T> tClass, String name) {

        for (T item : loadList(tClass, false)) {
            if (item.getName().equals(name)) return true;
        }
        return false;
    }

    public ArrayList<Calendar> getDatesWithData() {

        ArrayList<Calendar> datesOfVisit = VisitList.getDates(this);
        ArrayList<Calendar> datesOfWork = WorkList.getDates(this);
        ArrayList<Calendar> datesDoubled = new ArrayList<>();

        for (Calendar date0 : datesOfVisit) {
            for (Calendar date1 : datesOfWork) {

                if (CalendarUtil.isSameDay(date0, date1)) {
                    datesDoubled.add(date1);
                }
            }
        }

        datesOfWork.removeAll(datesDoubled);
        datesOfVisit.addAll(datesOfWork);

        Collections.sort(datesOfVisit, new Comparator<Calendar>() {
            @Override
            public int compare(Calendar calendar, Calendar t1) {
                return calendar.compareTo(t1);
            }
        });

        return new ArrayList<>(datesOfVisit);
    }

    public ArrayList<Calendar> getMonthsWithData() {

        ArrayList<Calendar> monthWithData = new ArrayList<>();
        ArrayList<Calendar> datesWithData = getDatesWithData();

        if (datesWithData.size() <= 0)
            return monthWithData;

        monthWithData.add(datesWithData.get(0));

        int dateIndex = 0;
        int monthIndex = 0;

        while (dateIndex < datesWithData.size() - 1) {
            dateIndex++;
            if (!CalendarUtil.isSameMonth(datesWithData.get(dateIndex), monthWithData.get(monthIndex))) {
                monthWithData.add(datesWithData.get(dateIndex));
                monthIndex++;
            }
        }

        return monthWithData;
    }

    public boolean hasWorkOrVisit() {
        return VisitList.loadList(this).size() + WorkList.loadList(this).size() > 0;
    }
}
