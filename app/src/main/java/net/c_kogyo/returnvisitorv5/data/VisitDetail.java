package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.list.PersonList;
import net.c_kogyo.returnvisitorv5.data.list.TagList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class VisitDetail extends DataItem  implements Cloneable{

    public static final String VISIT_DETAIL = "visit_detail";

    private String personId, visitId, placeId;
    private boolean seen, isStudy, isRV;
    private ArrayList<Placement> placements;
    private ArrayList<String> tagIds;
    private Person.Priority priority;


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
        this.priority = Person.Priority.NONE;
    }

    public VisitDetail(VisitDetail lastVisitDetail) {

        this(lastVisitDetail.personId,
                lastVisitDetail.visitId,
                lastVisitDetail.placeId);

        this.isStudy = lastVisitDetail.isStudy;
        this.isRV = lastVisitDetail.isRV;

        this.priority = lastVisitDetail.priority;

        this.tagIds = new ArrayList<>(lastVisitDetail.tagIds);

        this.seen = false;
    }

    public String getPersonData(Context context) {

        Person person = PersonList.getInstance().getById(personId);

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

    public void setPriority(Person.Priority priority) {
        this.priority = priority;
        
    }

    public Person.Priority getPriority() {
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

    public String toString(Context context, int indents, boolean withTags) {

        StringBuilder builder0 = new StringBuilder();
        for (int i = 0 ; i < indents ; i++) {
            builder0.append(" ");
        }
        String spaces = builder0.toString();

        StringBuilder builder = new StringBuilder();

        Person person = PersonList.getInstance().getById(personId);
        if (person != null) {
            builder.append(spaces).append(person.toString(context));
            builder.append("\n").append(spaces).append(context.getResources().getStringArray(R.array.priority_array)[getPriority().num()]);
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

        if (withTags) {
            if (tagIds.size() > 0 ) {
                builder.append("\n").append(context.getString(R.string.tag)).append(":");
                for (String id : tagIds) {
                    Tag tag = TagList.getInstance().getById(id);
                    if (tag != null) {
                        builder.append(" ").append(tag.getName());
                    }
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

    public String toStringForSearch(Context context) {

        String s = toString(context, 0, true);
        return s.replaceAll("\n", " ");
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

        for (Visit visit : VisitList.getInstance().getList()) {
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
