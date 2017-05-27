package net.c_kogyo.returnvisitorv5.activity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class VisitSuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_suggestion_activity);

        initFilterFrame();
    }

    private LinearLayout filterFrame;
    private boolean mIsFilterOpen = false;
    private void initFilterFrame() {
        filterFrame = (LinearLayout) findViewById(R.id.filter_frame);

        initFilterToggleBar();
    }

    private RelativeLayout filterToggleBar;
    private void initFilterToggleBar() {
        filterToggleBar = (RelativeLayout) findViewById(R.id.filter_toggle_bar);

        ViewUtil.setOnClickListener(filterToggleBar, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                openCloseFilter();
            }
        });
    }

    private void openCloseFilter() {

        ImageView toggleArrow = (ImageView) findViewById(R.id.toggle_arrow);

        int closeHeight = getResources().getDimensionPixelSize(R.dimen.filter_frame_height_close);
        int openHeight = getResources().getDimensionPixelSize(R.dimen.filter_frame_height_open);

        int origin, target;

        if (mIsFilterOpen) {
            origin = openHeight;
            target = closeHeight;

            toggleArrow.setBackgroundResource(R.drawable.white_upper_arrow);

        } else {
            origin = closeHeight;
            target = openHeight;

            toggleArrow.setBackgroundResource(R.drawable.white_down_arrow);
        }

        ValueAnimator animator = ValueAnimator.ofInt(origin, target);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                filterFrame.getLayoutParams().height = (int) animation.getAnimatedValue();
                filterFrame.requestLayout();
            }
        });
        animator.setDuration(200);
        animator.start();

        mIsFilterOpen = !mIsFilterOpen;
    }
}
