package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.DataItem;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class DataList extends BaseList {

    public DataList(String listHeader) {
        super(DataItem.class, listHeader);
    }

    public DataList(String listHeader, JSONObject object) {
        super(DataItem.class, listHeader, object);
    }

    @Override
    public DataItem getInstance(JSONObject object) {
        return new DataItem(object);
    }
}
