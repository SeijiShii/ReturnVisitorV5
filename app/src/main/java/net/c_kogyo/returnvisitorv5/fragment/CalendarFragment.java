package net.c_kogyo.returnvisitorv5.fragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.CalendarPagerActivity;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.data.AggregationOfDay;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.view.CalendarCell;
import net.c_kogyo.returnvisitorv5.view.CalendarRow;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/04.
 */

public class CalendarFragment extends Fragment {

    private Calendar mMonth, mWeekCounter;
    private CalendarPagerActivity.StartDay mStartDay;
    private static CalendarCell.CalendarCellListener mCellListener;

    public static CalendarFragment newInstance(Calendar month,
                                               CalendarPagerActivity.StartDay startDay,
                                               CalendarCell.CalendarCellListener listener) {

        CalendarFragment calendarFragment = new CalendarFragment();

        mCellListener = listener;

        Bundle arg = new Bundle();
        Intent intent = new Intent();

        intent.putExtra(Constants.MONTH_LONG, month.getTimeInMillis());
        intent.putExtra(Constants.START_DAY, startDay.toString());

        arg.putParcelable(Constants.CALENDAR_FRAGMENT_ARGUMENT, intent);

        calendarFragment.setArguments(arg);

        return calendarFragment;

    }

    private void setMonth() {

        mMonth = Calendar.getInstance();
        mWeekCounter = Calendar.getInstance();

        Intent intent = getArguments().getParcelable(Constants.CALENDAR_FRAGMENT_ARGUMENT);

        if (intent != null) {

            Long mLong =  intent.getLongExtra(Constants.MONTH_LONG, 0);
            if (mLong != 0) {
                mMonth.setTimeInMillis(mLong);
                mMonth.set(Calendar.DAY_OF_MONTH, 1);

                mWeekCounter = (Calendar) mMonth.clone();
            }

            mStartDay = CalendarPagerActivity.StartDay.valueOf(intent.getStringExtra(Constants.START_DAY));
        }

    }

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.calendar_fragment, container, false);

        setMonth();

        initDayRow();
        initCalendarFrame();

        return view;
    }

    private void initDayRow() {
        LinearLayout dayRow = (LinearLayout) view.findViewById(R.id.day_row);

        Calendar dayCal = Calendar.getInstance();

        if (mStartDay == CalendarPagerActivity.StartDay.SUNDAY) {
            dayCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        } else if (mStartDay == CalendarPagerActivity.StartDay.MONDAY) {
            dayCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }


        for ( int i = 0 ; i < 7 ; i++ ) {

            SimpleDateFormat sdf = new SimpleDateFormat("EEE");
            String dayText = sdf.format(dayCal.getTime());
            TextView dayTextView = new TextView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            dayTextView.setLayoutParams(params);
            dayTextView.setTextColor(Color.WHITE);
            dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            dayTextView.setText(dayText);

            dayRow.addView(dayTextView);

            dayCal.add(Calendar.DAY_OF_MONTH, 1);

        }
    }

    private void initCalendarFrame() {

        LinearLayout calendarFrame = (LinearLayout) view.findViewById(R.id.calendar_frame);
        calendarFrame.addView(generateBorder());

        while (CalendarUtil.isSameMonth(mMonth, mWeekCounter)) {

            CalendarRow row = new CalendarRow(getActivity(), mWeekCounter, mStartDay, mCellListener);
            calendarFrame.addView(row);
            calendarFrame.addView(generateBorder());

            int headDay = Calendar.SUNDAY;
            if (mStartDay == CalendarPagerActivity.StartDay.MONDAY) {
                headDay = Calendar.MONDAY;
            }

            mWeekCounter.add(Calendar.DAY_OF_MONTH, 1);
            while (mWeekCounter.get(Calendar.DAY_OF_WEEK) != headDay) {
                mWeekCounter.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

    }

    private View generateBorder() {
        View border = new View(getActivity());
        border.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        border.setBackgroundResource(R.color.textColorGray);
        return border;
    }




}
