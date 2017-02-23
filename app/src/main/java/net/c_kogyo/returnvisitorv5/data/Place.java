package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2016/07/17.
 */

public class Place extends DataItem {

    public static final String PLACE = "place";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ADDRESS = "address";

    private LatLng latLng;
    private String address;
    private String markerId;

    public Place() {
        super(PLACE);
        initCommon();
    }

    public Place(LatLng latLng) {
        this();

        this.latLng = latLng;
    }

    public Place(JSONObject object) {
        super(object);
        initCommon();

        try {
            if (object.has(LATITUDE) && object.has(LONGITUDE)) {
                double lat = object.getDouble(LATITUDE);
                double lng = object.getDouble(LONGITUDE);
                this.latLng = new LatLng(lat, lng);
            }

            if (object.has(ADDRESS)) this.address = object.getString(ADDRESS);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Place(Record record) {
        this(record.getJSON());
    }

    private void initCommon() {
        this.latLng = new LatLng(0, 0);
        this.address = "";
        this.markerId = null;
    }

    @Override
    public String toStringForSearch(Context context) {
        return name + " " + address;
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    @Override
    public JSONObject jsonObject() {

        JSONObject object = super.jsonObject();

        try {
            object.put(LATITUDE, latLng.latitude);
            object.put(LONGITUDE, latLng.longitude);
            object.put(ADDRESS, address);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

//    @Override
//    public HashMap<String, Object> toMap() {
//
//        HashMap<String, Object> map = super.toMap();
//
//        map.put(LATITUDE, latLng.latitude);
//        map.put(LONGITUDE, latLng.longitude);
//        map.put(ADDRESS, address);
//
//        return map;
//    }

//    @Override
//    public void setMap(@NonNull HashMap<String, Object> map) {
//        super.setMap(map);
//
//        double lat = Double.valueOf(map.get(LATITUDE).toString());
//        double lng = Double.valueOf(map.get(LONGITUDE).toString());
//
//        this.latLng = new LatLng(lat, lng);
//
//        this.address = map.get(ADDRESS).toString();
//
//
//    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAddressRequested() {
        return (address == null || address.equals(""));
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public String toString() {

        if (name.length() > 0) return name;
        if (address.length() > 0) return address;

        return "";
    }
}
