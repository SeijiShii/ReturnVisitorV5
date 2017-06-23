package net.c_kogyo.returnvisitorv5.view;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.service.TimeCountIntentService;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2016/09/11.
 */

public class WorkView extends BaseAnimateView {

    private static final String WORK_VIEW_TEST_TAG = "WorkViewTest";

    private Context mContext;
    private Work mWork;
    private WorkViewListener mListener;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(TimeCountIntentService.TIME_COUNTING_ACTION_TO_ACTIVITY)) {

                updateDurationText(intent);
                updateEndTimeText();

            } else if (intent.getAction().equals(TimeCountIntentService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY)) {

                updateEndTimeText();
                refreshEndTimeText();
            }
        }
    };

    int visitCellInitHeight;

    public WorkView(Work work,
                    Context context,
                    int initHeight,
                    WorkViewListener listener) {
        super(context, initHeight, R.layout.work_view);

        mContext = context;
        mWork = work;
        mListener = listener;

        broadcastManager = LocalBroadcastManager.getInstance(mContext);

        broadcastManager.registerReceiver(receiver, new IntentFilter(TimeCountIntentService.TIME_COUNTING_ACTION_TO_ACTIVITY));
        broadcastManager.registerReceiver(receiver, new IntentFilter(TimeCountIntentService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY));

        visitCellInitHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                + (int)(getContext().getResources().getDisplayMetrics().density * 5);

        initStartTimeText();
        initMenuButton();
        initEndTimeText();

        initDurationText();

        initVisitCellContainer();
    }

    @Override
    public void setLayoutParams(BaseAnimateView view){
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    private TextView startTimeText;
    private void initStartTimeText() {
        startTimeText = (TextView) getViewById(R.id.start_time_text);
        ViewUtil.setOnClickListener(startTimeText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                showStartTimePickerDialog(mWork.getStart());
            }
        });
        updateStartTimeText();
    }

    private void showStartTimePickerDialog(final Calendar start) {

        new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Calendar testTime = Calendar.getInstance();

                        testTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        testTime.set(Calendar.MINUTE, minute);
                        // DONE: 2017/04/14 Validate start and end
                        if (testTime.after(mWork.getEnd())) {
                            return;
                        }

                        start.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        start.set(Calendar.MINUTE, minute);

                        WorkList.getInstance().setOrAdd(mWork);
                        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getContext());

                        updateStartTimeText();
                        updateEndTimeText();
                        updateDurationText(null);
                        postChangeWorkTime();
                    }
                },
                start.get(Calendar.HOUR_OF_DAY),
                start.get(Calendar.MINUTE),
                true).show();
    }

    private void showEndTimePickerDialog(final Calendar end) {

        new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Calendar testTime = Calendar.getInstance();

                        testTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        testTime.set(Calendar.MINUTE, minute);
                        // DONE: 2017/04/14 Validate start and end
                        if (testTime.before(mWork.getStart())) {
                            return;
                        }

                        end.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        end.set(Calendar.MINUTE, minute);

                        WorkList.getInstance().setOrAdd(mWork);
                        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getContext());

                        updateStartTimeText();
                        updateEndTimeText();
                        updateDurationText(null);
                        postChangeWorkTime();
                    }
                },
                end.get(Calendar.HOUR_OF_DAY),
                end.get(Calendar.MINUTE),
                true).show();
    }

    private void updateStartTimeText() {

        String startString = DateTimeText.getTimeText(mWork.getStart(), false);
        startString = mContext.getString(R.string.start_time_text, startString);

        startTimeText.setText(startString);
    }

    private ImageView menuButton;
    private PopupMenu popupMenu;
    final int STOP_COUNT_MENU_ID = 100;
    private void initMenuButton() {

        menuButton = (ImageView) getViewById(R.id.work_view_menu_button);
        ViewUtil.setOnClickListener(menuButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                onClickMenuButton();
            }
        });
    }

    private void onClickMenuButton() {
        popupMenu = new PopupMenu(getContext(), menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.work_view_menu, popupMenu.getMenu());
        if (isWorkCountingTime()) {
            popupMenu.getMenu().add(Menu.NONE, STOP_COUNT_MENU_ID, Menu.NONE, R.string.stop_time_count);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.delete:
                        ConfirmDialog.confirmAndDeleteWork(getContext(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mListener != null) {
                                    mListener.onDeleteWork(WorkView.this);
                                }
                            }
                        }, mWork);
                        return true;
                    case STOP_COUNT_MENU_ID:
                        TimeCountIntentService.stopTimeCount();
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private boolean isWorkCountingTime() {

        if (!TimeCountIntentService.isTimeCounting())
            return false;

        Work work = TimeCountIntentService.getWork();
        if (work == null)
            return false;

        return mWork.equals(work);
    }

    private TextView endTimeText;
    private void initEndTimeText() {
        endTimeText = (TextView) getViewById(R.id.end_time_text);

        refreshEndTimeText();
        updateEndTimeText();
    }

    private void refreshEndTimeText() {
        if (TimeCountIntentService.isTimeCounting() && TimeCountIntentService.getWork() != null ) {
            if (TimeCountIntentService.getWork().equals(mWork)){
                endTimeText.setBackground(null);
                endTimeText.setClickable(false);
                ViewUtil.setOnClickListener(endTimeText, null);
            } else {
                endTimeText.setBackgroundResource(R.drawable.white_trans_circle);
                ViewUtil.setOnClickListener(endTimeText, new ViewUtil.OnViewClickListener() {
                    @Override
                    public void onViewClick(View v) {
                        showEndTimePickerDialog(mWork.getEnd());
                    }
                });
            }
        } else {
            endTimeText.setBackgroundResource(R.drawable.white_trans_circle);
            ViewUtil.setOnClickListener(endTimeText, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View v) {
                    showEndTimePickerDialog(mWork.getEnd());
                }
            });
        }
    }

    private void updateEndTimeText() {

        String endString;
        if (isWorkCountingTime()) {
            endString = DateTimeText.getTimeText(mWork.getEnd(), true);
        } else {
            endString = DateTimeText.getTimeText(mWork.getEnd(), false);
        }

        endString = mContext.getString(R.string.end_time_string, endString);
        endTimeText.setText(endString);
    }

    private TextView durationText;
    private void initDurationText() {

        durationText = (TextView) getViewById(R.id.duration_text);
        updateDurationText(null);
    }

    private void updateDurationText(@Nullable Intent intent) {

        if (isWorkCountingTime() && intent != null) {

            long duration = intent.getLongExtra(TimeCountIntentService.DURATION, 0);
            String durationString = DateTimeText.getDurationString(duration, true);
            durationString = mContext.getString(R.string.duration_string, durationString);
            durationText.setText(durationString);

        } else {

            String timeString = DateTimeText.getDurationString(mWork.getDuration(), false);
            timeString = mContext.getString(R.string.duration_string, timeString);
            durationText.setText(timeString);
        }

    }

    private LinearLayout visitCellContainer;
    private void initVisitCellContainer() {

        visitCellContainer = (LinearLayout) getViewById(R.id.visit_cell_container);

        for (Visit visit : VisitList.getInstance().getVisitsInWork(mWork)) {
            visitCellContainer.addView(generateVisitCell(visit, visitCellInitHeight));
        }
    }

    public void insertVisitCellToProperPosition(Visit visit) {

        // 挿入ポジションを特定
        // DONE: 2017/04/15 挿入ポジションが微妙に残念な件
        int pos = getProperPositionOfVisit(visit);
        final VisitCell cell = generateVisitCell(visit, 0);
        visitCellContainer.addView(cell, pos);

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (cell.getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(mListener != null) {
                                // TODO: 2017/06/03 WorkViewの終わり時間を変更するとこのあたりでヌルポが出るよ。
                                mListener.postAddVisitCell(cell);
                            }
                        }
                    });
                }
            }
        }).start();

    }

    private int getProperPositionOfVisit(Visit visit) {

        ArrayList<Visit> visitsInWork = VisitList.getInstance().getVisitsInWork(mWork);

        Collections.sort(visitsInWork, new Comparator<Visit>() {
            @Override
            public int compare(Visit o1, Visit o2) {
                // DONE: 2017/04/01 ソートの順番が正しいか検証
                return o1.getDatetime().compareTo(o2.getDatetime());
            }
        });

        // 挿入ポジションを特定

        for ( int i = 0 ; i < visitsInWork.size() ; i++ ) {
            if (visitsInWork.get(i).equals(visit)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isVisitCellInProperPosition(Visit visit) {

        int propPos = getProperPositionOfVisit(visit);

        for (int i = 0 ; i < visitCellContainer.getChildCount() ; i++ ) {

            VisitCell visitCell1 = (VisitCell) visitCellContainer.getChildAt(i);
            if (visitCell1.getVisit().equals(visit)) {
                if (propPos == i) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param visit
     * @return If removed visit cell
     */
    public boolean removeVisitCellIfNotInProperPosition(Visit visit, PostRemoveVisitCellListener postRemoveVisitCellListener) {
        if (isVisitCellInProperPosition(visit))
            return false;
        removeVisitCell(visit, postRemoveVisitCellListener);
        return true;
    }

    private VisitCell generateVisitCell(Visit visit, int initHeight) {

        return new VisitCell(getContext(), visit, initHeight, new VisitCell.VisitCellListener() {
            @Override
            public void postCompressVisitCell(VisitCell visitCell) {
                VisitList.getInstance().deleteById(visitCell.getVisit().getId());
                visitCellContainer.removeView(visitCell);

                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getContext());
            }

            @Override
            public void onEditClick(Visit visit) {
                if (mListener != null) {
                    mListener.onClickEditVisit(visit);
                }
            }

            @Override
            public void onClickToMap(Visit visit) {
                if (mListener != null) {
                    mListener.onClickToMap(visit);
                }
            }

            @Override
            public void onUpdateHeight() {
                WorkView.this.requestLayout();
            }
        }, VisitCell.HeaderContent.BOTH) {
            @Override
            public void setLayoutParams(BaseAnimateView view) {
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        };
    }

    public Work getWork() {
        return mWork;
    }

    private void removeVisitCell(Visit visit, final PostRemoveVisitCellListener postRemoveVisitCellListener) {

        final VisitCell visitCell = getVisitCell(visit.getId());
        if (visitCell == null) return;

        visitCell.compress(new PostAnimationListener() {
            @Override
            public void postAnimate(BaseAnimateView view) {
                visitCellContainer.removeView(visitCell);
                if (postRemoveVisitCellListener != null) {
                    postRemoveVisitCellListener.postRemoveVisitCell();
                }
            }
        });
    }

    @Nullable
    public VisitCell getVisitCell(String visitId) {

        for ( int i = 0 ; i < visitCellContainer.getChildCount() ; i++ ) {
            View view = visitCellContainer.getChildAt(i);
            if (view instanceof  VisitCell) {
                VisitCell visitCell = (VisitCell) view;
                if (visitCell.getVisit().getId().equals(visitId)) {
                    return visitCell;
                }
            }
        }
        return null;
    }

    public boolean hasVisitCell(Visit visit) {
        VisitCell visitCell = getVisitCell(visit.getId());
        return visitCell != null;
    }

    private void postChangeWorkTime() {

        ArrayList<Visit> renewedVisits = VisitList.getInstance().getVisitsInWork(mWork);

        ArrayList<Visit> oldVisits = getVisitsInContainer();

        ArrayList<Visit> visitsAdded = new ArrayList<>(renewedVisits);
        visitsAdded.removeAll(oldVisits);

        ArrayList<Visit> visitsRemoved = new ArrayList<>(oldVisits);
        visitsRemoved.removeAll(renewedVisits);

        for (Visit visit : visitsAdded) {
            insertVisitCellToProperPosition(visit);
        }

        for (Visit visit : visitsRemoved) {
            removeVisitCell(visit, null);
        }

        if (mListener != null) {
            mListener.onChangeTime(mWork, visitsAdded, visitsRemoved);
        }
    }

    private ArrayList<Visit> getVisitsInContainer() {
        ArrayList<Visit> visits = new ArrayList<>();
        for (int i = 0 ; i < visitCellContainer.getChildCount() ; i++) {
            View view = visitCellContainer.getChildAt(i);
            if (view instanceof  VisitCell) {
                VisitCell visitCell = (VisitCell) view;
                visits.add(visitCell.getVisit());
            }
        }
        return visits;
    }

    public interface WorkViewListener {

        void onChangeTime(Work work, ArrayList<Visit> visitsAdded, ArrayList<Visit> visitsRemoved);

        void onDeleteWork(WorkView workView);

        void onClickEditVisit(Visit visit);

        void onClickToMap(Visit visit);

        void postAddVisitCell(VisitCell cell);
    }

    public interface PostRemoveVisitCellListener {
        void postRemoveVisitCell();
    }

    // DONE: 2017/05/07 時間調整時にDurationが変わらない。

}
