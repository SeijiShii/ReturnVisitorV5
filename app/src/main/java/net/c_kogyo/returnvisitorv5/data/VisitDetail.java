package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class VisitDetail extends DataItem {

    public static final String VISIT_DETAIL = "visit_detail";
    public static final String PERSON_ID = "person_id";
    public static final String VISIT_ID = "visit_id";
    public static final String PLACEMENTS = "placements";
    public static final String TAG_IDS = "tag_ids";
    public static final String SEEN = "seen";
    public static final String IS_STUDY = "is_study";
    public static final String IS_RV = "is_rv";
    public static final String PRIORITY = "priority";


    private String personId, visitId;
    private boolean seen, isStudy, isRV;
    private ArrayList<Placement> placements;
    private ArrayList<String> tagIds;
    private Visit.Priority priority;

    public VisitDetail(String personId, String visitId) {
        super(VISIT_DETAIL);

        initCommon();

        this.personId = personId;
        this.visitId = visitId;

    }

    private void initCommon() {
        this.personId = "";
        this.visitId = "";

        this.seen = false;
        this.isStudy = false;
        this.isRV = false;

        this.placements = new ArrayList<>();
        this.tagIds = new ArrayList<>();
        this.priority = Visit.Priority.NONE;
    }

    public VisitDetail(VisitDetail lastVisitDetail) {

        this(lastVisitDetail.personId, lastVisitDetail.visitId);

        this.isStudy = lastVisitDetail.isStudy;
        this.isRV = lastVisitDetail.isRV;

        this.priority = lastVisitDetail.priority;
    }

    public VisitDetail(JSONObject object) {
        super(object);
        initCommon();

        try {
            if (object.has(PERSON_ID))
                this.personId = object.getString(PERSON_ID);
            if (object.has(VISIT_ID))
                this.visitId = object.getString(VISIT_ID);
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

        Person person = RVData.getInstance().getPersonList().getById(personId);

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
}
