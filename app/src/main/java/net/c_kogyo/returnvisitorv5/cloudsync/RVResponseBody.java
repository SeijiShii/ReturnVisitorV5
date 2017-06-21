package net.c_kogyo.returnvisitorv5.cloudsync;

import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/06/19.
 */

public class RVResponseBody {

//    private static final String STATUS_CODE = "status_code";

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

    private StatusCode statusCode;
    private String userName, password;

    public RVResponseBody(StatusCode statusCode, String userName, @Nullable String password) {
        this.statusCode = statusCode;
        this.userName = userName;
        this.password = password;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
