package net.c_kogyo.returnvisitorv5.list;

import net.c_kogyo.returnvisitorv5.data.Place;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class PlaceList extends DataList<Place> {

    public static final String PLACE_LIST = "place_list";

    public PlaceList() {
        super(Place.class, PLACE_LIST);
    }

    public PlaceList(JSONObject object) {
        super(Place.class, PLACE_LIST, object);
    }

    @Override
    public Place getInstance(JSONObject object) {
        return new Place(object);
    }
}
