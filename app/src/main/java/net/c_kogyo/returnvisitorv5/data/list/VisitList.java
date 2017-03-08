package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Visit;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/05.
 */

public class VisitList extends DataList<Visit> {

    public ArrayList<Visit> getVisitsForPlace(String placeId) {

        ArrayList<Visit> visits = new ArrayList<>();
        for (Visit visit : this) {
            if (visit.getPlaceId() != null) {
                if (visit.getPlaceId().equals(placeId)) {
                    visits.add(visit);
                }
            }
        }
        return visits;
    }

    @Nullable
    public Visit getLatestVisitToPlace(String placeId) {

        ArrayList<Visit> visits = getVisitsForPlace(placeId);

        if (visits.size() <= 0) {
            return null;
        }

        Visit visit = visits.get(0);
        for (Visit visit1 : visits) {
            if (visit1.getDatetime().after(visit.getDatetime())) {
                visit = visit1;
            }
        }
        return visit;
    }
}
