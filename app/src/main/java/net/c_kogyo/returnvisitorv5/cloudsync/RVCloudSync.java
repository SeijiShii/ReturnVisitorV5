package net.c_kogyo.returnvisitorv5.cloudsync;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

        CREATED_201,
        AUTHENTICATED_202,
        BAD_REQUEST_400_SHORT_USER_NAME(),
        BAD_REQUEST_400_SHORT_PASSWORD,
        BAD_REQUEST_400_DEPULICATE_USER_NAME,
        UNAUTHORIZED_401,
        NOT_FOUND_404,
        REQUEST_TIME_OUT
    }

    public static final int CREATED         = 201;
    public static final int AUTHENTICATED   = 202;
    public static final int BAD_REQUEST     = 400;
    public static final int UNAUTHORIZED    = 401;
    public static final int NOT_FOUND       = 404;

    private final String ROOT_URL = "http://192.168.3.3:1337";

    private static RVCloudSync instance = new RVCloudSync();
    private RVCloudSyncCallback mCallback;

    public static RVCloudSync getInstance() {
        return instance;
    }

    public void setCallback(@NonNull RVCloudSyncCallback callback) {
        mCallback = callback;
    }

    private RVCloudSync() {


    }

    public void startLogin(String userName, String password) throws RVCloudSyncException{
        if (mCallback == null)
            throw new RVCloudSyncException();

        UserData dataPair = new UserData(userName, password);
        new DoHttpLoginRequest().execute(dataPair);

    }


    public interface RVCloudSyncCallback {

        void onLoginResult(LoginStatusCode code,
                           @Nullable String userName,
                           @Nullable String password);

    }

    public class RVCloudSyncException extends Exception {
        private RVCloudSyncException() {
            super("RVCloudSyncCallback not set!");
        }
    }

    private class DoHttpLoginRequest extends AsyncTask<UserData, Void, Void> {

        private boolean isResponded;

        @Override
        protected Void doInBackground(UserData... params) {

            UserData dataPairSent = params[0];
            HttpURLConnection urlConnection = null;
            try {

                URL url = new URL(ROOT_URL + "/users/?user_name=" +dataPairSent.userName + "&password=" + dataPairSent.password);
                urlConnection = (HttpURLConnection) url.openConnection();

                isResponded = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            //
                        }
                        if (!isResponded) {
                            mCallback.onLoginResult(LoginStatusCode.REQUEST_TIME_OUT, null, null);
                        }
                    }
                }).start();

                urlConnection.setDoInput(true);
//                urlConnection.setDoOutput(true);

                int status = urlConnection.getResponseCode();
                isResponded = true;

                InputStream errorStream = urlConnection.getErrorStream();
                if (errorStream == null) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readInputStream(inputStream);

                    JSONObject object = toJSON(s);
                    LoginResult result = new LoginResult(object);
                    String userName = result.userData.userName;
                    String password = result.userData.password;

                    switch (status) {
                        case AUTHENTICATED:

                            mCallback.onLoginResult(LoginStatusCode.AUTHENTICATED_202, userName, password);
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
                    LoginResult result = new LoginResult(object);
                    String userName = result.userData.userName;
                    String password = result.userData.password;

                    switch (status) {
                        case UNAUTHORIZED:
                            mCallback.onLoginResult(LoginStatusCode.UNAUTHORIZED_401, userName, null);
                            break;

                        case NOT_FOUND:
                            mCallback.onLoginResult(LoginStatusCode.NOT_FOUND_404, userName, null);
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        private String readInputStream(InputStream inputStream) throws IOException{
            // TODO: 2017/05/11 バッファサイズを検証。不具合はないか
            byte[] buffer = new byte[1024];
            inputStream.read(buffer);
            inputStream.close();

            return new String(buffer);
        }


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

    private class UserData {
        String userName, password;

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

    private class LoginResult {
        UserData userData;
        String state;

        private LoginResult(JSONObject object) {
            try {
                if (object.has(USER_DATA))
                    this.userData = new UserData(object.getJSONObject(USER_DATA));
                if (object.has(LOGIN_STATE))
                    this.state = object.getString(LOGIN_STATE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // DONE: 2017/05/11 401 UNAUTHORIZED
    // DONE: 2017/05/11 ユーザの作成を提案
    // DONE: 2017/05/11 400 BAD REQUEST エラー内容で切り分け　バックエンド側

}
