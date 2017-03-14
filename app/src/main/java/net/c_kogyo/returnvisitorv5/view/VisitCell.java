package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class VisitCell extends BaseAnimateView {

    private Visit mVisit;

    public VisitCell(Context context, Visit visit) {
        super(context,
                (int) (context.getResources().getDisplayMetrics().density * 70),
                InitialHeightCondition.EX_HEIGHT,
                R.layout.visit_cell);
        mVisit = visit;
        initCommon();
    }

    public VisitCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    private void initCommon() {

        initMarker();
        initDateText();
        initOpenCloseButton();
        initVisitDataText();
        initEditButton();
    }

    private ImageView marker;
    private void initMarker() {
        marker = (ImageView) getViewById(R.id.marker);
        marker.setBackgroundResource(Constants.markerRes[mVisit.getPriority().num()]);
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) getViewById(R.id.date_text);
        dateText.setText(DateTimeText.getDateTimeText(mVisit.getDatetime(), getContext()));
    }

    private Button openCloseButton;
    private boolean isViewOpen;
    private void initOpenCloseButton() {
        openCloseButton = (Button) getViewById(R.id.open_close_button);
        openCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 開閉アニメーション DONE
                if (isViewOpen) {

                    int padding = (int) (getContext().getResources().getDisplayMetrics().density * 5);
                    int targetHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                            + padding;
                    VisitCell.this.changeViewHeight(AnimateCondition.TO_TARGET_HEIGHT, targetHeight, true, null, null);

                } else {
                    VisitCell.this.changeViewHeight(AnimateCondition.TO_EX_HEIGHT, 0, true, null, null);
                }

                rotateOpenCloseButton();

                isViewOpen = !isViewOpen;
            }
        });
    }

    private void rotateOpenCloseButton() {

        float originAngle, targetAngle;

        if (isViewOpen) {
            originAngle = 0f;
            targetAngle = 180f;
        } else {
            originAngle = 180f;
            targetAngle = 0f;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(openCloseButton, "rotation", originAngle, targetAngle);
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
                        // TODO: 2017/03/14 編集遷移
                        return true;
                    case R.id.delete:
                        // TODO: 2017/03/14 削除プロセス
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

}
