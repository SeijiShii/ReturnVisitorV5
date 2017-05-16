package net.c_kogyo.returnvisitorv5.cloudsync;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync extends WebSocketAdapter{

    private final String TAG = "WebSocket";

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
        REQUEST_TIME_OUT
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

//    private UserData userData;

    public static RVCloudSync getInstance() {
        return instance;
    }

    public void setCallback(@NonNull RVCloudSyncCallback callback, Handler handler) {
        mCallback = callback;
        mHandler = handler;
    }

    private WebSocket webSocket;
    private RVCloudSync() {}

    public void startSendingUserData(String userName,
                                     String password,
                                     final RVCloudSyncMethod method) throws RVCloudSyncException{
        if (mCallback == null)
            throw new RVCloudSyncException();

        createWebSocketIfNeeded();

        if (webSocket == null)
            return;

        final UserData userData = new UserData(userName, password);

        if (webSocket.isOpen()) {
            sendUserData(userData, method);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        webSocket.connect();

                        while (!webSocket.isOpen()) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e ) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                        sendUserData(userData, method);
                    }catch (WebSocketException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }).start();

        }
    }

    private void sendUserData(UserData userData, RVCloudSyncMethod method) { {

        final JSONObject object = new JSONObject();
        try {
            object.put(METHOD, method.toString());
            object.put(USER_DATA, userData.jsonObject());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        webSocket.sendText(object.toString());
    }

    }
    private void createWebSocketIfNeeded() {
        if (webSocket == null) {
            final WebSocketFactory factory = new WebSocketFactory();
            try {
                webSocket = factory.createSocket(ROOT_URL, 5000);
                webSocket.addListener(RVCloudSync.this);

            } catch (IOException e ) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);

        webSocket = websocket;
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onTextFrame(websocket, frame);

        String data = frame.getPayloadText();
        JSONObject object = new JSONObject(data);

        try {
            RVCloudSyncMethod method = RVCloudSyncMethod.valueOf(object.getString(METHOD));
            LoginStatus status = LoginStatus.valueOf(object.getString(STATE));
            UserData userData = new UserData(object.getJSONObject(USER_DATA));

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
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public interface RVCloudSyncCallback {

        void onLoginResult(LoginResult result);

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

        public LoginResult(UserData userData, LoginStatus statusCode) {
            this.userData = userData;
            this.statusCode = statusCode;
        }

        private LoginResult(JSONObject object) {
            try {
                if (object.has(USER_DATA))
                    this.userData = new UserData(object.getJSONObject(USER_DATA));
                if (object.has(LOGIN_STATE))
                    this.statusCode = LoginStatus.valueOf(object.getString(LOGIN_STATE));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
