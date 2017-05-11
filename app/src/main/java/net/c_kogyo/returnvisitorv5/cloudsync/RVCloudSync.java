package net.c_kogyo.returnvisitorv5.cloudsync;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync {

    private final String USER_ID = "user_id";
    private final String PASSWORD = "password";

    public enum LoginStatusCode {
        LOGIN_SUCCESS,
        LOGIN_FAILED
    }

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

    public void inquireLogin(String userId, String password) throws RVCloudSyncException{
        if (mCallback == null)
            throw new RVCloudSyncException();

        UserDataPair dataPair = new UserDataPair(userId, password);
        new DoHttpLoginRequest().execute(dataPair);

    }



    public interface RVCloudSyncCallback {

        void onLoginResponse(LoginStatusCode code, String userId, String password);

    }

    public class RVCloudSyncException extends Exception {
        private RVCloudSyncException() {
            super("RVCloudSyncCallback not set!");
        }
    }

    class DoHttpLoginRequest extends AsyncTask<UserDataPair, Void, Void> {

        @Override
        protected Void doInBackground(UserDataPair... params) {

            UserDataPair dataPairSent = params[0];
            HttpURLConnection urlConnection = null;
            try {

                URL url = new URL(ROOT_URL + "/users/?user_name=" +dataPairSent.userId + "&password=" + dataPairSent.password);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
//                urlConnection.setDoOutput(true);

                int status = urlConnection.getResponseCode();
                if (status == 200) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readInputStream(inputStream);
                    JSONObject object = toJSON(s);
                    UserDataPair dataPairReturned = new UserDataPair(object);
                    if (mCallback != null) {
                        mCallback.onLoginResponse(LoginStatusCode.LOGIN_SUCCESS, dataPairReturned.userId, dataPairReturned.password);
                    }

                } else if (status == 404) {

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

    private class UserDataPair {
        String userId, password;

        private UserDataPair(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }

        private UserDataPair(JSONObject object) {

            try {
                if (object.has(USER_ID))
                    this.userId = object.getString(USER_ID);
                if (object.has(PASSWORD))
                    this.password = object.getString(PASSWORD);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
