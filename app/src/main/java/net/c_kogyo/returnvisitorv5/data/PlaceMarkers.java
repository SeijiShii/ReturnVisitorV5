package net.c_kogyo.returnvisitorv5.data;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/05/09.
 */

public class PlaceMarkers {

    class PlaceMarker {

        String placeId;
        Marker marker;

        PlaceMarker(String placeId, Marker marker) {
            this.placeId = placeId;
            this.marker = marker;
        }

    }

    private ArrayList<PlaceMarker> markers;
    private GoogleMap mMap;

    public PlaceMarkers(GoogleMap map) {

        this.mMap = map;

        drawAllMarkers();
    }

    synchronized public void drawAllMarkers() {

        if (this.markers != null) {
            for (PlaceMarker marker : this.markers) {
                marker.marker.remove();
            }
        }

        this.markers = new ArrayList<>();

        for (Place place : PlaceList.getInstance()) {

            if (place.getCategory() != Place.Category.ROOM) {
                addMarker(place);
            }
        }
    }

    synchronized public void addMarker(Place place) {

        if (place.getCategory() == Place.Category.ROOM)
            return;

        MarkerOptions options = new MarkerOptions()
                .position(place.getLatLng())
                .draggable(true);
        if (place.getCategory() == Place.Category.HOUSE) {
            options.icon(BitmapDescriptorFactory.fromResource(Constants.markerRes[place.getPriority().num()]));
        } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            options.icon(BitmapDescriptorFactory.fromResource(Constants.complexRes[place.getPriority().num()]));
        }

        Marker marker = mMap.addMarker(options);
        markers.add(new PlaceMarker(place.getId(), marker));
    }

    synchronized public void refreshMarker(Place place) {
        PlaceMarker marker = getPlaceMarkerByPlace(place);
        if (marker != null) {
            if (place.getCategory() == Place.Category.HOUSE) {
                marker.marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.markerRes[place.getPriority().num()]));
            } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
                marker.marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.complexRes[place.getPriority().num()]));
            }
        } else {
            addMarker(place);
        }
    }

    synchronized private PlaceMarker getPlaceMarkerByPlace(Place place) {

        for (PlaceMarker marker : markers) {
            if (marker.placeId.equals(place.getId())) {
                return marker;
            }
        }
        return null;
    }

    @Nullable synchronized public Place getPlace(Marker marker) {

        for (PlaceMarker placeMarker : markers) {
            if (placeMarker.marker.equals(marker)) {
                return  PlaceList.getInstance().getById(placeMarker.placeId);
            }
        }
        return null;
    }

    synchronized public void removeByPlace(Place place) {
        PlaceMarker marker = getPlaceMarkerByPlace(place);
        if (marker == null) return;

        marker.marker.remove();
        markers.remove(marker);
    }

    public static Marker addSingleMarker(Place place, GoogleMap map) {

        MarkerOptions options = new MarkerOptions()
                .position(place.getLatLng())
                .draggable(false);
        if (place.getCategory() == Place.Category.HOUSE) {
            options.icon(BitmapDescriptorFactory.fromResource(Constants.markerRes[place.getPriority().num()]));
        } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            options.icon(BitmapDescriptorFactory.fromResource(Constants.complexRes[place.getPriority().num()]));
        } else if (place.getCategory() == Place.Category.ROOM) {
            options.icon(BitmapDescriptorFactory.fromResource(Constants.buttonRes[place.getPriority().num()]));
        }

        return map.addMarker(options);
    }


}
