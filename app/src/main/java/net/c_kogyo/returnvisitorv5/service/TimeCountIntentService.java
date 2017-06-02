package net.c_kogyo.returnvisitorv5.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

import java.util.Calendar;

import static android.content.Intent.ACTION_DELETE;

/**
 * Created by SeijiShii on 2017/05/29.
 */

public class TimeCountIntentService extends IntentService {

    private static final int TIME_NOTIFY_ID = 100;

    private static boolean timeCounting;
    private static Work mWork;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver;

    public static final String START_COUNTING_ACTION_TO_SERVICE
            = TimeCountIntentService.class.getName() + "_start_counting_action_to_service";
    public static final String RESTART_COUNTING_ACTION_TO_SERVICE
            = TimeCountIntentService.class.getName() + "_restart_counting_action_to_service";
    public static final String TIME_COUNTING_ACTION_TO_ACTIVITY
            = TimeCountIntentService.class.getName() + "_time_counting_action_to_activity";
    public static final String STOP_TIME_COUNT_ACTION_TO_ACTIVITY
            = TimeCountIntentService.class.getName() + "_stop_time_count_action_to_activity";
    public static final String START_TIME
            = TimeCountIntentService.class.getName() + "_start_time";
    public static final String DURATION
            = TimeCountIntentService.class.getName() + "_duration";
    public static final String COUNTING_WORK_ID
            = TimeCountIntentService.class.getName() + "_counting_work_id";
    public static final String CHANGE_START_ACTION_TO_SERVICE
            = TimeCountIntentService.class.getName() + "_change_start_action_to_service";

    public TimeCountIntentService() {
        super("TimeCountIntentService");

        initBroadcasting();

    }

    private void initBroadcasting() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CHANGE_START_ACTION_TO_SERVICE)) {

                    long startTime = intent.getLongExtra(START_TIME, mWork.getStart().getTimeInMillis());
                    mWork.getStart().setTimeInMillis(startTime);

                    RVData.getInstance().workList.setOrAdd(mWork);
                    RVData.getInstance().saveData(TimeCountIntentService.this);

                    RVCloudSync.syncDataIfLoggedIn(TimeCountIntentService.this);
                }
            }
        };

        broadcastManager.registerReceiver(receiver, new IntentFilter(CHANGE_START_ACTION_TO_SERVICE));
    }

    @Nullable
    public static Work getWork() {
        return mWork;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {

            if (intent.getAction().equals(START_COUNTING_ACTION_TO_SERVICE)) {
                mWork = new Work(Calendar.getInstance());
                RVData.getInstance().workList.setOrAdd(mWork);
                RVData.getInstance().saveData(this);
                RVCloudSync.syncDataIfLoggedIn(this);
            } else if (intent.getAction().equals(RESTART_COUNTING_ACTION_TO_SERVICE)) {
                String workId = intent.getStringExtra(COUNTING_WORK_ID);
                mWork = RVData.getInstance().workList.getById(workId);
                if (mWork == null) {
                    stopTimeCount();
                    return;
                }
            }
        }

        timeCounting = true;
        int minCounter = 0;

        initNotification(mWork.getDuration());

        while (timeCounting) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //
            }

            if (mWork != null) {
                final Intent timeBroadCastIntent = new Intent();
                timeBroadCastIntent.setAction(TIME_COUNTING_ACTION_TO_ACTIVITY);
                timeBroadCastIntent.putExtra(START_TIME, mWork.getStart().getTimeInMillis());
                timeBroadCastIntent.putExtra(DURATION, mWork.getDuration());
                timeBroadCastIntent.putExtra(COUNTING_WORK_ID, mWork.getId());
                mWork.setEnd(Calendar.getInstance());
                broadcastManager.sendBroadcast(timeBroadCastIntent);
                updateNotification(mWork.getDuration());

                // 約1分ごとに保存するようにする
                minCounter++;
                if (minCounter > 50) {

                    mWork.setEnd(Calendar.getInstance());

                    RVData.getInstance().workList.setOrAdd(mWork);
                    RVData.getInstance().saveData(this);
                    RVCloudSync.syncDataIfLoggedIn(this);
                    minCounter = 0;
                }
            }
        }

        if (notificationManager != null) {
            notificationManager.cancel(TIME_NOTIFY_ID);
        }

        mWork = null;

        Intent stopIntent = new Intent(STOP_TIME_COUNT_ACTION_TO_ACTIVITY);
        broadcastManager.sendBroadcast(stopIntent);

    }


    public static boolean isTimeCounting() {
        return timeCounting;
    }

    public static void stopTimeCount() {
        timeCounting = false;
    }

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    private void initNotification(long duration) {

        String durationText = getString(R.string.duration_string, DateTimeText.getDurationString(duration, true));

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.white_rv_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(durationText);


        Intent dummyIntent = new Intent(this, IntentCatcherDummyService.class);
        PendingIntent dummyPendingIntent = PendingIntent.getService(this, 0, dummyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(dummyPendingIntent);

        Intent deleteIntent = new Intent(this, TimeCountIntentService.class);
        deleteIntent.setAction(ACTION_DELETE);
        PendingIntent deletePendingIntent = PendingIntent.getService(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setDeleteIntent(deletePendingIntent);

        // キャンセルできないようにする
        mBuilder.setOngoing(true);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(TIME_NOTIFY_ID, mBuilder.build());
    }

    private void updateNotification(long duration) {

        String durationText = getString(R.string.duration_string, DateTimeText.getDurationString(duration, true));
        mBuilder.setContentText(durationText);

        // キャンセルできないようにする
        mBuilder.setOngoing(true);

        notificationManager.notify(TIME_NOTIFY_ID, mBuilder.build());
    }
}
