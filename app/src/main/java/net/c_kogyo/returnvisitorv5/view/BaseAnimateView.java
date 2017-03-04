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
    float multi;

    public enum InitialHeightCondition{
        ZERO(0),
        EX_HEIGHT(1);
//        EXTRACT_POST_DRAWN(2);

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
        TO_EX_HEIGHT,
        TO_TARGET_HEIGHT
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

        multi = getContext().getResources().getDisplayMetrics().density / 3;

        switch (mInitCondition) {
            case ZERO:
                this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                break;
            case EX_HEIGHT:
                this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mExHeight));
                break;
//            case EXTRACT_POST_DRAWN:
//                this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
//                extractPostDrawn();
//                break;
        }

    }

    public View getViewById(int resId) {
        return view.findViewById(resId);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void extractPostDrawn() {

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

                        changeViewHeight(AnimateCondition.FROM_0_TO_EX_HEIGHT, 0, true, null, null);
                    }
                });
            }
        }).start();
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
                                 int targetHeight,
                                 boolean toAnimate,
                                 final HeightUpdateListener heightUpdateListener,
                                 Animator.AnimatorListener animatorListener) {
        int originHeight = 0;
        int mTargetHeight = 0;

        switch (condition) {
            case FROM_0_TO_EX_HEIGHT:
                originHeight = 0;
                mTargetHeight = mExHeight;
                break;
            case FROM_EX_HEIGHT_TO_ZERO:
                originHeight = mExHeight;
                mTargetHeight = 0;
                break;
            case TO_EX_HEIGHT:
                originHeight = getMeasuredHeight();
                mTargetHeight = mExHeight;
                break;
            case TO_TARGET_HEIGHT:
                originHeight = getMeasuredHeight();
                mTargetHeight = targetHeight;
                break;
        }

        if (!toAnimate) {
            BaseAnimateView.this.getLayoutParams().height = mTargetHeight;
            BaseAnimateView.this.requestLayout();
        } else {

            ValueAnimator animator = ValueAnimator.ofInt(originHeight, mTargetHeight);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    BaseAnimateView.this.getLayoutParams().height = (int) animation.getAnimatedValue();
                    BaseAnimateView.this.requestLayout();

                    if (heightUpdateListener != null) heightUpdateListener.onUpdate();
                }
            });

            int duration =  (int) (Math.abs(mTargetHeight - originHeight) * multi);
            animator.setDuration(duration);

            if (animatorListener != null) animator.addListener(animatorListener);

            animator.start();
        }
    }

    public interface HeightUpdateListener {
        void onUpdate();
    }

    public void setExHeight(int exHeight) {
        this.mExHeight = exHeight;
    }
}
