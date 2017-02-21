package net.c_kogyo.returnvisitorv5.data;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class VisitDetail extends DataItem {

    public static final String PERSON_VISIT = "person_visit";

    @Override
    public String idHeader() {
        return PERSON_VISIT;
    }
}
