package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SeijiShii on 2016/07/17.
 */

public class Place extends BaseDataItem {

    public static final String PLACE = "place";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ADDRESS = "address";
    public static final String PERSON_IDS = "person_Ids";

    private LatLng latLng;
    private String address;
    private ArrayList<String> personIds;
    private String markerId;

    public Place() {
        super();
        initCommon();
    }

    public Place(LatLng latLng) {
        super();
        initCommon();

        this.latLng = latLng;
    }

//    public Place(JSONObject object) {
//        super(object);
//        initCommon();
//
//        try {
//            if (object.has(LATITUDE) && object.has(LONGITUDE)) {
//                double lat = object.getDouble(LATITUDE);
//                double lng = object.getDouble(LONGITUDE);
//                this.latLng = new LatLng(lat, lng);
//            }
//
//            if (object.has(ADDRESS)) this.address = object.getString(ADDRESS);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void initCommon() {
        this.latLng = new LatLng(0, 0);
        this.address = "";
        this.personIds = new ArrayList<>();
        this.markerId = null;
    }

    @Override
    public String getIdHeader() {
        return PLACE;
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

    //    @Override
//    public JSONObject getJSONObject() {
//
//        JSONObject object = super.getJSONObject();
//
//        try {
//            object.put(LATITUDE, latLng.latitude);
//            object.put(LONGITUDE, latLng.longitude);
//            object.put(ADDRESS, address);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return object;
//    }

    @Override
    public HashMap<String, Object> toMap() {

        HashMap<String, Object> map = super.toMap();

        map.put(LATITUDE, latLng.latitude);
        map.put(LONGITUDE, latLng.longitude);
        map.put(ADDRESS, address);

        map.put(PERSON_IDS, personIds);

        return map;
    }

    @Override
    public void setMap(@NonNull HashMap<String, Object> map) {
        super.setMap(map);

        double lat = Double.valueOf(map.get(LATITUDE).toString());
        double lng = Double.valueOf(map.get(LONGITUDE).toString());

        this.latLng = new LatLng(lat, lng);

        this.address = map.get(ADDRESS).toString();

        // HashMap to ArrayList
        Object o = map.get(PERSON_IDS);
        if ( o != null ) {

            this.personIds = (ArrayList<String>) o;
            o = null;
        }

    }

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

    public ArrayList<String> getPersonIds() {
        return personIds;
    }

    public void setPersonIds(ArrayList<String> personIds) {
        this.personIds = personIds;
    }

    public void addPersonId (String personId) {

        if (!this.personIds.contains(personId)) {

            this.personIds.add(personId);
        }
    }

    public void addPersonIds(ArrayList<String> personIds) {

        for (String id : personIds) {
            if (!this.personIds.contains(id)) {
                this.personIds.add(id);
            }
        }
    }

    public Person.Interest getInterest() {

        Person.Interest interest = Person.Interest.NONE;

        for ( String id : personIds ) {

            Person person = RVData.getInstance().personList.getById(id);
            if (person != null) {
                if (person.getInterest().num() > interest.num()) {
                    interest = person.getInterest();
                }
            }
        }
        return interest;
    }

    public boolean hasPersonId(String personId) {
        return personIds.contains(personId);
    }

    @Override
    public String toString() {

        if (name.length() > 0) return name;
        if (address.length() > 0) return address;

        return "";
    }
}
