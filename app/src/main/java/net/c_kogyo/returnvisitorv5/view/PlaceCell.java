package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.data.Place;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceCell extends FrameLayout {

    private Place mPlace;

    public PlaceCell(@NonNull Context context, Place place) {
        super(context);

        mPlace = place;
        initCommon();
    }

    public PlaceCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPlaceAndInitialize(Place place) {
        this.mPlace = place;
        initCommon();
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.place_cell, this);

        initPlaceMarker();
        initPlaceText();
        initEditButton();
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

    private Button editButton;
    private void initEditButton() {
        editButton = (Button) view.findViewById(R.id.edit_button);
    }

    public Button getEditButton() {
        return editButton;
    }
}
