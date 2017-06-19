package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by 56255 on 2016/07/19.
 */
public class TimePeriodItem extends DataItem{

    public static final String START = "start";
    public static final String END = "end";

    protected Calendar start;
    protected Calendar end;

    public TimePeriodItem(String idHeader, Calendar time) {

        super(idHeader);
        initCommon();
        this.start = (Calendar) time.clone();
        this.end = (Calendar) time.clone();
    }

    public TimePeriodItem(JSONObject object) {
        initCommon();
        setJSON(this, object);
    }

    public TimePeriodItem(RVRecord RVRecord) {
        this(RVRecord.getDataJSON());
    }

    private void initCommon() {
        this.start  = Calendar.getInstance();
        this.end    = Calendar.getInstance();
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
        onUpdate();
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
        onUpdate();
    }

    public long getDuration() {

        return end.getTimeInMillis() - start.getTimeInMillis();

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        TimePeriodItem item = (TimePeriodItem) super.clone();

        item.start = (Calendar) this.start.clone();
        item.end = (Calendar) this.end.clone();

        return item;
    }

    public JSONObject jsonObject() {
        JSONObject object = super.jsonObject();
        try {
            object.put(START, start.getTimeInMillis());
            object.put(END, end.getTimeInMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    static void setJSON(TimePeriodItem item, JSONObject object) {

        DataItem.setJSON(item, object);

        try {
            if (object.has(START))
                item.start.setTimeInMillis(object.getLong(START));
            if (object.has(END))
                item.end.setTimeInMillis(object.getLong(END));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
