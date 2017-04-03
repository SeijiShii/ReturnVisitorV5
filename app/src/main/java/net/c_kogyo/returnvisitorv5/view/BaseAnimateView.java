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

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/18.
 */

public abstract class BaseAnimateView extends FrameLayout {

    int mResId;
    int mInitialHeight;
    float multi;

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

    private View view;

    private void initCommon(){

        view = LayoutInflater.from(getContext()).inflate(mResId, this);

        multi = getContext().getResources().getDisplayMetrics().density / 3;

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
                        // TODO: 2017/04/03 要検証
                        if (extractHeight == LayoutParams.WRAP_CONTENT) {
                            BaseAnimateView.this.measure(0, 0);
                            int height2 = getMeasuredHeight();
                            changeViewHeight(height2, true, null, listener);
                        } else {
                            changeViewHeight(extractHeight, true, null, listener);
                        }
                    }
                });
            }
        }).start();
    }


    public void changeViewHeight(int targetHeight,
                                 boolean toAnimate,
                                 final HeightUpdateListener heightUpdateListener,
                                 Animator.AnimatorListener animatorListener) {
        int originHeight = getMeasuredHeight();
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

    public abstract void setLayoutParams(BaseAnimateView view);
}
