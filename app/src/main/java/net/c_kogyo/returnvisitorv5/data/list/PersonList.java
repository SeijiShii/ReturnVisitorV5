package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class PersonList extends DataList<Person> {

    public ArrayList<Person> getPersonsInPlace(Place place) {

        ArrayList<String> ids = new ArrayList<>();
        ArrayList<Visit> visits = RVData.getInstance().visitList.getVisitsForPlace(place.getId());
        for (Visit visit : visits) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
                if (!ids.contains(visitDetail.getPersonId())) {
                    ids.add(visitDetail.getPersonId());
                }
            }
        }

        return getList(ids);
    }
}
