package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class Tag extends DataItem {

    public static final String TAG = "tag";
    public Tag() {
        super(TAG);
    }

    public Tag(Record record) {
        super(record.getDataJSON());
    }

    public Tag(JSONObject object) {

        setJSON(this, object);
    }

    public static Tag setJSON(Tag tag, JSONObject object) {
        return (Tag) DataItem.setJSON(tag, object);
    }
}
