package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.activity.MapActivity;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public abstract class VisitCell extends BaseAnimateView {

    public enum HeaderContent {
        DATETIME,
        PLACE_DATA,
        BOTH
    }

    private Visit mVisit;
    private int mInitialHeight, mCollapseHeight, mExtractHeight;
    private VisitCellListener mListener;
    private HeaderContent mHeaderContent;

    public VisitCell(Context context,
                     Visit visit,
                     int initialHeight,
                     VisitCellListener listener,
                     HeaderContent headerContent) {
        super(context,
                initialHeight,
                R.layout.visit_cell);
        mVisit = visit;
        mListener = listener;
        mInitialHeight = initialHeight;
        mHeaderContent = headerContent;
        initCommon();
    }

    public VisitCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    private void initCommon() {

        mCollapseHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                + (int)(getContext().getResources().getDisplayMetrics().density * 5);
        mExtractHeight = (int) (getContext().getResources().getDisplayMetrics().density * 270);

        initHeaderRow();
        initVisitDataText();
        initEditButton();

        if (mInitialHeight <= 0) {
            extractPostDrawn(mCollapseHeight, null);
        }
    }

    private LinearLayout headerRow;
    private void initHeaderRow() {
        headerRow = (LinearLayout) getViewById(R.id.head_row);
        ViewUtil.setOnClickListener(headerRow, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                openCloseCell();
            }
        });

        initMarker();
        initHeaderText();
        initOpenCloseMark();
    }

    private ImageView marker;
    private void initMarker() {
        marker = (ImageView) getViewById(R.id.marker);
        marker.setBackgroundResource(Constants.buttonRes[mVisit.getPriority().num()]);
    }

    private TextView headerText;
    private void initHeaderText() {
        headerText = (TextView) getViewById(R.id.header_text);
        refreshHeaderText();
    }

    private void refreshHeaderText() {

        String text = "";
        Place place = RVData.getInstance().placeList.getById(mVisit.getPlaceId());

        switch (mHeaderContent) {
            case PLACE_DATA:
                if (place != null) {
                    text = place.toString();
                }
                break;
            case DATETIME:
                text = DateTimeText.getDateTimeText(mVisit.getDatetime(), getContext());
                break;
            case BOTH:
                StringBuilder builder
                        = new StringBuilder(DateTimeText.getDateTimeText(mVisit.getDatetime(), getContext()));
                if (place != null) {
                    builder.append(" ").append(place.toString());
                }
                text = builder.toString();
                break;
        }
        headerText.setText(text);
    }

    private ImageView openCloseMark;
    private void initOpenCloseMark() {
        openCloseMark = (ImageView) getViewById(R.id.open_close_mark);
    }

    private boolean isViewOpen = false;
    private void openCloseCell() {

        HeightUpdateListener listener = new HeightUpdateListener() {
            @Override
            public void onUpdate() {
                if (mListener != null) {
                    mListener.onUpdateHeight();
                }
            }
        };

        if (isViewOpen) {
            VisitCell.this.changeViewHeight(mCollapseHeight, true, listener, null);
        } else {
            VisitCell.this.changeViewHeight(mExtractHeight, true, listener, null);
        }

        rotateOpenCloseMark();

        isViewOpen = !isViewOpen;
    }

    private void rotateOpenCloseMark() {

        float originAngle, targetAngle;

        if (isViewOpen) {
            originAngle = 180f;
            targetAngle = 0f;
        } else {
            originAngle = 0f;
            targetAngle = 180f;
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

    final int SHOW_IN_MAP_MENU = 500;
    private void showPopup() {
        PopupMenu visitCellPopup = new PopupMenu(getContext(), editButton);
        visitCellPopup.inflate(R.menu.visit_cell_menu);
        if (!(getContext() instanceof MapActivity)) {
            visitCellPopup.getMenu().add(Menu.NONE, SHOW_IN_MAP_MENU, Menu.NONE, R.string.show_in_map_menu);
        }
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
                        ConfirmDialog.confirmAndDeleteVisit(getContext(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                compress(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        if (mListener != null) {
                                            mListener.postCompressVisitCell(VisitCell.this);
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
                        }, mVisit);
                        return true;
                    case SHOW_IN_MAP_MENU:
                        if (mListener != null) {
                            mListener.onClickToMap(mVisit);
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

        marker.setBackgroundResource(Constants.buttonRes[mVisit.getPriority().num()]);
        headerText.setText(DateTimeText.getDateTimeText(mVisit.getDatetime(), getContext()));
        visitDataText.setText(mVisit.toStringWithLineBreak(getContext()));

    }

    public Visit getVisit() {
        return mVisit;
    }

    public void compress(Animator.AnimatorListener listener) {
        this.changeViewHeight(0, true, null, listener);
    }

    public interface VisitCellListener {

        void postCompressVisitCell(VisitCell visitCell);

        void onEditClick(Visit visit);

        void onClickToMap(Visit visit);

        void onUpdateHeight();

    }


}
