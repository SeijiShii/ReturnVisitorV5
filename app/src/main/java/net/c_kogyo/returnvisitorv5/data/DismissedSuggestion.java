package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/30.
 */

public class DismissedSuggestion {

    static public final String DISMISSED_SUGGESTIONS = "dismissed_suggestions";

    static final String LATEST_VISIT_ID = "latest_visit_id";
    static final String DISMISSED_DATE_LONG = "dismissed_date_long";

    String latestVisitId;
    Calendar dismissedDate;

    public DismissedSuggestion(String latestVisitId, Calendar dismissedDate) {
        this.latestVisitId = latestVisitId;
        this.dismissedDate = dismissedDate;
    }

    public DismissedSuggestion(JSONObject object) {

        this.dismissedDate = Calendar.getInstance();
        try {
            if (object.has(LATEST_VISIT_ID))
                this.latestVisitId = object.getString(LATEST_VISIT_ID);
            if (object.has(DISMISSED_DATE_LONG)) {
                this.dismissedDate.setTimeInMillis(object.getLong(DISMISSED_DATE_LONG));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject jsonObject() {
        JSONObject object = new JSONObject();
        try {
            object.put(LATEST_VISIT_ID, latestVisitId);
            object.put(DISMISSED_DATE_LONG, dismissedDate.getTimeInMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public String getLatestVisitId() {
        return latestVisitId;
    }

    public Calendar getDismissedDate() {
        return dismissedDate;
    }
}