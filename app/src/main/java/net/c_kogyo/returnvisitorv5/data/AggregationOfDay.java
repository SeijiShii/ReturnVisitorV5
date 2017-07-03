package net.c_kogyo.returnvisitorv5.data;

import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2016/09/24.
 */

public class AggregationOfDay {

//    protected long time;
//    protected int placementCount;
//    protected int showVideoCount;
//    protected int rvCount;

    public static long time(Calendar date) {
        long time = 0;
        for (Work work : WorkList.getInstance().getWorksInDay(date)) {
            time += work.getDuration();
        }
        return time;
    }

    public static int placementCount(Calendar date) {
        int count = 0;
        for (Visit visit : VisitList.getInstance().getVisitsInDay(date)) {
            count += visit.getPlacementCount();
        }
        return count;
    }

    public static int rvCount(Calendar date) {
        int count = 0;
        for (Visit visit : VisitList.getInstance().getVisitsInDay(date)) {
            count += visit.getRVCount();
        }
        return count;
    }

    public static int showVideoCount(Calendar date) {
        int count = 0;
        for (Visit visit : VisitList.getInstance().getVisitsInDay(date)) {
            count += visit.getShowVideoCount();
        }
        return count;
    }

    public static int bsVisitCount(Calendar date) {
        int count = 0;
        for (Visit visit : VisitList.getInstance().getVisitsInDay(date)) {
            count += visit.getBSCount();
        }
        return count;
    }


    public static boolean hasWorkOrVisit(Calendar date) {
        return hasWork(date) || hasVisit(date);
    }

    public static boolean hasWork(Calendar date) {
        return WorkList.getInstance().getWorksInDay(date).size() > 0;
    }

    public static boolean hasVisit(Calendar date) {
        return VisitList.getInstance().getVisitsInDay(date).size() > 0;
    }
}
