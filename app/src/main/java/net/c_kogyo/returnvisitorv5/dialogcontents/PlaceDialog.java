package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.view.PlaceCell;
import net.c_kogyo.returnvisitorv5.view.VisitCell;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceDialog extends FrameLayout {

    private Place mPlace;

    public PlaceDialog(@NonNull Context context, Place place) {
        super(context);

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
    }

    private ListView visitListView;
    private void initVisitListView() {

        visitListView = (ListView) view.findViewById(R.id.visit_list_view);
        visitListView.setAdapter(new VisitListAdapter());

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
                view = new VisitCell(getContext(), (Visit) getItem(i));
            } else {
                ((VisitCell) view).refreshVisit((Visit) getItem(i));
            }

            return view;
        }
    }
}
