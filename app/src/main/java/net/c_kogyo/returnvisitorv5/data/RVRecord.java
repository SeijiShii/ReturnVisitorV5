package net.c_kogyo.returnvisitorv5.data;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * Created by SeijiShii on 2017/02/23.
 * デバイス上のsqlite3やバックエンドのサーバーに保存するときの形式となる
 */

public class RVRecord {

//    private static final String DATA = "data";
//    public static final String CLASS_NAME = "class_name";
//    private static final String CLASS_NAME = "className";
//    private static final String DOUBLE_QUOTES = "*double_quotes*";

//    private static final String UPDATED_AT = "updatedAt";
//    private static final String ID = "id";

    private String dataId;
    private String data;
    private String className;
    private long updatedAt;

    public <T extends DataItem> RVRecord(T item) {

        Gson gson = new Gson();

        this.dataId = item.id;
        this.updatedAt = item.updatedAt;
        this.data = StringUtil.replaceDoubleQuotes(gson.toJson(item));
        this.className = item.getClass().getSimpleName();
    }

    public RVRecord() {
    }

//    public RVRecord(JSONObject object) {
//
//        try {
//            if (object.has(ID))
//                this.dataId = object.getString(ID);
//            if (object.has(UPDATED_AT)){
////                this.updatedAt = Calendar.getInstance();
////                this.updatedAt.setTimeInMillis(object.getLong(UPDATED_AT));
//                this.updatedAt = object.getLong(UPDATED_AT);
//            }
//            if (object.has(DATA))
//                this.data = object.getString(DATA);
//                this.data = this.data.replace(DOUBLE_QUOTES, "\"");
//            if (object.has(CLASS_NAME))
//                this.className = object.getString(CLASS_NAME);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

    public String getDataJSON() {
        data = data.replace("\\\"", "\"");
        return data;
    }

//    public JSONObject getFullJSON() {
//
//        JSONObject object = new JSONObject();
//
//        try {
//            object.put(DataItem.ID, dataId);
////            object.put(DataItem.UPDATED_AT, updatedAt.getTimeInMillis());
//            object.put(UPDATED_AT, updatedAt);
//            object.put(DATA, data);
//            object.put(CLASS_NAME, className);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object;
//    }

    public String getClassName() {
        return className;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

//    public void setUpdatedAt(Calendar updatedAt) {
//        this.updatedAt = updatedAt;
//    }


//    public void setUpdatedAt(long updatedAt) {
//        this.updatedAt = updatedAt;
//    }

    public void setData(String data) {
        this.data = data;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
