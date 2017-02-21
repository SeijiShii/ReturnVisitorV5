package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.data.DataItem;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class TagView extends FrameLayout {

    private DataItem tag;

    public TagView(Context context, DataItem tag) {
        super(context);

        this.tag = tag;
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



}
