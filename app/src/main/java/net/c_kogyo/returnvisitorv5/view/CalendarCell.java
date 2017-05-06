package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.AggregationOfDay;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/05.
 */

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

    // TODO: 2017/05/05 タッチリスナの実装
}
