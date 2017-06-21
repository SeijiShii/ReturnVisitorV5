package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class DeletedData extends DataItem {

    public static final String DELETED_DATA = "deleted_data";

    public static final String CLASS_NAME = "class_name";
    public static final String DATA_ID = "data_id";

    private String className;
    private String dataId;

    public <T extends DataItem> DeletedData(T data) {

        super(DELETED_DATA);

        this.dataId = data.getId();
        this.className = data.getClass().getSimpleName();
    }

    public DeletedData(RVRecord record) {
        super(DELETED_DATA);

        this.dataId = record.getDataId();
        this.className = record.getClassName();
    }

//    public DeletedData(RVRecord RVRecord) {
//        this(RVRecord.getDataJSON());
//    }

//    public DeletedData(JSONObject object) {
//        super(object);
//        setJSON(this, object);
//    }

//    public JSONObject jsonObject() {
//        JSONObject object = super.jsonObject();
//
//        try {
//            object.put(CLASS_NAME, className);
//            object.put(DATA_ID, dataId);
//        }catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object;
//    }

//    public static DeletedData setJSON(DeletedData deletedData, JSONObject object) {
//
//        try {
//            if (object.has(CLASS_NAME))
//                deletedData.className = object.getString(CLASS_NAME);
//            if (object.has(DATA_ID))
//                deletedData.dataId = object.getString(DATA_ID);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return deletedData;
//    }

    public String getClassName() {
        return className;
    }

    public String getDataId() {
        return dataId;
    }

}
