package net.c_kogyo.returnvisitorv5.cloudsync;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/06/19.
 */

public class RVCloudSyncDataFrame {

    private static final String FRAME_CATEGORY = "frame_category";
    private static final String DATA_BODY = "data_body";
    private static final String TOKEN = "token";

    public enum FrameCategory {
        LOGIN_REQUEST,
        LOGIN_RESPONSE,
        CREATE_USER_REQUEST,
        CREATE_USER_RESPONSE,
        SYNC_DATA_REQUEST,
        SYNC_DATA_RESPONSE,
        DEVICE_DATA_FRAME,
        DEVICE_DATA_END_FRAME,
        CLOUD_DATA_FRAME,
        CLOUD_DATA_END_FRAME
    }

    private FrameCategory frameCategory;
    private String dataBody, token;

    public RVCloudSyncDataFrame(FrameCategory frameCategory,
                                @Nullable String dataBody,
                                @Nullable String token) {
        this.frameCategory = frameCategory;
        this.dataBody = dataBody;
        this.token = token;
    }

    public RVCloudSyncDataFrame() {
    }

    public FrameCategory getFrameCategory() {
        return frameCategory;
    }

    public String getDataBody() {
        return dataBody;
    }

    public String getToken() {
        return token;
    }

    public void setFrameCategory(FrameCategory frameCategory) {
        this.frameCategory = frameCategory;
    }

    public void setDataBody(String dataBody) {
        this.dataBody = dataBody;
    }

    public void setToken(String token) {
        this.token = token;
    }

    //    public RVCloudSyncDataFrame(String jsonString) {
//        try {
//            JSONObject object = new JSONObject(jsonString);
//            setJSON(this, object);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void setJSON(RVCloudSyncDataFrame frame, JSONObject object) {
//        try {
//            if (object.has(FRAME_CATEGORY))
//                frame.frameCategory = FrameCategory.valueOf(object.getString(FRAME_CATEGORY));
//            if (object.has(DATA_BODY))
//                frame.dataBody = object.getString(DATA_BODY);
//            if (object.has(TOKEN))
//                frame.token = object.getString(TOKEN);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private JSONObject jsonObject() {
//
//        JSONObject object = new JSONObject();
//        try {
//            object.put(FRAME_CATEGORY, frameCategory.toString());
//            object.put(DATA_BODY, dataBody);
//            object.put(TOKEN, token);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object;
//    }
//
//    public String jsonString() {
//        return jsonObject().toString();
//    }
}
