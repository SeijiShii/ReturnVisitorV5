package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceList extends DataList<Place> {

    public synchronized ArrayList<Place> getListByIds(ArrayList<String> ids) {
        ArrayList<Place> list = new ArrayList<>();
        for (String id : ids) {

            Place place = getById(id);
            if (place != null) {
                list.add(place);
            }
        }
        return list;
    }

    public synchronized ArrayList<Place> getRoomList(String parentId) {
        ArrayList<Place> roomList = new ArrayList<>();
        for (Place place : this) {
            if (place.getParentId() != null && place.getParentId().equals(parentId)) {
                roomList.add(place);
            }
        }

        // 名前でソート
        Collections.sort(roomList, new Comparator<Place>() {
            @Override
            public int compare(Place o1, Place o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return roomList;
    }

    @Nullable
    synchronized public Place getMostPriorRoom(String parentId) {
        ArrayList<Place> roomList = getRoomList(parentId);

        if (roomList.size() <= 0) return null;

        if (roomList.size() == 1) return roomList.get(0);

        return Collections.max(roomList, new Comparator<Place>() {
            @Override
            public int compare(Place lhs, Place rhs) {
                return lhs.getPriority().num() - rhs.getPriority().num();
            }
        });
    }

    public ArrayList<Place> getByPerson(Person person) {

        ArrayList<String> placeIds = new ArrayList<>();

        for (Visit visit : RVData.getInstance().visitList) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
                if (visitDetail.getPersonId().equals(person.getId())) {
                    if (!placeIds.contains(visitDetail.getPlaceId())) {
                        placeIds.add(visitDetail.getPlaceId());
                    }
                }
            }
        }
        return getList(placeIds);
    }
}
