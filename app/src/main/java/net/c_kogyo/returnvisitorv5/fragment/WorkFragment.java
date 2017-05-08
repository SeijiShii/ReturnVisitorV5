package net.c_kogyo.returnvisitorv5.fragment;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
import net.c_kogyo.returnvisitorv5.view.VisitCell;
import net.c_kogyo.returnvisitorv5.view.WorkView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2016/09/17.
 */

public class WorkFragment extends Fragment {

    // DONE: 2017/04/05 時間調整後のアニメーション

    private Calendar mDate;
    // TODO: 2017/05/08 Remove worksInDayList
    private ArrayList<Work> worksInDay;
//    private ArrayList<Visit> visitsInDayNotInWork;
    private static WorkFragmentListener mWorkFragmentListener;
    private int mVisitCellHeight;
    private Work mNewAddedWork;

    public static WorkFragment newInstance(Calendar date,
                                           @Nullable Work newAddedWork,
                                           WorkFragmentListener workFragmentListener) {

        // DONE: 2017/04/14 getItemで取得したとき初期化されない pagerAdapter.instantiate()が正しい

        mWorkFragmentListener = workFragmentListener;

        WorkFragment workFragment = new WorkFragment();

        Bundle arg = new Bundle();
        Intent intent = new Intent();

        intent.putExtra(Constants.DATE_LONG, date.getTimeInMillis());
        if (newAddedWork != null) {
            intent.putExtra(Work.WORK, newAddedWork.getId());
        }

        arg.putParcelable(Constants.WORK_FRAGMENT_ARGUMENT, intent);
        workFragment.setArguments(arg);

        return workFragment;
    }

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setParcelableData();

        view = inflater.inflate(R.layout.work_fragment, container, false);

        mVisitCellHeight = getResources().getDimensionPixelSize(R.dimen.ui_height_45dp);

        worksInDay = RVData.getInstance().workList.getWorksInDay(mDate);
//        visitsInDayNotInWork = RVData.getInstance().visitList.getVisitsInDayNotInWork(mDate);

        initContainer();

        insertNewWorkViewPostDrawnIfExists();

        return view;
    }

    private void setParcelableData() {

        mDate = Calendar.getInstance();

        Intent intent = getArguments().getParcelable(Constants.WORK_FRAGMENT_ARGUMENT);

        if (intent == null) return;

        Long dLong =  intent.getLongExtra(Constants.DATE_LONG, 0);

        if (dLong != 0) {
            mDate.setTimeInMillis(dLong);
        }

        String workId = intent.getStringExtra(Work.WORK);
        if (workId != null) {
            Work work = RVData.getInstance().workList.getById(workId);
            if (CalendarUtil.isSameDay(work.getStart(), mDate)) {
                mNewAddedWork = work;
            }
        }
    }

    private LinearLayout container;
    private void initContainer() {

        container = (LinearLayout) view.findViewById(R.id.container);


        int visitCounter = 0;
        int workCounter = 0;
        ArrayList<Visit> visitsInDayNotInWork = RVData.getInstance().visitList.getVisitsInDayNotInWork(mDate);

        while (visitCounter < visitsInDayNotInWork.size() && workCounter < worksInDay.size()) {

            if (visitsInDayNotInWork.get(visitCounter).getDatetime()
                    .before(worksInDay.get(workCounter).getStart())) {

                addVisitCell(visitsInDayNotInWork.get(visitCounter), false);
                visitCounter++;

            } else {

                if (!worksInDay.get(workCounter).equals(mNewAddedWork)) {
                    // 他アクティビティで追加したWorkなら描画後に挿入
                    addWorkView(worksInDay.get(workCounter), false);
                }
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
                        RVData.getInstance().visitList.deleteById(visitCell1.getVisit().getId());
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
                        // DONE: 2017/04/09 Adjust works action
                        RVData.getInstance().workList.setOrAdd(work);
                        ArrayList<Work> worksRemoved = RVData.getInstance().workList.onChangeTime(work);
                        removeWorkViews(worksRemoved);
                        RVData.getInstance().workList.removeList(worksRemoved);
                        // DONE: 2017/04/08 Add or Remove visitCells action
                        removeVisitCells(visitsAdded);
                        addVisitCells(visitsRemoved);

                        RVData.getInstance().saveData(getContext(), null);

                    }


                    @Override
                    public void onDeleteWork(WorkView workView) {
                        Work deletedWork = workView.getWork();
//                        RVData.getInstance().workList.deleteById(deletedWork.getId());
                        removeWorkView(deletedWork);
//                        RVData.getInstance().saveData(getContext(), null);
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

    // TODO: 2017/05/06 Add work時のポジションがおかしい
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

        Work work = RVData.getInstance().workList.getByVisit(visit);
        if (work != null) {
            WorkView workView = getWorkView(work.getId());
            if (workView != null) {
                workView.insertVisitCellToProperPosition(visit);
            } else {
                container.addView(visitCell, getInsertPosition(visit.getDatetime()));
            }
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
        for (Visit visit : RVData.getInstance().visitList.getVisitsInDayNotInWork(mDate)) {
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

                removeVisitCell(visit, null);
//                visitsInDayNotInWork.remove(visit);

            } else if (resultCode == Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE) {
                // VisitCellの内容が変更されたとき

                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId == null) return;

                Visit visit = RVData.getInstance().visitList.getById(visitId);
                if (visit == null) return;

                // VisitCellの時間を変化させたときに適正なポジションにあるかどうかをVerifyする必要あり
                // 必要なら位置を変更する
                moveVisitCellIfNeeded(visit);

                VisitCell visitCell = getVisitCell(visitId, true);
                if (visitCell == null) return;

                visitCell.refreshVisit(visit);
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

    @Nullable
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

    @Nullable
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

    private void moveVisitCellIfNeeded(final Visit visit) {
        // まずそのVisitCellがどこにあるかを特定する
        WorkView workView = getWorkViewOfVisit(visit);
        if (workView != null) {
            // そのworkViewにそのvisitのセルが現在含まれている
            if (workView.removeVisitCellIfNotInProperPosition(visit,
                    new WorkView.PostRemoveVisitCellListener() {
                        @Override
                        public void postRemoveVisitCell() {
                            insertVisitCellAndExtract(visit);
                        }
            })) ;
        } else {
            // visitCellは現在workViewには含まれていない。
            VisitCell visitCell = getVisitCell(visit.getId(), false);
            if (visitCell != null) {
                if (isVisitCellInProperPosition(visitCell))
                    return;
                removeVisitCell(visit, new PostRemoveVisitCellListener() {
                    @Override
                    public void postRemoveVisitCell() {
                        insertVisitCellAndExtract(visit);
                    }
                });
            }
        }
    }

    private boolean isVisitCellInProperPosition(VisitCell visitCell) {

        int currentPos = getPositionInContainer(visitCell);
        int propPos = getInsertPosition(visitCell.getVisit().getDatetime());

        return currentPos == propPos;
    }

    private  int getPositionInContainer(VisitCell visitCell) {

        Visit visit = visitCell.getVisit();

        for ( int i = 0 ; i < container.getChildCount() ; i++ ) {
            View view = container.getChildAt(i);
            if (view instanceof VisitCell) {
                VisitCell visitCell1 = (VisitCell) view;
                if (visitCell1.getVisit().equals(visit)) {
                    return i;
                }
            }
        }
        return -1;
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

                if (workView.hasVisitCell(visit)) {
                    return workView;
                }
            }
        }
        return null;
    }

    private void removeWorkView(final Work work) {
        final WorkView workView = getWorkView(work.getId());
        if (workView == null) return;

        workView.compress(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                container.removeView(workView);
                if (mWorkFragmentListener != null) {
                    mWorkFragmentListener.postRemoveWorkView(work);
                }
                verifyItemRemains();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        moveVisitCellsInRemoveWorkView(work);
    }

    private void moveVisitCellsInRemoveWorkView(Work work) {
        // DONE: 2017/04/15  削除したWork内のVisitを付け替える

        addVisitCells(RVData.getInstance().visitList.getVisitsInWork(work));
    }

    public void removeWorkViews(ArrayList<Work> works) {

        for (Work work : works) {
            removeWorkView(work);
        }
    }

    public void removeVisitCells(ArrayList<Visit> visits) {

        for (Visit visit : visits) {
            removeVisitCell(visit, null);
        }
    }

    private void removeVisitCell(Visit visit, final PostRemoveVisitCellListener postRemoveVisitCellListener) {

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

    public Calendar getDate() {
        return mDate;
    }

    public void addWorkViewAndExtract(Work work) {

        int pos = getInsertPosition(work.getStart());
//        addWorkView(work, true);
        container.addView(generateWorkView(work, true), pos);

    }

    private void verifyItemRemains() {

        if (container.getChildCount() <= 0) {
            mWorkFragmentListener.onAllItemRemoved(mDate);
        }

    }

    private void insertNewWorkViewPostDrawnIfExists() {

        if (mNewAddedWork == null) {
            return;
        }

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (container.getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        addWorkViewAndExtract(mNewAddedWork);
                    }
                });
            }
        }).start();
    }

    public interface WorkFragmentListener {

        void postRemoveWorkView(Work work);

        void onAllItemRemoved(Calendar date);

        void moveToMap(Visit visit);
    }

    public interface PostRemoveVisitCellListener{
        void postRemoveVisitCell();
    }

}
