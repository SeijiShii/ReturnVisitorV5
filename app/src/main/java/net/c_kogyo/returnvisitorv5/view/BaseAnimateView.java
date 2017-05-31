package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/18.
 */

public abstract class BaseAnimateView extends FrameLayout {

    int mResId;
    int mInitialHeight;
    float multi;

    private BaseAnimateViewListener mListener;

    public BaseAnimateView(Context context, int initialHeight,int resId) {
        super(context);
        this.mInitialHeight = initialHeight;
        this.mResId = resId;

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

        initCommon();
    }

    public void setListener(@Nullable BaseAnimateViewListener listener) {

        mListener = listener;
    }

    private View view;

    private void initCommon(){

        view = LayoutInflater.from(getContext()).inflate(mResId, this);

        multi = getContext().getResources().getDisplayMetrics().density / 1.5f;

        setLayoutParams(this);

        this.getLayoutParams().height = mInitialHeight;

    }

    public View getViewById(int resId) {
        return view.findViewById(resId);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void extractPostDrawn(final int extractHeight) {

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
                        // DONE: 2017/04/03 要検証
                        changeViewHeight(extractHeight, true, true, null);
                    }
                });
            }
        }).start();
    }


    public void changeViewHeight(int targetHeight,
                                 boolean toAnimate,
                                 boolean isInitialExtract,
                                 @Nullable PostAnimationListener postAnimationListener) {
        int originHeight = getMeasuredHeight();
        changeViewHeight(originHeight,
                targetHeight,
                toAnimate,
                isInitialExtract,
                postAnimationListener);
    }

    private void changeViewHeight(final int originHeight,
                                  final int targetHeight,
                                  boolean toAnimate,
                                  final boolean isInitialExtract,
                                  @Nullable final PostAnimationListener postAnimationListener) {

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

                    if (mListener != null) {
                        mListener.onUpdateHeight();
                    }
                }
            });

            int duration =  (int) (Math.abs(targetHeight - originHeight) * multi);
            animator.setDuration(duration);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    if (isInitialExtract && originHeight <= 0 && targetHeight > 0) {
                        if (mListener != null) {
                            mListener.postInitialExtract(BaseAnimateView.this);
                        }
                    }
                    if (postAnimationListener != null) {
                        postAnimationListener.postAnimate(BaseAnimateView.this);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.start();
        }

    }

    public void compress(PostAnimationListener postAnimationListener) {
        changeViewHeight(0, true, false, postAnimationListener);
    }

    public abstract void setLayoutParams(BaseAnimateView view);

    public interface BaseAnimateViewListener {

        void onUpdateHeight();

        void postInitialExtract(BaseAnimateView view);

    }

    public interface PostAnimationListener {

        void postAnimate(BaseAnimateView view);
    }

}
