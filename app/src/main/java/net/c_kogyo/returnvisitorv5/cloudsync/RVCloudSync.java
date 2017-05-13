package net.c_kogyo.returnvisitorv5.cloudsync;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync {

    private final String USER_DATA = "user";
    private final String LOGIN_STATE = "state";
    private final String USER_NAME = "user_name";
    private final String PASSWORD = "password";

    public enum LoginStatusCode {

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

    public static final int CREATED         = 201;
    public static final int AUTHENTICATED   = 202;
    public static final int BAD_REQUEST     = 400;
    public static final int UNAUTHORIZED    = 401;
    public static final int NOT_FOUND       = 404;

    private final String ROOT_URL = "http://192.168.3.3:13375";

    private static RVCloudSync instance = new RVCloudSync();
    private RVCloudSyncCallback mCallback;
    private Handler mHandler;

    public static RVCloudSync getInstance() {
        return instance;
    }

    public void setCallback(@NonNull RVCloudSyncCallback callback, Handler handler) {
        mCallback = callback;
        mHandler = handler;
    }

    private RVCloudSync() {


    }

    public void startLogin(String userName, String password) throws RVCloudSyncException{
        if (mCallback == null)
            throw new RVCloudSyncException();

        UserData dataPair = new UserData(userName, password);
        final DoHttpLoginRequest loginRequest = new DoHttpLoginRequest();
        loginRequest.execute(dataPair);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginRequest.cancel(true);
            }
        }, 5000);
    }


    public void startCreateAccount(String userName, String password) throws RVCloudSyncException {
        if (mCallback == null)
            throw new RVCloudSyncException();

        UserData dataPair = new UserData(userName, password);
    }


    public interface RVCloudSyncCallback {

        void onLoginResult(LoginResult result);

    }

    public class RVCloudSyncException extends Exception {
        private RVCloudSyncException() {
            super("RVCloudSyncCallback not set!");
        }
    }

    private class DoHttpLoginRequest extends AsyncTask<UserData, Void, Void> {

        private boolean isResponded = false;

        @Override
        protected Void doInBackground (UserData... params) {

            UserData dataPairSent = params[0];
            HttpURLConnection urlConnection = null;

            try {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isResponded)
                            mCallback.onLoginResult(new LoginResult(null, LoginStatusCode.REQUEST_TIME_OUT));
                    }
                }, 5000);

                URL url = new URL(ROOT_URL + "/users/?user_name=" +dataPairSent.userName + "&password=" + dataPairSent.password);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
//                urlConnection.setDoOutput(true);

                int status = urlConnection.getResponseCode();
                InputStream errorStream = urlConnection.getErrorStream();
                isResponded = true;
                if (errorStream == null) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readInputStream(inputStream);

                    JSONObject object = toJSON(s);
                    LoginResult result = new LoginResult(object);
                    String userName = result.userData.userName;
                    String password = result.userData.password;

                    switch (status) {
                        case AUTHENTICATED:

                            mCallback.onLoginResult(new LoginResult(new UserData(userName, password), LoginStatusCode.STATUS_202_AUTHENTICATED));
                            break;

//                        case UNAUTHORIZED:
//                            mCallback.onLoginResult(LoginStatusCode.UNAUTHORIZED_401, userName, null);
//                            break;
//
//                        case NOT_FOUND:
//                            mCallback.onLoginResult(LoginStatusCode.NOT_FOUND_404, userName, null);
//                            break;
                    }
                } else {

                    String s = readInputStream(errorStream);

                    JSONObject object = toJSON(s);
                    LoginResult loginResult = new LoginResult(object);

                    switch (status) {
                        case UNAUTHORIZED:
                            loginResult.statusCode = LoginStatusCode.STATUS_401_UNAUTHORIZED;
                            mCallback.onLoginResult(loginResult);
                            break;

                        case NOT_FOUND:
                            loginResult.statusCode = LoginStatusCode.STATUS_404_NOT_FOUND;
                            mCallback.onLoginResult(loginResult);
                            break;
                    }
                }
            } catch (IOException e) {
                if (e instanceof EOFException) {
                    mCallback.onLoginResult(new LoginResult(null, LoginStatusCode.REQUEST_TIME_OUT));

                }
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

    }

    private String readInputStream(InputStream inputStream) throws IOException{
        // TODO: 2017/05/11 バッファサイズを検証。不具合はないか
        byte[] buffer = new byte[1024];
        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer);
    }

    private JSONObject toJSON(String s) {
        JSONObject object = null;

        try {
            object = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
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
    }

    public class LoginResult {
        public UserData userData;
        public LoginStatusCode statusCode;

        public LoginResult(UserData userData, LoginStatusCode statusCode) {
            this.userData = userData;
            this.statusCode = statusCode;
        }

        private LoginResult(JSONObject object) {
            try {
                if (object.has(USER_DATA))
                    this.userData = new UserData(object.getJSONObject(USER_DATA));
                if (object.has(LOGIN_STATE))
                    this.statusCode = LoginStatusCode.valueOf(object.getString(LOGIN_STATE));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DoHttpCreateAccountRequest extends AsyncTask<UserData, Void, LoginResult> {

        @Override
        protected LoginResult doInBackground(UserData... params) {
            return null;
        }

        @Override
        protected void onPostExecute(LoginResult result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
