package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.PlaceMarkers;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import static android.content.Context.MODE_PRIVATE;
import static net.c_kogyo.returnvisitorv5.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.ZOOM_LEVEL;
import static net.c_kogyo.returnvisitorv5.activity.MapActivity.MY_LOCATION_TAG;

/**
 * Created by SeijiShii on 2017/05/09.
 */

public class SetPlaceDialog extends FrameLayout
                    implements OnMapReadyCallback,
                                GoogleMap.OnMarkerClickListener,
                                GoogleMap.OnMapLongClickListener{

    private SetPlaceDialogListener mListener;
    private MapView mMapView;

    public SetPlaceDialog(@NonNull Context context,
                          SetPlaceDialogListener listener,
                          MapView mapView) {
        super(context);

        mMapView = mapView;
        mListener = listener;

        initCommon();
    }

    public SetPlaceDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {

        view = LayoutInflater.from(getContext()).inflate(R.layout.set_place_dialog, this);

        initMapFrame();
        initCloseButton();

    }

    private void initMapFrame() {

        FrameLayout mapFrame = (FrameLayout) view.findViewById(R.id.map_view);

        if (mMapView == null)
            return;

        mapFrame.addView(mMapView);
        mMapView.getMapAsync(this);
    }

    private PlaceMarkers placeMarkers;

    private GoogleMap mMap;
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Permissionの扱いが変化するため
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            //
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (Build.VERSION.SDK_INT >= 23) {
            setMyLocationButton();
        }

        // xmlでの指定の方法が分からん
        int padding = (int) (getResources().getDisplayMetrics().density * 40);
        mMap.setPadding(0, padding, 0, padding);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        placeMarkers = new PlaceMarkers(googleMap);

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLongClickListener(this);

        loadCameraPosition();
    }

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 718;
    private void setMyLocationButton() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(MY_LOCATION_TAG, "Permissions yet given.");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                Log.d(MY_LOCATION_TAG, "Should show Explanation.");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity)getContext(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mListener != null) {
            mListener.onSetLatLng(latLng);
        }
        saveCameraPosition();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Place place = placeMarkers.getPlace(marker);
        if (place != null) {
            if (mListener != null) {
                mListener.onSetPlace(place);
            }
        }
        saveCameraPosition();
        return true;
    }

    private void loadCameraPosition() {
        SharedPreferences prefs = getContext().getSharedPreferences(RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        float zoomLevel = prefs.getFloat(ZOOM_LEVEL, 0f);
        double lat = Double.valueOf(prefs.getString(LATITUDE, "1000"));
        double lng = Double.valueOf(prefs.getString(LONGITUDE, "1000"));

        if (lat >= 1000 || lng >= 1000) return;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
    }

    private void saveCameraPosition() {

        SharedPreferences prefs = getContext().getSharedPreferences(RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float zoomLevel = mMap.getCameraPosition().zoom;
        String latSt = String.valueOf(mMap.getCameraPosition().target.latitude);
        String lngSt = String.valueOf(mMap.getCameraPosition().target.longitude);

        editor.putFloat(ZOOM_LEVEL, zoomLevel);
        editor.putString(LATITUDE, latSt);
        editor.putString(LONGITUDE, lngSt);

        editor.apply();
    }

    private void initCloseButton() {
        ImageView closeButton = (ImageView) view.findViewById(R.id.cancel_button);
        ViewUtil.setOnClickListener(closeButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    public interface SetPlaceDialogListener{

        void onSetPlace(Place place);

        void onSetLatLng(LatLng latLng);

        void onCancel();

    }
}
