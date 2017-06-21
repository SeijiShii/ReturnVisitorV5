package net.c_kogyo.returnvisitorv5.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static net.c_kogyo.returnvisitorv5.db.RVDBContract.CREATE_TABLE_QUERY;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.DB_NAME;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.DB_VERSION;
import static net.c_kogyo.returnvisitorv5.db.RVDBContract.TABLE_NAME;

/**
 * Created by SeijiShii on 2017/06/21.
 */

public class RVDBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "RVDBOpenHelper";

    // rv_data
    //      RVRecordを格納するデータベース
    //      * Integer id
    //      * String dataId
    //      * String className
    //      * long updatedAt;
    //      * String data (クラスインスタンスをJSON化したもの)

    public RVDBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, "onCreate version : " + db.getVersion());

        String createTableQuery = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + "(id integer, data_id text, class_name text, updated_at integer, data text):";
        db.execSQL(createTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade version : " + db.getVersion());
        Log.d(TAG, "onUpgrade oldVersion : " + oldVersion);
        Log.d(TAG, "onUpgrade newVersion : " + newVersion);
    }
}
