package net.c_kogyo.returnvisitorv5.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/03/17.
 */

public class MapLongClickDialog extends FrameLayout {

    private MapLongClickDialogListener mListener;

    public MapLongClickDialog(@NonNull Context context, MapLongClickDialogListener listener) {
        super(context);

        this.mListener = listener;

        initCommon();
    }

    public MapLongClickDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.map_long_click_dialog, this);

        initNewSinglePlaceButton();
        initHousingComplexButton();
        initRecordNotHomeButton();
        initCancelButton();
    }

    private void initNewSinglePlaceButton() {
        Button newSinglePlaceButton = (Button) view.findViewById(R.id.record_new_place_button);
        newSinglePlaceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/17 New Place Action
                if (mListener != null) {
                    mListener.onClickNewSinglePlaceButton();
                }
            }
        });
    }

    private void initHousingComplexButton() {
        Button housingComplexButton = (Button) view.findViewById(R.id.record_as_complex_button);
        housingComplexButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/17 housing complex action
                if (mListener != null) {
                    mListener.onClickHousingComplexButton();
                }
            }
        });
    }

    private void initRecordNotHomeButton() {
        Button notHomeButton = (Button) view.findViewById(R.id.record_not_home_button);
        notHomeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/17 Not Home Action
                if (mListener != null) {
                    mListener.onClickNotHomeButton();
                }
            }
        });
    }

    private void initCancelButton() {
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/17 cancel action
                if (mListener != null) {
                    mListener.onClickCancelButton();
                }
            }
        });
    }

    public interface MapLongClickDialogListener {

        void onClickNewSinglePlaceButton();

        void onClickHousingComplexButton();

        void onClickNotHomeButton();

        void onClickCancelButton();
    }

}
