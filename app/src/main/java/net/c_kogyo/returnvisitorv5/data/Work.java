package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2016/08/14.
 */

public class Work extends TimePeriodItem {

    public static final String WORK = "work";
    public static final String INTERVALS = "intervals";

    private ArrayList<TimePeriodItem> intervals;

    public Work(Calendar time) {
        super(WORK, time);

        this.end = (Calendar) this.start.clone();
        this.end.add(Calendar.MINUTE, 5);

        this.intervals = new ArrayList<>();
    }

    public Work(Record record) {
        this(record.getDataJSON());
    }

    public Work(JSONObject object) {
        super(object);
        this.intervals = new ArrayList<>();
        setJSON(this, object);
    }

    @Override
    public JSONObject jsonObject() {

        JSONObject object = super.jsonObject();

        JSONArray intervalArray = new JSONArray();
        for (TimePeriodItem item : intervals) {
            intervalArray.put(item.jsonObject());
        }

        try {
            object.put(INTERVALS, intervalArray);
        } catch (JSONException  e) {
            e.printStackTrace();
        }

        return object;
    }

    public static void setJSON(Work work, JSONObject object) {

//        TimePeriodItem.setJSON(work, object);

        try {
            if (object.has(INTERVALS)) {
                JSONArray array = object.getJSONArray(INTERVALS);
                work.intervals = new ArrayList<>();
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    work.intervals.add(new TimePeriodItem(array.getJSONObject(i)));
                }
             }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //    public boolean isTimeCounting() {
//
//        Work workingWork = TimeCountService.getWork();
//        if (workingWork == null) {
//            return false;
//        }
//
//        if (this.getId().equals(workingWork.getId())) {
//            return TimeCountService.isTimeCounting();
//        }
//        return false;
//    }

    public boolean isVisitInWork(Visit visit) {

        return this.getStart().before(visit.getDatetime()) && this.getEnd().after(visit.getDatetime());

    }

//    public VisitList.VisitsMoved setTimes(Calendar start, Calendar end) {
//
//        Work workBefore = null;
//        try {
//            workBefore = (Work) this.clone();
//        }catch (CloneNotSupportedException e) {
//            //
//        }
//
//        super.setStart(start);
//        super.setEnd(end);
//
//        RVData.getInstance().workList.addOrSet(this);
//
//        return new VisitList.VisitsMoved( workBefore, this);
//    }



    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public long getDuration() {
        long duration = super.getDuration();

        for (TimePeriodItem item : intervals) {
            duration = duration - item.getDuration();
        }
        return duration;
    }

    public void setDate(Calendar date) {
        this.start.set(Calendar.YEAR, date.get(Calendar.YEAR));
        this.start.set(Calendar.MONTH, date.get(Calendar.MONTH));
        this.start.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));

        this.end.set(Calendar.YEAR, date.get(Calendar.YEAR));
        this.end.set(Calendar.MONTH, date.get(Calendar.MONTH));
        this.end.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
    }
}
