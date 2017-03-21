package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.data.Tag;

/**
 * Created by SeijiShii on 2017/02/22.
 */

public class SmallTagView extends AppCompatTextView{

    private Tag mTag;
    private final int TAG_HEIGHT = 30;
    private int viewWidth;

    public SmallTagView(Context context, Tag tag) {
        super(context, null);

        this.mTag = tag;

        initCommon();
    }

    public SmallTagView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initCommon() {

        this.setBackgroundResource(R.drawable.green_grade_circle);

        int height = (int) (TAG_HEIGHT * getContext().getResources().getDisplayMetrics().density);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height));

        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        this.setText(mTag.getName());
        this.setTextColor(getContext().getResources().getColor(R.color.textColorGray));
        this.setGravity(Gravity.CENTER);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_normal);
        this.setPadding(padding, 0, padding, 0);

        this.measure(0, 0);
        this.viewWidth = getMeasuredWidth();
    }

    public int getViewWidth() {
        return viewWidth;
    }

//    public void setViewWidth(int viewWidth) {
//        this.viewWidth = viewWidth;
//    }
}
