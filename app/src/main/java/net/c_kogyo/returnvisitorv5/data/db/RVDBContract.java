package net.c_kogyo.returnvisitorv5.data.db;

import android.provider.BaseColumns;

/**
 * Created by SeijiShii on 2017/02/23.
 */

public final class RVDBContract {

    private RVDBContract() {}

    public static class RVDBEntry implements BaseColumns {
        public static final String TABLE_NAME = "rvdb_table";
        // 多分これ以外にもプライマリキーの整数idが付与されると思う
        public static final String COLUMN_DATA_ID       = "data_id";
        public static final String COLUMN_UPDATED_AT    = "updated_at";
        public static final String COLUMN_DATA          = "data";
        public static final String COLUMN_CLASS_NAME    = "class_name";
        public static final String COLUMN_IS_DELETED    = "is_deleted";
     }


}
