package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.c_kogyo.returnvisitorv5.data.Tag;

/**
 * Created by SeijiShii on 2017/06/01.
 */

public class SmallTagToggleButton extends SmallTagView {

    private boolean mChecked;
    private CheckChangeListener mListener;

    public SmallTagToggleButton(Context context,
                                Tag tag,
                                boolean checked,
                                CheckChangeListener listener) {
        super(context, tag);

        mChecked = checked;
        mListener = listener;

        refreshAlpha();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.5f);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        refreshAlpha();
                        return true;
                    case MotionEvent.ACTION_UP:
                        mChecked = !mChecked;
                        if (mListener != null) {
                            mListener.onCheckChange(mTag, mChecked);
                        }
                        refreshAlpha();
                        return true;
                }
                return false;
            }
        });
    }

    public SmallTagToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void refreshAlpha() {
        if (mChecked) {
            setAlpha(1f);
        } else {
            setAlpha(0.5f);
        }
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshAlpha();
    }

    public interface CheckChangeListener {
        void onCheckChange(Tag tag, boolean checked);
    }

}
