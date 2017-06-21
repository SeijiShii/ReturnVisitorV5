package net.c_kogyo.returnvisitorv5.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    private final String CREATE_TABLE_QUERY
            = "CREATE TABLE IF NOT EXISTS rv_data (id integer, data_id text, class_name text, updated_at integer, data text);";

    public RVDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, "onCreate version : " + db.getVersion());
        db.execSQL(CREATE_TABLE_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade version : " + db.getVersion());
        Log.d(TAG, "onUpgrade oldVersion : " + oldVersion);
        Log.d(TAG, "onUpgrade newVersion : " + newVersion);
    }
}
