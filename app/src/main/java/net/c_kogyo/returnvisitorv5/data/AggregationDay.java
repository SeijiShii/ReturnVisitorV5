package net.c_kogyo.returnvisitorv5.data;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2016/09/24.
 */

public class AggregationDay extends AggregationBase{

    private Calendar mDate;
    private ArrayList<Work> worksOfDay;
    private ArrayList<Visit> visitsOfDay;

    public AggregationDay(Calendar date) {

        super();

        mDate = date;

        worksOfDay = RVData.getInstance().workList.getWorksInDay(date);
        for (Work work : worksOfDay) {
            time += work.getDuration();
        }


        visitsOfDay = RVData.getInstance().visitList.getVisitsInDay(date);
        for (Visit visit : visitsOfDay) {

            placementCount += visit.getPlacementCount();
            showVideoCount += visit.getShowVideoCount();
            rvCount += visit.getRVCount();

        }

    }

    public boolean hasWorkOrVisit() {
        return worksOfDay.size() > 0 || visitsOfDay.size() > 0;
    }

    public Calendar getDate() {
        return mDate;
    }
}
