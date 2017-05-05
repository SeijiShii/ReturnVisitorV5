package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/05.
 */

public class CalendarRow extends LinearLayout {

    public enum StartDay {
        SUNDAY,
        MONDAY
    }

    private Calendar mDateCounter, mFirstDay;
    StartDay mStartDay;

    public CalendarRow(Context context, Calendar firstDay, StartDay startDay) {
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
        if (mStartDay == StartDay.SUNDAY) {
            weekStart = 1;
            weekEnd = 7;

        } else if (mStartDay == StartDay.MONDAY) {

            weekStart = 2;
            weekEnd = 8;
        }

        // TODO: 2017/05/04 週の途中始まりだったら空白で埋める
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

