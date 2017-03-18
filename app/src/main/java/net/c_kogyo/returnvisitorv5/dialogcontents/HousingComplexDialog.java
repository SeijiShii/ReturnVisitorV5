package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.HousingComplex;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/18.
 */

public class HousingComplexDialog extends FrameLayout {

    private HousingComplex mHousingComplex;
    private HousingComplexDialogListener mListener;

    public HousingComplexDialog(@NonNull Context context,
                                HousingComplex housingComplex,
                                HousingComplexDialogListener listener) {
        super(context);
        this.mHousingComplex = housingComplex;
        this.mListener = listener;
        initCommon();
    }

    public HousingComplexDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {

        view = LayoutInflater.from(getContext()).inflate(R.layout.housing_complex_dialog, this);

        initNameText();
        initAddressText();
        initRoomText();
        initAddRoomButton();
        initRoomListView();
        initOKButton();
        initCancelButton();
    }

    private EditText nameText;
    private void initNameText() {
        nameText = (EditText) view.findViewById(R.id.name_text);
    }

    private EditText addressText;
    private void initAddressText() {
        addressText = (EditText) view.findViewById(R.id.address_text);
    }

    private EditText roomText;
    private void initRoomText() {
        roomText = (EditText) view.findViewById(R.id.room_text);
    }

    private void initAddRoomButton() {
        Button addRoomButton = (Button) view.findViewById(R.id.add_room_button);
        addRoomButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickAddRoomButton(roomText.getText().toString());
                }
            }
        });
    }

    private ListViewCompat roomListView;
    private void initRoomListView() {
        roomListView = (ListViewCompat) view.findViewById(R.id.room_list_view);
        initRoomAdapter();
        refreshRoomList();
        roomListView.setAdapter(roomAdapter);

    }

    private ArrayAdapter<String> roomAdapter;
    private void initRoomAdapter() {

        ArrayList<Place> roomList = RVData.getInstance().getPlaceList().getList(mHousingComplex.getChildIds());
        ArrayList<String> nameList = new ArrayList<>();
        for (Place place : roomList) {
            nameList.add(place.getName());
        }
        roomAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, nameList);

    }

    private void refreshRoomList() {

        int cellHeight = (int) (getContext().getResources().getDisplayMetrics().density * 30);
        int height;
        if (roomAdapter.getCount() <= 0) {
            height = cellHeight;
        } else if (roomAdapter.getCount() > 5) {
            height = cellHeight * 5;
        } else {
            height = cellHeight * roomAdapter.getCount();
        }
        roomListView.getLayoutParams().height = height;
    }

    private void initOKButton() {
        Button okButton = (Button) view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickOkButton();
                }
            }
        });
    }

    private void initCancelButton() {
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickCancelButton();
                }
            }
        });
    }

    public interface HousingComplexDialogListener {

        void onClickAddRoomButton(String roomName);

        void onClickRoomCell();

        void onClickOkButton();

        void onClickCancelButton();
    }

}
