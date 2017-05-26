package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/05/26.
 */

public class TermBar extends FrameLayout {

    public static final String TAG = "TermBar";

    public TermBar(@NonNull Context context) {
        super(context);

        initCommon();
    }

    public TermBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initCommon();
    }

    private View view;
    private void initCommon() {
        view = View.inflate(getContext(), R.layout.term_bar, this);

        initToggle();
        initStateText();
    }

    private RelativeLayout toggle;
    private void initToggle() {
        toggle = (RelativeLayout) view.findViewById(R.id.bar_toggle);

        toggle.setOnTouchListener(new OnXSlideListener());

    }

    private TextView stateText;
    private void initStateText() {
        stateText = (TextView) view.findViewById(R.id.state_text);
    }

    private class OnXSlideListener implements View.OnTouchListener {

        private float downX;
        private int downLeftMargin;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // ViewGroup.MarginLayoutParamsでキャストすることで
            // FrameLayoutの子要素であっても同様に扱える。
            final ViewGroup.MarginLayoutParams param =
                    (ViewGroup.MarginLayoutParams)v.getLayoutParams();

            if( event.getAction() == MotionEvent.ACTION_DOWN ){

                downX = event.getRawX();

                downLeftMargin = param.leftMargin;

                return true;
            }
            else if( event.getAction() == MotionEvent.ACTION_MOVE){

                param.leftMargin = downLeftMargin + (int)(event.getRawX() - downX);

                int margin5dp = (int) (getContext().getResources().getDisplayMetrics().density * 5);
                if (param.leftMargin > margin5dp && param.leftMargin < TermBar.this.getWidth() - (toggle.getWidth() + margin5dp)) {
                    v.layout(
                            param.leftMargin,
                            param.topMargin,
                            param.leftMargin + v.getWidth(),
                            param.topMargin + v.getHeight());
                    int rate = (int)((float)(param.leftMargin - margin5dp) / (float)(TermBar.this.getWidth() - (toggle.getWidth() + margin5dp * 2)) * 100f);
                    Log.d(TAG, "Rate: " + rate);
                }

                return true;
            }

            return false;
        }
    }
}
