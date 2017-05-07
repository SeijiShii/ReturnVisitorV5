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
import android.view.Gravity;
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
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/04.
 */

public class CalendarFragment extends Fragment {

    private Calendar mMonth, mWeekCounter;
    private CalendarPagerActivity.StartDay mStartDay;
    private static CalendarCellListener mCellListener;

    public static CalendarFragment newInstance(Calendar month,
                                               CalendarPagerActivity.StartDay startDay,
                                               CalendarCellListener listener) {

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

            CalendarRow row = new CalendarRow(getActivity(), mWeekCounter, mStartDay);
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

    public class CalendarCell extends FrameLayout {

        private Calendar mDate;

        public CalendarCell(@NonNull Context context, Calendar date) {
            super(context);

            mDate = (Calendar) date.clone();

            initCommon();

        }

        public CalendarCell(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        private View view;
        private void initCommon() {

            view = LayoutInflater.from(getContext()).inflate(R.layout.calendar_cell, this);

            initDateNumberTextView();
            initTimeBar();
            initBarContainer();

            setTouchListenerIfNeeded();

        }

        private void initDateNumberTextView() {

            TextView dateNumberTextView = (TextView) view.findViewById(R.id.date_number_text);
            String dayNumberText = String.valueOf(mDate.get(Calendar.DAY_OF_MONTH));
            dateNumberTextView.setText(dayNumberText);

        }

        private void initTimeBar() {

            RelativeLayout timeBar = (RelativeLayout) view.findViewById(R.id.time_bar);

            long timeLong = AggregationOfDay.time(mDate);
            if (timeLong < 60000) {
                timeBar.setVisibility(INVISIBLE);
            } else {
                initTimeTextView();
            }
        }

        private void initTimeTextView() {
            TextView timeTextView = (TextView) view.findViewById(R.id.time_text_view);
            long timeLong = AggregationOfDay.time(mDate);
            String timeText = DateTimeText.getDurationString(timeLong, false);
            timeTextView.setText(timeText);
        }

        private void initBarContainer() {

            LinearLayout barContainer = (LinearLayout) view.findViewById(R.id.bar_container);

            if (AggregationOfDay.placementCount(mDate) > 0) {
                barContainer.addView(generateBar(R.color.placement_blue));
            }

            if (AggregationOfDay.showVideoCount(mDate) > 0) {
                barContainer.addView(generateBar(R.color.video_green));
            }

            if (AggregationOfDay.rvCount(mDate) > 0) {
                barContainer.addView(generateBar(R.color.rv_pink));
            }

            if (AggregationOfDay.bsVisitCount(mDate) > 0) {
                barContainer.addView(generateBar(R.color.study_purple));
            }


        }

        private View generateBar(int colorRes) {

            View bar = new View(getContext());
            float density = getContext().getResources().getDisplayMetrics().density;
            int barWidth = (int) (density * 20);
            int barHeight = (int) (density * 4);
            int margin = (int) (density * 2);
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(barWidth, barHeight);
            params.setMargins(margin, margin, margin, margin);
            params.gravity = Gravity.RIGHT;
            bar.setLayoutParams(params);
            bar.setBackgroundResource(colorRes);

            return bar;
        }

        // DONE: 2017/05/05 タッチリスナの実装
        private void setTouchListenerIfNeeded() {
            if (AggregationOfDay.hasWorkOrVisit(mDate)) {
                ViewUtil.setOnClickListener(this, new ViewUtil.OnViewClickListener() {
                    @Override
                    public void onViewClick() {
                        if (mCellListener != null) {
                            mCellListener.onTouch(mDate);
                        }
                    }
                });
            }
        }

    }

    public interface CalendarCellListener {
        void onTouch(Calendar date);
    }

    public class CalendarRow extends LinearLayout {

        private Calendar mDateCounter, mFirstDay;
        private CalendarPagerActivity.StartDay mStartDay;

        public CalendarRow(Context context, Calendar firstDay,
                           CalendarPagerActivity.StartDay startDay) {
            super(context);

            mFirstDay = (Calendar) firstDay.clone();
            mDateCounter = (Calendar) firstDay.clone();

            mStartDay = startDay;

            initCommon();
        }

        public CalendarRow(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        private void initCommon() {
            this.setOrientation(HORIZONTAL);

            LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            this.setLayoutParams(params);

            this.addView(generateBorder());

            int weekStart = 0;
            int weekEnd = 0;
            if (mStartDay == CalendarPagerActivity.StartDay.SUNDAY) {
                weekStart = 1;
                weekEnd = 7;
            } else if (mStartDay == CalendarPagerActivity.StartDay.MONDAY) {
                weekStart = 2;
                weekEnd = 8;
            }

            // DONE: 2017/05/04 週の途中始まりだったら空白で埋める
            for (int i = weekStart ; i < mFirstDay.get(Calendar.DAY_OF_WEEK) ; i++ ) {
                addBlankCell();
                this.addView(generateBorder());
            }

            for (int i = mFirstDay.get(Calendar.DAY_OF_WEEK) ; i <= weekEnd ; i++ ) {
                if (CalendarUtil.isSameMonth(mFirstDay, mDateCounter)) {
                    addCalendarCell(mDateCounter);
                } else {
                    addBlankCell();
                }
                this.addView(generateBorder());
                mDateCounter.add(Calendar.DAY_OF_MONTH, 1);
            }


        }

        private View generateBorder() {
            View border = new View(getContext());
            border.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
            border.setBackgroundResource(R.color.textColorGray);
            return border;
        }

        private void addCalendarCell(Calendar date) {
            CalendarCell cell = new CalendarCell(getContext(), date);
            LinearLayout.LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            cell.setLayoutParams(params);
            this.addView(cell);
        }

        private void addBlankCell() {
            View blankCell = new View(getContext());
            LinearLayout.LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            blankCell.setLayoutParams(params);
            this.addView(blankCell);
        }
    }


}
