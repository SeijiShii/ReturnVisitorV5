package net.c_kogyo.returnvisitorv5.data.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SeijiShii on 2017/02/23.
 */

public class RVDBOpenHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ReturnVisitor.db";

    private static final String TEXT_TYPE       = " TEXT";
    private static final String DATETIME_TYPE   = " DATETIME";
    private static final String INT_TYPE        = " INTEGER";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RVDBContract.RVDBEntry.TABLE_NAME + " (" +
                    RVDBContract.RVDBEntry._ID + " INTEGER PRIMARY KEY," +
                    RVDBContract.RVDBEntry.COLUMN_DATA_ID + TEXT_TYPE + COMMA_SEP +
                    RVDBContract.RVDBEntry.COLUMN_UPDATED_AT + DATETIME_TYPE + COMMA_SEP +
                    RVDBContract.RVDBEntry.COLUMN_DATA + TEXT_TYPE + COMMA_SEP +
                    RVDBContract.RVDBEntry.COLUMN_CLASS_NAME + TEXT_TYPE + COMMA_SEP +
                    RVDBContract.RVDBEntry.COLUMN_IS_DELETED + INT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RVDBContract.RVDBEntry.TABLE_NAME;

    public RVDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
