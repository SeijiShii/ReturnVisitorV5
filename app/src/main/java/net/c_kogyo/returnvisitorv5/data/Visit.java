package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by 56255 on 2016/07/19.
 */
public class Visit extends DataItem {

    public static final String VISIT = "visit";
    public static final String PLACE_ID = "place_id";
    public static final String PLACEMENTS = "placements";
    public static final String VISIT_DETAILS = "visit_details";
    public static final String DATE_TIME = "datetime";

    private Calendar datetime;
    private String placeId;
    private ArrayList<Placement> placements;
    private ArrayList<VisitDetail> visitDetails;

    public enum Priority {

        NONE(0),
        NEGATIVE(1),
        NOT_HOME(2),
        BUSY(3),
        LOW(4),
        MIDDLE(5),
        HIGH(6);

        private final int num;

        Priority(int num) {
            this.num = num;
        }

        public static Priority getEnum (int num) {

            Priority[] enumArray = Priority.values();

            for (Priority priority : enumArray) {

                if (priority.num() == num) return priority;

            }
            return null;
        }
        public int num(){
            return num;
        }

    }

    public Visit() {
        super(VISIT);

        initCommon();
    }

    public Visit(JSONObject object) {

        super(object);
        initCommon();

//        try {
//            if (object.has(PLACE_ID)) this.placeId = object.getString(PLACE_ID);
//            if (object.has(DATE_TIME)) this.datetime.setTimeInMillis(object.getLong(DATE_TIME));
//            if (object.has(PLACEMENTS)) {
//                JSONArray array = object.getJSONArray(PLACEMENTS);
//                for ( int i = 0 ; i < array.length() ; i++ ) {
//                    this.placements.add(new Placement(array.getJSONObject(i)));
//                }
//            }
//            if (object.has(VISIT_DETAILS)) {
//                JSONArray array = object.getJSONArray(VISIT_DETAILS);
//                for ( int i = 0 ; i < array.length() ; i++ ) {
//                    this.visitDetails.add(new VisitDetail(array.getJSONObject(i)));
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

    }

    public Visit(Record record) {
        this(record.getDataJSON());
    }

    private void initCommon() {
        this.datetime = Calendar.getInstance();
        this.placeId = null;
        this.placements = new ArrayList<>();
        this.visitDetails = new ArrayList<>();
    }

    public void addPlacement(Placement placement) {
        placements.add(placement);
    }

    public void removePlacement(Placement placement) {
        placements.remove(placement);
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public ArrayList<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(ArrayList<Placement> placements) {
        this.placements = placements;
    }

    public void addVisitDetail(VisitDetail visitDetail) {
        this.visitDetails.add(visitDetail);
    }


    // そう考えると場所無き訪問もあるよね

    public int getPlacementCount() {

        int count = 0;
        for (Placement plc : placements) {
            if (plc.getCategory() != Placement.Category.SHOW_VIDEO
                    && plc.getCategory() != Placement.Category.OTHER) {
                count++;
            }
        }
        return count;
    }

    public int getShowVideoCount() {

        int count = 0;
        for (Placement plc : placements) {
            if (plc.getCategory() == Placement.Category.SHOW_VIDEO) {
                count++;
            }
        }
        return count;
    }

    public static Visit setJSON(Visit visit, JSONObject object) {

        try {
            if (object.has(PLACE_ID)) visit.placeId = object.getString(PLACE_ID);
            if (object.has(DATE_TIME)) visit.datetime.setTimeInMillis(object.getLong(DATE_TIME));
            if (object.has(PLACEMENTS)) {
                JSONArray array = object.getJSONArray(PLACEMENTS);
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    visit.placements.add(new Placement(array.getJSONObject(i)));
                }
            }
            if (object.has(VISIT_DETAILS)) {
                JSONArray array = object.getJSONArray(VISIT_DETAILS);
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    visit.visitDetails.add(new VisitDetail(array.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return visit;
    }

    public Calendar getDatetime() {
        return datetime;
    }
    
    public Priority getPriority() {
        return Priority.NOT_HOME;
        // // TODO: 2017/03/05 VisitDetailからpriorityを抽出して 
    }
}
