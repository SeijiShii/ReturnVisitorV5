package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.DataItem;

/**
 * Created by SeijiShii on 2017/02/22.
 */

public class SmallTagView extends TextView{

        private DataItem mTag;
        private final int TAG_HEIGHT = 30;

        public SmallTagView(Context context, DataItem tag) {
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
            this.setTextSize(12f);
            this.setText(mTag.getName());
            this.setTextColor(getContext().getResources().getColor(R.color.textColorGray));
            this.setGravity(Gravity.CENTER);
            this.setPadding(30, 0, 30, 0);
        }
}
