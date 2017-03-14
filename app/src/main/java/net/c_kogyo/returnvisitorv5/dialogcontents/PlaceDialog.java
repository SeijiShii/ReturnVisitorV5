package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.data.Place;

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

    private void initCommon() {

    }
}
