package net.c_kogyo.returnvisitorv5.db;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * Created by SeijiShii on 2017/02/23.
 * デバイス上のsqlite3やバックエンドのサーバーに保存するときの形式となる
 */

public class RVRecord {

    private String dataId;
    private String data;
    private String className;
    private long updatedAt;
    private boolean isDeleted;

    public <T extends DataItem> RVRecord(T item) {

        Gson gson = new Gson();

        this.dataId = item.getId();
        this.updatedAt = Calendar.getInstance().getTimeInMillis();
        this.data = StringUtil.replaceDoubleQuotes(gson.toJson(item));
        this.className = item.getClass().getSimpleName();
        this.isDeleted = false;
    }

    public <T extends DataItem> RVRecord (T item, boolean isDeleted) {
        this(item);
        this.isDeleted = isDeleted;
    }

    public RVRecord() {
    }

    public String getDataJSON() {
        data = data.replace("\\\"", "\"");
        return data;
    }

    public String getClassName() {
        return className;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
