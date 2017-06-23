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
    }

    public ArrayList<Placement> getPlacements() {
        
        return placements;
    }

    public void addVisitDetail(VisitDetail visitDetail) {
        this.visitDetails.add(visitDetail);
    }

    // そう考えると場所無き訪問もあるよね

    public ArrayList<Placement> getAllPlacements() {
        
        ArrayList<Placement> placements = new ArrayList<>(this.placements);

        for (VisitDetail visitDetail : visitDetails) {
            placements.addAll(visitDetail.getPlacements());
        }
        return placements;
    }

    public int getPlacementCount() {

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

        
        int count = 0;
        for (Placement plc : getAllPlacements()) {
            if (plc.getCategory() == Publication.Category.SHOW_VIDEO) {
                count++;
            }
        }
        return count;
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
        
        return visitDetails;
    }

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
