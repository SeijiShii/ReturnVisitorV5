package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceCell extends FrameLayout {

    private Place mPlace;
    private PlaceCellListener mListener;

    public PlaceCell(@NonNull Context context, Place place) {
        super(context);

        mPlace = place;
        initCommon();
    }

    public PlaceCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPlaceAndInitialize(Place place, PlaceCellListener listener) {
        this.mListener = listener;
        this.mPlace = place;
        initCommon();
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.place_cell, this);

        initPlaceMarker();
        initPlaceText();
        initMenuButton();
    }

    private ImageView placeMarker;
    private void initPlaceMarker() {
        placeMarker = (ImageView) view.findViewById(R.id.place_marker);

        if (mPlace.getCategory() == Place.Category.HOUSE) {
            placeMarker.setBackgroundResource(Constants.markerRes[mPlace.getPriority().num()]);
        } else if (mPlace.getCategory() == Place.Category.ROOM) {
            placeMarker.setBackgroundResource(Constants.buttonRes[mPlace.getPriority().num()]);
        }
    }

    private TextView placeText;
    private void initPlaceText() {
        placeText = (TextView) view.findViewById(R.id.place_text);
        placeText.setText(mPlace.toString());
    }

    private Button menuButton;
    private void initMenuButton() {
        menuButton = (Button) view.findViewById(R.id.edit_button);
        menuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });
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
                    });
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public interface PlaceCellListener {

        void onDeletePlace(Place place);
    }

//    public Button getMenuButton() {
//        return menuButton;
//    }
}
