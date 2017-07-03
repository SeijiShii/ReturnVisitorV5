package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/05/09.
 */

public class RightTextSwitch extends LinearLayout {

    private String mCheckedText, mUncheckedText;
    private boolean mIsChecked;
    public RightTextSwitch(Context context,
                           RightTextSwitchOnCheckChangeListener listener) {
        super(context);

        mListener = listener;
        initCommon();
    }

    public RightTextSwitch(Context context,
                           @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RightTextSwitch,
                0, 0);
        mCheckedText = a.getString(R.styleable.RightTextSwitch_checked_text);
        mUncheckedText = a.getString(R.styleable.RightTextSwitch_unchecked_text);

        if(mUncheckedText == null)
            mUncheckedText = mCheckedText;

        initCommon();
    }

    private void initCommon() {

        this.mIsClickable = true;

        this.setOrientation(HORIZONTAL);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        initSwitch();
        initTextView();

        refreshClickable();

    }

    private boolean mIsClickable;
    private void refreshClickable() {
        if (mIsClickable) {
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            RightTextSwitch.this.setAlpha(0.5f);
                            return true;
                        case MotionEvent.ACTION_UP:
                            RightTextSwitch.this.setAlpha(1f);
                            setChecked(!mIsChecked);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            RightTextSwitch.this.setAlpha(1f);
                            return true;
                    }
                    return true;
                }
            });
        } else {
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }

    public void setChecked(boolean checked) {

        if (mIsChecked == checked)
            return;

        mIsChecked = checked;
        aSwitch.setChecked(checked);
        if (mIsChecked) {
            textView.setAlpha(1f);
            textView.setText(mCheckedText);
        } else {
            textView.setAlpha(0.7f);
            textView.setText(mUncheckedText);
        }

        if (mListener != null) {
            mListener.onCheckChange(mIsChecked);
        }

    }

    private RightTextSwitchOnCheckChangeListener mListener;
    public void setOnCheckChangeListener(RightTextSwitchOnCheckChangeListener listener) {
        mListener = listener;
    }

    private SwitchCompat aSwitch;
    private void initSwitch() {
        aSwitch = new SwitchCompat(getContext());
        aSwitch.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        aSwitch.setChecked(mIsChecked);
        aSwitch.setClickable(false);
        this.addView(aSwitch);
    }

    private TextView textView;
    private void initTextView() {
        textView = new TextView(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (getContext().getResources().getDisplayMetrics().density * 10);
        params.setMargins(margin, 0, 0, 0);
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        textView.setTextColor(getContext().getResources().getColor(R.color.textColorGray));

        if (mIsChecked) {
            textView.setText(mCheckedText);
        } else {
            textView.setText(mUncheckedText);
        }
        textView.setGravity(Gravity.CENTER + Gravity.LEFT);
        this.addView(textView);
    }

    public interface RightTextSwitchOnCheckChangeListener{
        void onCheckChange(boolean checked);
    }

    @Override
    public void setClickable(boolean clickable) {
        mIsClickable = clickable;
        refreshClickable();
    }
}
