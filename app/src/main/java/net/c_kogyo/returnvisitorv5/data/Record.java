package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * Created by SeijiShii on 2017/02/23.
 * デバイス上のsqlite3やバックエンドのサーバーに保存するときの形式となる
 */

public class Record {

    private String id;
    private Calendar updatedAt;
    private String data;
    private String className;

    public <T extends DataItem> Record(T item) {
        this.id = item.id;
        this.updatedAt = (Calendar) item.getUpdatedAt().clone();
        this.data = item.jsonObject().toString();
        this.className = item.getClass().getSimpleName();
    }

    public <T extends DataItem> T toData() {

        T item = null;
        switch (className) {
            case "DataItem":
                item = (T) new  DataItem(this);
                break;
            case "Place":
                item = (T) new Place(this);
                break;
            case "Person":
                item = (T) new Person(this);
                break;
            case "Visit":
                item = (T) new Visit(this);
                break;
        }
        return item;
    }

    public JSONObject getJSON() {
        JSONObject object = new JSONObject();

        try {
            object = new JSONObject(data);
        } catch (JSONException e) {
            //
        }
        return object;
    }
}
