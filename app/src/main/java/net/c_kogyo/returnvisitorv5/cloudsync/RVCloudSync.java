package net.c_kogyo.returnvisitorv5.cloudsync;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

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

        try {
            URL url = new URL(ROOT_URL + "/users/?user_name=" + userId + "&password=" + password);
            new DoHttpRequest().execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }



    public interface RVCloudSyncCallback {

        void onSuccessLogin(String userId, String password);

    }

    public class RVCloudSyncException extends Exception {
        private RVCloudSyncException() {
            super("RVCloudSyncCallback not set!");
        }
    }

    class DoHttpRequest extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) params[0].openConnection();

                urlConnection.setDoInput(true);
//                urlConnection.setDoOutput(true);

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                return readInputStream(inputStream);

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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
