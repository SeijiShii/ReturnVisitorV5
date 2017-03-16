package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Created by sayjey on 2015/07/17.
 */
public class DataItem implements Cloneable{

    public static final String ID           = "id";
    public static final String NAME         = "name";
    public static final String NOTE         = "note";
    public static final String UPDATED_AT   = "updated_at";
    public static final String UPDATED_AT_STRING = "updated_at_string";

    protected String id;
    protected String name;
    protected String note;

    protected Calendar updatedAt;

    protected String idHeader;
    protected boolean isDeleted;

    public DataItem(){}

    public DataItem(String idHeader){

        this.idHeader = idHeader;
        initCommon();

    }

    public DataItem(JSONObject object) {

        initCommon();

        setJSON(this, object);

//        try {
//            if (object.has(ID)) {
//                this.id = object.getString(ID);
//            }
//
//            if (object.has(UPDATED_AT)) {
//                this.updatedAt = Calendar.getInstance();
//                this.updatedAt.setTimeInMillis(object.getLong(UPDATED_AT));
//            }
//
//            if (object.has(NAME)) {
//                this.name = object.getString(NAME);
//            }
//
//            if (object.has(NOTE)) {
//                this.note = object.getString(NOTE);
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    public DataItem(Record record) {

        this(record.getDataJSON());

    }

    private void initCommon() {
        this.updatedAt = Calendar.getInstance();
        this.id = generateNewId();
        this.name = "";
        this.note = "";
        this.isDeleted = false;
    }

    public JSONObject jsonObject() {

        JSONObject object = new JSONObject();

        try {
            object.put(ID, this.id);
            object.put(UPDATED_AT, this.updatedAt.getTimeInMillis());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd, E, HH:mm:ss", Locale.JAPAN);
            object.put(UPDATED_AT_STRING, sdf.format(this.updatedAt.getTime()));

            object.put(NAME, this.name);
            object.put(NOTE, this.note);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


    /**
     * カレンダーのミリ秒を文字列にした末尾に1000までの乱数を加えて初期値を生成する
     * @return String id
     */
    private String generateNewId() {

        long milNum = Calendar.getInstance().getTimeInMillis();
        String sMilNum = String.valueOf(milNum);

        int ranNum = (int)(Math.random() * 10000);
        String sRanNum = String.format("%05d", ranNum);

        String mId = sMilNum + sRanNum;

        return idHeader + "_" + mId;
    }

    public String getId() {
        return id;
    }

    public Calendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt() {
        updatedAt = Calendar.getInstance();
    }

    public String getName() {
        return name;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        DataItem item = (DataItem) super.clone();

        item.id = this.id;
        item.name = this.name;
        item.note = this.note;
        item.updatedAt = (Calendar) this.updatedAt.clone();

        return item;
    }

    public String toStringForSearch(Context context){

        return name + " " + note + " ";

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUpdatedAt(Calendar updatedAt) {
        this.updatedAt = updatedAt;
    }

    public <T extends DataItem> boolean equals(T o) {
        return this.getId().equals(o.getId()) && !this.isDeleted;
        // 削除されていたら反応しない
    }

    public void delete(boolean toDelete) {
        this.isDeleted = toDelete;
    }

    public static DataItem setJSON(DataItem item, JSONObject object) {

        try {
            if (object.has(ID)) {
                item.id = object.getString(ID);
            }

            if (object.has(UPDATED_AT)) {
                item.updatedAt = Calendar.getInstance();
                item.updatedAt.setTimeInMillis(object.getLong(UPDATED_AT));
            }

            if (object.has(NAME)) {
                item.name = object.getString(NAME);
            }

            if (object.has(NOTE)) {
                item.note = object.getString(NOTE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item;
    }






    //    public HashMap<String, Object> toMap() {
//
//        HashMap<String, Object> map = new HashMap<>();
//
//        map.put(ID, this.id);
//        map.put(NAME, this.name);
//        map.put(NOTE, this.note);
//        map.put(UPDATED_AT, this.updatedAt.getTimeInMillis());
//
//        // 目視したときわかりやすいように。読み出しはしない。
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd, E, HH:mm:ss", Locale.JAPAN);
//        map.put(UPDATED_AT_STRING, sdf.format(this.updatedAt.getTime()));
//
//        return map;
//    }

//    public void setMap(@NonNull HashMap<String, Object> map){
//
//        this.id = map.get(ID).toString();
//        this.name = map.get(NAME).toString();
//        this.note = map.get(NOTE).toString();
//        this.updatedAt = Calendar.getInstance();
//        this.updatedAt.setTimeInMillis(Long.valueOf(map.get(UPDATED_AT).toString()));
//    }




}
