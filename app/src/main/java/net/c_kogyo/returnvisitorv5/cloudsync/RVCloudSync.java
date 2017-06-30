package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
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

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync implements RVWebSocketClient.RVWebSocketClientCallback, RVDBHelper.SaveRecordsListener{

    public static final String TAG = "RVCloudSync";

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

    public void loginWithName(final String userName,
                              String password,
                              boolean passAlreadyEncrypted,
                              Context context) {

        String mPass;
        if (passAlreadyEncrypted) {
            mPass = password;
        } else {
            mPass = EncryptUtil.toEncryptedHashValue("SHA-256", password);
        }
        final RVCloudSyncDataFrame dataFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.LOGIN_REQUEST)
                        .setUserName(userName)
                        .setPassword(mPass)
                            .create();
        startSendingData(context, dataFrame,
                new SendDataCallback() {
                    @Override
                    public void onStart() {
                        if (mCallback != null) {
                            mCallback.onStartRequest(dataFrame.getFrameCategory());
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
                            mCallback.onResponse(frame);
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
        final RVCloudSyncDataFrame dataFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.CREATE_USER_REQUEST)
                    .setUserName(userName)
                    .setPassword(mPass)
                    .create();

        startSendingData(context, dataFrame,
                new SendDataCallback() {
                    @Override
                    public void onStart() {
                        if (mCallback != null) {
                            mCallback.onStartRequest(dataFrame.getFrameCategory());
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
                            mCallback.onResponse(frame);                        }
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
                boolean isDataSent = false;
                @Override
                public void run() {
                    int timeCounter = 0;
                    while (!socketClient.isOpen()) {
                        try {
                            Thread.sleep(50);
                            timeCounter += 50;
                            if (timeCounter > 50000) {
                                if (!isDataSent) {
                                    callback.onTimedOut();
                                }
                                return;
                            }
                        } catch (InterruptedException e ) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    isDataSent = true;
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
    public void requestDataSyncIfLoggedIn(final Context context) {

        switch (LoginHelper.getLoginProvider(context)) {
            case USER_NAME:
                if (!LoginHelper.isLoggedIn(context)) return;
                break;
            case FACEBOOK:
                if (AccessToken.getCurrentAccessToken() == null) {
                    return;
                }
                break;
        }

        if (mCallback != null) {
            mCallback.onStartRequest(RVCloudSyncDataFrame.FrameCategory.SYNC_DATA_REQUEST_WITH_NAME);
        }

        initSocketClient(context);

        cloudDataList = new ArrayList<>();

        long lastSyncTime = LoginHelper.loadLastSyncDate(context);

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(lastSyncTime);
        Log.d(TAG, "Last sync date: " + DateTimeText.getDateTimeText(date, context));

        deviceDataList = RVDBHelper.getInstance().loadRecordsLaterThanTime(lastSyncTime);

        RVCloudSyncDataFrame dataFrame = null;

        switch (LoginHelper.getLoginProvider(context)) {
            case USER_NAME:
                dataFrame = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.SYNC_DATA_REQUEST_WITH_NAME)
                        .setUserName(LoginHelper.getUserName(context))
                        .setPassword(LoginHelper.getPassword(context))
                        .setLastSyncDate(lastSyncTime)
                        .create();
                break;
            case FACEBOOK:

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                String token = accessToken.getToken();

                dataFrame = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.SYNC_DATA_REQUEST_WITH_FACEBOOK)
                        .setAuthToken(token)
                        .setLastSyncDate(lastSyncTime)
                        .create();
                break;
        }

        startSendingData(context, dataFrame, new SendDataCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onTimedOut() {
                if (mCallback != null) {

                    RVCloudSyncDataFrame frame
                            = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.SYNC_DATA_RESPONSE)
                                .setUserName(LoginHelper.getUserName(context))
                                .create();
                    mCallback.onResponse(frame);
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
            case CREATE_USER_RESPONSE:
                if (mCallback != null) {
                    mCallback.onResponse(dataFrame);
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
                if (mCallback != null) {
                    mCallback.onResponse(dataFrame);
                }
                sendDeviceData(dataFrame.getServerToken());
                break;

            case STATUS_401_UNAUTHORIZED:
            case STATUS_404_NOT_FOUND:
                if (mCallback != null) {
                    mCallback.onResponse(dataFrame);
                }
                break;
        }
    }

    private void sendDeviceData(String serverToken) {
        for (RVRecord record : deviceDataList) {
            RVCloudSyncDataFrame dataFrame =
                    new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.DEVICE_DATA_FRAME)
                        .setDataBody(mGson.toJson(record))
                        .setServerToken(serverToken)
                        .create();
            socketClient.send(mGson.toJson(dataFrame));
        }
        // DONE: 2017/06/19 デバイスに蓄積したデータの伝送
        RVCloudSyncDataFrame deviceDataEndFrame
                = new RVCloudSyncDataFrame.Builder(RVCloudSyncDataFrame.FrameCategory.DEVICE_DATA_END_FRAME)
                    .setServerToken(serverToken)
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
                    mCallback.onResponse(dataFrame);
                }
            }
       });
        socketClient.close();
    }

    @Override
    public void onFinishSave() {

    }

    public interface RVCloudSyncCallback {

        void onStartRequest(RVCloudSyncDataFrame.FrameCategory frameCategory);

        void onResponse(RVCloudSyncDataFrame dataFrame);

    }

    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
