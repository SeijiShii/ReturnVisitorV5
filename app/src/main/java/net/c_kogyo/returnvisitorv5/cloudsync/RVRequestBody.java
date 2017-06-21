package net.c_kogyo.returnvisitorv5.cloudsync;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/06/19.
 */

public class RVRequestBody {

    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    private static final String LAST_SYNC_DATE = "last_sync_date";

    private String userName, password;
    private long lastSyncDate;

    public RVRequestBody(String userName, String password, long lastSyncDate) {
        this.userName = userName;
        this.password = password;
        this.lastSyncDate = lastSyncDate;
    }

//    public RVRequestBody(String jsonString) {
//        try {
//            JSONObject object = new JSONObject(jsonString);
//            setJSON(this, object);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private JSONObject jsonObject() {
//        JSONObject object = new JSONObject();
//        try {
//            object.put(USER_NAME, userName);
//            object.put(PASSWORD, password);
//            object.put(LAST_SYNC_DATE, lastSyncDate);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object;
//    }
//
//    public String jsonString() {
//        return jsonObject().toString();
//    }
//
//    private static void setJSON(RVRequestBody body, JSONObject object) {
//
//        try {
//            if (object.has(USER_NAME))
//                body.userName = object.getString(USER_NAME);
//            if (object.has(PASSWORD))
//                body.password = object.getString(PASSWORD);
//            if (object.has(LAST_SYNC_DATE))
//                body.lastSyncDate = object.getLong(LAST_SYNC_DATE);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}
