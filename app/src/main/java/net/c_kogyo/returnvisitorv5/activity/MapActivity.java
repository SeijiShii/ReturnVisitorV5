package net.c_kogyo.returnvisitorv5.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.dialogcontents.PlaceDialog;

import static net.c_kogyo.returnvisitorv5.activity.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_PLACE_ACTION;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS;
import static net.c_kogyo.returnvisitorv5.activity.Constants.SharedPrefTags.ZOOM_LEVEL;

public class MapActivity extends AppCompatActivity
                            implements OnMapReadyCallback,
                                        GoogleMap.OnMapLongClickListener,
                                        GoogleMap.OnMarkerClickListener,
                                        GoogleMap.OnMarkerDragListener,
                                        RVData.RVDataStoreCallback{

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初期化のために一回ゲットする
        RVData.getInstance().loadData(this);

        setContentView(R.layout.map_activity);

        initLogoButton();
        initMapView(savedInstanceState);
        initDialogOverlay();

    }

    private boolean isDataLoaded = false;
    @Override
    public void onDataLoaded() {
        isDataLoaded = true;
    }

    @Override
    public void onDataSaved() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    private MapView mapView;
    private void initMapView(Bundle savedInstanceState){
        mapView = (MapView) findViewById(R.id.map_view);

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }

    private static final String MY_LOCATION_TAG = "my_location";
    private GoogleMap mMap;
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Permissionの扱いが変化するため
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {

            Log.d(MY_LOCATION_TAG,e.getMessage());
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (Build.VERSION.SDK_INT >= 23) {

            setMyLocationButton();
        }

        // xmlでの指定の方法が分からん
        int topPadding = (int) (getResources().getDisplayMetrics().density * 50);
        mMap.setPadding(0, topPadding, 0, 0);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        loadCameraPosition();

        initDataOnMap();
    }
    
    private void initDataOnMap() {
        
        final Handler handler = new Handler();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                
                while (!isDataLoaded) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        mMap.setOnMapLongClickListener(MapActivity.this);
                        mMap.setOnMarkerClickListener(MapActivity.this);
                        mMap.setOnMarkerDragListener(MapActivity.this);

                        // DONE: 2017/03/01 ここにマーカー描画処理を記述する
                        drawAllMarkers();
                    }
                });
                
            }
        }).start();
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        saveCameraPosition();

        try {

            mMap.setMyLocationEnabled(false);
        } catch (SecurityException e) {
            //
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    private void loadCameraPosition() {
        SharedPreferences prefs = getSharedPreferences(RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        float zoomLevel = prefs.getFloat(ZOOM_LEVEL, 0f);
        double lat = Double.valueOf(prefs.getString(LATITUDE, "1000"));
        double lng = Double.valueOf(prefs.getString(LONGITUDE, "1000"));

        if (lat >= 1000 || lng >= 1000) return;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
    }

    private void saveCameraPosition() {

        SharedPreferences prefs = getSharedPreferences(RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float zoomLevel = mMap.getCameraPosition().zoom;
        String latSt = String.valueOf(mMap.getCameraPosition().target.latitude);
        String lngSt = String.valueOf(mMap.getCameraPosition().target.longitude);

        editor.putFloat(ZOOM_LEVEL, zoomLevel);
        editor.putString(LATITUDE, latSt);
        editor.putString(LONGITUDE, lngSt);

        editor.apply();
    }

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 717;
    private void setMyLocationButton() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(MY_LOCATION_TAG, "Permissions yet given.");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                Log.d(MY_LOCATION_TAG, "Should show Explanation.");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Log.d(MY_LOCATION_TAG, "YES! permissions given.");

                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.d(MY_LOCATION_TAG,e.getMessage());
                    }
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Intent recordVisitIntent = new Intent(this, RecordVisitActivity.class);
        recordVisitIntent.setAction(NEW_PLACE_ACTION);
        recordVisitIntent.putExtra(LATITUDE, latLng.latitude);
        recordVisitIntent.putExtra(LONGITUDE, latLng.longitude);
        startActivityForResult(recordVisitIntent, NEW_VISIT_REQUEST_CODE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Place place = RVData.getInstance().getPlaceList().getByMarkerId(marker.getId());
        if (place == null) return false;

        showPlaceDialog(place);

        return false;
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private RelativeLayout dialogOverlay;
    private void initDialogOverlay() {
        dialogOverlay = (RelativeLayout) findViewById(R.id.dialog_overlay);
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                fadeOutDialogOverlay();
                return true;
            }
        });

        initDialogFrame();
    }

    private void fadeInDialogOverlay() {

        dialogOverlay.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dialogOverlay.setAlpha((float) valueAnimator.getAnimatedValue());
                dialogOverlay.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    private void fadeOutDialogOverlay() {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dialogOverlay.setAlpha((float) valueAnimator.getAnimatedValue());
                dialogOverlay.requestLayout();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                dialogOverlay.setVisibility(View.INVISIBLE);
                dialogFrame.removeAllViews();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.setDuration(500);
        animator.start();
    }

    private LinearLayout dialogFrame;
    private void initDialogFrame() {
        dialogFrame = (LinearLayout) findViewById(R.id.dialog_frame);
    }

    private void drawAllMarkers() {
        mMap.clear();
        for (Place place : RVData.getInstance().getPlaceList()) {

            addMarker(place);

        }
    }

    private void addMarker(Place place) {
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(Constants.markerRes[place.getPriority().num()]))
                .position(place.getLatLng());

        Marker marker = mMap.addMarker(options);
        place.setMarkerId(marker.getId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE:
                if (resultCode == VISIT_ADDED_RESULT_CODE) {
                    String visitId = data.getStringExtra(Visit.VISIT);
                    if (visitId != null) {
                        Visit visit = RVData.getInstance().getVisitList().getById(visitId);
                        if (visit != null) {
                            String placeId = visit.getPlaceId();
                            Place place = RVData.getInstance().getPlaceList().getById(placeId);
                            if (place != null) {
                                addMarker(place);
                            }
                        }
                    }
                }
                break;
        }
    }

    private void initLogoButton() {
        final ImageView logoButton = (ImageView) findViewById(R.id.logo_button);
        logoButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        logoButton.setAlpha(0.5f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        logoButton.setAlpha(1f);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        logoButton.setAlpha(1f);
                        return true;
                }

                return false;
            }
        });
    }

    private void showPlaceDialog(Place place) {

        PlaceDialog placeDialog = new PlaceDialog(this, place);
        dialogFrame.addView(placeDialog);

        fadeInDialogOverlay();
    }


}
