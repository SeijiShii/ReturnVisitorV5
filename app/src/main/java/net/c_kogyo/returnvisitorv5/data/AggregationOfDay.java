package net.c_kogyo.returnvisitorv5.data;

import net.c_kogyo.returnvisitorv5.activity.MapActivity;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

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

    public static long time(Calendar date, RVDBHelper helper) {
        long time = 0;
        for (Work work : WorkList.getWorksInDay(date, helper)) {
            time += work.getDuration();
        }
        return time;
    }

    public static int placementCount(Calendar date, RVDBHelper helper) {
        int count = 0;
        for (Visit visit : VisitList.getVisitsInDay(date, helper)) {
            count += visit.getPlacementCount();
        }
        return count;
    }

    public static int rvCount(Calendar date, RVDBHelper helper) {
        int count = 0;
        for (Visit visit : VisitList.getVisitsInDay(date, helper)) {
            count += visit.getRVCount();
        }
        return count;
    }

    public static int showVideoCount(Calendar date, RVDBHelper helper) {
        int count = 0;
        for (Visit visit : VisitList.getVisitsInDay(date, helper)) {
            count += visit.getShowVideoCount();
        }
        return count;
    }

    public static int bsVisitCount(Calendar date, RVDBHelper helper) {
        int count = 0;
        for (Visit visit : VisitList.getVisitsInDay(date, helper)) {
            count += visit.getBSCount();
        }
        return count;
    }


    public static boolean hasWorkOrVisit(Calendar date, RVDBHelper helper) {
        return hasWork(date, helper) || hasVisit(date, helper);
    }

    public static boolean hasWork(Calendar date, RVDBHelper helper) {
        return WorkList.getWorksInDay(date, helper).size() > 0;
    }

    public static boolean hasVisit(Calendar date, RVDBHelper helper) {
        return VisitList.getVisitsInDay(date, helper).size() > 0;
    }
}
