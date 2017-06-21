package net.c_kogyo.returnvisitorv5.db;

/**
 * Created by SeijiShii on 2017/06/21.
 */

public class RVDBContract {

    // rv_data
    //      RVRecordを格納するデータベース
    //      * Integer id
    //      * String dataId
    //      * String className
    //      * long updatedAt;
    //      * String data (クラスインスタンスをJSON化したもの)

    public static final String DB_NAME = "returnvisitor_db";
    public static final String TABLE_NAME = "rv_data";
    public static final int DB_VERSION = 1;
    public static final String CREATE_TABLE_QUERY
            = "CREATE TABLE IF NOT EXISTS rv_data " +
            "(id integer primary key autoincrement, " +
            "data_id text, class_name text, updated_at integer, data text + is_deleted integer);";

    public static final String ID           = "id";
    public static final String DATA_ID      = "data_id";
    public static final String CLASS_NAME   = "class_name";
    public static final String UPDATED_AT   = "updated";
    public static final String DATA         = "data";
    public static final String IS_DELETED   = "is_deleted";

    public static final String AND          = " AND ";

}
