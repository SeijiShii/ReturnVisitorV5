package net.c_kogyo.returnvisitorv5.data;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.c_kogyo.returnvisitorv5.Constants;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/05/09.
 */

public class PlaceMarkers {

    private class PlaceMarker {

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
        this.markers = new ArrayList<>();

        drawAllMarkers();
    }

    public void drawAllMarkers() {

        for (PlaceMarker marker : this.markers) {
            marker.marker.remove();
        }

        this.markers = new ArrayList<>();

        for (Place place : RVData.getInstance().placeList) {

            if (place.getCategory() != Place.Category.ROOM) {
                addMarker(place);
            }
        }
    }

    public void addMarker(Place place) {

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

    public void refreshMarker(Place place) {
        Marker marker = getMarkerByPlace(place);
        if (marker != null) {
            if (place.getCategory() == Place.Category.HOUSE) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.markerRes[place.getPriority().num()]));
            } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.complexRes[place.getPriority().num()]));
            }
        } else {
            addMarker(place);
        }
    }

    public Marker getMarkerByPlace(Place place) {

        for (PlaceMarker marker : markers) {
            if (marker.placeId.equals(place.getId())) {
                return marker.marker;
            }
        }
        return null;
    }

    @Nullable public Place getPlace(Marker marker) {

        for (PlaceMarker placeMarker : markers) {
            if (placeMarker.marker.equals(marker)) {
                return  RVData.getInstance().placeList.getById(placeMarker.placeId);
            }
        }
        return null;
    }

    public void removeByPlace(Place place) {
        Marker marker = getMarkerByPlace(place);
        if (marker == null) return;

        markers.remove(marker);
        marker.remove();
    }

}
