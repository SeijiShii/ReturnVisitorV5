package net.c_kogyo.returnvisitorv5.cloudsync;

import android.support.annotation.NonNull;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class RVCloudSync {

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
    }

    public interface RVCloudSyncCallback {

        void onSuccessLogin(String userId, String password);

    }

    public class RVCloudSyncException extends Exception {
        private RVCloudSyncException() {
            super("RVCloudSyncCallback not set!");
        }
    }
}
