package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceCell extends BaseAnimateView {

    private Place mPlace;
    private PlaceCellListener mListener;
    private boolean mTransparent;

    @Override
    public void setLayoutParams(BaseAnimateView view) {
        int dp45 = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_45dp);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp45));
    }

    public PlaceCell(@NonNull Context context,
                     Place place,
                     @Nullable PlaceCellListener listener,
                     boolean transParent) {

        super(context, context.getResources().getDimensionPixelSize(R.dimen.ui_height_45dp), R.layout.place_cell);

        mTransparent = transParent;
        mPlace = place;
        mListener = listener;

        initCommon();
    }

    public PlaceCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.layout.place_cell);
        mTransparent = true;
    }

    public void setPlaceAndInitialize(Place place, PlaceCellListener listener) {
        this.mListener = listener;
        this.mPlace = place;
        initCommon();
    }

    private void initCommon() {

        if (mTransparent) {
            setBackgroundResource(R.drawable.gray_trans_circle);
        } else {
            setBackgroundResource(R.drawable.gray_white_circle);
        }

        initHeaderRow();
        initPersonContainer();
        initMenuButton();
    }

    private void initHeaderRow() {
        LinearLayout headerRow = (LinearLayout) getViewById(R.id.header_row);
        ViewUtil.setOnClickListener(headerRow, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View view) {
                openCloseView();
            }
        });
        initPlaceMarker();
        initPlaceText();
        initArrowMark();
    }

    private ImageView arrowMark;
    private void initArrowMark () {
        arrowMark = (ImageView) getViewById(R.id.arrow_mark);
    }

    private void rotateArrowMark() {

        float origin, target;

        if (mIsViewOpen) {
            origin = 180f;
            target = 0f;
        } else {
            origin = 0f;
            target = 180f;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(arrowMark, "rotation", origin, target);
        animator.setDuration(500);
        animator.start();
    }

    private boolean mIsViewOpen = false;
    private void openCloseView() {

        int target = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_45dp);

        if (mIsViewOpen) {

        } else {
            target = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        changeViewHeight(target, true, false, null);
        rotateArrowMark();
        mIsViewOpen = !mIsViewOpen;
    }

    private void initPlaceMarker() {
        ImageView placeMarker = (ImageView) getViewById(R.id.place_marker);

        switch (mPlace.getCategory()) {
            case HOUSE:
                placeMarker.setBackgroundResource(Constants.markerRes[mPlace.getPriority().num()]);
                break;
            case ROOM:
                placeMarker.setBackgroundResource(Constants.buttonRes[mPlace.getPriority().num()]);
                break;
            case HOUSING_COMPLEX:
                placeMarker.setBackgroundResource(Constants.complexRes[mPlace.getPriority().num()]);
                break;
        }
    }

    private void initPlaceText() {
        TextView placeText = (TextView) getViewById(R.id.place_text);
        placeText.setText(mPlace.toString());
    }

    private LinearLayout personContainer;
    private void initPersonContainer() {
        personContainer = (LinearLayout) getViewById(R.id.person_container);
        for (Person person : RVData.getInstance().personList.getPersonsInPlace(mPlace)) {
            PersonCell cell = new PersonCell(getContext(), person, true, new PersonCell.PersonCellListener() {
                @Override
                public void onClickDelete(Person person) {
                   removePersonCell(person);
                    RVData.getInstance().personList.deleteById(person.getId());
                    RVData.getInstance().saveData(getContext());
                    RVCloudSync.syncDataIfLoggedIn(getContext());
                }

                @Override
                public void onClickEdit(Person person) {
                    if (mListener != null) {
                        mListener.onClickEditPerson(person);
                    }
                }
            }){
                @Override
                public void setLayoutParams(BaseAnimateView view) {
                    int dp40 = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
                    view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp40));
                }
            };
            personContainer.addView(cell);
        }
    }

    private void removePersonCell(Person person) {
        final PersonCell cell = getPersonCell(person);
        if (cell == null) return;

        cell.compress(new PostAnimationListener() {
            @Override
            public void postAnimate(BaseAnimateView view) {
                personContainer.removeView(cell);
            }
        });
    }

    @Nullable
    private PersonCell getPersonCell(Person person) {

        for ( int i = 0 ; i < personContainer.getChildCount() ; i++ ) {
            PersonCell personCell = (PersonCell) personContainer.getChildAt(i);
            if (personCell.getPerson().equals(person)) {
                return personCell;
            }
        }
        return null;
    }

    private ImageView menuButton;
    private void initMenuButton() {
        RelativeLayout menuRow = (RelativeLayout) getViewById(R.id.menu_row);
        menuButton = (ImageView) getViewById(R.id.place_edit_button);

        int dp40 = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);

        if (mListener != null) {

            menuRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp40));

            menuButton.setVisibility(VISIBLE);
            ViewUtil.setOnClickListener(menuButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View view) {
                    showPopupMenu();
                }
            });
        } else {
            menuRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            menuButton.setVisibility(INVISIBLE);
        }
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.delete_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.delete) {
                    ConfirmDialog.confirmAndDeletePlace(getContext(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mListener != null) {
                                mListener.onDeletePlace(mPlace);
                            }
                        }
                    }, mPlace);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public interface PlaceCellListener {

        void onDeletePlace(Place place);

        void onClickEditPerson(Person person);

    }
}
