package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
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
        placeCell.setPlaceAndInitialize(mPlace, new PlaceCell.PlaceCellListener() {
            @Override
            public void onDeletePlace(Place place) {
                if (mListener != null) {
                    mListener.onDeleteClick(mPlace);
                }
            }
        });
        placeCell.setOnTouchListener(new OnTouchListener() {
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

        int initialHeight;
        VisitListAdapter() {
            initialHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                    + (int)(getContext().getResources().getDisplayMetrics().density * 5);
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
                view = new VisitCell(getContext(),
                        (Visit) getItem(i),
                        initialHeight,
                        new VisitCell.VisitCellListener() {
                            @Override
                            public void onDeleteVisit(final VisitCell visitCell) {

                                visitCell.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        RVData.getInstance().visitList.deleteById(visitCell.getVisit().getId());
                                        RVData.getInstance().saveData(getContext(), null);
                                        notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                                ConfirmDialog.confirmAndDeleteVisit(getContext(),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                            }

                            @Override
                            public void onEditClick(Visit visit) {
                                if (mListener != null) {
                                    mListener.onEditVisitClick(visit);
                                }
                            }

                            @Override
                            public void onClickToMap(Visit visit) {

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

        void onCancelClick();

        void onDeleteClick(Place place);

        void onEditVisitClick(Visit visit);
    }

}
