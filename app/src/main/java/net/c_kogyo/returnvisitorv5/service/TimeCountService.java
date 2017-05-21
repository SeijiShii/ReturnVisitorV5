package net.c_kogyo.returnvisitorv5.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
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
 * Created by SeijiShii on 2016/08/13.
 */

public class TimeCountService extends Service {

    private static final int TIME_NOTIFY_ID = 100;

    private static boolean timeCounting;
    private static Work mWork;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver;

    public static final String START_COUNTING_ACTION_TO_SERVICE
            = TimeCountService.class.getName() + "_start_counting_action_to_service";
    public static final String RESTART_COUNTING_ACTION_TO_SERVICE
            = TimeCountService.class.getName() + "_restart_counting_action_to_service";
    public static final String TIME_COUNTING_ACTION_TO_ACTIVITY
            = TimeCountService.class.getName() + "_time_counting_action_to_activity";
    public static final String STOP_TIME_COUNT_ACTION_TO_ACTIVITY
            = TimeCountService.class.getName() + "_stop_time_count_action_to_activity";
    public static final String START_TIME
            = TimeCountService.class.getName() + "_start_time";
    public static final String DURATION
            = TimeCountService.class.getName() + "_duration";
    public static final String COUNTING_WORK_ID
            = TimeCountService.class.getName() + "_counting_work_id";
    public static final String CHANGE_START_ACTION_TO_SERVICE
            = TimeCountService.class.getName() + "_change_start_action_to_service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Nullable
    public static Work getWork() {
        return mWork;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CHANGE_START_ACTION_TO_SERVICE)) {

                    long startTime = intent.getLongExtra(START_TIME, mWork.getStart().getTimeInMillis());
                    mWork.getStart().setTimeInMillis(startTime);

                    RVData.getInstance().workList.setOrAdd(mWork);
                    RVData.getInstance().saveData(getApplicationContext());

                    RVCloudSync.syncDataIfLoggedIn(getApplicationContext());
                }
            }
        };

        broadcastManager.registerReceiver(receiver, new IntentFilter(CHANGE_START_ACTION_TO_SERVICE));
    }

//    private long duration;
    private int minCounter;
    private Handler countStopHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        timeCounting = true;
        minCounter = 0;
        countStopHandler = new Handler();

        if (intent.getAction().equals(START_COUNTING_ACTION_TO_SERVICE)) {
            mWork = new Work(Calendar.getInstance());
            RVData.getInstance().workList.setOrAdd(mWork);
            RVData.getInstance().saveData(getApplicationContext());

            RVCloudSync.syncDataIfLoggedIn(getApplicationContext());
        } else if (intent.getAction().equals(RESTART_COUNTING_ACTION_TO_SERVICE)) {
            String workId = intent.getStringExtra(COUNTING_WORK_ID);
            mWork = RVData.getInstance().workList.getById(workId);
            if (mWork == null) {
                stopTimeCount();
                return START_NOT_STICKY;
            }
        }

        initNotification(mWork.getDuration());

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            RVData.getInstance().saveData(getApplicationContext());
                            RVCloudSync.syncDataIfLoggedIn(getApplicationContext());
                            minCounter = 0;
                        }
                    }
                }

                countStopHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TimeCountService.this, R.string.time_stop, Toast.LENGTH_SHORT).show();
                    }
                });

                if (notificationManager != null) {
                    notificationManager.cancel(TIME_NOTIFY_ID);
                }

                mWork = null;

                Intent stopIntent = new Intent(STOP_TIME_COUNT_ACTION_TO_ACTIVITY);
                broadcastManager.sendBroadcast(stopIntent);

                stopSelf();

            }
        }).start();

        // システムの都合で強制終了されたとき再起動しないようにする
        //　明示的にコントロールするため
        return START_NOT_STICKY;
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

        Intent deleteIntent = new Intent(this, TimeCountService.class);
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

    // エラーで停止した後通知が消えない
    // DONE: TimeCountがシステム側の都合で停止したときで再開できる仕組みにする

}
