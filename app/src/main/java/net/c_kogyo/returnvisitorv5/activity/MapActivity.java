package net.c_kogyo.returnvisitorv5.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
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
import net.c_kogyo.returnvisitorv5.data.HousingComplex;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.dialogcontents.HousingComplexDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.MapLongClickDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.PlaceDialog;
import net.c_kogyo.returnvisitorv5.view.SmallTagView;

import java.util.ArrayList;

import static net.c_kogyo.returnvisitorv5.activity.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.EDIT_VISIT_ACTION;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.EDIT_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_PLACE_ACTION;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_VISIT_ACTION_WITH_PLACE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS;
import static net.c_kogyo.returnvisitorv5.activity.Constants.SharedPrefTags.ZOOM_LEVEL;
import static net.c_kogyo.returnvisitorv5.data.Place.PLACE;
import static net.c_kogyo.returnvisitorv5.data.Visit.VISIT;

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
        RVData.getInstance().loadData(this, this);

        setContentView(R.layout.map_activity);

        initLogoButton();
        initMapView(savedInstanceState);
        initDialogOverlay();

//        testView();

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

    private PlaceMarkers placeMarkers;
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
                        placeMarkers = new PlaceMarkers();
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
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        showMapLongClickDialog(latLng);
    }

    private void showMapLongClickDialog(final LatLng latLng) {

        final Place tmpPlace = new Place(latLng);
        placeMarkers.addMarker(tmpPlace);

        MapLongClickDialog mapLongClickDialog
                = new MapLongClickDialog(this,
                new MapLongClickDialog.MapLongClickDialogListener() {
            @Override
            public void onClickNewSinglePlaceButton() {

                startRecordVisitActivityForNewPlace(latLng);

                // DONE: 2017/03/17 record single place action
                placeMarkers.removeByPlace(tmpPlace);
                fadeOutDialogOverlay(normalFadeOutListener);

            }

            @Override
            public void onClickHousingComplexButton() {
                // TODO: 2017/03/17 record complex action
                placeMarkers.removeByPlace(tmpPlace);
                fadeOutDialogOverlay(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dialogFrame.removeAllViews();
                        showHousingComplexDialog(tmpPlace.getLatLng());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });


            }

            @Override
            public void onClickNotHomeButton() {
                // TODO: 2017/03/17 record not home action
                placeMarkers.removeByPlace(tmpPlace);
                recordNotHome(tmpPlace);
                fadeOutDialogOverlay(normalFadeOutListener);
            }

            @Override
            public void onClickCancelButton() {
                placeMarkers.removeByPlace(tmpPlace);
                fadeOutDialogOverlay(normalFadeOutListener);
            }
        });
        dialogFrame.addView(mapLongClickDialog);
        fadeInDialogOverlay();

        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                placeMarkers.removeByPlace(tmpPlace);
                fadeOutDialogOverlay(normalFadeOutListener);
                return true;
            }
        });

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

    private void fadeOutDialogOverlay(Animator.AnimatorListener listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dialogOverlay.setAlpha((float) valueAnimator.getAnimatedValue());
                dialogOverlay.requestLayout();
            }
        });
        if (listener != null)
            animator.addListener(listener);
        animator.setDuration(500);
        animator.start();
    }

    private LinearLayout dialogFrame;
    private void initDialogFrame() {
        dialogFrame = (LinearLayout) findViewById(R.id.dialog_frame);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE:
                if (resultCode == VISIT_ADDED_RESULT_CODE) {
                    String visitId = data.getStringExtra(VISIT);
                    if (visitId != null) {
                        Visit visit = RVData.getInstance().getVisitList().getById(visitId);
                        if (visit != null) {
                            String placeId = visit.getPlaceId();
                            Place place = RVData.getInstance().getPlaceList().getById(placeId);
                            if (place != null) {
                                placeMarkers.addMarker(place);
                            }
                        }
                    }
                }
                break;
            case EDIT_VISIT_REQUEST_CODE:
                if (resultCode == VISIT_EDITED_RESULT_CODE) {
                    String visitId = data.getStringExtra(VISIT);
                    if (visitId != null) {
                        Visit visit = RVData.getInstance().getVisitList().getById(visitId);
                        if (visit != null) {
                            String placeId = visit.getPlaceId();
                            Place place = RVData.getInstance().getPlaceList().getById(placeId);
                            if (place != null) {
                                placeMarkers.refreshMarker(place);
                            }
                        }
                    }
                }
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

        PlaceDialog placeDialog
                = new PlaceDialog(this,
                        place,
                        new PlaceDialog.PlaceDialogListener() {
                            @Override
                            public void onRecordVisitClick(Place place) {
                                fadeOutDialogOverlay(normalFadeOutListener);
                                // DONE: 2017/03/16 Record Visitへの遷移
                                startRecordVisitActivityForNewVisit(place);

                            }

                            @Override
                            public void onCancelClick() {
                                fadeOutDialogOverlay(normalFadeOutListener);
                            }

                            @Override
                            public void onDeleteClick(Place place) {
                                fadeOutDialogOverlay(normalFadeOutListener);
                                RVData.getInstance().getPlaceList().removeById(place.getId());
                                RVData.getInstance().saveData(MapActivity.this, null);
                                placeMarkers.removeByPlace(place);
                            }

                            @Override
                            public void onEditVisitClick(Visit visit) {
                                fadeOutDialogOverlay(normalFadeOutListener);
                                startRecordVisitActivityToEditVisit(visit);
                            }
                        });
        dialogFrame.addView(placeDialog);

        fadeInDialogOverlay();
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fadeOutDialogOverlay(normalFadeOutListener);
                return true;
            }
        });
    }

    // Method for Record Visit Activity
    private void startRecordVisitActivityForNewPlace(LatLng latLng) {
        Intent recordVisitIntent = new Intent(this, RecordVisitActivity.class);
        recordVisitIntent.setAction(NEW_PLACE_ACTION);
        recordVisitIntent.putExtra(LATITUDE, latLng.latitude);
        recordVisitIntent.putExtra(LONGITUDE, latLng.longitude);
        startActivityForResult(recordVisitIntent, NEW_VISIT_REQUEST_CODE);
    }

    private void startRecordVisitActivityToEditVisit(Visit visit) {
        // DONE: 2017/03/16
        Intent editVisitIntent = new Intent(this, RecordVisitActivity.class);
        editVisitIntent.setAction(EDIT_VISIT_ACTION);
        editVisitIntent.putExtra(VISIT, visit.getId());
        startActivityForResult(editVisitIntent, EDIT_VISIT_REQUEST_CODE);
    }

    private void startRecordVisitActivityForNewVisit(Place place) {
        Intent newVisitIntent = new Intent(this, RecordVisitActivity.class);
        newVisitIntent.setAction(NEW_VISIT_ACTION_WITH_PLACE);
        newVisitIntent.putExtra(PLACE, place.getId());
        startActivityForResult(newVisitIntent, NEW_VISIT_REQUEST_CODE);
    }

    private void showHousingComplexDialog(LatLng latLng) {

        HousingComplex housingComplex = new HousingComplex(latLng);
        HousingComplexDialog housingComplexDialog
                = new HousingComplexDialog(this,
                        housingComplex,
                        new HousingComplexDialog.HousingComplexDialogListener() {
                            @Override
                            public void onClickAddRoomButton(String roomName) {

                            }

                            @Override
                            public void onClickRoomCell() {

                            }

                            @Override
                            public void onClickOkButton() {

                            }

                            @Override
                            public void onClickCancelButton() {

                            }
                        });
        dialogFrame.addView(housingComplexDialog);
        fadeInDialogOverlay();
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                fadeOutDialogOverlay(normalFadeOutListener);
                return true;
            }
        });

    }

    private Animator.AnimatorListener normalFadeOutListener
            = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            hideSoftKeyboard();
            dialogOverlay.setVisibility(View.INVISIBLE);
            dialogFrame.removeAllViews();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private class PlaceMarkers {
        private ArrayList<Marker> markers;

        public PlaceMarkers() {
            this.markers = new ArrayList<>();
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
            markers.add(marker);
        }

        private void refreshMarker(Place place) {
            Marker marker = getMarkerByPlace(place);
            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.markerRes[place.getPriority().num()]));
            }
        }

        private Marker getMarkerByPlace(Place place) {

            for (Marker marker : markers) {
                if (place.getMarkerId().equals(marker.getId())) {
                    return marker;
                }
            }
            return null;
        }

        private void removeByPlace(Place place) {
            Marker marker = getMarkerByPlace(place);
            if (marker == null) return;

            markers.remove(marker);
            marker.remove();
        }

    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void recordNotHome(Place place) {
        Visit visit = new Visit(place);
        visit.setPriority(Visit.Priority.NOT_HOME);
        RVData.getInstance().getPlaceList().setOrAdd(place);
        RVData.getInstance().getVisitList().setOrAdd(visit);
        RVData.getInstance().saveData(this, null);

        placeMarkers.addMarker(place);
    }

    private void testView() {
        SmallTagView smallTagView = new SmallTagView(this, new Tag("hogehoge"));
        Log.d("", "SmallTagViewWidth: " + smallTagView.getViewWidth());
//        dialogFrame.addView(smallTagView);
    }

}
