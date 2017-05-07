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
import android.view.View;
import android.view.ViewGroup;
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
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(TimeCountService.TIME_COUNTING_ACTION_TO_ACTIVITY)) {

                updateDurationText(intent);
                updateEndTimeText();

            } else if (intent.getAction().equals(TimeCountService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY)) {

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

        refreshEndTimeText();
        updateEndTimeText();
    }

    private void refreshEndTimeText() {
        if (TimeCountService.isTimeCounting() && TimeCountService.getWork() != null ) {
            if (TimeCountService.getWork().equals(mWork)){
                endTimeText.setBackground(null);
                endTimeText.setClickable(false);
                ViewUtil.setOnClickListener(endTimeText, null);
            }
        } else {
            endTimeText.setBackgroundResource(R.drawable.white_trans_circle);
            ViewUtil.setOnClickListener(endTimeText, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick() {
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

            long duration = intent.getLongExtra(TimeCountService.DURATION, 0);
            String durationString = DateTimeText.getDurationString(duration, true);

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

        for (Visit visit : RVData.getInstance().visitList.getVisitsInWork(mWork)) {
            visitCellContainer.addView(generateVisitCell(visit, visitCellInitHeight));
        }
    }

    public void insertVisitCellToProperPosition(Visit visit) {

        // 挿入ポジションを特定
        // TODO: 2017/04/15 挿入ポジションが微妙に残念な件
        int pos = getProperPositionOfVisit(visit);
        visitCellContainer.addView(generateVisitCell(visit, 0), pos);

    }

    private int getProperPositionOfVisit(Visit visit) {

        ArrayList<Visit> visitsInWork = RVData.getInstance().visitList.getVisitsInWork(mWork);

        Collections.sort(visitsInWork, new Comparator<Visit>() {
            @Override
            public int compare(Visit o1, Visit o2) {
                // TODO: 2017/04/01 ソートの順番が正しいか検証
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
        VisitCell visitCell = new VisitCell(getContext(), visit, initHeight,new VisitCell.VisitCellListener() {
            @Override
            public void onDeleteVisit(final VisitCell visitCell1) {
                visitCell1.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        RVData.getInstance().visitList.deleteById(visitCell1.getVisit().getId());
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

    private void removeVisitCell(Visit visit, final PostRemoveVisitCellListener postRemoveVisitCellListener) {

        final VisitCell visitCell = getVisitCell(visit.getId());
        if (visitCell == null) return;

        visitCell.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                visitCellContainer.removeView(visitCell);
                if (postRemoveVisitCellListener != null) {
                    postRemoveVisitCellListener.postRemoveVisitCell();
                }
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

    public boolean hasVisitCell(Visit visit) {
        VisitCell visitCell = getVisitCell(visit.getId());
        return visitCell != null;
    }

    private void postChangeWorkTime() {

        ArrayList<Visit> renewedVisits = RVData.getInstance().visitList.getVisitsInWork(mWork);

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


    public void compress(Animator.AnimatorListener listener) {
        this.changeViewHeight(0, true, null, listener);
    }

    public interface WorkViewListener {

        void onChangeTime(Work work, ArrayList<Visit> visitsAdded, ArrayList<Visit> visitsRemoved);

        void onDeleteWork(WorkView workView);

        void onClickEditVisit(Visit visit);

        void onClickToMap(Visit visit);
    }

    public interface PostRemoveVisitCellListener {
        void postRemoveVisitCell();
    }

    // DONE: 2017/05/07 時間調整時にDurationが変わらない。



}
