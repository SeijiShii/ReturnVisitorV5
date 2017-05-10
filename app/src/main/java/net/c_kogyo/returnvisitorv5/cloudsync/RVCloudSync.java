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

    public void inquireLogin(String userId, String password) {

    }



    public interface RVCloudSyncCallback {

        void onSuccessLogin();

    }
}
