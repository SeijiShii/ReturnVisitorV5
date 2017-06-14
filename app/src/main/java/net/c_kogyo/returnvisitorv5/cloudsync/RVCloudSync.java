package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.util.EncryptUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import static android.content.Context.MODE_PRIVATE;
import static net.c_kogyo.returnvisitorv5.Constants.DATA_ARRAY_LATER_THAN_TIME;
import static net.c_kogyo.returnvisitorv5.Constants.LOADED_DATA_ARRAY;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.LAST_DEVICE_SYNC_TIME;
import static net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync.RVCloudSyncMethod.CREATE_USER;
import static net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync.RVCloudSyncMethod.LOGIN;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync implements RVWebSocketClient.RVWebSocketClientCallback{

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

    public enum ResultStatus {

        STATUS_200_SYNC_OK,
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

    private final String ROOT_URL = "https://c-kogyo.work:1337";

    private static RVCloudSync instance = new RVCloudSync();
    private RVCloudSyncCallback mCallback;

    private RVWebSocketClient socketClient;
//    private UserData userData;

    public static RVCloudSync getInstance() {
        return instance;
    }

    public void setCallback(@NonNull RVCloudSyncCallback callback) {
        mCallback = callback;
    }

//    private WebSocket webSocket;
    private RVCloudSync() {}

    public void login(String userName,
                           String password,
                           boolean passAlreadyEncrypted,
                           Context context) {

        if (mCallback != null) {
            mCallback.onStartLoginRequest();
        }
        initSocketClient(context);

        if (socketClient == null)
            return;

        final UserData userData = new UserData(userName, password, passAlreadyEncrypted);

        if (socketClient.isOpen()) {
            loginWithData(userData);
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
                                if (mCallback != null) {
                                    RequestResult result = new RequestResult(userData, ResultStatus.REQUEST_TIME_OUT);
                                    mCallback.onLoginResult(result);
                                }
                                return;
                            }
                        } catch (InterruptedException e ) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    loginWithData(userData);
                }
            }).start();
            socketClient.connect();

        }
    }

    public void createUser(String userName,
                                String password,
                                boolean passAlreadyEncrypted,
                                Context context) {

        if (mCallback != null) {
            mCallback.onStartCreateUserRequest();
        }
        initSocketClient(context);

        if (socketClient == null)
            return;

        final UserData userData = new UserData(userName, password, passAlreadyEncrypted);

        if (socketClient.isOpen()) {
            createUserWithData(userData);
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
                                if (mCallback != null) {
                                    RequestResult result = new RequestResult(userData, ResultStatus.REQUEST_TIME_OUT);
                                    mCallback.onCreateUserResult(result);
                                }
                                return;
                            }
                        } catch (InterruptedException e ) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    createUserWithData(userData);
                }
            }).start();
            socketClient.connect();
        }
    }

    public void syncDataIfLoggedIn (Context context) {

        LoginState loginState = LoginState.getInstance();
        if (!loginState.isLoggedIn()) return;

        if (mCallback != null) {
            mCallback.onStartSyncDataRequest();
        }

        initSocketClient(context);

        SharedPreferences prefs
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        long lastSyncTime = prefs.getLong(LAST_DEVICE_SYNC_TIME, 0);

        final UserData userData = new UserData(loginState.getUserName(), loginState.getPassword(), true);

        final SyncData syncData
                = new SyncData(userData,
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

                                if (mCallback != null) {
                                    RequestResult result = new RequestResult(userData, ResultStatus.REQUEST_TIME_OUT);
                                    mCallback.onSyncDataResult(result);
                                }
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

    private void initSocketClient(final Context context) {
        try {
            socketClient = new RVWebSocketClient(new URI(ROOT_URL), context, this);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void loginWithData(UserData userData) {
        final JSONObject object = new JSONObject();
        try {
            object.put(METHOD, LOGIN);
            object.put(USER_DATA, userData.jsonObject());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        socketClient.send(object.toString());
    }

    private void createUserWithData(UserData userData) {
        final JSONObject object = new JSONObject();
        try {
            object.put(METHOD, CREATE_USER);
            object.put(USER_DATA, userData.jsonObject());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        socketClient.send(object.toString());
    }


    public class UserData {
        public String userName, password;

        private UserData(@Nullable String userName, @Nullable String password, boolean passAlreadyEncrypted) {

            if (userName == null || password == null) {
                return;
            }

            this.userName = userName;
            if (passAlreadyEncrypted) {
                this.password = password;
            } else {
                this.password = EncryptUtil.toEncryptedHashValue("SHA-256", password);
            }
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

    public class RequestResult {
        public UserData userData;
        public ResultStatus statusCode;

        private RequestResult(UserData userData, ResultStatus statusCode) {
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

    @Override
    public void onWebSocketMessage(String s) {
        try {
            JSONObject object = new JSONObject(s);
            RVCloudSyncMethod method = RVCloudSyncMethod.valueOf(object.getString(METHOD));
            ResultStatus status = ResultStatus.valueOf(object.getString(STATE));
            UserData userData = new UserData(object.getJSONObject(USER_DATA));

            JSONArray loadedArray = new JSONArray();
            if (object.has(LOADED_DATA_ARRAY))
                loadedArray = object.getJSONArray(Constants.LOADED_DATA_ARRAY);

            final RequestResult result = new RequestResult(userData, status);

//            if (status == ResultStatus.STATUS_202_AUTHENTICATED
//                    || status == ResultStatus.STATUS_201_CREATED
//                    || status == ResultStatus.STATUS_200_SYNC_OK) {
//                LoginState.onSuccessLogin(userData.userName, userData.password, context);
//            } else {
//                LoginState.onLoggedOut(context);
//            }

            switch (method) {
                case LOGIN:
                    if (mCallback != null) {
                        mCallback.onLoginResult(result);
                    }
                case CREATE_USER:
                    if (mCallback != null) {
                        mCallback.onCreateUserResult(result);
                    }
                    break;

                case SYNC_DATA:
                    RVData.getInstance().setFromRecordArray(loadedArray, RVData.RecordArraySource.FROM_CLOUD);
                    RVData.getInstance().removeDeletedData();
                    if (mCallback != null) {
                        mCallback.onSyncDataResult(result);
                    }
                    break;
            }
            socketClient.close();

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public interface RVCloudSyncCallback {

        void onStartLoginRequest();

        void onLoginResult(RequestResult result);

        void onStartCreateUserRequest();

        void onCreateUserResult(RequestResult result);

        void onStartSyncDataRequest();

        void onSyncDataResult(RequestResult result);

    }

    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
