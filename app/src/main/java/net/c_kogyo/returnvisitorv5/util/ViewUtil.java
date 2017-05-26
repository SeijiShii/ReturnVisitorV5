package net.c_kogyo.returnvisitorv5.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

/**
 * Created by SeijiShii on 2017/04/02.
 */

public class ViewUtil {
    public static void setOnClickListener(View view, final OnViewClickListener listener) {

        if (listener == null) {
            view.setOnTouchListener(null);
        } else {
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
    }

    public interface OnViewClickListener {

        void onViewClick();
    }

    /**
     * Used to scroll to the given view.
     *
     * @param scrollViewParent Parent ScrollView
     * @param view View to which we need to scroll.
     */
    public static void scrollToView(final ScrollView scrollViewParent, final View view) {
        // Get deepChild Offset
        Point childOffset = new Point();
        getDeepChildOffset(scrollViewParent, view.getParent(), view, childOffset);
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y);
    }

    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumalated Offset.
     */
    private static void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
    }

    public static void fadeView(final View view,
                                final boolean fadeIn,
                                @Nullable View.OnTouchListener onTouchListener,
                                @Nullable final PostFadeViewListener fadeViewListener,
                                int duration) {
        float origin, target;

        if (fadeIn) {
            origin = 0f;
            target = 1f;

            if (onTouchListener != null) {
                view.setOnTouchListener(onTouchListener);
            }
            view.setVisibility(View.VISIBLE);

        } else {
            origin = 1f;
            target = 0f;
            view.setOnTouchListener(null);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(origin, target);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!fadeIn) {
                    view.setVisibility(View.INVISIBLE);
                }
                if (fadeViewListener != null) {
                    fadeViewListener.postFade(view);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void halfFadeView(final View view,
                                final boolean fadeIn,
                                @Nullable View.OnTouchListener onTouchListener,
                                @Nullable final PostFadeViewListener fadeViewListener,
                                int duration) {
        float origin, target;

        if (fadeIn) {
            origin = 0.5f;
            target = 1f;

            if (onTouchListener != null) {
                view.setOnTouchListener(onTouchListener);
            }

        } else {
            origin = 1f;
            target = 0.5f;
            view.setOnTouchListener(null);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(origin, target);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (fadeViewListener != null) {
                    fadeViewListener.postFade(view);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public interface PostFadeViewListener {
        void postFade(View view);
    }

    public static Point getDisplaySize(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }



}
