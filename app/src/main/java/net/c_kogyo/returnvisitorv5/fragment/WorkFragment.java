package net.c_kogyo.returnvisitorv5.fragment;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.activity.RecordVisitActivity;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
import net.c_kogyo.returnvisitorv5.view.VisitCell;
import net.c_kogyo.returnvisitorv5.view.WorkView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2016/09/17.
 */

public class WorkFragment extends Fragment {

    private Calendar mDate;
    private ArrayList<Work> worksInDay;
    private ArrayList<Visit> visitsInDayNotInWork;
    private static WorkFragmentListener mWorkFragmentListener;
    private int mVisitCellHeight;

    public static WorkFragment newInstance(Calendar date, WorkFragmentListener workFragmentListener) {

        mWorkFragmentListener = workFragmentListener;

        WorkFragment workFragment = new WorkFragment();

        Bundle arg = new Bundle();
        Intent intent = new Intent();
        intent.putExtra(Constants.DATE_LONG, date.getTimeInMillis());
        arg.putParcelable(Constants.DATE_LONG, intent);
        workFragment.setArguments(arg);

        return workFragment;
    }

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setDate();

        view = inflater.inflate(R.layout.work_fragment, container, false);

        mVisitCellHeight = getResources().getDimensionPixelSize(R.dimen.ui_height_45dp);

        worksInDay = RVData.getInstance().workList.getWorksInDay(mDate);
        visitsInDayNotInWork = RVData.getInstance().visitList.getVisitsInDayNotInWork(mDate);

        initContainer();

        return view;
    }

    private void setDate() {

        mDate = Calendar.getInstance();

        Intent intent = getArguments().getParcelable(Constants.DATE_LONG);

        if (intent == null) return;
        Long dLong =  intent.getLongExtra(Constants.DATE_LONG, 0);

        if (dLong == 0) return;
        mDate.setTimeInMillis(dLong);

    }

    private LinearLayout container;
    private void initContainer() {

        container = (LinearLayout) view.findViewById(R.id.container);


        int visitCounter = 0;
        int workCounter = 0;

        while (visitCounter < visitsInDayNotInWork.size() && workCounter < worksInDay.size()) {

            if (visitsInDayNotInWork.get(visitCounter).getDatetime()
                    .before(worksInDay.get(workCounter).getStart())) {

                addVisitCell(visitsInDayNotInWork.get(visitCounter), false);
                visitCounter++;

            } else {

                addWorkView(worksInDay.get(workCounter), false);
                workCounter++;
            }
        }

        if (visitCounter < visitsInDayNotInWork.size()) {

            for (int i = visitCounter ; i < visitsInDayNotInWork.size() ; i++ ) {
                addVisitCell(visitsInDayNotInWork.get(i), false);
            }

        } else if (workCounter < worksInDay.size()) {

            for (int i = workCounter ; i < worksInDay.size() ; i++ ) {
                addWorkView(worksInDay.get(i), false);
            }
        }
    }

    private VisitCell generateVisitCell(Visit visit, boolean fromZero) {

        int initHeight;
        if (fromZero) {
            initHeight = 0;
        } else {
            initHeight = mVisitCellHeight;
        }

        VisitCell cell = new VisitCell(getContext(), visit, initHeight,new VisitCell.VisitCellListener() {
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
                        container.removeView(visitCell1);
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
                // DONE: 2017/04/01 Edit Visit Action
                startRecordVisitForEdit(visit);
            }

            @Override
            public void onClickToMap(Visit visit) {
                // DONE: 2017/04/01 MapActivityへの遷移
                if (mWorkFragmentListener != null) {
                    mWorkFragmentListener.moveToMap(visit);
                }
            }

        }, VisitCell.HeaderContent.BOTH) {
            @Override
            public void setLayoutParams(BaseAnimateView view) {
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            }
        };
        if (fromZero) {
            cell.extractPostDrawn(mVisitCellHeight, null);
        }
        return cell;
    }

    private void addVisitCell(Visit visit, boolean fromZero) {

        VisitCell visitCell = generateVisitCell(visit, fromZero);
        container.addView(visitCell);
    }

    private void addWorkView(Work work, boolean fromZero) {
        WorkView workView = generateWorkView(work, fromZero);
        container.addView(workView);
    }

    private WorkView generateWorkView(Work work, boolean fromZero) {

        int initHeight;
//        int exHeight = mWorkViewHeight + mVisitCellHeight * (RVData.getInstance().visitList.getVisitsInWork(work).size());
        if (fromZero) {
            initHeight = 0;
        } else {
            initHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        WorkView workView = new WorkView(work,
                getContext(),
                initHeight,
                new WorkView.WorkViewListener() {

                    @Override
                    public void onChangeTime(Work work, ArrayList<Visit> visitsAdded, ArrayList<Visit> visitsRemoved) {

                    }


                    @Override
                    public void onDeleteWork(final WorkView workView) {
                        workView.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Work work1 = workView.getWork();
                                RVData.getInstance().workList.removeById(work1.getId());
                                RVData.getInstance().saveData(getContext(), null);
                                container.removeView(workView);
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
                    public void onClickEditVisit(Visit visit) {
                        // DONE: 2017/04/05 Edit Visit Action
                        startRecordVisitForEdit(visit);

                    }

                    @Override
                    public void onClickToMap(Visit visit) {
                        if (mWorkFragmentListener != null) {
                            mWorkFragmentListener.moveToMap(visit);
                        }
                    }
                }) {
                    @Override
                    public void setLayoutParams(BaseAnimateView view) {
                        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    }
                };

        if (fromZero) {
            workView.extractPostDrawn(ViewGroup.LayoutParams.WRAP_CONTENT, null);
        }
        return workView;
    }

    private int getInsertPosition(Calendar time) {

        for ( int i = 0 ; i < container.getChildCount() ; i++ ) {

            Calendar time1 = null;

            View view = container.getChildAt(i);
            if (view instanceof VisitCell) {

                VisitCell visitCell = (VisitCell) view;
                Visit visit = visitCell.getVisit();
                time1 = visit.getDatetime();

            } else if (view instanceof WorkView) {

                WorkView workView = (WorkView) view;
                Work work = workView.getWork();
                time1 = work.getStart();
            }

            if (time1 != null) {
                if (time.before(time1)) {
                    return i;
                }
            }
        }
        return container.getChildCount();
    }

    private void insertVisitCellAndExtract(Visit visit) {

        VisitCell visitCell = generateVisitCell(visit, true);
        //WorkViewまたはcontainerの適切なほうに挿入する
        WorkView workView = getWorkViewOfVisit(visit);
        if (workView != null) {
            workView.addVisitCell(visit);
        } else {
            container.addView(visitCell, getInsertPosition(visit.getDatetime()));
        }
    }

    private void addVisitCells(ArrayList<Visit> visits) {

        for (Visit visit : visits) {
            insertVisitCellAndExtract(visit);
        }
    }

    private void startRecordVisitForEdit(Visit visit) {

        Intent editVisitIntent = new Intent(getActivity(), RecordVisitActivity.class);
        editVisitIntent.setAction(Constants.RecordVisitActions.EDIT_VISIT_ACTION);
        editVisitIntent.putExtra(Visit.VISIT, visit.getId());

        startActivityForResult(editVisitIntent, Constants.RecordVisitActions.EDIT_VISIT_REQUEST_CODE);

    }

    @Nullable
    private Visit getVisit(String visitId) {
        for (Visit visit : visitsInDayNotInWork) {
            if (visit.getId().equals(visitId)) {
                return visit;
            }
        }
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.RecordVisitActions.EDIT_VISIT_REQUEST_CODE) {

            if (resultCode == Constants.RecordVisitActions.DELETE_VISIT_RESULT_CODE) {
                // VisitCellが削除されたとき

                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId == null) return;

                Visit visit = getVisit(visitId);
                if (visit == null) return;

                removeVisitCell(visit);
                visitsInDayNotInWork.remove(visit);

            } else if (resultCode == Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE) {
                // VisitCellの内容が変更されたとき

                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId == null) return;

                Visit visit = RVData.getInstance().visitList.getById(visitId);
                if (visit == null) return;

                VisitCell visitCell = getVisitCell(visitId, true);
                if (visitCell == null) return;

                visitCell.refreshVisit(visit);
                // VisitCellの時間を変化させたときに適正なポジションにあるかどうかをVerifyする必要あり
                // 必要なら位置を変更する
                moveVisitCellIfNeeded(visitCell);

            }

        } else if (requestCode == Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE) {

            if (resultCode == Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE) {

                // 追加されたのはWorkかVisitか
                String workId = data.getStringExtra(Work.WORK);
                if (workId != null) {
                    Work work = RVData.getInstance().workList.getById(workId);
                    if (work != null) {
                        addWorkViewAndExtract(work);
                    }
                }

                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId != null) {
                    Visit visit = RVData.getInstance().visitList.getById(visitId);
                    if (visit != null) {
                        insertVisitCellAndExtract(visit);
                    }
                }
            }
        }
    }

    private VisitCell getVisitCell(String visitId, boolean fromDeep) {

        for ( int i = 0 ; i < container.getChildCount() ; i++ ) {

            View view = container.getChildAt(i);

            if (view instanceof VisitCell) {

                VisitCell visitCell = (VisitCell) view;
                if (visitCell.getVisit().getId().equals(visitId)) {
                    return visitCell;
                }
            } else if (view instanceof WorkView) {

                if (fromDeep){

                    WorkView workView = (WorkView) view;
                    VisitCell visitCell = workView.getVisitCell(visitId);

                    if (visitCell != null) {
                        return visitCell;
                    }
                }
            }
        }
        return null;
    }

    private WorkView getWorkView(String workId) {

        for ( int i = 0 ; i < container.getChildCount() ; i++ ) {

            View view = container.getChildAt(i);

            if (view instanceof WorkView) {

                WorkView workView = (WorkView) view;

                if (workView.getWork().getId().equals(workId)) {
                    return workView;
                }
            }
        }
        return null;
    }

    private void moveVisitCellIfNeeded(final VisitCell visitCell) {
        // 必要ならVisitCellの位置を変更する
        // このVisitCellをふくむべきWorkViewは存在するか
        if (getWorkViewOfVisit(visitCell.getVisit()) != null) {
            // 存在した
            WorkView workView = getWorkViewOfVisit(visitCell.getVisit());
            if (workView == null) return;

            workView.addVisitCell(visitCell.getVisit());
            removeVisitCell(visitCell.getVisit());

        } else {
            // このVisitCellを含むべきWorkViewは存在しなかった
            removeVisitCell(visitCell.getVisit());
            insertVisitCellAndExtract(visitCell.getVisit());
        }
    }

    /**
     *
     * @param visit
     * @return もしそのVisitを含むべきWorkViewがあれば返す。なければNULL
     */
    @Nullable
    private WorkView getWorkViewOfVisit(Visit visit) {

        // 現状containerにあるWorkViewsを検証する

        for ( int i = 0 ; i < container.getChildCount() ; i++ ) {

            View view = container.getChildAt(i);
            if (view instanceof WorkView) {

                WorkView workView = (WorkView) view;
                Work work = workView.getWork();

                if (work.isVisitInWork(visit)) {
                    return workView;
                }
            }
        }
        return null;
    }

    private void removeWorkView(Work work) {
        final WorkView workView = getWorkView(work.getId());
        if (workView == null) return;

        workView.compress(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                container.removeView(workView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void removeWorkViews(ArrayList<Work> works) {

        for (Work work : works) {
            removeWorkView(work);
        }
    }

    private void removeVisitCells(ArrayList<Visit> visits) {

        for (Visit visit : visits) {
            removeVisitCell(visit);
        }
    }

    private void removeVisitCell(Visit visit) {

        final VisitCell visitCell = getVisitCell(visit.getId(), false);

        if (visitCell == null) return;

        visitCell.compress(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ViewParent parent = visitCell.getParent();
                LinearLayout linearLayout = (LinearLayout) parent;
                linearLayout.removeView(visitCell);
                verifyItemRemains();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public Calendar getDate() {
        return mDate;
    }

    public void addWorkViewAndExtract(Work work) {

        int pos = getInsertPosition(work.getStart());
        addWorkView(work, true);
        container.addView(generateWorkView(work, true), pos);

    }

    private void verifyItemRemains() {

        if (container.getChildCount() <= 0) {
            mWorkFragmentListener.onAllItemRemoved(mDate);
        }

    }

    public interface WorkFragmentListener {
        void onAllItemRemoved(Calendar date);

        void moveToMap(Visit visit);
    }

}
