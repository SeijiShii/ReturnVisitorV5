package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/03/05.
 */

public class VisitList extends DataList<Visit> {

    private static VisitList instance;
    private VisitList() {
        super(Visit.class);
    }

    public static VisitList getInstance() {
        if (instance == null) {
            instance = new VisitList();
        }
        return instance;
    }

    public synchronized ArrayList<Visit> getVisitsForPlace(String placeId) {

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
    synchronized public Visit getLatestVisitToPlace(String placeId) {

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

    synchronized private ArrayList<Visit> getVisitsInMonth(Calendar month) {
        ArrayList<Visit> visitsInMonth = new ArrayList<>();
        for (Visit visit : list) {
            if (CalendarUtil.isSameMonth(month, visit.getDatetime())) {
                visitsInMonth.add(visit);
            }
        }
        return visitsInMonth;
    }

    synchronized public ArrayList<Visit> getVisitsInDay(Calendar date) {
        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : list) {
            if (CalendarUtil.isSameDay(visit.getDatetime(), date)) {
                visits.add(visit);
            }
        }
        return visits;
    }

    synchronized public ArrayList<Visit> getVisitsInWork(Work work) {
        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : list) {
            if (visit.getDatetime().after(work.getStart())
                    && visit.getDatetime().before(work.getEnd())) {
                visits.add(visit);
            }
        }
        return visits;
    }

    synchronized public ArrayList<Visit> getVisitsInWorkInDay(Calendar date) {

        ArrayList<Work> worksInDay = WorkList.getInstance().getWorksInDay(date);
        ArrayList<Visit> visitsInWorkInDay = new ArrayList<>();
        for (Work work : worksInDay) {
            visitsInWorkInDay.addAll(getVisitsInWork(work));
        }
        return visitsInWorkInDay;
    }

    synchronized public ArrayList<Visit> getVisitsInDayNotInWork(Calendar date) {
        ArrayList<Visit> visits = getVisitsInDay(date);
        visits.removeAll(getVisitsInWorkInDay(date));
        return visits;
    }

    synchronized public ArrayList<Calendar> getDates() {

        ArrayList<Calendar> dates = new ArrayList<>();

        for (Visit visit : list) {
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

    synchronized public ArrayList<VisitDetail> getBSVisitDetailsInMonth(Calendar month) {
        ArrayList<VisitDetail> bsVisitDetails = new ArrayList<>();
        for (Visit visit : getVisitsInMonth(month)) {
            bsVisitDetails.addAll(visit.getBSVisitDetails());
        }
        return bsVisitDetails;
    }

    public ArrayList<Visit> getVisitsToPerson(String personId) {

        ArrayList<Visit> visits = new ArrayList<>();

        for (Visit visit : list) {

            if (visit.hasPerson(personId)) {
                visits.add(visit);
            }
        }

        Collections.sort(visits, new Comparator<Visit>() {
            @Override
            public int compare(Visit o1, Visit o2) {
                return o1.getDatetime().compareTo(o2.getDatetime());
            }
        });

        return visits;
    }

    @Nullable
    public Visit getLatestVisitToPerson(String personId) {

        ArrayList<Visit> visits = getVisitsToPerson(personId);

        if (visits.size() <= 0) return null;

        return visits.get(visits.size() - 1);
    }

    @Nullable public Visit getLatestVisitSeenToPerson(String personId) {

        ArrayList<Visit> visits = getVisitsToPerson(personId);

        if (visits.size() <= 0) return null;

        for (int i = visits.size() - 1 ; i >= 0 ; i-- ) {
            Visit visit = visits.get(i);
            VisitDetail visitDetail = visit.getVisitDetail(personId);
            if (visitDetail != null) {
                if (visitDetail.isSeen()) {
                    return visit;
                }
            }
        }
        return null;
    }

    public ArrayList<Visit> getAllNotHomeVisitsInOneMonth() {
        ArrayList<Visit> nhVisits = new ArrayList<>();
        for (Visit visit : list) {
            if (visit.getPriority() == Person.Priority.NOT_HOME) {
                if (CalendarUtil.daysPast(visit.getDatetime(), Calendar.getInstance()) < 32) {
                    nhVisits.add(visit);
                }
            }
        }
        return nhVisits;
    }

    public void setPlaceIdToVisitDetails() {

        for (Visit visit : list) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
                if (visitDetail.getPlaceId().equals("")) {
                    visitDetail.setPlaceId(visit.getPlaceId());
                }
            }
        }
    }
}
