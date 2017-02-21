package net.c_kogyo.returnvisitorv5.list;

import net.c_kogyo.returnvisitorv5.data.Visit;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class VisitList extends DataList<Visit> {

    public static final String VISIT_LIST = "visit_list";

    public VisitList() {
        super(Visit.class, VISIT_LIST);
    }

    public VisitList(JSONObject object) {
        super(Visit.class, VISIT_LIST, object);
    }

    @Override
    public Visit getInstance(JSONObject object) {
        return new Visit(object);
    }
}
