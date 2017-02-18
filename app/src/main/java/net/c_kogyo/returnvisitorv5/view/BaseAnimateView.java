package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/18.
 */

public class BaseAnimateView extends FrameLayout {

    int mExHeight;
    int mResId;
    InitialHeightCondition mInitCondition;

    public enum InitialHeightCondition{
        ZERO(0),
        EX_HEIGHT(1);

        private final int num;

        InitialHeightCondition(int num) {
            this.num = num;
        }

        public static InitialHeightCondition getEnum(int num) {
            InitialHeightCondition[] enumArray = InitialHeightCondition.values();
            for (InitialHeightCondition condition : enumArray) {
                if (condition.num == num) return condition;
            }
            return null;
        }

        public int num() {
            return num;
        }
    }

    public BaseAnimateView(Context context, int exHeight, InitialHeightCondition initCondition, int resId) {
        super(context);

        this.mResId = resId;
        this.mExHeight = exHeight;
        this.mInitCondition = initCondition;

        initCommon();
    }

    public BaseAnimateView(Context context, AttributeSet attrs, int resId) {
        super(context, attrs);

        mResId = resId;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BaseAnimateView,
                0, 0);
        mExHeight = a.getDimensionPixelSize(R.styleable.BaseAnimateView_exHeight, 0);
        mInitCondition = InitialHeightCondition.getEnum(a.getInteger(R.styleable.BaseAnimateView_initialHeightCondition, 0));

        initCommon();
    }

    private View view;

    private void initCommon(){

        view = LayoutInflater.from(getContext()).inflate(mResId, this);

        setHeightPostDrawn();

    }

    public View getViewById(int resId) {
        return view.findViewById(resId);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    private void setHeightPostDrawn() {

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (BaseAnimateView.this.getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = 0;
                        if (mInitCondition == InitialHeightCondition.EX_HEIGHT) {
                            height = mExHeight;
                        }
                        // 親がLinearLayoutでなくてはならない。
                        BaseAnimateView.this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
                        requestLayout();
                    }
                });
            }
        }).start();
    }
}
