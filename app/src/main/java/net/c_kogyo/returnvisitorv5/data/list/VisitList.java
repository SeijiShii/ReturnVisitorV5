package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/03/05.
 */

public class VisitList extends DataList<Visit> {

    public ArrayList<Visit> getVisitsForPlace(String placeId) {

        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : this) {
            if (visit.getPlaceId() != null) {
                if (visit.getPlaceId().equals(placeId)) {
                    visits.add(visit);
                }
            }
        }
        return visits;
    }

    @Nullable
    public Visit getLatestVisitToPlace(String placeId) {

        ArrayList<Visit> visits = getVisitsForPlace(placeId);

        if (visits.size() <= 0) {
            return null;
        }

        Visit visit = visits.get(0);
        for (Visit visit1 : visits) {
            if (visit1.getDatetime().after(visit.getDatetime())) {
                visit = visit1;
            }
        }
        return visit;
    }

    private ArrayList<Visit> getVisitsInMonth(Calendar month) {
        ArrayList<Visit> visitsInMonth = new ArrayList<>();
        for (Visit visit : getList()) {
            if (CalendarUtil.isSameMonth(month, visit.getDatetime())) {
                visitsInMonth.add(visit);
            }
        }
        return visitsInMonth;
    }

    public ArrayList<Visit> getVisitsInDay(Calendar date) {
        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : getList()) {
            if (CalendarUtil.isSameDay(visit.getDatetime(), date)) {
                visits.add(visit);
            }
        }
        return visits;
    }

    public ArrayList<Visit> getVisitsInWork(Work work) {
        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : getList()) {
            if (visit.getDatetime().after(work.getStart())
                    && visit.getDatetime().before(work.getEnd())) {
                visits.add(visit);
            }
        }
        return visits;
    }

    public ArrayList<Visit> getVisitsInWorkInDay(Calendar date) {

        ArrayList<Work> worksInDay = RVData.getInstance().workList.getWorksInDay(date);
        ArrayList<Visit> visitsInWorkInDay = new ArrayList<>();
        for (Work work : worksInDay) {
            visitsInWorkInDay.addAll(getVisitsInWork(work));
        }
        return visitsInWorkInDay;
    }

    public ArrayList<Visit> getVisitsInDayNotInWork(Calendar date) {
        ArrayList<Visit> visits = getVisitsInDay(date);
        visits.removeAll(getVisitsInWorkInDay(date));
        return visits;
    }

    public ArrayList<Calendar> getDates() {

        ArrayList<Calendar> dates = new ArrayList<>();

        for (Visit visit : getList()) {
            dates.add(visit.getDatetime());
        }

        // 重複する日付を削除
        ArrayList<Calendar> datesToRemove = new ArrayList<>();

        for (int i = 0 ; i < dates.size() - 1 ; i++ ) {

            Calendar date0 = dates.get(i);

            for ( int j = i + 1 ; j < dates.size() ; j++ ) {

                Calendar date1 = dates.get(j);

                if (CalendarUtil.isSameDay(date0, date1)) {

                    datesToRemove.add(date1);
                }
            }
        }
        dates.removeAll(datesToRemove);
        return dates;
    }

    public ArrayList<VisitDetail> getBSVisitDetailsInMonth(Calendar month) {
        ArrayList<VisitDetail> bsVisitDetails = new ArrayList<>();
        for (Visit visit : getVisitsInMonth(month)) {
            bsVisitDetails.addAll(visit.getBSVisitDetails());
        }
        return bsVisitDetails;
    }


}
