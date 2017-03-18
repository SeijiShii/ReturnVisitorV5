package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Place;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceList extends DataList<Place> {

    @Nullable
    public Place getByMarkerId(String markerId) {

        for (Place place : this) {
            if (place.getMarkerId().equals(markerId)) {
                return place;
            }
        }
        return null;
    }

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
}
