package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;
import net.c_kogyo.returnvisitorv5.db.RVRecord;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.EncryptUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.LAST_DEVICE_SYNC_TIME;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync implements RVWebSocketClient.RVWebSocketClientCallback, RVDBHelper.SaveRecordsListener{

    public static final String TAG = "RVCloudSync";

//    public static final int CREATED         = 201;
//    public static final int AUTHENTICATED   = 202;
//    public static final int BAD_REQUEST     = 400;
//    public static final int UNAUTHORIZED    = 401;
//    public static final int NOT_FOUND       = 404;

    private final String ROOT_URL = "https://c-kogyo.work:1337";
//    private final String ROOT_URL = "http://192.168.3.4:1337";

    private static RVCloudSync instance = new RVCloudSync();
    private RVCloudSyncCallback mCallback;
    private Gson mGson;

    private RVWebSocketClient socketClient;
//    private UserData userData;

    public static RVCloudSync getInstance() {
        return instance;
    }

    public void setCallback(@NonNull RVCloudSyncCallback callback) {
        mCallback = callback;
    }

//    private WebSocket webSocket;
    private RVCloudSync() {
        mGson = new Gson();
    }

    public void login(final String userName,
                      String password,
                      boolean passAlreadyEncrypted,
                      Context context) {

        String mPass;
        if (passAlreadyEncrypted) {
            mPass = password;
        } else {
            mPass = EncryptUtil.toEncryptedHashValue("SHA-256", password);
        }
        RVCloudSyncDataFrame dataFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.LOGIN_REQUEST)
                        .setUserName(userName)
                        .setPassword(mPass)
                            .create();
        startSendingData(context, dataFrame,
                new SendDataCallback() {
                    @Override
                    public void onStart() {
                        if (mCallback != null) {
                            mCallback.onStartLoginRequest();
                        }
                    }

                    @Override
                    public void onTimedOut() {
                        if (mCallback != null) {
                            RVCloudSyncDataFrame frame
                                    = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.LOGIN_RESPONSE)
                                        .setStatusCode(RVCloudSyncDataFrame.StatusCode.STATUS_TIMED_OUT)
                                        .setUserName(userName)
                                            .create();
                            mCallback.onLoginResult(frame);
                        }
                    }
                });
    }

    public void createUser(final String userName,
                                String password,
                                boolean passAlreadyEncrypted,
                                Context context) {

        String mPass;
        if (passAlreadyEncrypted) {
            mPass = password;
        } else {
            mPass = EncryptUtil.toEncryptedHashValue("SHA-256", password);
        }
        RVCloudSyncDataFrame dataFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.CREATE_USER_REQUEST)
                    .setUserName(userName)
                    .setPassword(mPass)
                    .create();

        startSendingData(context, dataFrame,
                new SendDataCallback() {
                    @Override
                    public void onStart() {
                        if (mCallback != null) {
                            mCallback.onStartCreateUserRequest();
                        }
                    }

                    @Override
                    public void onTimedOut() {
                        if (mCallback != null) {
                            RVCloudSyncDataFrame frame
                                    = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.CREATE_USER_RESPONSE)
                                        .setStatusCode(RVCloudSyncDataFrame.StatusCode.STATUS_TIMED_OUT)
                                        .setUserName(userName)
                                        .create();
                            mCallback.onLoginResult(frame);                        }
                    }
                });
    }

    private void startSendingData(Context context,
                                  final RVCloudSyncDataFrame dataFrame,
                                  final SendDataCallback callback) {
        callback.onStart();

        initSocketClient(context);

        if (socketClient == null)
            return;

        if (socketClient.isOpen()) {
            socketClient.send(mGson.toJson(dataFrame));
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
                                callback.onTimedOut();
                                return;
                            }
                        } catch (InterruptedException e ) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    socketClient.send(mGson.toJson(dataFrame));
                }
            }).start();
            socketClient.connect();

        }
    }

    private interface SendDataCallback {

        void onStart();

        void onTimedOut();
    }

    private ArrayList<RVRecord> cloudDataList, deviceDataList;
    private int cloudDataCount = 0;
    public void requestDataSyncIfLoggedIn(Context context) {

        final LoginState loginState = LoginState.getInstance();
        if (!loginState.isLoggedIn()) return;

        if (mCallback != null) {
            mCallback.onStartSyncDataRequest();
        }

        initSocketClient(context);

        cloudDataList = new ArrayList<>();

        SharedPreferences prefs
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        long lastSyncTime = prefs.getLong(LAST_DEVICE_SYNC_TIME, 0);

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(lastSyncTime);
        Log.d(TAG, "Last sync date: " + DateTimeText.getDateTimeText(date, context));

        deviceDataList = RVDBHelper.getInstance().loadRecordsLaterThanTime(lastSyncTime);

        RVCloudSyncDataFrame dataFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.SYNC_DATA_REQUEST)
                    .setUserName(loginState.getUserName())
                    .setPassword(loginState.getPassword())
                    .setLastSyncDate(lastSyncTime)
                    .create();
        startSendingData(context, dataFrame, new SendDataCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onTimedOut() {
                if (mCallback != null) {
                    RVCloudSyncDataFrame frame
                            = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.SYNC_DATA_RESPONSE)
                                .setUserName(loginState.getUserName())
                                .create();
                    mCallback.onSyncDataResult(frame);
                }
            }
        });
    }

    private void initSocketClient(final Context context) {
        try {
            socketClient = new RVWebSocketClient(new URI(ROOT_URL), context, this);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onWebSocketMessage(String s) {

        RVCloudSyncDataFrame dataFrame = mGson.fromJson(s, RVCloudSyncDataFrame.class);
        Log.d(TAG, "FrameCategory: " + dataFrame.getFrameCategory().toString());

        switch (dataFrame.getFrameCategory()) {
            case LOGIN_RESPONSE:
                if (mCallback != null) {
                    mCallback.onLoginResult(dataFrame);
                }
                break;

            case CREATE_USER_RESPONSE:
                if (mCallback != null) {
                    mCallback.onCreateUserResult(dataFrame);
                }
                break;

            case SYNC_DATA_RESPONSE:
                onSyncDataResponse(dataFrame);
                break;

            case CLOUD_DATA_FRAME:
                Log.d(TAG, "Cloud data count: " + cloudDataCount++ + ", List number: " + cloudDataList.size());
                onCloudDataFrame(dataFrame);
                break;

            case CLOUD_DATA_END_FRAME:
                onCloudDataEndFrame(dataFrame);
                break;
        }
    }

    private void onSyncDataResponse(RVCloudSyncDataFrame dataFrame) {
        switch (dataFrame.getStatusCode()) {
            case STATUS_200_SYNC_START_OK:
                sendDeviceData(dataFrame.getAuthToken());
                break;

            case STATUS_401_UNAUTHORIZED:
            case STATUS_404_NOT_FOUND:
                if (mCallback != null) {
                    mCallback.onSyncDataResult(dataFrame);
                }
                break;
        }
    }

    private void sendDeviceData(String authToken) {
        for (RVRecord record : deviceDataList) {
            RVCloudSyncDataFrame dataFrame =
                    new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.DEVICE_DATA_FRAME)
                        .setDataBody(mGson.toJson(record))
                        .setAuthToken(authToken)
                        .create();
            socketClient.send(mGson.toJson(dataFrame));
        }
        // DONE: 2017/06/19 デバイスに蓄積したデータの伝送
        RVCloudSyncDataFrame deviceDataEndFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.DEVICE_DATA_END_FRAME)
                    .setAuthToken(authToken)
                    .create();
        socketClient.send(mGson.toJson(deviceDataEndFrame));
    }

    private void onCloudDataFrame(RVCloudSyncDataFrame dataFrame) {
        RVRecord record = mGson.fromJson(dataFrame.getDataBody(), RVRecord.class);
        cloudDataList.add(record);

        if (cloudDataList.size() > 300) {
//            Log.d(TAG, "cloudDataList.size: " + cloudDataList.size());
            ArrayList<RVRecord> bufferedList = new ArrayList<>(cloudDataList);
            cloudDataList.clear();
            RVDBHelper.getInstance().saveRecords(bufferedList, null);
        }
    }

    private void onCloudDataEndFrame(final RVCloudSyncDataFrame dataFrame) {
        RVDBHelper.getInstance().saveRecords(cloudDataList, new RVDBHelper.SaveRecordsListener() {

            @Override
            public void onFinishSave() {
                RVDBHelper.getInstance().deleteDeletedData();

                if (mCallback != null) {
                    mCallback.onSyncDataResult(dataFrame);
                }
            }
       });
        socketClient.close();
    }

    @Override
    public void onFinishSave() {

    }

    public interface RVCloudSyncCallback {

        void onStartLoginRequest();

        void onLoginResult(RVCloudSyncDataFrame dataFrame);

        void onStartCreateUserRequest();

        void onCreateUserResult(RVCloudSyncDataFrame dataFrame);

        void onStartSyncDataRequest();

        void onSyncDataResult(RVCloudSyncDataFrame dataFrame);

    }

    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
