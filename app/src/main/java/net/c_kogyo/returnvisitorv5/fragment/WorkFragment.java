package net.c_kogyo.returnvisitorv5.fragment;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.activity.RecordVisitActivity;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
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

    private static final String TAG = "WorkFragmentDebugTag";

    private Calendar mDate;
    // DONE: 2017/05/08 Remove worksInDayList
    private static WorkFragmentListener mWorkFragmentListener;
    private int mVisitCellHeight;
    private Work mNewAddedWork;

    public static WorkFragment newInstance(Calendar date,
                                           @Nullable Work newAddedWork,
                                           boolean toExtractAddedWorkView,
                                           WorkFragmentListener workFragmentListener) {

        // DONE: 2017/04/14 getItemで取得したとき初期化されない pagerAdapter.instantiate()が正しい

        mWorkFragmentListener = workFragmentListener;

        WorkFragment workFragment = new WorkFragment();

        Bundle arg = new Bundle();
        Intent intent = new Intent();

        intent.putExtra(Constants.DATE_LONG, date.getTimeInMillis());
        if (newAddedWork != null) {
            intent.putExtra(Constants.WorkFragmentConstants.ADDED_WORK_ID, newAddedWork.getId());
        }
        intent.putExtra(Constants.WorkFragmentConstants.TO_EXTRACT_WORK_VIEW, toExtractAddedWorkView);

        arg.putParcelable(Constants.WorkFragmentConstants.WORK_FRAGMENT_ARGUMENT, intent);
        workFragment.setArguments(arg);

        return workFragment;
    }

    private View view;
    private boolean mToExtractAddedWorkView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setParcelableData();

        Log.d(TAG, "WorkFragment, onCreateView, mDate: " + DateTimeText.getDateTimeText(mDate, getContext()));

        view = inflater.inflate(R.layout.work_fragment, container, false);

        mVisitCellHeight = getResources().getDimensionPixelSize(R.dimen.ui_height_45dp);

//        worksInDay = RVData.getInstance().workList.getWorksInDay(mDate);
//        visitsInDayNotInWork = RVData.getInstance().visitList.getVisitsInDayNotInWork(mDate);

        initScrollView();
        initContainer();

        insertNewWorkViewPostDrawnIfExists();

        return view;
    }

    private void setParcelableData() {

        mDate = Calendar.getInstance();

        Intent intent = getArguments().getParcelable(Constants.WorkFragmentConstants.WORK_FRAGMENT_ARGUMENT);

        if (intent == null) return;

        Long dLong =  intent.getLongExtra(Constants.DATE_LONG, 0);

        if (dLong != 0) {
            mDate.setTimeInMillis(dLong);
        }

        String workId = intent.getStringExtra(Constants.WorkFragmentConstants.ADDED_WORK_ID);
        if (workId != null) {
            Work work = WorkList.getInstance().getById(workId);
            if (work != null) {
                if (CalendarUtil.isSameDay(work.getStart(), mDate)) {
                    mNewAddedWork = work;
                }
            }
        }

        mToExtractAddedWorkView = intent.getBooleanExtra(Constants.WorkFragmentConstants.TO_EXTRACT_WORK_VIEW, false);
    }

    private LinearLayout container;
    private void initContainer() {

        container = (LinearLayout) view.findViewById(R.id.container);
        ArrayList<Work> worksInDay = WorkList.getInstance().getWorksInDay(mDate);

        int visitCounter = 0;
        int workCounter = 0;
        ArrayList<Visit> visitsInDayNotInWork = VisitList.getInstance().getVisitsInDayNotInWork(mDate);

        while (visitCounter < visitsInDayNotInWork.size() && workCounter < worksInDay.size()) {

            if (visitsInDayNotInWork.get(visitCounter).getDatetime()
                    .before(worksInDay.get(workCounter).getStart())) {

                addVisitCell(visitsInDayNotInWork.get(visitCounter), false);
                visitCounter++;

            } else {

                if (mNewAddedWork != null) {
                    if (!worksInDay.get(workCounter).equals(mNewAddedWork) || !mToExtractAddedWorkView) {
                        // 他アクティビティで追加したWorkなら描画後に挿入
                        addWorkView(worksInDay.get(workCounter), false);
                    }
                } else {
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
                if (mNewAddedWork != null) {
                    if (!worksInDay.get(i).equals(mNewAddedWork) || !mToExtractAddedWorkView) {
                        // 他アクティビティで追加したWorkなら描画後に挿入
                        addWorkView(worksInDay.get(i), false);
                    }
                } else {
                    addWorkView(worksInDay.get(i), false);
                }
            }
        }
    }

    private VisitCell generateVisitCell(final Visit visit, boolean fromZero) {

        int initHeight;
        if (fromZero) {
            initHeight = 0;
        } else {
            initHeight = mVisitCellHeight;
        }

        VisitCell cell = new VisitCell(getContext(), visit, initHeight,new VisitCell.VisitCellListener() {

            @Override
            public void postCompressVisitCell(final VisitCell visitCell1) {

                container.removeView(visitCell1);

                VisitList.getInstance().deleteById(visitCell1.getVisit().getId());
                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(WorkFragment.this.getActivity());

                verifyItemRemains();
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

            @Override
            public void onUpdateHeight() {

            }
        }, VisitCell.HeaderContent.BOTH) {
            @Override
            public void setLayoutParams(BaseAnimateView view) {
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            }
        };
        if (fromZero) {
            cell.extractPostDrawn(mVisitCellHeight);
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
                        WorkList.getInstance().setOrAdd(work);
                        ArrayList<Work> worksRemoved = WorkList.getInstance().onChangeTime(work);
                        removeWorkViews(worksRemoved);
                        WorkList.getInstance().removeList(worksRemoved);
                        // DONE: 2017/04/08 Add or Remove visitCells action
                        removeVisitCells(visitsAdded);
                        addVisitCells(visitsRemoved);

                        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getActivity());

                    }


                    @Override
                    public void onDeleteWork(WorkView workView) {
                        Work deletedWork = workView.getWork();
//                        RVData.getInstance().workList.deleteById(deletedWork.getId());
                        removeWorkView(deletedWork);
//                        RVData.getInstance().saveData(getActivity(), null);
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

                    @Override
                    public void postAddVisitCell(VisitCell cell) {
                        ViewUtil.scrollToView(scrollView, cell);
                    }
                }) {
                    @Override
                    public void setLayoutParams(BaseAnimateView view) {
                        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    }
                };

        if (fromZero) {
            workView.extractPostDrawn(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return workView;
    }

    // DONE: 2017/05/06 Add work時のポジションがおかしい
    // DONE: 2017/05/08 workViewが二つ挿入される
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

        Work work = WorkList.getInstance().getByVisit(visit);
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
        for (Visit visit : VisitList.getInstance().getVisitsInDayNotInWork(mDate)) {
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

//                Visit visit = getVisit(visitId);
//                if (visit == null) return;

                VisitCell visitCell = getVisitCell(visitId, false);
                if (visitCell == null) return;

                removeVisitCell(visitCell, null);
//                visitsInDayNotInWork.remove(visit);

            } else if (resultCode == Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE) {
                // VisitCellの内容が変更されたとき

                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId == null) return;

                Visit visit = VisitList.getInstance().getById(visitId);
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
                    Work work = WorkList.getInstance().getById(workId);
                    if (work != null) {
                        addWorkViewAndExtract(work);
                    }
                }

                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId != null) {
                    Visit visit = VisitList.getInstance().getById(visitId);
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
                removeVisitCell(visitCell, new PostRemoveVisitCellListener() {
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

        workView.compress(new BaseAnimateView.PostAnimationListener() {
            @Override
            public void postAnimate(BaseAnimateView view) {
                container.removeView(workView);
                if (mWorkFragmentListener != null) {
                    mWorkFragmentListener.postRemoveWorkView(work);
                }
                verifyItemRemains();
            }
        });

        moveVisitCellsInRemoveWorkView(work);
    }

    private void moveVisitCellsInRemoveWorkView(Work work) {
        // DONE: 2017/04/15  削除したWork内のVisitを付け替える

        addVisitCells(VisitList.getInstance().getVisitsInWork(work));
    }

    public void removeWorkViews(ArrayList<Work> works) {

        for (Work work : works) {
            removeWorkView(work);
        }
    }

    public void removeVisitCells(ArrayList<Visit> visits) {

        for (Visit visit : visits) {
            VisitCell visitCell = getVisitCell(visit.getId(), false);
            if (visitCell != null) {
                removeVisitCell(visitCell, null);
            }
        }
    }

    private void removeVisitCell(final VisitCell visitCell, final PostRemoveVisitCellListener postRemoveVisitCellListener) {

        visitCell.compress(new BaseAnimateView.PostAnimationListener() {
            @Override
            public void postAnimate(BaseAnimateView view) {
                ViewParent parent = visitCell.getParent();
                LinearLayout linearLayout = (LinearLayout) parent;
                linearLayout.removeView(visitCell);
                if (postRemoveVisitCellListener != null) {
                    postRemoveVisitCellListener.postRemoveVisitCell();
                }
                verifyItemRemains();
            }
        });
    }

    public Calendar getDate() {
        return mDate;
    }

    // DONE: 2017/05/09 WorkView追加時に表示するまでスクロールする
    public void addWorkViewAndExtract(Work work) {

        int pos = getInsertPosition(work.getStart());

        Log.d(TAG, "addWorkViewAndExtract Called! pos: " + pos);

//        addWorkView(work, true);
        WorkView workView = generateWorkView(work, true);
        container.addView(workView, pos);

    }

    public void insertVisitCell(Visit visit) {
        // DONE: 2017/05/09  insertVisitCell
        Work work = WorkList.getInstance().getByVisit(visit);
        if (work != null) {
            WorkView workView = getWorkView(work.getId());
            if (workView != null) {
                workView.insertVisitCellToProperPosition(visit);
                VisitCell cell = workView.getVisitCell(visit.getId());
                if (cell != null) {
                }
            }
        } else {
            VisitCell cell = generateVisitCell(visit, true);
            container.addView(cell);
        }
    }

    /**
     * Used to scroll to the given view.
     *
     * @param targetView View to which we need to scroll.
     */
    private ScrollView scrollView;
    private void initScrollView() {

        scrollView = (ScrollView) view.findViewById(R.id.scroll_view);


    }

    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumalated Offset.
     */
    private void getDeepChildOffset(ViewParent parent,
                                    View child,
                                    final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        int height = ((ViewGroup) parent).getHeight();
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(scrollView)) {
            return;
        }
        getDeepChildOffset(parentGroup.getParent(), parentGroup, accumulatedOffset);
    }

    private void verifyItemRemains() {

        if (container.getChildCount() <= 0) {
            mWorkFragmentListener.onAllItemRemoved(mDate);
        }

    }

    private void insertNewWorkViewPostDrawnIfExists() {

        if (mNewAddedWork == null || !mToExtractAddedWorkView) {
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
                        if (mWorkFragmentListener != null) {
                            mWorkFragmentListener.postExtractAddedWorkView();
                        }
                    }
                });
            }
        }).start();
    }

    public interface WorkFragmentListener {

        void postExtractAddedWorkView();

        void postRemoveWorkView(Work work);

        void onAllItemRemoved(Calendar date);

        void moveToMap(Visit visit);
    }

    public interface PostRemoveVisitCellListener{
        void postRemoveVisitCell();
    }

}
