package net.c_kogyo.returnvisitorv5.data.list;

import android.util.Log;

import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class PersonList extends DataList<Person> {

    private static final String TAG = "PersonList";

    private static PersonList instance;
    private PersonList() {
        super(Person.class);
    }

    public static PersonList getInstance() {
        if (instance == null) {
            instance = new PersonList();
        }
        return instance;
    }

    public ArrayList<Person> getPersonsInPlace(Place place) {

        ArrayList<Place> places = new ArrayList<>();
        if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            places = new ArrayList<>(PlaceList.getInstance().getRoomList(place.getId()));
        } else {
            places.add(place);
        }

        ArrayList<String> ids = new ArrayList<>();
        for (Place place1 : places) {
            ArrayList<Visit> visits = VisitList.getInstance().getVisitsForPlace(place1.getId());
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

    // for maintenance
    public void removeItemsWithoutVisitDetail() {

        ArrayList<String> validIds = new ArrayList<>();

        for (Visit visit : VisitList.getInstance()) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
               if (!validIds.contains(visitDetail.getPersonId())) {
                   validIds.add(visitDetail.getPersonId());
               }
            }
        }

        ArrayList<Person> validPersons = new ArrayList<>();
        for (String validId : validIds) {
            Person person = getById(validId);
            if (person != null) {
                validPersons.add(person);
            }
        }

        ArrayList<Person> deleteList = new ArrayList<>();
        for (Person person : list) {
            if (!validPersons.contains(person)) {
                deleteList.add(person);
            }
        }

        for (Person deletedPerson : deleteList) {
            deleteById(deletedPerson.getId());
        }

        Log.d(TAG, "Valid person count: " + validIds.size());
        Log.d(TAG, "Invalid person count: " + deleteList.size());

    }

    // for change
    public void setPriorityToPersons() {
        for (Person person : list) {
            person.setPriorityFromLatestVisitDetail();
        }
    }
}
