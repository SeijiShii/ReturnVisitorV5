package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * Created by SeijiShii on 2017/02/23.
 * デバイス上のsqlite3やバックエンドのサーバーに保存するときの形式となる
 */

public class Record {

    public static final String DATA = "data";
    public static final String CLASS_NAME = "class_name";

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

    public Record(JSONObject object) {

        try {
            if (object.has(DataItem.ID))
                this.id = object.getString(DataItem.ID);
            if (object.has(DataItem.UPDATED_AT)){
                this.updatedAt = Calendar.getInstance();
                this.updatedAt.setTimeInMillis(object.getLong(DataItem.UPDATED_AT));
            }
            if (object.has(DATA))
                this.data = object.getString(DATA);
            if (object.has(CLASS_NAME))
                this.className = object.getString(CLASS_NAME);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    public <T extends DataItem> T toInstance() {
//
////        T item = null;
//
//        Class<?> klass = null;
//        try {
//            klass = Class.forName(className);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (klass == null) {
//            return null;
//        }
//
//        Object item = null;
//
//        try {
//            item = klass.newInstance();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        return (T) item;
//
//
////        switch (className) {
//////            case "DataItem":
//////                item = (T) new  DataItem(this);
//////                break;
////            case "Place":
////                klass = (T) new Place(this);
////                break;
////            case "Person":
////                klass = (T) new Person(this);
////                break;
////            case "Visit":
////                klass = (T) new Visit(this);
////                break;
////            case "Tag":
////                klass = (T) new Tag(this);
////                break;
////            case "NoteCompItem":
////                klass = (T) new NoteCompItem(this);
////                break;
////            case "Publication":
////                klass = (T) new Publication(this);
////                break;
////        }
////        return klass;
//    }

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
            object.put(DataItem.ID, id);
            object.put(DataItem.UPDATED_AT, updatedAt);
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
