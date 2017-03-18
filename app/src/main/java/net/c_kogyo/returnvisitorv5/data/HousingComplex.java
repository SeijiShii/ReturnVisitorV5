package net.c_kogyo.returnvisitorv5.data;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/18.
 */

public class HousingComplex extends DataItem {

    private static final String HOUSING_COMPLEX = "housing_complex";
    private static final String CHILD_IDS = "child_ids";
    private static final String ADDRESS = "address";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    private String address;
    private ArrayList<String> childIds;
    private LatLng latLng;

    public HousingComplex(LatLng latLng) {
        super(HOUSING_COMPLEX);
        this.latLng = latLng;
        initCommon();
    }

    public HousingComplex(JSONObject object) {
        super(object);
        initCommon();
        setJSON(this, object);
    }

    public HousingComplex(Record record) {
        this(record.getDataJSON());
    }

    private void initCommon() {

        address = null;
        childIds = new ArrayList<>();
    }

    private static void setJSON(HousingComplex housingComplex, JSONObject object) {

        try {
            if (object.has(LATITUDE) && object.has(LONGITUDE)) {
                double lat = object.getDouble(LATITUDE);
                double lng = object.getDouble(LONGITUDE);
                housingComplex.latLng = new LatLng(lat, lng);
            }

            if (object.has(ADDRESS))
                housingComplex.address = object.getString(ADDRESS);

            if (object.has(CHILD_IDS)) {
                housingComplex.childIds = new ArrayList<>();
                JSONArray array = object.getJSONArray(CHILD_IDS);
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    housingComplex.childIds.add(array.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject jsonObject() {

        JSONObject object = super.jsonObject();
        try {

            object.put(LATITUDE, latLng.latitude);
            object.put(LONGITUDE, latLng.longitude);
            object.put(ADDRESS, address);

            JSONArray array = new JSONArray();
            for (String id : childIds) {
                array.put(id);
            }
            object.put(CHILD_IDS, array);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<String> getChildIds() {
        return childIds;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
