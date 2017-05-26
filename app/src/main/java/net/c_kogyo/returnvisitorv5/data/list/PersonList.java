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

        ArrayList<Place> places = new ArrayList<>();
        if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            places = new ArrayList<>(RVData.getInstance().placeList.getRoomList(place.getId()));
        } else {
            places.add(place);
        }

        ArrayList<String> ids = new ArrayList<>();
        for (Place place1 : places) {
            ArrayList<Visit> visits = RVData.getInstance().visitList.getVisitsForPlace(place1.getId());
            for (Visit visit : visits) {
                for (VisitDetail visitDetail : visit.getVisitDetails()) {
                    if (!ids.contains(visitDetail.getPersonId())) {
                        ids.add(visitDetail.getPersonId());
                    }
                }
            }
        }
        return getList(ids);
    }
}
