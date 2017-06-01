package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class ToggleColorButton extends RelativeLayout {

    private boolean mChecked;

    private int mTrueRes, mFalseRes;

    public ToggleColorButton(Context context, int trueRes, int falseRes, boolean initialState) {
        super(context);

        mTrueRes = trueRes;
        mFalseRes = falseRes;
        mChecked = initialState;

        initCommon();
    }

    public ToggleColorButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ToggleColorButton,
                0, 0);
        mTrueRes = array.getResourceId(R.styleable.ToggleColorButton_trueResId, R.mipmap.button_maker_orange);
        mFalseRes = array.getResourceId(R.styleable.ToggleColorButton_falseResId, R.mipmap.button_marker_gray);
        mChecked = array.getBoolean(R.styleable.ToggleColorButton_initialState, false);

        initCommon();
    }

    private ImageView imageView;
    private void initCommon() {

        int dp40 = (int)(getContext().getResources().getDisplayMetrics().density * 40);
        this.setLayoutParams(new ViewGroup.LayoutParams(dp40, dp40));

        imageView = new ImageView(getContext());
        int dp30 = (int)(getContext().getResources().getDisplayMetrics().density * 30);
        RelativeLayout.LayoutParams params = new LayoutParams(dp30, dp30);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(params);

        this.addView(imageView);

        refreshBackground();

        ViewUtil.setOnClickListener(this, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                mChecked = !mChecked;
                refreshBackground();

                if (mListener != null) {
                    mListener.onCheckChange(mChecked);
                }
            }
        });

    }

    private void refreshBackground() {
        if (mChecked) {
            imageView.setBackgroundResource(mTrueRes);
        } else {
            imageView.setBackgroundResource(mFalseRes);
        }
    }

    private CheckChangeListener mListener;
    public void setCheckChangeListener(CheckChangeListener listener) {
        mListener = listener;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
    }

    public interface CheckChangeListener {
        void onCheckChange(boolean checked);
    }
}
