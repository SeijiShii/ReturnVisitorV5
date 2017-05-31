package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/05/26.
 */

public class SearchCell extends FrameLayout {

    private Object mObject;
    private SearchCellListener mListener;

    public SearchCell(@NonNull Context context, Object data, SearchCellListener listener) {
        super(context);

        mObject = data;
        mListener = listener;

        initCommon();
    }

    public SearchCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private ImageView marker;
    private TextView textView;

    private void initCommon() {
        view = View.inflate(getContext(), R.layout.search_cell, this);

        marker = (ImageView) view.findViewById(R.id.marker);
        textView = (TextView) view.findViewById(R.id.text_view);

        refreshData(null);

        ViewUtil.setOnClickListener(this, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                if (mListener != null) {
                    mListener.onClick(mObject);
                }
            }
        });
    }

    public void refreshData(@Nullable Object data) {
        if (data != null) {
            mObject = data;
        }

        if (mObject instanceof Place) {
            Place place = (Place) mObject;

            switch (place.getCategory()) {
                case HOUSE:
                    marker.setBackgroundResource(Constants.markerRes[place.getPriority().num()]);
                    break;
                case ROOM:
                    marker.setBackgroundResource(Constants.buttonRes[place.getPriority().num()]);
                    break;
                case HOUSING_COMPLEX:
                    marker.setBackgroundResource(Constants.complexRes[place.getPriority().num()]);
                    break;
            }

            textView.setText(place.toString());

        } else if (mObject instanceof Person) {
            Person person = (Person) mObject;
            marker.setBackgroundResource(Constants.buttonRes[person.getPriority().num()]);

            textView.setText(person.toString(getContext()));
        }
    }

    public Object getData() {
        return mObject;
    }

    public interface SearchCellListener {
        void onClick(Object data);
    }
}
