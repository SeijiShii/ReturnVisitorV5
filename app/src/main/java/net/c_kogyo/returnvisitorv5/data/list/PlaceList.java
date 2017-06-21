package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;
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
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceList{

    @Nullable
    public static Place loadPlace(String placeId, RVDBHelper helper) {

        RVRecord record = helper.loadRecord(placeId, false);
        if (record == null) {
            return null;
        }
        return new Gson().fromJson(record.getDataJSON(), Place.class);
    }

    public static ArrayList<Place> loadList(RVDBHelper helper) {
        return helper.loadList(Place.class, false);
    }

//    public static ArrayList<Place> getListByIds(ArrayList<String> ids, RVDBHelper helper) {
//
//        ArrayList<Place> list = new ArrayList<>();
//        for (String id : ids) {
//
//            Place place = loadPlace(id, helper);
//            if (place != null) {
//                list.add(place);
//            }
//        }
//        return list;
//    }

    public static ArrayList<Place> getRoomList(String parentId, RVDBHelper helper) {
        ArrayList<Place> roomList = new ArrayList<>();
        for (Place place : loadList(helper)) {
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
    public static Place getMostPriorRoom(String parentId, RVDBHelper helper, final Context context) {
        ArrayList<Place> roomList = getRoomList(parentId, helper);

        if (roomList.size() <= 0) return null;

        if (roomList.size() == 1) return roomList.get(0);

        return Collections.max(roomList, new Comparator<Place>() {
            @Override
            public int compare(Place lhs, Place rhs) {
                return lhs.getPriority(context).num() - rhs.getPriority(context).num();
            }
        });
    }

    public static ArrayList<Place> getListByPerson(Person person, RVDBHelper helper) {

        ArrayList<String> placeIds = new ArrayList<>();

        for (Visit visit : VisitList.loadList(helper)) {
            for (VisitDetail visitDetail : visit.getVisitDetails()) {
                if (visitDetail.getPersonId().equals(person.getId())) {
                    if (!placeIds.contains(visitDetail.getPlaceId())) {
                        placeIds.add(visitDetail.getPlaceId());
                    }
                }
            }
        }
        return helper.loadListByIds(Place.class, placeIds);
    }

}
