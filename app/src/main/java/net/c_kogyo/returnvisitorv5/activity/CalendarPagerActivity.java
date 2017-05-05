package net.c_kogyo.returnvisitorv5.activity;

import android.content.Context;
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

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.fragment.CalendarFragment;

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
    }

    private ViewPager mPager;
    private CalendarPagerAdapter mAdapter;
    private void initPager() {
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new CalendarPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
    }

    class CalendarPagerAdapter extends FragmentStatePagerAdapter {

        public CalendarPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarFragment.newInstance(RVData.getInstance().getMonthsWithData().get(position), StartDay.MONDAY);
        }

        @Override
        public int getCount() {
            return RVData.getInstance().getMonthsWithData().size();
        }
    }
 }
