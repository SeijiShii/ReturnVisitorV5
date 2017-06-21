package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.service.FetchAddressIntentService;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.util.InputUtil;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SeijiShii on 2017/03/18.
 */

public class HousingComplexDialog extends DialogFragment {

    // DONE: 2017/05/08 部屋名ソート
    static private Place mHousingComplex;
    static private HousingComplexDialogListener mListener;
    private ArrayList<Place> addedRooms;
    private ArrayList<Place> removedRooms;
    static private boolean mShowDeleteButton, mShowOkButton;
    
    private static HousingComplexDialog instance;
    
    public static HousingComplexDialog getInstance(Place housingComplex, 
                                                   HousingComplexDialogListener listener,
                                                   boolean showDeleteButton,
                                                   boolean showOkButton) {
        mListener = listener;
        mHousingComplex = housingComplex;
        mShowDeleteButton = showDeleteButton;
        mShowOkButton = showOkButton;
        
        if (instance == null) {
            instance = new HousingComplexDialog();
        }
        return instance;
        
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initCommon();
        builder.setView(view);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                RVData.getInstance().placeList.setOrAdd(mHousingComplex);
                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getActivity());

                if (mListener != null) {
                    mListener.onOkClick(mHousingComplex);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onCloseDialog();
                }
            }
        });

        return builder.create();
    }

    private View view;
    private void initCommon() {

        addedRooms = new ArrayList<>();
        removedRooms = new ArrayList<>();

        view = View.inflate(getActivity(), R.layout.housing_complex_dialog, null);

        initNameText();
        initMenuButton();
        initAddressText();
        initRoomText();
        initAddRoomButton();
        initRoomListView();

        initBroadcasting();
        if (mHousingComplex.needsAddressRequest()) {
            FetchAddressIntentService.inquireAddress(mHousingComplex, getActivity());
        }
    }

    private EditText nameText;
    private void initNameText() {
        nameText = (EditText) view.findViewById(R.id.name_text);
        if (mHousingComplex.getName() != null && !mHousingComplex.getName().equals("")) {
            nameText.setText(mHousingComplex.getName());
        }
    }

    private void initMenuButton() {
        final Button menuButton = (Button) view.findViewById(R.id.menu_button);
        if (mShowDeleteButton) {
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), menuButton);
                    popupMenu.getMenuInflater().inflate(R.menu.delete_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.delete) {
                                ConfirmDialog.confirmAndDeletePlace(getActivity(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mListener != null) {
                                            mListener.onDeleteHousingComplex(mHousingComplex);
                                        }
                                        dismiss();
                                    }
                                }, mHousingComplex);
                                return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        } else {
            menuButton.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
        }

    }

    private EditText addressText;
    private void initAddressText() {
        addressText = (EditText) view.findViewById(R.id.address_text);
        if (mHousingComplex.getAddress() != null && !mHousingComplex.getAddress().equals("")) {
            addressText.setText(mHousingComplex.getAddress());
        }
    }

    private EditText roomText;
    private void initRoomText() {
        roomText = (EditText) view.findViewById(R.id.room_text);
    }

    private void initAddRoomButton() {
        Button addRoomButton = (Button) view.findViewById(R.id.add_room_button);
        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RVData.getInstance().placeList.setOrAdd(mHousingComplex);
                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getActivity());

                InputUtil.hideSoftKeyboard(getActivity());

                // DONE: 2017/03/27 リストに部屋を追加する処理
                String name = roomText.getText().toString();
                roomText.setText("");

                // すでにリストに存在する名前なら何もしない。追加できない
                if (roomAdapter.hasRoomWithName(name)) {
                    return;
                }

                Place newRoom = new Place(mHousingComplex.getLatLng(), Place.Category.ROOM);
                newRoom.setName(name);
                newRoom.setParentId(mHousingComplex.getId());
                newRoom.setAddress(nameText.getText().toString());

                addedRooms.add(newRoom);

                roomAdapter.addRoom(newRoom);
                roomAdapter.notifyDataSetChanged();

                refreshRoomListHeight();
                confirmEdit();

                if (mListener != null) {
                    mListener.onClickAddRoomButton(newRoom);
                }

                dismiss();
            }
        });
    }

    private ListView roomListView;
    private void initRoomListView() {
        roomListView = (ListView) view.findViewById(R.id.room_list_view);
        initRoomAdapter();
        refreshRoomListHeight();
//        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Place room = (Place) roomAdapter.getItem(position);
//                if (mListener != null) {
//                    mListener.onClickRoomCell(room);
//                }
//
//                dismiss();
//            }
//        });

    }

    private RoomListAdapter roomAdapter;
    private void initRoomAdapter() {

        ArrayList<Place> roomList = RVData.getInstance().placeList.getRoomList(mHousingComplex.getId());
        roomList.addAll(addedRooms);
        roomList.removeAll(removedRooms);
        roomAdapter = new RoomListAdapter(roomList);
        roomListView.setAdapter(roomAdapter);

    }

    private void refreshRoomListHeight() {

        int cellHeight = (int) (getActivity().getResources().getDimensionPixelSize(R.dimen.ui_height_small));
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

    public interface HousingComplexDialogListener {

        void onClickAddRoomButton(Place addedRoom);

        void onClickRoomCell(Place room);

        void onDeleteHousingComplex(Place housingComplex);

        void onOkClick(Place housingComplex);

        void onCloseDialog();
    }

    private void initBroadcasting() {

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.registerReceiver(receiver, new IntentFilter(FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION));

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION:
                    String address = intent.getStringExtra(FetchAddressIntentService.ADDRESS_FETCHED);
                    mHousingComplex.setAddress(address);
                    addressText.setText(mHousingComplex.getAddress());
                    break;
            }
        }
    };

    private void confirmAndDeleteRoom(final Place place) {
        ConfirmDialog.confirmAndDeletePlace(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removedRooms.add(place);
                initRoomAdapter();
                refreshRoomListHeight();
            }
        }, place);
    }

    private void confirmEdit() {

        mHousingComplex.setName(nameText.getText().toString());
        ArrayList<Place> rooms = RVData.getInstance().placeList.getRoomList(mHousingComplex.getId());
        for (Place room : rooms) {
            room.setAddress(mHousingComplex.getName());
        }
        for (Place room : addedRooms) {
            room.setAddress(mHousingComplex.getName());
        }
        RVData.getInstance().placeList.setOrAdd(mHousingComplex);
        RVData.getInstance().placeList.addList(addedRooms);
        RVData.getInstance().placeList.removeList(removedRooms);
        RVData.getInstance().saveData(getActivity());

        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(getActivity());
    }

    private class RoomListAdapter extends BaseAdapter{

        private ArrayList<Place> mRoomList;
        RoomListAdapter(ArrayList<Place> roomList) {
            this.mRoomList = roomList;
        }

        @Override
        public int getCount() {
            return mRoomList.size();
        }

        @Override
        public Object getItem(int position) {
            return mRoomList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView
                        = new RoomListCell(getActivity(),
                                (Place) getItem(position));
            } else {
                ((RoomListCell) convertView).refreshData((Place) getItem(position));
            }

            return convertView;
        }

        boolean hasRoomWithName(String name) {

            for (Place place : mRoomList) {
                if (place.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }

        void addRoom(Place room) {
            mRoomList.add(room);
        }
    }

    class RoomListCell extends FrameLayout {

        private Place mRoom;

        public RoomListCell(Context context, Place room) {
            super(context);

            this.mRoom = room;
            initCommon();
        }

        public RoomListCell(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        private View view;
        private void initCommon() {

            view = LayoutInflater.from(getActivity()).inflate(R.layout.room_list_cell, this);
            initPriorityMarker();
            initNameText();
            initMenuButton();

            refreshData(null);

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    InputUtil.hideSoftKeyboard((Activity) getActivity());

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            RoomListCell.this.setAlpha(0.5f);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            RoomListCell.this.setAlpha(1f);
                            return true;
                        case MotionEvent.ACTION_UP:
                            RoomListCell.this.setAlpha(1f);
                            confirmEdit();
                            if (mListener != null) {
                                mListener.onClickRoomCell(mRoom);
                            }
                            dismiss();
                            return true;
                    }
                    return false;
                }
            });
        }

        private ImageView marker;
        private void initPriorityMarker() {

            marker = (ImageView) findViewById(R.id.place_marker);

        }

        private TextView nameText;
        private void initNameText() {
            nameText = (TextView) view.findViewById(R.id.place_text);
        }

        private void initMenuButton() {
            Button menuButton = (Button) view.findViewById(R.id.edit_button);
            menuButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), RoomListCell.this);
                    popupMenu.getMenuInflater().inflate(R.menu.delete_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.delete) {
                                confirmAndDeleteRoom(mRoom);
                                return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        public void refreshData(@Nullable Place room) {
            if (room != null) {
                mRoom = room;
            }
            marker.setBackgroundResource(Constants.buttonRes[mRoom.getPriority().num()]);
            nameText.setText(mRoom.getName());
        }
    }

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShowing.getAndSet(true)) return;

        try {
            super.show(manager, tag);
        } catch (Exception e) {
            isShowing.set(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowing.set(false);
    }

   }
