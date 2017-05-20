package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.data.RVData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static net.c_kogyo.returnvisitorv5.Constants.DATA_ARRAY_LATER_THAN_TIME;
import static net.c_kogyo.returnvisitorv5.Constants.LOADED_DATA_ARRAY;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.LAST_DEVICE_SYNC_TIME;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync {

    public static final String TAG = "RVCloudSync";

    private final String USER_DATA = "user";
    private final String LOGIN_STATE = "state";
    private final String USER_NAME = "user_name";
    private final String PASSWORD = "password";

    private final String METHOD = "method";

    private final String STATE = "state";

    public enum RVCloudSyncMethod {
        LOGIN,
        CREATE_USER,
        SYNC_DATA,
    }

    public enum LoginStatus {

        STATUS_200_OK,
        STATUS_201_CREATED,
        STATUS_202_AUTHENTICATED,
        STATUS_400_DUPLICATE_USER_NAME,
        STATUS_400_SHORT_USER_NAME,
        STATUS_400_SHORT_PASSWORD,
        STATUS_401_UNAUTHORIZED,
        STATUS_404_NOT_FOUND,
        REQUEST_TIME_OUT,
        SERVER_NOT_AVAILABLE
    }

//    public static final int CREATED         = 201;
//    public static final int AUTHENTICATED   = 202;
//    public static final int BAD_REQUEST     = 400;
//    public static final int UNAUTHORIZED    = 401;
//    public static final int NOT_FOUND       = 404;

    private final String ROOT_URL = "http://192.168.3.4:1337";

    private static RVCloudSync instance = new RVCloudSync();
    private RVCloudSyncCallback mCallback;
    private Handler mHandler;

    private RVWebSocketClient socketClient;
//    private UserData userData;

    public static RVCloudSync getInstance() {
        return instance;
    }

    public void setCallback(@NonNull RVCloudSyncCallback callback, Handler handler) {
        mCallback = callback;
        mHandler = handler;
    }

//    private WebSocket webSocket;
    private RVCloudSync() {}

    public void startSendingUserData(@Nullable String userName,
                                     @Nullable String password,
                                     final RVCloudSyncMethod method,
                                     Context context) throws RVCloudSyncException{
        if (userName == null || password == null)
            return;

        if (mCallback == null)
            throw new RVCloudSyncException();

        try {
            if (socketClient == null) {
                socketClient = new RVWebSocketClient(new URI(ROOT_URL), context) {
                    @Override
                    public void onMessage(String s) {

                        try {
                            JSONObject object = new JSONObject(s);
                            RVCloudSyncMethod method = RVCloudSyncMethod.valueOf(object.getString(METHOD));
                            LoginStatus status = LoginStatus.valueOf(object.getString(STATE));
                            UserData userData = new UserData(object.getJSONObject(USER_DATA));

                            JSONArray loadedArray = new JSONArray();
                            if (object.has(LOADED_DATA_ARRAY))
                                loadedArray = object.getJSONArray(Constants.LOADED_DATA_ARRAY);

                            final LoginResult result = new LoginResult(userData, status);

                            switch (method) {
                                case LOGIN:
                                case CREATE_USER:
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mCallback.onLoginResult(result);
                                        }
                                    });
                                    break;

                                case SYNC_DATA:
                                    RVData.getInstance().setFromRecordArray(loadedArray);
                                    mCallback.postDataSynchronized();
                                    break;
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                };
            }
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }

        if (socketClient == null)
            return;


        final UserData userData = new UserData(userName, password);

        if (socketClient.isOpen()) {
            sendUserData(userData, RVCloudSyncMethod.LOGIN);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int timeCounter = 0;
                    while (!socketClient.isOpen()) {
                        try {
                            Thread.sleep(50);
                            timeCounter += 50;
                            if (timeCounter > 50000) {
                                postErrorResult(LoginStatus.REQUEST_TIME_OUT);
                                return;
                            }
                        } catch (InterruptedException e ) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    sendUserData(userData, RVCloudSyncMethod.LOGIN);
                }
            }).start();
            socketClient.connect();

        }
    }

    private void postErrorResult(final LoginStatus status) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onLoginResult(new LoginResult(new UserData(null, null), status));
            }
        });
    }

    private void sendUserData(UserData userData, RVCloudSyncMethod method) { {

        final JSONObject object = new JSONObject();
        try {
            object.put(METHOD, method.toString());
            object.put(USER_DATA, userData.jsonObject());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
            socketClient.send(object.toString());
        }
    }

    public void startDataSync(@Nullable String userName, @Nullable String password, Context context) {

        SharedPreferences prefs
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        long lastSyncTime = prefs.getLong(LAST_DEVICE_SYNC_TIME, 0);

        final SyncData syncData
                = new SyncData(new UserData(userName, password),
                lastSyncTime,
                RVData.getInstance().getJSONArrayLaterThanTime(lastSyncTime));

        if (socketClient.isOpen()) {
            socketClient.send(syncData.jsonObject().toString());
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int timeCounter = 0;
                    while (!socketClient.isOpen()) {
                        try {
                            Thread.sleep(50);
                            timeCounter += 50;
                            if (timeCounter > 50000) {
                                postErrorResult(LoginStatus.REQUEST_TIME_OUT);
                                return;
                            }
                        } catch (InterruptedException e ) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    socketClient.send(syncData.jsonObject().toString());
                }
            }).start();
            socketClient.connect();
        }
    }




    public interface RVCloudSyncCallback {

        void onLoginResult(LoginResult result);

        void postDataSynchronized();

    }

    public class RVCloudSyncException extends Exception {
        private RVCloudSyncException() {
            super("RVCloudSyncCallback not set!");
        }
    }

    public class UserData {
        public String userName, password;

        private UserData(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        private UserData(JSONObject object) {

            try {
                if (object.has(USER_NAME))
                    this.userName = object.getString(USER_NAME);
                if (object.has(PASSWORD))
                    this.password = object.getString(PASSWORD);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private JSONObject jsonObject() {

            JSONObject object = new JSONObject();
            try {
                object.put(USER_NAME, userName);
                object.put(PASSWORD, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }

    public class LoginResult {
        public UserData userData;
        public LoginStatus statusCode;

        private LoginResult(UserData userData, LoginStatus statusCode) {
            this.userData = userData;
            this.statusCode = statusCode;
        }

    }

    private class SyncData {
        private UserData userData;
        private long lastDeviceSyncTime;
        private JSONArray laterDataArray;

        private SyncData(UserData userData, long lastDeviceSyncTime, JSONArray laterDataArray) {
            this.userData = userData;
            this.lastDeviceSyncTime = lastDeviceSyncTime;
            this.laterDataArray = laterDataArray;
        }

        public JSONObject jsonObject() {
            JSONObject object = new JSONObject();
            try {
                object.put(USER_DATA, userData.jsonObject());
                object.put(LAST_DEVICE_SYNC_TIME, lastDeviceSyncTime);
                object.put(DATA_ARRAY_LATER_THAN_TIME, laterDataArray);
                object.put(METHOD, RVCloudSyncMethod.SYNC_DATA.toString());
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            return object;
        }
    }




    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
