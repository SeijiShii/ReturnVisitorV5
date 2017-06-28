package net.c_kogyo.returnvisitorv5.cloudsync;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/06/19.
 */

public class RVCloudSyncDataFrame {

//    private static final String FRAME_CATEGORY = "frame_category";
//    private static final String DATA_BODY = "data_body";
//    private static final String TOKEN = "token";

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

    public enum StatusCode {
        STATUS_202_AUTHENTICATED,
        STATUS_401_UNAUTHORIZED,
        STATUS_404_NOT_FOUND,
        STATUS_201_CREATED_USER,
        STATUS_400_DUPLICATE_USER_NAME,
        STATUS_400_SHORT_USER_NAME,
        STATUS_400_SHORT_PASSWORD,
        STATUS_200_SYNC_START_OK,
        STATUS_200_SYNC_END_OK,
        STATUS_TIMED_OUT,
        STATUS_SERVER_NOT_AVAILABLE
    }


    private FrameCategory frameCategory;
    private String dataBody, authToken, userName, password;
    private StatusCode statusCode;
    private long lastSyncDate;

    private RVCloudSyncDataFrame(){}

    FrameCategory getFrameCategory() {
        return frameCategory;
    }

    String getDataBody() {
        return dataBody;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public long getLastSyncDate() {
        return lastSyncDate;
    }

    public static class Builder {
        private RVCloudSyncDataFrame frame;
        public Builder(FrameCategory category) {
            frame = new RVCloudSyncDataFrame();
            frame.frameCategory = category;
        }

        public Builder setUserName(String userName) {
            frame.userName = userName;
            return this;
        }

        public Builder setPassword(String password) {
            frame.password = password;
            return this;
        }

        public Builder setDataBody(String dataBody) {
            frame.dataBody = dataBody;
            return this;
        }

        public Builder setAuthToken (String authToken) {
            frame.authToken = authToken;
            return this;
        }

        public Builder setLastSyncDate(long lastSyncDate) {
            frame.lastSyncDate = lastSyncDate;
            return this;
        }

        public Builder setStatusCode(StatusCode statusCode) {
            frame.statusCode = statusCode;
            return this;
        }

        public RVCloudSyncDataFrame create() {
            return frame;
        }


    }

}
