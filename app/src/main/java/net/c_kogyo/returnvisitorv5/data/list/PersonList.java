package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVRecord;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class PersonList {

    public static ArrayList<Person> loadList(RVDBHelper helper) {

        return helper.loadList(Person.class, false);
    }

    @Nullable
    public static Person loadPerson(String personId, RVDBHelper helper) {
        RVRecord record = helper.loadRecord(personId, false);
        if (record == null) return null;

        return new Gson().fromJson(record.getDataJSON(), Person.class);
    }

    public static ArrayList<Person> getPersonsInPlace(Place place, RVDBHelper helper) {

        ArrayList<Place> places = new ArrayList<>();
        if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            places = new ArrayList<>(PlaceList.getRoomList(place.getId(), helper));
        } else {
            places.add(place);
        }

        ArrayList<String> ids = new ArrayList<>();
        for (Place place1 : places) {
            ArrayList<Visit> visits = VisitList.getVisitsForPlace(place1.getId(), helper);
            for (Visit visit : visits) {
                for (VisitDetail visitDetail : visit.getVisitDetails()) {
                    if (!ids.contains(visitDetail.getPersonId())) {
                        ids.add(visitDetail.getPersonId());
                    }
                }
            }
        }
        return helper.loadListByIds(Person.class, ids);
    }

}
