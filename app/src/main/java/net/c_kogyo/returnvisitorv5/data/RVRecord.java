package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * Created by SeijiShii on 2017/02/23.
 * デバイス上のsqlite3やバックエンドのサーバーに保存するときの形式となる
 */

public class RVRecord {

    public static final String DATA = "data";
    public static final String CLASS_NAME = "class_name";
    public static final String DOUBLE_QUOTES = "*double_quotes*";

    private String dataId;
    private Calendar updatedAt;
    private String data;
    private String className;

    public <T extends DataItem> RVRecord(T item) {
        this.dataId = item.id;
        this.updatedAt = (Calendar) item.getUpdatedAt().clone();
        this.data = item.jsonObject().toString().replace("\"", DOUBLE_QUOTES);
        this.className = item.getClass().getSimpleName();
    }

    public RVRecord(JSONObject object) {

        try {
            if (object.has(DataItem.ID))
                this.dataId = object.getString(DataItem.ID);
            if (object.has(DataItem.UPDATED_AT)){
                this.updatedAt = Calendar.getInstance();
                this.updatedAt.setTimeInMillis(object.getLong(DataItem.UPDATED_AT));
            }
            if (object.has(DATA))
                this.data = object.getString(DATA);
                this.data = this.data.replace(DOUBLE_QUOTES, "\"");
            if (object.has(CLASS_NAME))
                this.className = object.getString(CLASS_NAME);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getDataJSON() {
        JSONObject object = new JSONObject();

        try {
            object = new JSONObject(data);
        } catch (JSONException e) {
            //
        }
        return object;
    }

    public JSONObject getFullJSON() {

        JSONObject object = new JSONObject();

        try {
            object.put(DataItem.ID, dataId);
            object.put(DataItem.UPDATED_AT, updatedAt.getTimeInMillis());
            object.put(DATA, data);
            object.put(CLASS_NAME, className);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public String getClassName() {
        return className;
    }
}
