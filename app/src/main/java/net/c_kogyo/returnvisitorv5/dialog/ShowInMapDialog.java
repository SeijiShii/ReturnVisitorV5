package net.c_kogyo.returnvisitorv5.dialog;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.PlaceMarkers;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.c_kogyo.returnvisitorv5.activity.MapActivity.MY_LOCATION_TAG;

/**
 * Created by SeijiShii on 2017/05/30.
 */

public class ShowInMapDialog extends DialogFragment
        implements OnMapReadyCallback,
                    GoogleMap.OnMarkerClickListener {

    private static MapDialogListener mListener;

    private static final String PLACE_ID = "place_id";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    public static ShowInMapDialog newInstance(String placeId, MapDialogListener listener) {

        mListener = listener;

        Bundle arg = new Bundle();
        arg.putString(PLACE_ID, placeId);

        ShowInMapDialog dialog = new ShowInMapDialog();

        dialog.setArguments(arg);

        return dialog;
    }

    private Place mPlace;

    private View view;
    private MapView mMapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        getArg();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        view = View.inflate(getActivity(), R.layout.map_dialog, null);

        initMapView(savedInstanceState);

        builder.setView(view);

        builder.setTitle(R.string.show_in_map);
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onCloseDialog();
                }
            }
        });

        return builder.create();
    }

    private void getArg() {

        Bundle arg = getArguments();

        String placeId = arg.getString(PLACE_ID);
        mPlace = PlaceList.getInstance().getById(placeId);

    }

    private GoogleMap mMap;
    private void initMapView(Bundle savedInstanceState) {

        mMapView = (MapView) view.findViewById(R.id.map_view);

//        // *** IMPORTANT ***
//        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
//        // objects or sub-Bundles.
//        Bundle mapViewBundle = null;
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
//        }
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);
    }

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

        mMap.getUiSettings().setMapToolbarEnabled(false);

        // xmlでの指定の方法が分からん
        int padding = (int) (getResources().getDisplayMetrics().density * 40);
        mMap.setPadding(0, 0, 0, padding);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(this);

        PlaceMarkers.addSingleMarker(mPlace, mMap);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPlace.getLatLng(), 19));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (mListener != null) {
            mListener.onMarkerClick(mPlace);
            dismiss();
            return true;
        }
        return false;
    }

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 718;
    private void setMyLocationButton() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(MY_LOCATION_TAG, "Permissions yet given.");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                Log.d(MY_LOCATION_TAG, "Should show Explanation.");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mMapView.onStop();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        mMapView.onResume();
        isShowing.set(false);

    }

    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface MapDialogListener {
        void onMarkerClick(Place place);

        void onCloseDialog();
    }

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShowing.getAndSet(true)) return;

        try {
            super.show(manager, tag);
        } catch (Exception e) {
            isShowing.set(false);
        }
    }


}
