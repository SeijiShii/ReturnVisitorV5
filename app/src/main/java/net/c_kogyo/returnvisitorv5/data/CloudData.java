package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class CloudData {

    private String id;
    private Calendar updatedAt;
    private String data;

    public <T extends DataItem> CloudData(T item) {

        this.id = item.id;
        this.updatedAt = (Calendar) item.updatedAt.clone();
        this.data = item.jsonObject().toString();
    }

    public JSONObject getJson() {

        try {
            return new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }
}
