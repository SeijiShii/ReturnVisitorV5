package net.c_kogyo.returnvisitorv5.data;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 56255 on 2016/07/19.
 */
public class Visit extends DataItem implements Cloneable{

    public static final String VISIT = "visit";
    public static final String PLACE_ID = "place_id";
    public static final String PLACEMENTS = "publications";
    public static final String VISIT_DETAILS = "visit_details";
    public static final String DATE_TIME = "datetime";

    private Calendar datetime;
    private String placeId;
    private ArrayList<Placement> placements;
    private ArrayList<VisitDetail> visitDetails;


    public Visit() {
        super(VISIT);
        initCommon(null);
    }

    public Visit(Place place) {
        super(VISIT);

        initCommon(place);
    }

    public Visit(Visit lastVisit) {
        super(VISIT);
        initCommon(null);

        // DONE: 2017/03/21 先回の訪問で生成
        this.placeId = lastVisit.getPlaceId();

        for (VisitDetail visitDetail : lastVisit.getVisitDetails()) {
            this.visitDetails.add(new VisitDetail(visitDetail));
        }
    }

    public Visit(JSONObject object) {

        super(object);
        initCommon(null);

        setJSON(this, object);
    }

    public Visit(RVRecord RVRecord) {
        this(RVRecord.getDataJSON());
    }

    private void initCommon(@Nullable Place place) {
        this.datetime = Calendar.getInstance();
        if (place == null) {
            this.placeId = null;
        } else {
            this.placeId = place.getId();
        }
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
        onUpdate();
    }

    public ArrayList<Placement> getPlacements() {
        onUpdate();
        return placements;
    }

//    public void setPlacements(ArrayList<Placement> placements) {
//        this.placements = placements;
//        onUpdate();
//    }

    public void addVisitDetail(VisitDetail visitDetail) {
        this.visitDetails.add(visitDetail);
    }

    @Override
    public JSONObject jsonObject() {

        // DONE: 2017/03/14 Missing jsonObject Method
        JSONObject object = super.jsonObject();
        try {
            object.put(DATE_TIME, datetime.getTimeInMillis());
            object.put(PLACE_ID, placeId);

            JSONArray detailArray = new JSONArray();
            for (VisitDetail visitDetail : visitDetails) {
                detailArray.put(visitDetail.jsonObject());
            }
            object.put(VISIT_DETAILS, detailArray);

            JSONArray plcArray = new JSONArray();
            for (Placement placement : placements) {
                plcArray.put(placement.jsonObject());
            }
            object.put(PLACEMENTS, plcArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    // そう考えると場所無き訪問もあるよね

    public ArrayList<Placement> getAllPlacements() {
        onUpdate();
        ArrayList<Placement> placements = new ArrayList<>(this.placements);

        for (VisitDetail visitDetail : visitDetails) {
            placements.addAll(visitDetail.getPlacements());
        }
        return placements;
    }

    public int getPlacementCount() {

        onUpdate();
        int count = 0;

        for (Placement plc : getAllPlacements()) {
            if (plc.getCategory() != Publication.Category.SHOW_VIDEO
                    && plc.getCategory() != Publication.Category.OTHER) {
                count++;
            }
        }

        return count;
    }

    public int getShowVideoCount() {

        onUpdate();
        int count = 0;
        for (Placement plc : getAllPlacements()) {
            if (plc.getCategory() == Publication.Category.SHOW_VIDEO) {
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
    
    public Person.Priority getPriority() {

        if (visitDetails.size() <= 0) return Person.Priority.NOT_HOME;

        return getPriorVisitDetail().getPriority();
    }

    private VisitDetail getPriorVisitDetail() {

        return Collections.max(visitDetails, new Comparator<VisitDetail>() {
            @Override
            public int compare(VisitDetail o1, VisitDetail o2) {
                return o1.getPriority().num() - o2.getPriority().num();
            }
        });
    }

    @Nullable
    public VisitDetail getVisitDetail(String personId) {

        for (VisitDetail visitDetail : visitDetails) {
            if (visitDetail.getPersonId().equals(personId)) {
                return visitDetail;
            }
        }
        return null;
    }

    public ArrayList<VisitDetail> getVisitDetails() {
        onUpdate();
        return visitDetails;
    }

//    public String toStringWithLineBreak(Context context, boolean withDetails) {
//        StringBuilder builder = new StringBuilder();
//
//        builder.append(DateTimeText.getDateTimeText(datetime, context));
//
//        builder.append("\n").append(context.getResources().getStringArray(R.array.priority_array)[getPriority().num()]);
//
//        if (withDetails) {
//            if (visitDetails.size() >= 0) {
//                for (VisitDetail visitDetail : visitDetails) {
//                    builder.append("\n").append(visitDetail.toString(context, 4, false));
//                    builder.append("\n");
//                }
//            }
//        }
//
//        builder.append(getPlacementsString());
//
//        return builder.toString();
//    }

    public String getPlacementsString() {
        StringBuilder builder = new StringBuilder();
        if (placements.size() >= 0) {
            for (Placement placement : placements) {
                builder.append("\n    ").append(placement.getPublicationData());
            }
        }
        return builder.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Visit clonedVisit = (Visit) super.clone();

        clonedVisit.datetime = (Calendar) this.datetime.clone();
        clonedVisit.placeId = this.placeId;

        clonedVisit.placements = new ArrayList<>();
        for (Placement placement : this.placements) {
            clonedVisit.placements.add((Placement) placement.clone());
        }

        clonedVisit.visitDetails = new ArrayList<>();
        for (VisitDetail detail : this.visitDetails) {
            clonedVisit.visitDetails.add((VisitDetail) detail.clone());
        }

        return clonedVisit;
    }

    public int getRVCount() {
        int count = 0;
        for (VisitDetail detail : visitDetails) {
            if (detail.isRV()) {
                count++;
            }
        }
        return count;
    }

    public ArrayList<VisitDetail> getBSVisitDetails() {
        ArrayList<VisitDetail> bsVisitDetails = new ArrayList<>();
        for (VisitDetail visitDetail : visitDetails) {
            if (visitDetail.isStudy()) {
                bsVisitDetails.add(visitDetail);
            }
        }
        return bsVisitDetails;
    }

    public int getBSCount() {
        return getBSVisitDetails().size();
    }

    public void setDatetime(Calendar datetime) {
        this.datetime = datetime;
        onUpdate();
    }

    public boolean hasPerson(String personId) {
        for (VisitDetail visitDetail : visitDetails) {
            if (visitDetail.getPersonId().equals(personId)) {
                return true;
            }
        }
        return false;
    }
}
