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
    public static final String DATE_TIME = "datetime";

    private Calendar datetime;
    private String placeId;
    private ArrayList<Placement> placements;

    public enum Priority {

        NEGATIVE(0),
        NOT_HOME(1),
        BUSY(2),
        NONE(3),
        LOW(4),
        HIGH(5);

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

        try {
            if (object.has(PLACE_ID)) this.placeId = object.getString(PLACE_ID);
            if (object.has(DATE_TIME)) this.datetime.setTimeInMillis(object.getLong(DATE_TIME));
            if (object.has(PLACEMENTS)) {
                JSONArray array = object.getJSONArray(PLACEMENTS);
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    this.placements.add(new Placement(array.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initCommon() {
        this.datetime = Calendar.getInstance();
        this.placeId = null;
        this.placements = new ArrayList<>();
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

}
