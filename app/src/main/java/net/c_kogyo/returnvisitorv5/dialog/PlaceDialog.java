package net.c_kogyo.returnvisitorv5.dialog;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
import net.c_kogyo.returnvisitorv5.view.PlaceCell;
import net.c_kogyo.returnvisitorv5.view.VisitCell;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceDialog extends DialogFragment {

    private static Place mPlace;
    private static PlaceDialogListener mListener;
    private static PlaceDialog instance;
    
    public static PlaceDialog getInstance(Place place, PlaceDialogListener listener) {

        mListener = listener;
        mPlace = place;

        if (instance == null) {
            instance = new PlaceDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        initCommon();
        builder.setView(view);

        builder.setPositiveButton(R.string.record_visit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onRecordVisitClick(mPlace);
                }
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
        
        
    }

    private View view;
    private void initCommon() {
        view = View.inflate(getActivity(), R.layout.place_dialog, null);

        initPlaceCell();
        initVisitListView();
    }

    private PlaceCell placeCell;
    private void initPlaceCell(){
        placeCell = (PlaceCell) view.findViewById(R.id.place_cell);
        placeCell.setPlaceAndInitialize(mPlace, new PlaceCell.PlaceCellListener() {
            @Override
            public void onDeletePlace(Place place) {
                if (mListener != null) {
                    mListener.onDeleteClick(mPlace);
                }
                dismiss();
            }
        });
        placeCell.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private ListView visitListView;
    private void initVisitListView() {

        visitListView = (ListView) view.findViewById(R.id.visit_list_view);
        visitListView.setAdapter(new VisitListAdapter());

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (visitListView.getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Point point = ViewUtil.getDisplaySize((Activity) getActivity());
                        int height = point.y - (int) (getActivity().getResources().getDisplayMetrics().density * 300);
                        visitListView.getLayoutParams().height = height;
                        visitListView.requestLayout();
                    }
                });
            }
        }).start();

    }

    class VisitListAdapter extends BaseAdapter {

        int initialHeight;
        VisitListAdapter() {
            initialHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                    + (int)(getActivity().getResources().getDisplayMetrics().density * 5);
        }

        @Override
        public int getCount() {
            return RVData.getInstance().visitList.getVisitsForPlace(mPlace.getId()).size();
        }

        @Override
        public Object getItem(int i) {
            return RVData.getInstance().visitList.getVisitsForPlace(mPlace.getId()).get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = new VisitCell(getActivity(),
                        (Visit) getItem(i),
                        initialHeight,
                        new VisitCell.VisitCellListener() {
                            @Override
                            public void postCompressVisitCell(VisitCell visitCell) {

                                RVData.getInstance().visitList.deleteById(visitCell.getVisit().getId());
                                RVData.getInstance().saveData(getActivity());
                                notifyDataSetChanged();

                                RVCloudSync.syncDataIfLoggedIn(getActivity());
                            }

                            @Override
                            public void onEditClick(Visit visit) {
                                if (mListener != null) {
                                    mListener.onEditVisitClick(visit);
                                }
                                dismiss();
                            }

                            @Override
                            public void onClickToMap(Visit visit) {

                            }

                            @Override
                            public void onUpdateHeight() {
                                visitListView.requestLayout();
                            }
                        },
                        VisitCell.HeaderContent.DATETIME){
                    @Override
                    public void setLayoutParams(BaseAnimateView view) {
                        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    }

                };
            } else {
                ((VisitCell) view).refreshVisit((Visit) getItem(i));
            }

            return view;
        }

    }

    public interface PlaceDialogListener {

        void onRecordVisitClick(Place place);

        void onDeleteClick(Place place);

        void onEditVisitClick(Visit visit);
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
