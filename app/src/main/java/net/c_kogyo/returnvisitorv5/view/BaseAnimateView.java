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
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/18.
 */

public abstract class BaseAnimateView extends FrameLayout {

//    int mCollapseHeight, mExtractHeight;
    int mResId;
    int mInitialHeight;
//    HeightCondition mInitCondition;
    float multi;

//    public enum HeightCondition {
//
//        ZERO(0),
//        COLLAPSE(1),
//        EXTRACT(2);
//
//        private final int num;
//
//        HeightCondition(int num) {
//            this.num = num;
//        }
//
//        public static HeightCondition getEnum(int num) {
//            HeightCondition[] enumArray = HeightCondition.values();
//            for (HeightCondition condition : enumArray) {
//                if (condition.num == num) return condition;
//            }
//            return null;
//        }
//
//        public int num() {
//            return num;
//        }
//    }

//    public enum AnimateCondition {
//        FROM_0_TO_EX_HEIGHT,
//        FROM_EX_HEIGHT_TO_ZERO,
//        TO_EX_HEIGHT,
//        TO_TARGET_HEIGHT
//    }

    public BaseAnimateView(Context context, int initialHeight,int resId) {
        super(context);

        this.mInitialHeight = initialHeight;
        this.mResId = resId;
//        this.mCollapseHeight = collapseHeight;
//        this.mExtractHeight = extractHeight;
//        this.mInitCondition = initCondition;

        initCommon();
    }

    public BaseAnimateView(Context context, AttributeSet attrs, int resId) {
        super(context, attrs);

        mResId = resId;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BaseAnimateView,
                0, 0);
        mInitialHeight = a.getDimensionPixelSize(R.styleable.BaseAnimateView_initialHeight, 0);
//        mCollapseHeight = a.getDimensionPixelSize(R.styleable.BaseAnimateView_collapseHeight, 0);
//        mExtractHeight = a.getDimensionPixelSize(R.styleable.BaseAnimateView_extractHeight, 0);
//        mInitCondition = HeightCondition.getEnum(a.getInteger(R.styleable.BaseAnimateView_initialHeightCondition, 0));

        initCommon();
    }

    private View view;

    private void initCommon(){

        view = LayoutInflater.from(getContext()).inflate(mResId, this);

        multi = getContext().getResources().getDisplayMetrics().density / 3;

        setLayoutParams();

        this.getLayoutParams().height = mInitialHeight;

        // DONE: 2017/03/19 親のLayoutParams
//        switch (mInitCondition) {
//            case COLLAPSE:
//                this.getLayoutParams().height = mCollapseHeight;
//                break;
//            case EXTRACT:
//                this.getLayoutParams().height = mExtractHeight;
//                break;
//        }

    }

    public View getViewById(int resId) {
        return view.findViewById(resId);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void extractPostDrawn(final int extractHeight, final Animator.AnimatorListener listener) {

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

                        changeViewHeight(extractHeight, true, null, listener);
                    }
                });
            }
        }).start();
    }

//    private void setHeightPostDrawn() {
//
//        final Handler handler = new Handler();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (BaseAnimateView.this.getWidth() <= 0) {
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        //
//                    }
//                }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        int height = 0;
//                        if (mInitCondition == HeightCondition.EXTRACT) {
//                            height = mExtractHeight;
//                        }
//                        // 親がLinearLayoutでなくてはならない。
//                        BaseAnimateView.this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
//                        requestLayout();
//                    }
//                });
//            }
//        }).start();
//    }

    public void changeViewHeight(int targetHeight,
                                 boolean toAnimate,
                                 final HeightUpdateListener heightUpdateListener,
                                 Animator.AnimatorListener animatorListener) {
        int originHeight = getMeasuredHeight();
//        int targetHeight
//                targetHeight = 0;
//
//        switch (targetCondition) {
//            case COLLAPSE:
//                originHeight = mExtractHeight;
//                targetHeight = mCollapseHeight;
//                break;
//            case EXTRACT:
//                originHeight = mCollapseHeight;
//                targetHeight = mExtractHeight;
//                break;
//        }

        changeViewHeight(originHeight, targetHeight, toAnimate, heightUpdateListener, animatorListener);

    }

    private void changeViewHeight(int originHeight,
                                  int targetHeight,
                                  boolean toAnimate,
                                  final HeightUpdateListener heightUpdateListener,
                                  Animator.AnimatorListener animatorListener) {

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

            int duration =  (int) (Math.abs(targetHeight - originHeight) * multi);
            animator.setDuration(duration);

            if (animatorListener != null) animator.addListener(animatorListener);

            animator.start();
        }

    }

    public interface HeightUpdateListener {
        void onUpdate();
    }

//    public void setExtractHeight(int exHeight) {
//        this.mExtractHeight = exHeight;
//    }

    public abstract void setLayoutParams();
}
