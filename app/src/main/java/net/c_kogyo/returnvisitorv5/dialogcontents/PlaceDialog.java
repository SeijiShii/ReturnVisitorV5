package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.FrameLayout;
import android.widget.ListView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.view.PlaceCell;
import net.c_kogyo.returnvisitorv5.view.VisitCell;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceDialog extends FrameLayout {

    private Place mPlace;
    private PlaceDialogListener mListener;

    public PlaceDialog(@NonNull Context context, Place place, PlaceDialogListener listener) {
        super(context);

        mListener = listener;
        mPlace = place;

        initCommon();
    }

    public PlaceDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.place_dialog, this);

        initPlaceCell();
        initVisitListView();
        initRecordVisitButton();
        initCancelButton();
    }

    private PlaceCell placeCell;
    private void initPlaceCell(){
        placeCell = (PlaceCell) view.findViewById(R.id.place_cell);
        placeCell.setPlaceAndInitialize(mPlace);
        placeCell.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        placeCell.getEditButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/16 editMenu
                PopupMenu editMenuPopup = new PopupMenu(getContext(), placeCell.getEditButton());
                editMenuPopup.getMenuInflater().inflate(R.menu.place_cell_menu, editMenuPopup.getMenu());
                editMenuPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.delete:

                                ConfirmDialog.confirmAndDeletePlace(getContext(),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (mListener != null) {
                                                    mListener.onDeleteClick(mPlace);
                                                }
                                            }
                                        });
                                return true;
                        }
                        return false;
                    }
                });
                editMenuPopup.show();
            }
        });


    }

    private ListView visitListView;
    private void initVisitListView() {

        visitListView = (ListView) view.findViewById(R.id.visit_list_view);
        visitListView.setAdapter(new VisitListAdapter());

    }

    private void initRecordVisitButton() {
        Button recordVisitButton = (Button) view.findViewById(R.id.record_visit_button);
        recordVisitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/16 Record Visitへの遷移
                if (mListener != null) {
                    mListener.onRecordVisitClick(mPlace);
                }
            }
        });
    }

    private void initCancelButton() {
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/16 Cancel Action
                if (mListener != null) {
                    mListener.onCancelClick();
                }
            }
        });
    }

    class VisitListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return RVData.getInstance().getVisitList().getVisitsForPlace(mPlace.getId()).size();
        }

        @Override
        public Object getItem(int i) {
            return RVData.getInstance().getVisitList().getVisitsForPlace(mPlace.getId()).get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = new VisitCell(getContext(),
                        (Visit) getItem(i),
                        new VisitCell.VisitCellListener() {
                            @Override
                            public void onDeleteClick(final Visit visit) {

                                ConfirmDialog.confirmAndDeleteVisit(getContext(),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                RVData.getInstance().getVisitList().removeById(visit.getId());
                                                RVData.getInstance().saveData(getContext(), null);
                                                notifyDataSetChanged();
                                            }
                                        });
                            }

                            @Override
                            public void onEditClick(Visit visit) {
                                if (mListener != null) {
                                    mListener.onEditVisitClick(visit);
                                }
                            }
                        });
            } else {
                ((VisitCell) view).refreshVisit((Visit) getItem(i));
            }

            return view;
        }



    }

    public interface PlaceDialogListener {

        void onRecordVisitClick(Place place);

        void onCancelClick();

        void onDeleteClick(Place place);

        void onEditVisitClick(Visit visit);
    }

}
