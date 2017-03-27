package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class VisitCell extends BaseAnimateView {

    @Override
    public void setLayoutParams() {
        this.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    private Visit mVisit;
    private int mCollapseHeight, mExtractHeight;
    private VisitCellListener mListener;

    public VisitCell(Context context, Visit visit, VisitCellListener listener) {
        super(context,
                context.getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                 + (int)(context.getResources().getDisplayMetrics().density * 5),
                R.layout.visit_cell);
        mVisit = visit;
        mListener = listener;
        initCommon();
    }

    public VisitCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    private void initCommon() {

        mCollapseHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                + (int)(getContext().getResources().getDisplayMetrics().density * 5);
        mExtractHeight = (int) (getContext().getResources().getDisplayMetrics().density * 270);

        initHeadRow();
        initVisitDataText();
        initEditButton();

//        this.getLayoutParams().height = collapseHeight;
    }

    private LinearLayout headRow;
    private void initHeadRow() {
        headRow = (LinearLayout) getViewById(R.id.head_row);
        headRow.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        headRow.setAlpha(0.5f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        headRow.setAlpha(1f);
                        openCloseCell();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        headRow.setAlpha(1f);
                        return true;
                }
                return false;
            }
        });

        initMarker();
        initDateText();
        initOpenCloseMark();
    }

    private ImageView marker;
    private void initMarker() {
        marker = (ImageView) getViewById(R.id.marker);
        marker.setBackgroundResource(Constants.buttonRes[mVisit.getPriority().num()]);
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) getViewById(R.id.date_text);
        dateText.setText(DateTimeText.getDateTimeText(mVisit.getDatetime(), getContext()));
    }

    private ImageView openCloseMark;
    private void initOpenCloseMark() {
        openCloseMark = (ImageView) getViewById(R.id.open_close_mark);
    }

    private boolean isViewOpen = false;
    private void openCloseCell() {
        if (isViewOpen) {
            VisitCell.this.changeViewHeight(mCollapseHeight, true, null, null);
        } else {
            VisitCell.this.changeViewHeight(mExtractHeight, true, null, null);
        }

        rotateOpenCloseMark();

        isViewOpen = !isViewOpen;
    }

    private void rotateOpenCloseMark() {

        float originAngle, targetAngle;

        if (isViewOpen) {
            originAngle = 0f;
            targetAngle = 180f;
        } else {
            originAngle = 180f;
            targetAngle = 0f;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(openCloseMark, "rotation", originAngle, targetAngle);
        animator.setDuration(300);
        animator.start();
    }

    private TextView visitDataText;
    private void initVisitDataText() {
        visitDataText = (TextView) getViewById(R.id.visit_data_text);
        visitDataText.setText(mVisit.toStringWithLineBreak(getContext()));
    }

    private Button editButton;
    private void initEditButton() {
        editButton = (Button) getViewById(R.id.edit_button);
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });
    }

    private void showPopup() {
        PopupMenu visitCellPopup = new PopupMenu(getContext(), editButton);
        visitCellPopup.inflate(R.menu.visit_cell_menu);
        visitCellPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.edit_visit:
                        // DONE: 2017/03/14 編集遷移
                        if (mListener != null) {
                            mListener.onEditClick(mVisit);
                        }
                        return true;
                    case R.id.delete:
                        // DONE: 2017/03/14 削除プロセス
                        if (mListener != null) {
                            mListener.onDeleteClick(mVisit);
                        }
                        return true;
                }
                return false;
            }
        });
        visitCellPopup.show();
    }

    public void refreshVisit(Visit visit) {
        mVisit = visit;

        marker.setBackgroundResource(Constants.markerRes[mVisit.getPriority().num()]);
        dateText.setText(DateTimeText.getDateTimeText(mVisit.getDatetime(), getContext()));
        visitDataText.setText(mVisit.toStringWithLineBreak(getContext()));

    }

    public interface VisitCellListener {

        void onDeleteClick(Visit visit);

        void onEditClick(Visit visit);

    }



}
