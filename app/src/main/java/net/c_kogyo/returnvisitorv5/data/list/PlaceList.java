package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Place;

/**
 * Created by SeijiShii on 2017/03/14.
 */

public class PlaceList extends DataList<Place> {

    @Nullable
    public Place getByMarkerId(String markerId) {

        for (Place place : list) {
            if (place.getMarkerId().equals(markerId)) {
                return place;
            }
        }
        return null;
    }
}
