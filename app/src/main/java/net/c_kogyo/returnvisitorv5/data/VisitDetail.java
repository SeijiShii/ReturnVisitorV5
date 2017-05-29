package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static net.c_kogyo.returnvisitorv5.data.Visit.PLACE_ID;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class VisitDetail extends DataItem  implements Cloneable{

    public static final String VISIT_DETAIL = "visit_detail";
    public static final String PERSON_ID = "person_id";
    public static final String VISIT_ID = "visit_id";
    public static final String PLACE_ID = "place_id";
    public static final String PLACEMENTS = "placements";
    public static final String TAG_IDS = "tag_ids";
    public static final String SEEN = "seen";
    public static final String IS_STUDY = "is_study";
    public static final String IS_RV = "is_rv";
    public static final String PRIORITY = "priority";


    private String personId, visitId, placeId;
    private boolean seen, isStudy, isRV;
    private ArrayList<Placement> placements;
    private ArrayList<String> tagIds;
    private Visit.Priority priority;


    public VisitDetail(String personId, String visitId, String placeId) {
        super(VISIT_DETAIL);

        initCommon();

        this.personId = personId;
        this.visitId = visitId;
        this.placeId = placeId;

    }

    private void initCommon() {
        this.personId = "";
        this.visitId = "";
        this.placeId = "";

        this.seen = false;
        this.isStudy = false;
        this.isRV = false;

        this.placements = new ArrayList<>();
        this.tagIds = new ArrayList<>();
        this.priority = Visit.Priority.NONE;
    }

    public VisitDetail(VisitDetail lastVisitDetail) {

        this(lastVisitDetail.personId,
                lastVisitDetail.visitId,
                lastVisitDetail.placeId);

        this.isStudy = lastVisitDetail.isStudy;
        this.isRV = lastVisitDetail.isRV;

        this.priority = lastVisitDetail.priority;

        this.tagIds = new ArrayList<>(lastVisitDetail.tagIds);
    }

    public VisitDetail(JSONObject object) {
        super(object);
        initCommon();

        try {
            if (object.has(PERSON_ID))
                this.personId = object.getString(PERSON_ID);
            if (object.has(VISIT_ID))
                this.visitId = object.getString(VISIT_ID);
            if (object.has(PLACE_ID))
                this.placeId = object.getString(PLACE_ID);
            if (object.has(SEEN))
                this.seen = object.getBoolean(SEEN);
            if (object.has(IS_RV))
                this.isRV = object.getBoolean(IS_RV);
            if (object.has(IS_STUDY))
                this.isStudy = object.getBoolean(IS_STUDY);

            if (object.has(PRIORITY))
                this.priority = Visit.Priority.valueOf(object.getString(PRIORITY));

            if (object.has(PLACEMENTS)) {
                JSONArray pArray = object.getJSONArray(PLACEMENTS);
                for ( int i = 0 ; i < pArray.length() ; i++ ) {
                    this.placements.add(new Placement(pArray.getJSONObject(i)));
                }
            }

            if (object.has(TAG_IDS)) {
                JSONArray tArray = object.getJSONArray(TAG_IDS);
                for ( int i = 0 ; i < tArray.length() ; i++ ) {
                    this.tagIds.add(tArray.getString(i));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject jsonObject() {
        JSONObject object = super.jsonObject();

        try {
            object.put(PERSON_ID, this.personId);
            object.put(VISIT_ID, this.visitId);
            object.put(PLACE_ID, this.placeId);

            object.put(SEEN, this.seen);
            object.put(IS_RV, this.isRV);
            object.put(IS_STUDY, this.isStudy);

            object.put(PRIORITY, this.priority.toString());

            JSONArray pArray = new JSONArray();
            for (Placement placement : placements) {
                pArray.put(placement.jsonObject());
            }
            object.put(PLACEMENTS, pArray);

            JSONArray tArray = new JSONArray();
            for (String tagId : tagIds) {
                tArray.put(tagId);
            }
            object.put(TAG_IDS, tArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    public String getPersonData(Context context) {

        Person person = RVData.getInstance().personList.getById(personId);

        if (person == null) {
            return null;
        }

        return person.toString(context);
    }

    public void setIsRv(boolean isRv) {
        this.isRV = isRv;
    }

    public void setIsStudy(boolean isStudy) {
        this.isStudy = isStudy;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setPriority(Visit.Priority priority) {
        this.priority = priority;
    }

    public Visit.Priority getPriority() {
        return priority;
    }

    public String getPersonId() {
        return personId;
    }

    public ArrayList<String> getTagIds() {
        return tagIds;
    }

    public ArrayList<Placement> getPlacements() {
        return placements;
    }

    public boolean isSeen() {
        return seen;
    }

    public boolean isStudy() {
        return isStudy;
    }

    public boolean isRV() {
        return isRV;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String toString(Context context, int indents) {

        StringBuilder builder0 = new StringBuilder();
        for (int i = 0 ; i < indents ; i++) {
            builder0.append(" ");
        }
        String spaces = builder0.toString();

        StringBuilder builder = new StringBuilder();

        Person person = RVData.getInstance().personList.getById(personId);
        if (person != null) {
            builder.append(spaces).append(person.toString(context));
        }

        builder.append("\n").append(spaces);

        if (seen) {
            builder.append(context.getString(R.string.seen));
        } else {
            builder.append(context.getString(R.string.not_seen));
        }

        if (isRV) {
            builder.append("\n").append(spaces).append(context.getString(R.string.return_visit));
        }

        if (isStudy) {
            builder.append("\n").append(spaces).append(context.getString(R.string.study));
        }

        if (tagIds.size() > 0 ) {
            builder.append("\n").append(context.getString(R.string.tag)).append(":");
            for (String id : tagIds) {
                Tag tag = RVData.getInstance().tagList.getById(id);
                if (tag != null) {
                    builder.append(" ").append(tag.getName());
                }
            }
        }

        if (placements.size() > 0) {
            builder.append("\n").append(context.getString(R.string.placement)).append(":");
            for (Placement placement : placements) {
                builder.append("\n").append(spaces).append(placement.getPublicationData());
            }
        }

        if (note.length() >= 0) {
            builder.append("\n").append(note);
        }

        return builder.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        VisitDetail clonedDetail = (VisitDetail) super.clone();

        clonedDetail.personId = this.personId;
        clonedDetail.visitId = this.visitId;
        clonedDetail.seen = this.seen;
        clonedDetail.isRV = this.isRV;
        clonedDetail.isStudy = this.isStudy;

        clonedDetail.placements = new ArrayList<>();
        for (Placement placement : this.placements) {
            clonedDetail.placements.add((Placement) placement.clone());
        }

        clonedDetail.tagIds = new ArrayList<>(this.tagIds);
        clonedDetail.priority = this.priority;

        return clonedDetail;
    }

    public int getPlacementCount() {

        int count = 0;
        for (Placement placement : placements) {
            if (placement.getCategory() != Publication.Category.SHOW_VIDEO
                    && placement.getCategory() != Publication.Category.OTHER) {
                count++;
            }
        }
        return count;
    }

    public int getShowVideoCount() {

        int count = 0;
        for (Placement placement : placements) {
            if (placement.getCategory() == Publication.Category.SHOW_VIDEO) {
                count++;
            }
        }
        return count;
    }

    public boolean belongsToMultiplePlace() {

        ArrayList<String> placeIds = new ArrayList<>();

        for (Visit visit : RVData.getInstance().visitList.getList()) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
                if (visitDetail.personId.equals(this.personId)) {
                    if (!placeIds.contains(visit.getPlaceId()) && !visit.getPlaceId().equals("")) {
                        placeIds.add(visit.getPlaceId());
                    }
                }
            }
        }
        if (!placeIds.contains(this.placeId) && !this.placeId.equals("")) {
            placeIds.add(this.placeId);
        }

        return placeIds.size() > 1;
    }
}
