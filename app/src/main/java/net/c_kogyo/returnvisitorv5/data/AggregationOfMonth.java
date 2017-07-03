package net.c_kogyo.returnvisitorv5.data;

import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/04/16.
 */

public class AggregationOfMonth {

//    long time;
//    int placementCount;
//    int showVideoCount;
//    int rvCount;

    public static int hour(Calendar month) {
        long oneHour = 1000 * 60 * 60;
        int hour = (int) (time(month) / oneHour);
        return hour;
    }

    public static long time(Calendar month) {

        Calendar mCal = (Calendar) month.clone();
        long time = 0;

        mCal.set(Calendar.DAY_OF_MONTH, 1);
        while (CalendarUtil.isSameMonth(month, mCal)) {
            if (AggregationOfDay.hasWork(mCal)) {
                time += AggregationOfDay.time(mCal);
            }
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        //次の月に進んでしまっているので前月にするには2つ戻す。
        mCal.add(Calendar.MONTH, -2);
        long timeUpToLastMonth = getTimeUpToThisMonth(mCal);

        time += getCarryOver(timeUpToLastMonth);
        return time;
    }

    public static long getCarryOver(long time) {
        long minute = 60 * 1000;
        long hour = minute * 60;
        return time - (time / hour * hour);
    }

    private static long getTimeUpToThisMonth(Calendar month) {

        long time = 0;

        Calendar nextMonth = (Calendar) month.clone();
        nextMonth.add(Calendar.MONTH, 1);

        for (Work work : WorkList.getInstance()) {
            if (CalendarUtil.oneIsBeforeTwo(work.getStart(), nextMonth)) {
                time += work.getDuration();
            }
        }
        return time;
    }

    public static int placementCount(Calendar month) {

        Calendar mCal = (Calendar) month.clone();
        int count = 0;

        mCal.set(Calendar.DAY_OF_MONTH, 1);
        while (CalendarUtil.isSameMonth(month, mCal)) {
            if (AggregationOfDay.hasVisit(mCal)) {
                count += AggregationOfDay.placementCount(mCal);
            }
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return count;
    }

    public static int showVideoCount(Calendar month) {

        Calendar mCal = (Calendar) month.clone();
        int count = 0;

        mCal.set(Calendar.DAY_OF_MONTH, 1);
        while (CalendarUtil.isSameMonth(month, mCal)) {
            if (AggregationOfDay.hasVisit(mCal)) {
                count += AggregationOfDay.showVideoCount(mCal);
            }
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return count;
    }

    public static int rvCount(Calendar month) {

        Calendar mCal = (Calendar) month.clone();
        int count = 0;

        mCal.set(Calendar.DAY_OF_MONTH, 1);
        while (CalendarUtil.isSameMonth(month, mCal)) {
            if (AggregationOfDay.hasVisit(mCal)) {
                count += AggregationOfDay.rvCount(mCal);
            }
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return count;
    }

    public static int bsCount(Calendar month) {

        ArrayList<VisitDetail> bsVisitDetails = VisitList.getInstance().getBSVisitDetailsInMonth(month);
        ArrayList<String> personIds = new ArrayList<>();
        for (VisitDetail visitDetail : bsVisitDetails) {
            if (!personIds.contains(visitDetail.getPersonId())) {
                personIds.add(visitDetail.getPersonId());
            }
        }
        return personIds.size();
    }

}
