package net.c_kogyo.returnvisitorv5.cloudsync;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/06/19.
 */

public class RVCloudSyncDataFrame {

    public enum FrameCategory {
        SYNC_DATA_REQUEST_WITH_GOOGLE,
        SYNC_DATA_RESPONSE,
        DEVICE_DATA_FRAME,
        DEVICE_DATA_END_FRAME,
        CLOUD_DATA_FRAME,
        CLOUD_DATA_END_FRAME,
    }

    public enum StatusCode {
        STATUS_200_SYNC_START_OK,
        STATUS_200_SYNC_END_OK,
        STATUS_401_UNAUTHORIZED,
        STATUS_404_NOT_FOUND,
        STATUS_TIMED_OUT,
        STATUS_SERVER_NOT_AVAILABLE
    }


    private FrameCategory frameCategory;
    private String dataBody, authToken;
    private StatusCode statusCode;
    private long lastSyncDate;

    private RVCloudSyncDataFrame(){}

    public FrameCategory getFrameCategory() {
        return frameCategory;
    }

    String getDataBody() {
        return dataBody;
    }

    public String getAuthToken() {
        return authToken;
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

        Builder setDataBody(String dataBody) {
            frame.dataBody = dataBody;
            return this;
        }

        Builder setAuthToken (String authToken) {
            frame.authToken = authToken;
            return this;
        }

        Builder setLastSyncDate(long lastSyncDate) {
            frame.lastSyncDate = lastSyncDate;
            return this;
        }

        Builder setStatusCode(StatusCode statusCode) {
            frame.statusCode = statusCode;
            return this;
        }

        public RVCloudSyncDataFrame create() {
            return frame;
        }

    }


}
