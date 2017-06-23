package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
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
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
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
    private int mInitialHeight, mCollapseHeight;
//            , mExtractHeight;
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
        super.setListener(new BaseAnimateViewListener() {
            @Override
            public void onUpdateHeight() {
                if (mListener != null) {
                    mListener.onUpdateHeight();
                }
            }

            @Override
            public void postInitialExtract(BaseAnimateView view) {

            }
        });
    }

    public VisitCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    private void initCommon() {

        mCollapseHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                + (int)(getContext().getResources().getDisplayMetrics().density * 5);
//        mExtractHeight = (int) (getContext().getResources().getDisplayMetrics().density * 270);

        initViews();
        initEditButton();

        if (mInitialHeight <= 0) {
            extractPostDrawn(mCollapseHeight);
        }
    }

    private LinearLayout headerRow;
    private ImageView visitMarker;
    private TextView headerText;
    private ImageView openCloseMark;
    private TextView visitDataText;
    private LinearLayout visitDetailContainer;

    private void initViews() {
        headerRow = (LinearLayout) getViewById(R.id.head_row);
        ViewUtil.setOnClickListener(headerRow, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                openCloseCell();
            }
        });

        visitMarker = (ImageView) getViewById(R.id.visit_marker);
        headerText = (TextView) getViewById(R.id.header_text);
        openCloseMark = (ImageView) getViewById(R.id.open_close_mark);
        visitDetailContainer = (LinearLayout) getViewById(R.id.visit_detail_container);
        visitDataText = (TextView) getViewById(R.id.visit_data_text);

        refreshVisit(null);
    }

    private void refreshHeaderText() {

        String text = "";
        Place place = PlaceList.getInstance().getById(mVisit.getPlaceId());

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

    private boolean isViewOpen = false;
    private void openCloseCell() {

        if (isViewOpen) {
            VisitCell.this.changeViewHeight(mCollapseHeight, true, false, null);
        } else {
            extract();
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
            visitCellPopup.getMenu().add(Menu.NONE, SHOW_IN_MAP_MENU, Menu.NONE, R.string.show_in_map);
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

                                compress(new PostAnimationListener() {
                                    @Override
                                    public void postAnimate(BaseAnimateView view) {
                                        if (mListener != null) {
                                            mListener.postCompressVisitCell(VisitCell.this);
                                        }
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

    public void refreshVisit(@Nullable  Visit visit) {

        if (visit != null) {
            mVisit = visit;
        }

        visitMarker.setBackgroundResource(Constants.buttonRes[mVisit.getPriority().num()]);
        refreshHeaderText();
        visitDataText.setText(mVisit.getPlacementsString());

        setVisitDetailCells();
    }

    public Visit getVisit() {
        return mVisit;
    }

    private void extract() {

        measure(0, 0);
        int height = getMeasuredHeight();
        changeViewHeight(height, true, false, null);

    }

    private void setVisitDetailCells() {
        visitDetailContainer.removeAllViews();
        for (VisitDetail visitDetail : mVisit.getVisitDetails()) {
            VisitDetailCell cell = new VisitDetailCell(getContext(), visitDetail);
            visitDetailContainer.addView(cell);
        }
    }

    public interface VisitCellListener {

        void postCompressVisitCell(VisitCell visitCell);

        void onEditClick(Visit visit);

        void onClickToMap(Visit visit);

        void onUpdateHeight();

    }


}
