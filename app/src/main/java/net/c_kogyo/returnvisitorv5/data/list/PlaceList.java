package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceList extends DataList<Place> {

    public ArrayList<Place> getListByIds(ArrayList<String> ids) {
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
    public Place getMostPriorRoom(String parentId) {
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
}
