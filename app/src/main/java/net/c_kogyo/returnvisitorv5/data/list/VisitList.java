package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.RVRecord;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/03/05.
 */

public class VisitList {

    @Nullable
    public static Visit loadVisit(String visitId, RVDBHelper helper) {

        RVRecord record = helper.loadRecord(visitId, false);
        if (record == null) {
            return null;
        }
        return new Gson().fromJson(record.getDataJSON(), Visit.class);
    }

    public static ArrayList<Visit> loadList(RVDBHelper helper) {
        ArrayList<Visit> visitList = new ArrayList<>();
        for (RVRecord record : helper.loadRecords(Visit.class)) {
            visitList.add(new Gson().fromJson(record.getDataJSON(), Visit.class));
        }
        return visitList;
    }

    public static ArrayList<Visit> getVisitsForPlace(String placeId, RVDBHelper helper) {

        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : loadList(helper)) {
            if (visit.getPlaceId() != null) {
                if (visit.getPlaceId().equals(placeId)) {
                    visits.add(visit);
                }
            }
        }
        return visits;
    }

    @Nullable
    public static Visit getLatestVisitToPlace(String placeId, RVDBHelper helper) {

        ArrayList<Visit> visits = getVisitsForPlace(placeId, helper);

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

    private static ArrayList<Visit> getVisitsInMonth(Calendar month, RVDBHelper helper) {
        ArrayList<Visit> visitsInMonth = new ArrayList<>();
        for (Visit visit : loadList(helper)) {
            if (CalendarUtil.isSameMonth(month, visit.getDatetime())) {
                visitsInMonth.add(visit);
            }
        }
        return visitsInMonth;
    }

    public static ArrayList<Visit> getVisitsInDay(Calendar date, RVDBHelper helper) {
        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : loadList(helper)) {
            if (CalendarUtil.isSameDay(visit.getDatetime(), date)) {
                visits.add(visit);
            }
        }
        return visits;
    }

    public static ArrayList<Visit> getVisitsInWork(Work work, RVDBHelper helper) {
        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : loadList(helper)) {
            if (visit.getDatetime().after(work.getStart())
                    && visit.getDatetime().before(work.getEnd())) {
                visits.add(visit);
            }
        }
        return visits;
    }

    public static ArrayList<Visit> getVisitsInWorkInDay(Calendar date, RVDBHelper helper) {

        ArrayList<Work> worksInDay = WorkList.getWorksInDay(date, helper);
        ArrayList<Visit> visitsInWorkInDay = new ArrayList<>();
        for (Work work : worksInDay) {
            visitsInWorkInDay.addAll(getVisitsInWork(work, helper));
        }
        return visitsInWorkInDay;
    }

    public static ArrayList<Visit> getVisitsInDayNotInWork(Calendar date, RVDBHelper helper) {
        ArrayList<Visit> visits = getVisitsInDay(date, helper);
        visits.removeAll(getVisitsInWorkInDay(date, helper));
        return visits;
    }

    public static ArrayList<Calendar> getDates(RVDBHelper helper) {

        ArrayList<Calendar> dates = new ArrayList<>();

        for (Visit visit : loadList(helper)) {
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

    public static ArrayList<VisitDetail> getBSVisitDetailsInMonth(Calendar month, RVDBHelper helper) {
        ArrayList<VisitDetail> bsVisitDetails = new ArrayList<>();
        for (Visit visit : getVisitsInMonth(month, helper)) {
            bsVisitDetails.addAll(visit.getBSVisitDetails());
        }
        return bsVisitDetails;
    }

    public static ArrayList<Visit> getVisitsToPerson(String personId, RVDBHelper helper) {

        ArrayList<Visit> visits = new ArrayList<>();

        for (Visit visit : loadList(helper)) {

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
    public static Visit getLatestVisitToPerson(String personId, RVDBHelper helper) {

        ArrayList<Visit> visits = getVisitsToPerson(personId, helper);

        if (visits.size() <= 0) return null;

        return visits.get(visits.size() - 1);
    }

    @Nullable
    public static Visit getLatestVisitSeenToPerson(String personId, RVDBHelper helper) {

        ArrayList<Visit> visits = getVisitsToPerson(personId, helper);

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

    public static ArrayList<Visit> getAllNotHomeVisitsInOneMonth(RVDBHelper helper) {
        ArrayList<Visit> nhVisits = new ArrayList<>();
        for (Visit visit : loadList(helper)) {
            if (visit.getPriority() == Person.Priority.NOT_HOME) {
                if (CalendarUtil.daysPast(visit.getDatetime(), Calendar.getInstance()) < 32) {
                    nhVisits.add(visit);
                }
            }
        }
        return nhVisits;
    }

    public static void setPlaceIdToVisitDetails(RVDBHelper helper) {

        for (Visit visit : loadList(helper)) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
                if (visitDetail.getPlaceId().equals("")) {
                    visitDetail.setPlaceId(visit.getPlaceId());
                }
            }
        }
    }
}
