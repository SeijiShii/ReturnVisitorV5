package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SeijiShii on 2016/07/17.
 */

public class Place extends DataItem {

    public static final String PLACE = "place";

    public enum Category {
        UNDEFINED(0),
        HOUSE(1),
        ROOM(2),
        HOUSING_COMPLEX(3);

        private final int num;

        Category(int num) {
            this.num = num;
        }

        public static Category getEnum (int num) {

            Category[] enumArray = Category.values();

            for (Category category : enumArray) {

                if (category.num() == num) return category;

            }

            return null;
        }

        public int num(){
            return num;
        }
    }


    private LatLng latLng;
    private String address;
//    private String markerId;
    private Category category;
    private String parentId;

    public Place() {
        super(PLACE);
        initCommon();
    }

    public Place(LatLng latLng, Category category) {
        this();

        this.latLng = latLng;
        this.category = category;
    }

    private void initCommon() {
        this.latLng = new LatLng(0, 0);
        this.address = null;
        this.parentId = null;
        this.category = Category.HOUSE;
    }

    @Override
    public String toStringForSearch(Context context) {
        return name + " " + address;
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

    @Override
    public String toString() {

        switch (category) {
            case HOUSE:
            case HOUSING_COMPLEX:
                if (name != null && name.length() > 0) return name;
                if (address != null && address.length() > 0) return address;
            case ROOM:
                StringBuilder builder = new StringBuilder();
                if (address != null && address.length() > 0) {
                    builder.append(address);
                    if (name != null && name.length() > 0) {
                        builder.append(" ").append(name);
                    }
                } else if (name != null && name.length() > 0) {
                    builder.append(name);
                }
                return builder.toString();
            default:
                return "";
        }
    }

    public Person.Priority getPriority() {

        switch (category) {
            case HOUSE:
            case ROOM:
                Visit visit = VisitList.getInstance().getLatestVisitToPlace(this.id);
                if (visit == null) {
                    return Person.Priority.NONE;
                }
                return visit.getPriority();

            case HOUSING_COMPLEX:
                Place room = PlaceList.getInstance().getMostPriorRoom(this.id);
                if (room == null) {
                    return Person.Priority.NONE;
                }
                return room.getPriority();
            default:
                return Person.Priority.NONE;
        }
        // DONE: 2017/03/05 実際のpriority処理を記述
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        Place clonedPlace = (Place) super.clone();

        clonedPlace.latLng = new LatLng(this.latLng.latitude, this.latLng.longitude);
        clonedPlace.address = this.address;
//        clonedPlace.markerId = this.markerId;

        return clonedPlace;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
        
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        
    }

    public int getChildCount() {

        if (category == Category.HOUSING_COMPLEX) {
            return PlaceList.getInstance().getRoomList(id).size();
        }

        return -1;
    }

    public boolean needsAddressRequest() {
        return address == null || address.trim().length() <= 0;
    }
}
