package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.service.TimeCountService;
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
    private ArrayList<Visit> visitsInWork;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(TimeCountService.TIME_COUNTING_ACTION_TO_ACTIVITY)) {

                updateDurationText(intent);

            } else if (intent.getAction().equals(TimeCountService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY)) {

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

        broadcastManager.registerReceiver(receiver, new IntentFilter(TimeCountService.TIME_COUNTING_ACTION_TO_ACTIVITY));
        broadcastManager.registerReceiver(receiver, new IntentFilter(TimeCountService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY));

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
            public void onViewClick() {
                showTimePickerDialog(mWork.getStart());
            }
        });
        updateStartTimeText();
    }

    private void showTimePickerDialog(final Calendar time) {
        new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute);
                        updateStartTimeText();
                        updateEndTimeText();
                        postChangeWorkTime();
                    }
                },
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                true).show();
    }

    private void updateStartTimeText() {

        String startString = DateTimeText.getTimeText(mWork.getStart());
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
            public void onViewClick() {
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
                        });
                        return true;
                    case STOP_COUNT_MENU_ID:
                        TimeCountService.stopTimeCount();
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private boolean isWorkCountingTime() {

        if (!TimeCountService.isTimeCounting())
            return false;

        Work work = TimeCountService.getWork();
        if (work == null)
            return false;

        return mWork.equals(work);
    }

    private TextView endTimeText;
    private void initEndTimeText() {
        endTimeText = (TextView) getViewById(R.id.end_time_text);
        ViewUtil.setOnClickListener(endTimeText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                showTimePickerDialog(mWork.getEnd());
            }
        });
        refreshEndTimeText();
        updateEndTimeText();
    }

    private void refreshEndTimeText() {
        if (TimeCountService.isTimeCounting() && TimeCountService.getWork() != null ) {
            if (TimeCountService.getWork().equals(mWork)){
                endTimeText.setBackground(null);
                endTimeText.setClickable(false);
            }
        } else {
            endTimeText.setBackgroundResource(R.drawable.white_trans_circle);
            endTimeText.setClickable(true);
        }
    }

    private void updateEndTimeText() {

        String endString = DateTimeText.getTimeText(mWork.getEnd());
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

            long duration = intent.getLongExtra(TimeCountService.DURATION, 0);
            String durationString = DateTimeText.getDurationString(duration, true);

            durationText.setText(durationString);

        } else {

            String timeString = DateTimeText.getDurationString(mWork.getDuration(), true);
            timeString = mContext.getString(R.string.duration_text, timeString);
            durationText.setText(timeString);
        }

    }

    private LinearLayout visitCellContainer;
    private void initVisitCellContainer() {

        visitCellContainer = (LinearLayout) getViewById(R.id.visit_cell_container);

        visitsInWork = RVData.getInstance().visitList.getVisitsInWork(mWork);

        for (Visit visit : visitsInWork) {
            visitCellContainer.addView(generateVisitCell(visit, visitCellInitHeight));
        }
    }

    public void addVisitCell(Visit visit) {

        visitsInWork.add(visit);
        Collections.sort(visitsInWork, new Comparator<Visit>() {
            @Override
            public int compare(Visit o1, Visit o2) {
                // TODO: 2017/04/01 ソートの順番が正しいか検証
                return o1.getDatetime().compareTo(o2.getDatetime());
            }
        });

        // 挿入ポジションを特定
        int pos = visitsInWork.indexOf(visit);
        visitCellContainer.addView(generateVisitCell(visit, 0), pos);

    }

    private VisitCell generateVisitCell(Visit visit, int initHeight) {
        VisitCell visitCell = new VisitCell(getContext(), visit, initHeight,new VisitCell.VisitCellListener() {
            @Override
            public void onDeleteVisit(final VisitCell visitCell1) {
                visitCell1.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        RVData.getInstance().visitList.removeById(visitCell1.getVisit().getId());
                        RVData.getInstance().saveData(getContext(), null);
                        visitCellContainer.removeView(visitCell1);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

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

        }, VisitCell.HeaderContent.BOTH)
        {
            @Override
            public void setLayoutParams(BaseAnimateView view) {
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            }
        };
        visitCell.setHeightUpdateListener(new HeightUpdateListener() {
            @Override
            public void onUpdate() {
                WorkView.this.requestLayout();
            }
        });
        return visitCell;
    }

    public Work getWork() {
        return mWork;
    }

    private void removeVisitCell(Visit visit) {

        final VisitCell visitCell = getVisitCell(visit.getId());
        if (visitCell == null) return;

        visitCell.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                visitCellContainer.removeView(visitCell);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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

    private void postChangeWorkTime() {

        ArrayList<Visit> oldVisits = new ArrayList<>(visitsInWork);
        visitsInWork = RVData.getInstance().visitList.getVisitsInWork(mWork);

        ArrayList<Visit> visitsAdded = new ArrayList<>(visitsInWork);
        visitsAdded.removeAll(oldVisits);

        ArrayList<Visit> visitsRemoved = new ArrayList<>(oldVisits);
        visitsRemoved.removeAll(visitsInWork);

        for (Visit visit : visitsAdded) {
            addVisitCell(visit);
        }

        for (Visit visit : visitsRemoved) {
            removeVisitCell(visit);
        }

        if (mListener != null) {
            mListener.onChangeTime(mWork, visitsAdded, visitsRemoved);
        }
    }

    public void toTheHeight() {

    }

    public void compress(Animator.AnimatorListener listener) {
        this.changeViewHeight(0, true, null, listener);
    }

    public interface WorkViewListener {

        void onChangeTime(Work work, ArrayList<Visit> visitsAdded, ArrayList<Visit> visitsRemoved);

        void onDeleteWork(WorkView workView);

        void onClickEditVisit(Visit visit);

        void onClickToMap(Visit visit);
    }



}
