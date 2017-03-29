package net.c_kogyo.returnvisitorv5.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import net.c_kogyo.returnvisitorv5.activity.MapActivity;

/**
 * Created by SeijiShii on 2016/01/06.
 */
public class IntentCatcherDummyService extends Service {

    public static final String DUMMY_SERVICE_LAUNCH_INTENT = IntentCatcherDummyService.class.getName() + "_launch_dummy_service";
    public static final String TAG = IntentCatcherDummyService.class.getCanonicalName() + "_TAG";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Intent Catcher Dummy Service Started!");

        if (!MapActivity.isInForeground()) {
            Intent maIntent = new Intent(this, MapActivity.class);
            maIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(maIntent);
        }

        Log.i(TAG, "Intent Catcher Dummy Service Stopped!");
        stopSelf();

        return Service.START_NOT_STICKY;
    }
}
