package net.c_kogyo.returnvisitorv5.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.fragment.DefaultPublicationListFragment;
import net.c_kogyo.returnvisitorv5.fragment.RankedPublicationListFragment;
import net.c_kogyo.returnvisitorv5.fragment.SwitchablePagerBaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by SeijiShii on 2017/05/24.
 */

public class SwitchablePager <T extends SwitchablePagerBaseFragment> extends LinearLayout implements View.OnClickListener {

    public SwitchablePager(Context context) {
        super(context);

        initCommon();
    }

    public SwitchablePager(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initCommon();
    }

    private View view;
    private LinearLayout switchContainer;
    private FrameLayout barContainer;
    private ViewPager viewPager;

    private void initCommon() {

        view = LayoutInflater.from(getContext()).inflate(R.layout.switchable_pager, this);

        switchContainer = (LinearLayout) view.findViewById(R.id.switch_container);
        barContainer = (FrameLayout) view.findViewById(R.id.bar_container);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
    }

    private ArrayList<T> mContents;
    private ViewContentAdapter mAdapter;

    private Handler handler;
    public void setContents(List<T> contents, FragmentManager fragmentManager) {

        mContents = new ArrayList<>(contents);

        mAdapter = new ViewContentAdapter(fragmentManager);
        viewPager.setAdapter(mAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                slideBar();
                refreshButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (SwitchablePager.this.getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initSwitches();
                        initBar();

                        slideBar();
                        refreshButtons();
                    }
                });
            }
        }).start();
    }


    private Button[] buttons;
    private void initSwitches() {
//        int buttonWidth = switchContainer.getWidth() / 2;
        buttons = new Button[mContents.size()];
        for (int i = 0 ; i < mContents.size() ; i++) {
            buttons[i] = new Button(getContext());

            LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            buttons[i].setLayoutParams(params);

            buttons[i].setText(mContents.get(i).getTitle());
            buttons[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            buttons[i].setGravity(Gravity.CENTER);
            buttons[i].setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            buttons[i].setBackground(null);
            buttons[i].setOnClickListener(this);

            switchContainer.addView(buttons[i]);
        }

    }

    private View bar;
    private int barWidth;
    private void initBar() {

        int containerWidth = barContainer.getWidth();

        barWidth = containerWidth / mContents.size();
        bar = new View(getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(barWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(barWidth * viewPager.getCurrentItem(), 0, 0, 0);
        bar.setLayoutParams(params);

        bar.setBackgroundResource(R.color.colorPrimary);

        barContainer.addView(bar);
    }

    private void slideBar() {

        int targetMargin = viewPager.getCurrentItem() * barWidth;
        ValueAnimator animator = ValueAnimator.ofInt(((MarginLayoutParams) bar.getLayoutParams()).leftMargin, targetMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

//                ((MarginLayoutParams) bar.getLayoutParams()).leftMargin = (Integer) animation.getAnimatedValue();

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(barWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins((int) animation.getAnimatedValue(), 0, 0, 0);
                bar.setLayoutParams(params);
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private void refreshButtons() {

        for (int i = 0 ; i < buttons.length ; i++ ) {
            if (i == viewPager.getCurrentItem()) {
                buttons[i].setAlpha(1f);
            } else {
                buttons[i].setAlpha(0.5f);
            }
        }
    }

    @Override
    public void onClick(View v) {

        if (v instanceof Button) {

            for ( int i = 0 ; i < mContents.size() ; i++ ) {
                Button button = buttons[i];

                if (v.equals(button)) {
                    viewPager.setCurrentItem(i, true);
                }
            }

        }
    }

    

    class ViewContentAdapter extends FragmentStatePagerAdapter{

        public ViewContentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return mContents.get(position);

        }

        @Override
        public int getCount() {
            return mContents.size();
        }
    }

}
