package net.c_kogyo.returnvisitorv5.util;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by SeijiShii on 2017/04/02.
 */

public class ViewUtil {
    public static void setOnClickListener(View view, final OnViewClickListener listener) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        v.setAlpha(1f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(1f);
                        listener.onViewClick();
                        return true;
                }
                return false;
            }
        });
    }

    public interface OnViewClickListener {

        void onViewClick();
    }
}
