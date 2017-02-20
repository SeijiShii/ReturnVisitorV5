package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
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

    public enum AnimateCondition {
        FROM_0_TO_EX_HEIGHT,
        FROM_EX_HEIGHT_TO_ZERO,
        TO_EX_HEIGHT
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

//        setHeightPostDrawn();

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

    public void changeViewHeight(AnimateCondition condition,
                                 boolean toAnimate,
                                 final HeightUpdateListener heightUpdateListener,
                                 Animator.AnimatorListener animatorListener) {
        int originHeight = 0;
        int targetHeight = 0;

        switch (condition) {
            case FROM_0_TO_EX_HEIGHT:
                originHeight = 0;
                targetHeight = mExHeight;
                break;
            case FROM_EX_HEIGHT_TO_ZERO:
                originHeight = mExHeight;
                targetHeight = 0;
                break;
            case TO_EX_HEIGHT:
                originHeight = getMeasuredHeight();
                targetHeight = mExHeight;
                break;
        }

        if (!toAnimate) {
            BaseAnimateView.this.getLayoutParams().height = targetHeight;
            BaseAnimateView.this.requestLayout();
        } else {

            ValueAnimator animator = ValueAnimator.ofInt(originHeight, targetHeight);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    BaseAnimateView.this.getLayoutParams().height = (int) animation.getAnimatedValue();
                    BaseAnimateView.this.requestLayout();

                    if (heightUpdateListener != null) heightUpdateListener.onUpdate();
                }
            });

            int duration = Math.abs(targetHeight - originHeight) * 3;
            animator.setDuration(duration);

            if (animatorListener != null) animator.addListener(animatorListener);

            animator.start();
        }
    }

    public interface HeightUpdateListener {
        void onUpdate();
    }
}