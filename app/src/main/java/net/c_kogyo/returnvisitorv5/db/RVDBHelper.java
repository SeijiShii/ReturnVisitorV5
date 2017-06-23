package net.c_kogyo.returnvisitorv5.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.data.Person;

import java.util.ArrayList;
import java.util.Calendar;
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

    private void deleteAllDataFromDB() {
        mDB.execSQL("DELETE FROM " + TABLE_NAME + ";");
    }

    private void saveRecord(RVRecord record, boolean checkIncludeDeleted) {

        if (!hasSameId(record, checkIncludeDeleted)) {
            insertRecord(record);
        } else if (!hasLaterOrSameUpdatedRecord(record, checkIncludeDeleted)) {
            updateRecord(record);
        }
    }

    private void saveRecord(RVRecord record) {
        saveRecord(record, false);
    }

    @Nullable
    public RVRecord loadRecord(String dataId, boolean loadDeleted) {

        RVRecord record = null;
        String whereClause;

        if (loadDeleted) {
            whereClause = DATA_ID + "= ?";
        } else {
            whereClause = DATA_ID + "= ?" + AND + IS_DELETED + "= 0";
        }

        Cursor cursor = mDB.query(false,
                TABLE_NAME,
                new String[]{},
                whereClause,
                new String[]{dataId},
                null, null, null, null);

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        boolean isEOf = cursor.moveToFirst();
        while (isEOf) {
            record = generateRecordFromCursor(cursor);
            isEOf = cursor.moveToNext();
        }
        cursor.close();
        return record;
    }

    public RVRecord loadRecord(String dataId) {
        return loadRecord(dataId, false);
    }

    private RVRecord generateRecordFromCursor(Cursor cursor) {
        RVRecord record = new RVRecord();
        record.setDataId(cursor.getString(1));
        record.setClassName(cursor.getString(2));
        record.setUpdatedAt(cursor.getLong(3));
        record.setData(cursor.getString(4));
        record.setDeleted(cursor.getInt(5) == 1);
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
            RVRecord record = generateRecordFromCursor(cursor);
            records.add(record);
        }
        cursor.close();
        return records;

    }

    private boolean hasSameId(RVRecord record, boolean checkIncludeDeleted) {
        return loadRecord(record.getDataId(), checkIncludeDeleted) != null;
    }

    private boolean hasLaterOrSameUpdatedRecord(RVRecord record, boolean includesDeleted) {

        if (!hasSameId(record, includesDeleted)) return false;
        RVRecord recordInDB = loadRecord(record.getDataId(), includesDeleted);
        return recordInDB != null && record.getUpdatedAt() <= recordInDB.getUpdatedAt();
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
            RVRecord record = generateRecordFromCursor(cursor);
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
                RVRecord record =generateRecordFromCursor(cursor);
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


    // Tests
    private boolean isTestDataSaved;
    private long startSaveTime;
    private boolean isTestDataLoaded;
    private long startLoadTime;
    public void testSaveAndLoad() {

        deleteAllDataFromDB();

        Gson gson = new Gson();
        final Person person1 = new Person();
        person1.setPriority(Person.Priority.HIGH);
        person1.setName("HOGE HOGE");
        person1.setSex(Person.Sex.MALE);
        person1.setAge(Person.Age.AGE_31_40);
        String id = "test_id_00000005";
        person1.setId(id);

        isTestDataSaved = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isTestDataSaved) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                long endSaveTime = Calendar.getInstance().getTimeInMillis();
                long diff = endSaveTime - startSaveTime;
                Log.d(RVDBHelper.TAG, "Saving data takes time: " + diff + " ms.");
            }
        }).start();

        startSaveTime = Calendar.getInstance().getTimeInMillis();
        Log.d(RVDBHelper.TAG, "person1: " + gson.toJson(person1));

        deleteAllDataFromDB();
        save(person1);
        isTestDataSaved = true;

        isTestDataLoaded = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isTestDataLoaded) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                long endLoadTime = Calendar.getInstance().getTimeInMillis();
                long diff = endLoadTime - startLoadTime;
                Log.d(RVDBHelper.TAG, "Loading data takes time: " + diff + " ms.");
            }
        }).start();

        startLoadTime = Calendar.getInstance().getTimeInMillis();
        net.c_kogyo.returnvisitorv5.db.RVRecord record = loadRecord(id);
        isTestDataLoaded = true;

        Person person2 = gson.fromJson(record.getDataJSON(), Person.class);
        Log.d(RVDBHelper.TAG, "person2: " + gson.toJson(person2));

    }

    public void testHasLaterOrSameUpdated() {

        deleteAllDataFromDB();

        long updated = 1500000;
        String id = "test_id_000000098";

        Person personOriginal = new Person("place_hoge");
        personOriginal.setPriority(Person.Priority.HIGH);
        personOriginal.setName("HOGE HOGE");
        personOriginal.setSex(Person.Sex.MALE);
        personOriginal.setAge(Person.Age.AGE_31_40);
        personOriginal.setId(id);

        RVRecord originalRecord = new RVRecord(personOriginal);
        originalRecord.setUpdatedAt(updated);
        insertRecord(originalRecord);

        Person personSame = new Person("place_fuga");
        personSame.setPriority(Person.Priority.HIGH);
        personSame.setName("HOGE HOGE");
        personSame.setSex(Person.Sex.MALE);
        personSame.setAge(Person.Age.AGE_31_40);
        personSame.setId(id);

        RVRecord sameRecord = new RVRecord(personSame);
        sameRecord.setUpdatedAt(updated);
        Log.d(TAG, "has SAME update: " + hasLaterOrSameUpdatedRecord(sameRecord, false));

        Person personEarlier = new Person("place_early");
        personEarlier.setPriority(Person.Priority.HIGH);
        personEarlier.setName("HOGE HOGE");
        personEarlier.setSex(Person.Sex.MALE);
        personEarlier.setAge(Person.Age.AGE_31_40);
        personEarlier.setId(id);

        RVRecord earlyRecord = new RVRecord(personEarlier);
        earlyRecord.setUpdatedAt(updated - 5000);
        Log.d(TAG, "has EARLIER update: " + hasLaterOrSameUpdatedRecord(earlyRecord, false));

        Person personLater = new Person("place_later");
        personLater.setPriority(Person.Priority.HIGH);
        personLater.setName("HOGE HOGE");
        personLater.setSex(Person.Sex.MALE);
        personLater.setAge(Person.Age.AGE_31_40);
        personLater.setId(id);

        RVRecord laterRecord = new RVRecord(personLater);
        laterRecord.setUpdatedAt(updated + 5000);
        Log.d(TAG, "has LATER update: " + hasLaterOrSameUpdatedRecord(laterRecord, false));

    }


//    public ArrayList<Calendar> getDatesWithData() {
//
//        ArrayList<Calendar> datesOfVisit = VisitList.getDates(this);
//        ArrayList<Calendar> datesOfWork = WorkList.getDates(this);
//        ArrayList<Calendar> datesDoubled = new ArrayList<>();
//
//        for (Calendar date0 : datesOfVisit) {
//            for (Calendar date1 : datesOfWork) {
//
//                if (CalendarUtil.isSameDay(date0, date1)) {
//                    datesDoubled.add(date1);
//                }
//            }
//        }
//
//        datesOfWork.removeAll(datesDoubled);
//        datesOfVisit.addAll(datesOfWork);
//
//        Collections.sort(datesOfVisit, new Comparator<Calendar>() {
//            @Override
//            public int compare(Calendar calendar, Calendar t1) {
//                return calendar.compareTo(t1);
//            }
//        });
//
//        return new ArrayList<>(datesOfVisit);
//    }

//    public ArrayList<Calendar> getMonthsWithData() {
//
//        ArrayList<Calendar> monthWithData = new ArrayList<>();
//        ArrayList<Calendar> datesWithData = getDatesWithData();
//
//        if (datesWithData.size() <= 0)
//            return monthWithData;
//
//        monthWithData.add(datesWithData.get(0));
//
//        int dateIndex = 0;
//        int monthIndex = 0;
//
//        while (dateIndex < datesWithData.size() - 1) {
//            dateIndex++;
//            if (!CalendarUtil.isSameMonth(datesWithData.get(dateIndex), monthWithData.get(monthIndex))) {
//                monthWithData.add(datesWithData.get(dateIndex));
//                monthIndex++;
//            }
//        }
//
//        return monthWithData;
//    }

//    public boolean hasWorkOrVisit() {
//        return VisitList.loadList(this).size() + WorkList.loadList(this).size() > 0;
//    }
}