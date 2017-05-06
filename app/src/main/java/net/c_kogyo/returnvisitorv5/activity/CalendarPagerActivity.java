package net.c_kogyo.returnvisitorv5.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.fragment.CalendarFragment;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/04.
 */

public class CalendarPagerActivity extends AppCompatActivity {

    public enum StartDay {
        SUNDAY,
        MONDAY
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_pager_activity);

        initPager();

        initMonthTextView();
        initLeftButton();
        initRightButton();

        scrollToMonth();
    }

    private ViewPager mPager;
    private CalendarPagerAdapter mAdapter;
    private void initPager() {
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new CalendarPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshMonthText();
                refreshLeftButton();
                refreshRightButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private TextView monthTextView;
    private void initMonthTextView() {

        monthTextView = (TextView) findViewById(R.id.month_text);
        refreshMonthText();
    }

    private void refreshMonthText() {

        Calendar currentMonth = mAdapter.getMonth(mPager.getCurrentItem());
        String monthText = DateTimeText.getMonthText(currentMonth);
        monthTextView.setText(monthText);
    }

    private ImageView leftButton;
    private void initLeftButton() {
        leftButton = (ImageView) findViewById(R.id.left_button);
        refreshLeftButton();
    }

    private void refreshLeftButton() {

        float originAlpha, targetAlpha;

        if (mPager.getCurrentItem() == 0) {
            originAlpha = 1f;
            targetAlpha = 0f;
            ViewUtil.setOnClickListener(leftButton, null);
        } else {
            originAlpha = 0f;
            targetAlpha = 1f;
            ViewUtil.setOnClickListener(leftButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick() {
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
                    refreshLeftButton();
                    refreshRightButton();
                }
            });
        }

        ValueAnimator animator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                leftButton.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(300);
        animator.start();

    }

    private ImageView rightButton;
    private void initRightButton() {
        rightButton = (ImageView) findViewById(R.id.right_button);
        refreshRightButton();
    }

    private void refreshRightButton() {

        float originAlpha, targetAlpha;

        if (mPager.getCurrentItem() >= mAdapter.getCount() - 1) {
            originAlpha = 1f;
            targetAlpha = 0f;
            ViewUtil.setOnClickListener(rightButton, null);
        } else {
            originAlpha = 0f;
            targetAlpha = 1f;
            ViewUtil.setOnClickListener(rightButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick() {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
                    refreshLeftButton();
                    refreshRightButton();
                }
            });
        }

        ValueAnimator animator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rightButton.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private void scrollToMonth() {

        Calendar month = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent == null) return;

        long monthLong = intent.getLongExtra(Constants.MONTH_LONG, 0);
        if (monthLong > 0) {
            month.setTimeInMillis(monthLong);
        }

        int pos = mAdapter.getClosestPositionByMonth(month);
        mPager.setCurrentItem(pos, false);
    }

    // DONE: 2017/05/06 月で遷移
    // DONE: 2017/05/06 getClosestPosition

    private class CalendarPagerAdapter extends FragmentStatePagerAdapter {

        public CalendarPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarFragment.newInstance(getMonth(position), StartDay.MONDAY);
        }

        @Override
        public int getCount() {
            return RVData.getInstance().getMonthsWithData().size();
        }

        private Calendar getMonth(int position) {
            return RVData.getInstance().getMonthsWithData().get(position);
        }

        private int getClosestPositionByMonth(Calendar month) {

            int pos;
            // 指定した月が存在するればそのindexを返す。
            pos = getPositionByMonth(month);
            if (pos >= 0) {
                return pos;
            }

            // 存在しなければ
            Calendar forwardCal, backwardCal;
            forwardCal = (Calendar) month.clone();
            backwardCal = (Calendar) month.clone();

            while (true) {

                forwardCal.add(Calendar.MONTH, 1);
                int forwardPos = getPositionByMonth(forwardCal);
                if (forwardPos >= 0) {
                    return forwardPos;
                }

                backwardCal.add(Calendar.MONTH, -1);
                int backwardPos = getPositionByMonth(backwardCal);
                if (backwardPos >= 0) {
                    return backwardPos;
                }

                if (backwardCal.get(Calendar.YEAR) <= 2000) {
                    return -1;
                }
            }
        }

        private int getPositionByMonth(Calendar month) {
            ArrayList<Calendar> monthsWithData = RVData.getInstance().getMonthsWithData();

            for ( int i = 0 ; i < monthsWithData.size() ; i++ ) {
                if (CalendarUtil.isSameMonth(month, monthsWithData.get(i))) {
                    return i;
                }
            }

            return -1;
        }
    }
 }
