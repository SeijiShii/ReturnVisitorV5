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

    protected String id;
    protected String name;
    protected String note;

    protected String idHeader;

    public DataItem(){}

    public DataItem(String idHeader){

        this.idHeader = idHeader;
        initCommon();

    }

    private void initCommon() {
        this.id = generateNewId();
        this.name = "";
        this.note = "";
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

    public String getName() {
        return name;
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

        return item;
    }

    public String toStringForSearch(Context context){

        return name + " " + note + " ";

    }

    public void setId(String id) {
        this.id = id;
    }

    public <T extends DataItem> boolean equals(T o) {
        return this.getId().equals(o.getId());
    }

}
