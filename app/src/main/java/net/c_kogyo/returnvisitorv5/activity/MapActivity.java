package net.c_kogyo.returnvisitorv5.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.LoginHelper;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSyncDataFrame;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.PlaceMarkers;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.data.list.PersonList;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;
import net.c_kogyo.returnvisitorv5.dialog.AddWorkDialog;
import net.c_kogyo.returnvisitorv5.dialog.HousingComplexDialog;
import net.c_kogyo.returnvisitorv5.dialog.MapLongClickDialog;
import net.c_kogyo.returnvisitorv5.dialog.PersonDialog;
import net.c_kogyo.returnvisitorv5.dialog.PlaceDialog;
import net.c_kogyo.returnvisitorv5.dialog.SearchDialog;
import net.c_kogyo.returnvisitorv5.dialog.TermOfUseDialog;
import net.c_kogyo.returnvisitorv5.service.FetchAddressIntentService;
import net.c_kogyo.returnvisitorv5.service.TimeCountIntentService;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ErrorLogIntentService;
import net.c_kogyo.returnvisitorv5.util.InputUtil;
import net.c_kogyo.returnvisitorv5.util.SoftKeyboard;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.CountTimeFrame;

import java.util.ArrayList;
import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.EDIT_VISIT_ACTION;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.EDIT_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_HOUSE_ACTION;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_PLACE_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_VISIT_ACTION_WITH_PLACE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.PLACE_ADDED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.ZOOM_LEVEL;
import static net.c_kogyo.returnvisitorv5.data.Place.PLACE;
import static net.c_kogyo.returnvisitorv5.data.Visit.VISIT;

public class MapActivity extends AppCompatActivity
                            implements OnMapReadyCallback,
                                        GoogleMap.OnMapLongClickListener,
                                        GoogleMap.OnMarkerClickListener,
                                        GoogleMap.OnMarkerDragListener,
                                        RVCloudSync.RVCloudSyncCallback,
                                        GoogleApiClient.OnConnectionFailedListener,
                                        GoogleApiClient.ConnectionCallbacks{

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private static boolean isForeground;
    private static final String TAG = "MapActivity";


    private Handler cloudResultHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RVDBHelper.initialize(this);

        // log
        Intent errorLogIntent = new Intent(this, ErrorLogIntentService.class);
        startService(errorLogIntent);

        cloudResultHandler = new Handler();

        setContentView(R.layout.map_activity);
        initWaitScreen();
        initProgressFrame();
        initWaitMessageText();
        initLogoButton();

        initDummyFocusView();
        initSoftKeyboard();
        initSearchText();

        RVCloudSync.getInstance().setCallback(this);

        initLocalBroadcast();

        AdMobHelper.setAdView(this);

        initMapView(savedInstanceState);

        initDrawerOverlay();

        refreshWorkButton();
        refreshCalendarButton();
        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);

    }


//    private boolean isDataLoaded = false;

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

    public static final String MY_LOCATION_TAG = "my_location";
    private GoogleMap mMap;
    private PlaceMarkers placeMarkers;

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setAllGesturesEnabled(true);

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
        int dp50 = (int) (getResources().getDisplayMetrics().density * 50);
        int dp25 = (int) (getResources().getDisplayMetrics().density * 25);
        mMap.setPadding(0, dp50, 0, dp25);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        loadCameraPosition();

        mMap.setOnMapLongClickListener(MapActivity.this);
        mMap.setOnMarkerClickListener(MapActivity.this);
        mMap.setOnMarkerDragListener(MapActivity.this);

        // DONE: 2017/03/01 ここにマーカー描画処理を記述する
        if (placeMarkers == null) {
            placeMarkers = new PlaceMarkers(mMap);
        } else {
            placeMarkers.drawAllMarkers();
        }

//        refreshLogoButton();
        refreshWorkButton();
        refreshCalendarButton();
        enableWaitScreen(false);

//        waitForDataLoadedAndRefreshUI();
    }


    @Override
    protected void onStart() {
        super.onStart();

        InputUtil.hideSoftKeyboard(this);
        mapView.onStart();
        isForeground = true;

        refreshWorkButton();
        refreshCalendarButton();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        isForeground = false;
    }

    public static boolean isInForeground() {
        return isForeground;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mMap.setMyLocationEnabled(false);
        } catch (SecurityException e) {
            //
        }

        mapView.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        softKeyboard.unRegisterSoftKeyboardCallback();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        saveCameraPosition();
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
        restartTimeCountIfNeeded();

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

        if (mMap == null) return;

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        animateCameraIfHasIntentPosition(intent);
        animateCameraIfHasIntentPlace(intent);
    }

    private void animateCameraIfHasIntentPosition(Intent intent) {

        // DONE: 2017/04/03 intentのデータが消失する件
        if (intent == null)
            return;

        double lat = intent.getDoubleExtra(LATITUDE, 1000);
        double lng = intent.getDoubleExtra(LONGITUDE, 1000);

        if (lat != 1000 && lng != 1000 && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        }
    }

    private void animateCameraIfHasIntentPlace(Intent intent) {

        if (intent == null)
            return;

        String placeId = intent.getStringExtra(Place.PLACE);

        if (placeId == null) return;

        final Place place = PlaceList.getInstance().getById(placeId);

        if (place == null) return;

        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showDialogFitToPlace(place);
            }
        }, 1000);

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
        showMapLongClickPopup(latLng);
    }

    private void showMapLongClickPopup(final LatLng latLng) {

        final Place tmpPlace = new Place(latLng, Place.Category.HOUSE);
        placeMarkers.addMarker(tmpPlace);

        final PopupWindow popupWindow = new PopupWindow(this);

        // 背景設定
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray_fog_circle));

        // タップ時に他のViewでキャッチされないための設定
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        popupWindow.setContentView(new MapLongClickDialog(this, new MapLongClickDialog.MapLongClickDialogListener() {
            @Override
            public void onClickNewSinglePlaceButton() {

                popupWindow.dismiss();

                startRecordVisitActivityForNewPlace(latLng);

                // DONE: 2017/03/17 record single place action
                placeMarkers.removeByPlace(tmpPlace);

            }

            @Override
            public void onClickHousingComplexButton() {

                popupWindow.dismiss();

                showHousingComplexDialog(tmpPlace);
            }

            @Override
            public void onClickNotHomeButton() {

                popupWindow.dismiss();

                // DONE: 2017/03/17 record not home action
                placeMarkers.removeByPlace(tmpPlace);
                recordNotHome(tmpPlace);
            }

            @Override
            public void onClickCancelButton() {

                popupWindow.dismiss();

                placeMarkers.removeByPlace(tmpPlace);
            }
        }));
        popupWindow.showAtLocation(mapView, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                placeMarkers.removeByPlace(tmpPlace);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Place place = placeMarkers.getPlace(marker);
        if (place == null) return false;

        if (place.getCategory() == Place.Category.HOUSE) {
            showPlaceDialog(place);
        } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            showHousingComplexDialog(place);
        }
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
        Place place = placeMarkers.getPlace(marker);

        if (place == null) return;

        place.setLatLng(marker.getPosition());
        place.setAddress(null);

        RVDBHelper.getInstance().save(place);
        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == VISIT_ADDED_RESULT_CODE
                || resultCode == PLACE_ADDED_RESULT_CODE
                || resultCode == VISIT_EDITED_RESULT_CODE) {
            String visitId = data.getStringExtra(VISIT);
            if (visitId != null) {
                Visit visit = VisitList.getInstance().getById(visitId);
                if (visit != null) {
                    String placeId = visit.getPlaceId();
                    Place place = PlaceList.getInstance().getById(placeId);
                    if (place != null) {
                        if (resultCode == PLACE_ADDED_RESULT_CODE) {
                            // 新しい場所の追加である
                            if (place.getCategory() == Place.Category.HOUSE) {
                                // 新しい家なのでマーカーを追加
                                placeMarkers.addMarker(place);
                            } else if (place.getCategory() == Place.Category.ROOM) {
                                // 新しい場所だが、部屋なのでアパートを更新
                                String parentId = place.getParentId();
                                Place parent = PlaceList.getInstance().getById(parentId);
                                if (parent != null) {
                                    placeMarkers.refreshMarker(parent);
                                }
                            }
                        } else {
                            // 訪問の編集、または既にある場所に訪問を追加した
                            placeMarkers.refreshMarker(place);
                        }
                    }
                }
            }
        }
        else if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private ImageView logoButton;
    private void initLogoButton() {
        logoButton = (ImageView) findViewById(R.id.logo_button);
        ViewUtil.setOnClickListener(logoButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View view) {
                openCloseDrawer();
            }
        });
    }

    private void showPlaceDialog(Place place) {

        PlaceDialog.getInstance(place,
                        new PlaceDialog.PlaceDialogListener() {
                            @Override
                            public void onRecordVisitClick(Place place) {

                                // DONE: 2017/03/16 RVRecord Visitへの遷移
                                startRecordVisitActivityForNewVisit(place);

                            }

                            @Override
                            public void onDeleteClick(Place place) {

                                placeMarkers.removeByPlace(place);
                                PlaceList.getInstance().deleteById(place.getId());

                                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(MapActivity.this);
                            }

                            @Override
                            public void onEditVisitClick(Visit visit) {
                                startRecordVisitActivityToEditVisit(visit);
                            }

                            @Override
                            public void onCloseDialog() {
                                InputUtil.hideSoftKeyboard(MapActivity.this);
                            }

                            @Override
                            public void onClickEditPerson(Person person) {
                                showPersonDialogForEdit(person);
                            }

                            @Override
                            public void onClickNotHomeButton(Place place) {
                                // DONE: 2017/06/10 Not home action
                                recordNotHome(place);
                            }

                            @Override
                            public void onDeleteVisit(Place place, Visit visit) {
                                placeMarkers.refreshMarker(place);
                            }
                        }).show(getFragmentManager(), null);

    }

    // Method for RVRecord Visit Activity
    private void startRecordVisitActivityForNewPlace(LatLng latLng) {
        Intent recordVisitIntent = new Intent(this, RecordVisitActivity.class);
        recordVisitIntent.setAction(NEW_HOUSE_ACTION);
        recordVisitIntent.putExtra(LATITUDE, latLng.latitude);
        recordVisitIntent.putExtra(LONGITUDE, latLng.longitude);
        startActivityForResult(recordVisitIntent, NEW_PLACE_REQUEST_CODE);
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

    private void showHousingComplexDialog(final Place housingComplex) {

        housingComplex.setCategory(Place.Category.HOUSING_COMPLEX);

        HousingComplexDialog.getInstance(housingComplex,
                new HousingComplexDialog.HousingComplexDialogListener() {
                    @Override
                    public void onClickAddRoomButton(Place newRoom) {

                        startRecordVisitActivityForNewVisit(newRoom);
                    }

                    @Override
                    public void onClickRoomCell(final Place room) {

                        showPlaceDialog(room);
                    }

                    @Override
                    public void onOkClick(Place housingComplex) {

                        placeMarkers.refreshMarker(housingComplex);
                    }

                    @Override
                    public void onDeleteHousingComplex(Place housingComplex) {

                        placeMarkers.removeByPlace(housingComplex);
                        PlaceList.getInstance().deleteById(housingComplex.getId());

                        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(MapActivity.this);
                    }

                    @Override
                    public void onCloseDialog() {
                        if (housingComplex.getChildCount() == 0) {
                            placeMarkers.removeByPlace(housingComplex);
                        } else {
                            placeMarkers.refreshMarker(housingComplex);
                        }
                        InputUtil.hideSoftKeyboard(MapActivity.this);
                    }

                }, true, true).show(getFragmentManager(), null);

    }

    private void recordNotHome(Place place) {

        Visit visit;

        Visit lastVisit = VisitList.getInstance().getLatestVisitToPlace(place.getId());

        if (lastVisit == null) {
            visit = new Visit(place);
        } else {
            visit = new Visit(lastVisit);
        }

        PlaceList.getInstance().setOrAdd(place);
        VisitList.getInstance().setOrAdd(visit);

        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);

        placeMarkers.addMarker(place);

        if (place.needsAddressRequest()) {
            FetchAddressIntentService.inquireAddress(place, this);
        }
    }

    // DONE: 2017/03/27 HousingComplexMarkerRes
    // PENDING: 2017/03/27 Mapを回転させる
    // DONE: 2017/03/27 ドロワー
    private View drawerOverlay;
    private ScrollView drawer;
    int drawerWidth;

    private void initDrawerOverlay() {
        drawerOverlay = findViewById(R.id.drawer_overlay);
        drawerOverlay.setAlpha(0f);

        initDrawer();
    }

    private void initDrawer() {
        drawer = (ScrollView) findViewById(R.id.drawer);

        drawerWidth = (int) (getResources().getDisplayMetrics().density * 250);

        FrameLayout.LayoutParams params
                = new FrameLayout.LayoutParams(drawerWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(-drawerWidth, 0, 0, 0);
        drawer.setLayoutParams(params);

        initDrawerLogoButton();
        drawer.setOnTouchListener(new View.OnTouchListener() {

            float x1, x2;
            boolean swiped;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        swiped = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        x2 = event.getX();
                        if (x2 < x1 && !swiped) {
                            openCloseDrawer();
                            swiped = true;
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }

                return false;
            }
        });

        initSignInButton();
        initGoogleApiClient();

        initCountTimeFrame();
        initWorkButton();
        initCalendarButton();
        initAddWorkButton();
        initAddVisitButton();
        initSuggestionButton();
        initTermOfUseButton();
    }

    private void initDrawerLogoButton() {
        final ImageView drawerLogoButton = (ImageView) findViewById(R.id.drawer_logo_button);
        drawerLogoButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drawerLogoButton.setAlpha(0.5f);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        drawerLogoButton.setAlpha(1f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        drawerLogoButton.setAlpha(1f);
                        openCloseDrawer();
                        return true;
                }
                return false;
            }
        });
    }

    private boolean isDrawerOpen = false;
    private void openCloseDrawer() {

        float originAlpha, targetAlpha;
        if (isDrawerOpen) {
            originAlpha = 1f;
            targetAlpha = 0f;
            drawerOverlay.setOnTouchListener(null);
        } else {
            originAlpha = 0f;
            targetAlpha = 1f;
            drawerOverlay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    openCloseDrawer();
                    return true;
                }
            });
        }

        ValueAnimator overlayAnimator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        overlayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawerOverlay.setAlpha((float) animation.getAnimatedValue());
                drawerOverlay.requestLayout();
            }
        });

        int originMargin, targetMargin;
        if (isDrawerOpen) {
            originMargin = 0;
            targetMargin = -drawerWidth;
        } else {
            originMargin = -drawerWidth;
            targetMargin = 0;
        }

        ValueAnimator drawerAnimator = ValueAnimator.ofInt(originMargin, targetMargin);
        drawerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ((FrameLayout.LayoutParams) drawer.getLayoutParams())
                        .setMargins((int) animation.getAnimatedValue(), 0, 0, 0);
                drawer.requestLayout();
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.play(overlayAnimator).with(drawerAnimator);
        animatorSet.start();

        isDrawerOpen = !isDrawerOpen;
    }

//    private TextView loginStateText;
//    private void initLoginStateText() {
//        loginStateText = (TextView) findViewById(R.id.login_state_text);
//    }

//    private void refreshLoginStateText() {
//        if (LoginHelper.isLoggedIn(this)) {
//            String userName = LoginHelper.getAccountName(this);
//            if (userName == null) {
//                userName = "";
//            }
//            String s = getString(R.string.login_state, userName);
//            loginStateText.setText(s);
//        } else {
//            loginStateText.setText(R.string.sync_data);
//        }
//    }

    private GoogleApiClient mApiClient;
    private void initGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.web_client_id))
                .build();
        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mApiClient.connect();
    }

    private LinearLayout signInButton;
    private TextView signInText;
    private void initSignInButton() {

        signInButton = (LinearLayout) findViewById(R.id.sign_in_button);
        signInText = (TextView) findViewById(R.id.sign_in_text);
        refreshSignInButton();

    }

    private void refreshSignInButton() {
        if (LoginHelper.isLoggedIn(this)) {
            String s = getString(R.string.sign_out, LoginHelper.getAccountName(this));
            signInText.setText(s);
            ViewUtil.setOnClickListener(signInButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View view) {
                    confirmSignOut();
                }
            });
        } else {
            signInText.setText(R.string.sign_in);
            ViewUtil.setOnClickListener(signInButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View view) {
                    openCloseDrawer();
                    onClickSignIn();
                }
            });
        }
    }

    private static final int GOOGLE_SIGN_IN_REQUEST_CODE = 5001;
    private void onClickSignIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    private void confirmSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out_title);
        builder.setMessage(R.string.sign_out_message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.sign_out_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signOut();
                openCloseDrawer();
            }
        });
        builder.create().show();
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    LoginHelper.onLogOut(MapActivity.this);
                    refreshSignInButton();
                }
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mApiClient);
        if (pendingResult.isDone()) {
            GoogleSignInResult result = pendingResult.get();
            handleSignInResult(result);
        } else {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // サインインが成功したら、サインインボタンを消して、ユーザー名を表示する
            GoogleSignInAccount mAccount = result.getSignInAccount();
            if (mAccount != null) {
                String token = mAccount.getIdToken();
                Log.d(ACCOUNT_TEST_TAG, "token: " + token);
                LoginHelper.onLogin(this, mAccount.getEmail(), token);
                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);

            }

        } else {
            // Signed out, show unauthenticated UI.
            LoginHelper.onLogOut(this);
        }
        refreshSignInButton();
    }

    private CountTimeFrame countTimeFrame;
    private Handler timeCountHandler;
    private void initCountTimeFrame() {

        timeCountHandler = new Handler();

        countTimeFrame = (CountTimeFrame) findViewById(R.id.count_time_frame);
        countTimeFrame.setExtracted(TimeCountIntentService.isTimeCounting());
        countTimeFrame.setListener(new CountTimeFrame.CountTimeFrameListener() {
            @Override
            public void onClickStartButton() {
                Intent startTimeCountIntent = new Intent(getApplicationContext(), TimeCountIntentService.class);
                startTimeCountIntent.setAction(TimeCountIntentService.START_COUNTING_ACTION_TO_SERVICE);
                startService(startTimeCountIntent);
            }

            @Override
            public void onClickStopButton() {
                TimeCountIntentService.stopTimeCount();
            }

            @Override
            public void onChangeStart(Calendar calendar) {
                Intent changeStartIntent = new Intent(TimeCountIntentService.CHANGE_START_ACTION_TO_SERVICE);
                changeStartIntent.putExtra(TimeCountIntentService.START_TIME, calendar.getTimeInMillis());
                LocalBroadcastManager.getInstance(MapActivity.this).sendBroadcast(changeStartIntent);
            }
        });
    }

    private void restartTimeCountIfNeeded() {
        SharedPreferences preferences
            = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        boolean isCounting = preferences.getBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, false);
        if (isCounting) {

            if (TimeCountIntentService.isTimeCounting()) {
                return;
            }

            String workId = preferences.getString(Constants.SharedPrefTags.COUNTING_WORK_ID, null);

            if (workId == null) return;

            Intent restartCountIntent = new Intent(this, TimeCountIntentService.class);
            restartCountIntent.setAction(TimeCountIntentService.RESTART_COUNTING_ACTION_TO_SERVICE);
            restartCountIntent.putExtra(TimeCountIntentService.COUNTING_WORK_ID, workId);
            startService(restartCountIntent);
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           if (intent.getAction().equals(TimeCountIntentService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY)) {
               countTimeFrame.updateUI(false, 0, null, null);

               SharedPreferences preferences = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
               SharedPreferences.Editor editor = preferences.edit();
               editor.putBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, false);
               editor.apply();

               timeCountHandler.post(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(MapActivity.this, R.string.stop_time_count, Toast.LENGTH_SHORT).show();
                   }
               });

           } else if (intent.getAction().equals(TimeCountIntentService.TIME_COUNTING_ACTION_TO_ACTIVITY)) {

               long startTime = intent.getLongExtra(TimeCountIntentService.START_TIME, 0);
               Calendar start = Calendar.getInstance();
               start.setTimeInMillis(startTime);
               String startText = getString(R.string.start_time_text, DateTimeText.getTimeText(start, false));

               long duration = intent.getLongExtra(TimeCountIntentService.DURATION, 0);
               String durationText = getString(R.string.duration_string, DateTimeText.getDurationString(duration, true));

               countTimeFrame.updateUI(true, startTime, startText, durationText);

               String countingWorkId = intent.getStringExtra(TimeCountIntentService.COUNTING_WORK_ID);
               if (countingWorkId != null) {
                   SharedPreferences preferences = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
                   SharedPreferences.Editor editor = preferences.edit();
                   editor.putBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, true);
                   editor.putString(Constants.SharedPrefTags.COUNTING_WORK_ID, countingWorkId);
                   editor.apply();
               }
           } else if (intent.getAction().equals(FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION)) {
               String placeId = intent.getStringExtra(PLACE);
               if (placeId == null) return;

               Place place = PlaceList.getInstance().getById(placeId);
               if (place == null) return;

               String address = intent.getStringExtra(FetchAddressIntentService.ADDRESS_FETCHED);
               if (address == null) return;

               place.setAddress(address);
               PlaceList.getInstance().setOrAdd(place);
               RVCloudSync.getInstance().requestDataSyncIfLoggedIn(MapActivity.this);
           }
        }
    };

    private void initLocalBroadcast() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        manager.registerReceiver(receiver, new IntentFilter(TimeCountIntentService.TIME_COUNTING_ACTION_TO_ACTIVITY));
        manager.registerReceiver(receiver, new IntentFilter(TimeCountIntentService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY));
        manager.registerReceiver(receiver, new IntentFilter(FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION));
    }

//    private void restartTimeCountIfNeeded() {
//        SharedPreferences preferences
//                = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
//        boolean isCounting = preferences.getBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, false);
//        if (isCounting) {
//            String workId = preferences.getString(Constants.SharedPrefTags.COUNTING_WORK_ID, null);
//
//            if (workId == null) return;
//
//            Intent restartCountIntent = new Intent(this, TimeCountService.class);
//            restartCountIntent.setAction(TimeCountService.RESTART_COUNTING_ACTION_TO_SERVICE);
//            restartCountIntent.putExtra(TimeCountService.COUNTING_WORK_ID, workId);
//            startService(restartCountIntent);
//        }
//
//    }

    private Button workButton;
    private void initWorkButton() {
        workButton = (Button) findViewById(R.id.work_button);
    }

    private void refreshWorkButton() {
        if (RVDBHelper.getInstance().hasWorkOrVisit()) {
            workButton.setAlpha(1f);
            workButton.setClickable(true);
            workButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickWorkButton();
                    openCloseDrawer();
                }
            });
        } else {
            workButton.setAlpha(0.5f);
            workButton.setClickable(false);
            workButton.setOnClickListener(null);
        }
    }

    private void onClickWorkButton() {
        Intent workIntent = new Intent(this, WorkPagerActivity.class);
        startActivity(workIntent);

    }

    private Button calendarButton;
    private void initCalendarButton() {
        calendarButton = (Button) findViewById(R.id.calendar_button);
    }

    private void refreshCalendarButton() {

        if (RVDBHelper.getInstance().hasWorkOrVisit()) {
            calendarButton.setAlpha(1f);
            calendarButton.setClickable(true);
            calendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCalendarButton();
                    openCloseDrawer();
                }
            });
        } else {
            calendarButton.setAlpha(0.5f);
            calendarButton.setClickable(false);
            calendarButton.setOnClickListener(null);
        }
    }

    private void onClickCalendarButton() {
        // DONE: 2017/05/05 CalendarActivity遷移
        Intent calendarIntent = new Intent(this, CalendarPagerActivity.class);
        startActivity(calendarIntent);
    }

    // DONE: 2017/05/08 Add Work
    private void initAddWorkButton() {
        Button addWorkButton = (Button) findViewById(R.id.add_work_button);
        addWorkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCloseDrawer();
                showAddWorkDialog();
            }
        });
    }

    private void showAddWorkDialog() {
        AddWorkDialog.getInstance(new AddWorkDialog.AddWorkDialogListener() {
                    @Override
                    public void onOkClick(Work work) {
                        startWorkPagerActivityWithNewWork(work);

                    }

                      @Override
                      public void onCloseDialog() {
                          InputUtil.hideSoftKeyboard(MapActivity.this);
                      }
                },
                true,
                Calendar.getInstance()).show(getFragmentManager(), null);
    }

    private void initSuggestionButton() {
        Button suggestionButton = (Button) findViewById(R.id.suggestion_button);
        suggestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCloseDrawer();
                Intent intent = new Intent(MapActivity.this, VisitSuggestionActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startWorkPagerActivityWithNewWork(Work work) {

        WorkList.getInstance().setOrAdd(work);

        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);

        Intent withNewWorkIntent = new Intent(this, WorkPagerActivity.class);
        withNewWorkIntent.setAction(Constants.WorkPagerActivityActions.START_WITH_NEW_WORK);
        withNewWorkIntent.putExtra(Work.WORK, work.getId());
        startActivity(withNewWorkIntent);
    }

    // DONE: 2017/04/01 集合住宅のマークがでかすぎる

    // DONE: 2017/05/05 データ読み込みまでボタンを押せなくする
    // DONE: 2017/05/08 データ読み込みwait画面
    private View waitScreen;
    private void initWaitScreen() {
        waitScreen = findViewById(R.id.wait_screen);
        waitScreen.setAlpha(0f);
    }

    private LinearLayout progressFrame;
    private void initProgressFrame() {
        progressFrame = (LinearLayout) findViewById(R.id.progress_frame);
        progressFrame.setAlpha(0f);
    }

    private void enableWaitScreen(boolean enabled) {

        ViewUtil.fadeView(waitScreen, enabled, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        }, null, 500);

    }

    private void fadeProgressFrame(boolean fadeIn) {
        ViewUtil.fadeView(progressFrame, fadeIn, null, null, 500);
    }

    private TextView waitMessageText;
    private void initWaitMessageText() {
        waitMessageText = (TextView) findViewById(R.id.wait_message_text);
    }

    private void initAddVisitButton() {
        Button addVisitButton = (Button) findViewById(R.id.add_visit_button);
        addVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecordVisitActivityNoPlace();
                openCloseDrawer();
            }
        });
    }

    private void startRecordVisitActivityNoPlace() {
        Intent intent = new Intent(this, RecordVisitActivity.class);
        intent.setAction(Constants.RecordVisitActions.NEW_VISIT_ACTION_NO_PLACE);
        startActivity(intent);
    }

//    private void refreshLoginButton() {
//
//
//    }

//    private void onLoginClicked() {
//        showLoginDialog();
//    }

////    private static String LOGIN_DIALOG = "login_dialog";
//    private LoginDialog loginDialog;
//    private void showLoginDialog() {
//
//        loginDialog = LoginDialog.getInstance(new LoginDialog.LoginDialogListener() {
//
//            @Override
//            public void onLogoutClick() {
//                // DONE: 2017/05/13 onLogoutClick
//                confirmLogout();
//            }
//
//            @Override
//            public void onCloseDialog() {
//                InputUtil.hideSoftKeyboard(MapActivity.this);
//            }
//
//        }, new Handler());
//        loginDialog.show(getFragmentManager(), null);
//    }

//    private void confirmLogout() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.logout_title);
//        builder.setMessage(R.string.logout_message);
//        builder.setPositiveButton(R.string.logout_button_small, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                logout();
//            }
//        });
//        builder.setNegativeButton(R.string.cancel, null);
//        builder.create().show();
//    }

//    private void logout() {
//
//        LoginHelper.onLoggedOut(this);
//
//        if (loginDialog != null) {
//            loginDialog.postLogout();
//        }
//    }

    // DONE: 2017/05/05 データがないときにWORKやカレンダーに遷移しないようにする(実装済み、要検証)
    // DONE: 2017/05/06 AdView to Real
    // DONE: 2017/05/08 開始時間に秒はいらない
    // DONE: 2017/05/08 ログイン画面
    // DONE: 2017/05/08 データ同期まわり

    // DONE: 2017/05/16 save loginWithName state





    // DONE: 2017/05/21 データロード中、同期中の表示
    // DONE: 2017/05/21 同期結果の表示

    // RVDataCallback implementation
//    @Override
//    public void onStartSavingData() {
//        fadeProgressFrame(true);
//        waitMessageText.setText(R.string.saving);
//    }
//
//    @Override
//    public void onFinishSavingData() {
//        fadeProgressFrame(false);
//        waitMessageText.setText("");
//    }
//
//    @Override
//    public void onStartLoadingData() {
//        enableWaitScreen(true);
//        fadeProgressFrame(true);
//        waitMessageText.setText(R.string.loading);
//    }
//
//    @Override
//    public void onFinishLoadingData() {
//        refreshLogoButton();
//        isDataLoaded = true;
//        fadeProgressFrame(false);
//        enableWaitScreen(false);
//        waitMessageText.setText("");
//    }

    // RVCloudSync Implementation


    @Override
    public void onStartRequest(final RVCloudSyncDataFrame.FrameCategory frameCategory) {
        cloudResultHandler.post(new Runnable() {
            @Override
            public void run() {
                fadeProgressFrame(true);
                switch (frameCategory) {
                    case LOGIN_REQUEST:
                        enableWaitScreen(true);
                        waitMessageText.setText(R.string.start_login);
                        break;
                    case CREATE_USER_REQUEST:
                        enableWaitScreen(true);
                        waitMessageText.setText(R.string.creating_user);
                        break;
                    case SYNC_DATA_REQUEST_WITH_NAME:
                    case SYNC_DATA_REQUEST_WITH_FACEBOOK:
                        enableWaitScreen(false);
                        waitMessageText.setText(R.string.on_sync);
                        break;
                }
            }
        });
    }

    @Override
    public void onResponse(RVCloudSyncDataFrame dataFrame) {

        switch (dataFrame.getStatusCode()) {
            case STATUS_200_SYNC_END_OK:
                onDataSyncSuccess(dataFrame);
                break;

            case STATUS_201_CREATED_USER:
                LoginHelper.saveLastSyncDate(0, this);
                break;

            case STATUS_202_AUTHENTICATED:
                break;

            case STATUS_200_SYNC_START_OK:
                postRequestResult(dataFrame);
                break;

            case STATUS_400_DUPLICATE_USER_NAME:
            case STATUS_400_SHORT_USER_NAME:
            case STATUS_400_SHORT_PASSWORD:
            case STATUS_401_UNAUTHORIZED:
            case STATUS_404_NOT_FOUND:
                onFailRequest(dataFrame);
                break;

            case STATUS_TIMED_OUT:
            case STATUS_SERVER_NOT_AVAILABLE:
                postRequestResult(dataFrame);
                break;
        }
    }

//    private void onSuccessLogin(RVCloudSyncDataFrame dataFrame) {
//
//        LoginHelper.onLogin(LoginHelper.LoginProvider.USER_NAME,
//                dataFrame.getUserName(),
//                dataFrame.getPassword(),
//                this);
//        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);
//
//        postRequestResult(dataFrame);
//
//    }

    private void onDataSyncSuccess(final RVCloudSyncDataFrame dataFrame) {

        PlaceList.getInstance().refreshByDB();
        PersonList.getInstance().refreshByDB();
        VisitList.getInstance().refreshByDB();

        Log.d(RVDBHelper.TAG, "Place data count after refreshing list: " + PlaceList.getInstance().size());

        LoginHelper.saveLastSyncDate(Calendar.getInstance().getTimeInMillis(), this);

        cloudResultHandler.post(new Runnable() {
            @Override
            public void run() {

                if (placeMarkers == null) {
                    placeMarkers = new PlaceMarkers(mMap);
                } else {
                    placeMarkers.drawAllMarkers();
                }
                postRequestResult(dataFrame);
            }
        });
    }

    private void onFailRequest(RVCloudSyncDataFrame dataFrame) {
        postRequestResult(dataFrame);
    }

    public void postRequestResult(final RVCloudSyncDataFrame dataFrame) {

        cloudResultHandler.post(new Runnable() {
            @Override
            public void run() {

                enableWaitScreen(false);
                fadeProgressFrame(false);
                waitMessageText.setText("");

                refreshWorkButton();
                refreshCalendarButton();

            }
        });
    }
    // DONE: 2017/05/22 SAVEのたびにUIが停止するのはいただけない。

    private TextView searchText;
    private void initSearchText() {
        searchText = (TextView) findViewById(R.id.search_text);
        searchText.setAlpha(0.5f);
        searchText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        searchText.setAlpha(0.3f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        searchText.setAlpha(0.5f);
                        showSearchDialog();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        searchText.setAlpha(0.5f);
                        return true;
                }

                return false;
            }
        });

    }

    private View dummyFocusView;
    private void initDummyFocusView() {
        dummyFocusView = findViewById(R.id.dummy_focus_view);
        dummyFocusView.requestFocus();
    }

    private SoftKeyboard softKeyboard;
    private void initSoftKeyboard() {
        final Handler handler = new Handler();
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        InputMethodManager inputMethodManager =  (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(mainLayout, inputMethodManager);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dummyFocusView.requestFocus();
                        searchText.setAlpha(0.5f);
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchText.setAlpha(1f);
                    }
                });
            }
        });
    }

    private String SEARCH_DIALOG = "search_dialog";
    private void showSearchDialog() {

        final SearchDialog searchDialog = SearchDialog.getInstance(new SearchDialog.SearchDialogListener() {

            @Override
            public void onCloseDialog() {
                if (searchText.getText().length() > 0) {
                    searchText.setText("");
                }
                InputUtil.hideSoftKeyboard(MapActivity.this);
            }

            @Override
            public void onClickShowPersonInMap(final Person person) {

                ArrayList<Place> places = PlaceList.getInstance().getByPerson(person);
                if (places.size() > 0) {
                    Place place = places.get(0);
                    showDialogFitToPlace(place);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                } else {
                    Toast.makeText(MapActivity.this, R.string.place_not_found, Toast.LENGTH_SHORT).show();
                }

                if (searchText.getText().length() > 0) {
                    searchText.setText("");
                }

            }

            @Override
            public void onClickShowPlaceInMap(final Place place) {

                if (searchText.getText().length() > 0) {
                    searchText.setText("");
                }

                showDialogFitToPlace(place);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }

            @Override
            public void onClickEditPerson(Person person) {
                showPersonDialogForEdit(person);
            }
        });
        searchDialog.show(getFragmentManager(), SEARCH_DIALOG);

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getFragmentManager().findFragmentByTag(SEARCH_DIALOG) != null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (searchText.getText().length() > 0) {
                            searchText.setText("");
                        }
                    }
                });
            }
        }).start();
    }

    private void showDialogFitToPlace(Place place) {
        switch (place.getCategory()) {
            case HOUSE:
                showPlaceDialog(place);
                break;
            case HOUSING_COMPLEX:
                showHousingComplexDialog(place);
                break;
            case ROOM:
                Place parent = PlaceList.getInstance().getById(place.getParentId());
                if (parent != null) {
                    showHousingComplexDialog(parent);
                } else {
                    Toast.makeText(this, R.string.place_not_found, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showPersonDialogForEdit(Person person) {
        PersonDialog.getInstance(person, new PersonDialog.PersonDialogListener() {
            @Override
            public void onOkClick(Person person) {
                PersonList.getInstance().setOrAdd(person);
                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(MapActivity.this);
                InputUtil.hideSoftKeyboard(MapActivity.this);
            }

            @Override
            public void onDeleteClick(Person person) {
                PersonList.getInstance().deleteById(person.getId());
                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(MapActivity.this);
                InputUtil.hideSoftKeyboard(MapActivity.this);
            }

            @Override
            public void onCloseDialog() {
                InputUtil.hideSoftKeyboard(MapActivity.this);
            }
        }, true).show(getFragmentManager(), null);
    }

    private void initTermOfUseButton() {
        Button termOfUseButton = (Button) findViewById(R.id.term_of_use_button);
        termOfUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermOfUseDialog.getInstance().show(getFragmentManager(), null);
            }
        });
    }


    public static final String ACCOUNT_TEST_TAG = "AccountTest";
//    private void showAccountDialog() {
//
//        Intent accountIntent = AccountManager.get(this).newChooseAccountIntent(null,
//                null,
//                new String[]{GOOGLE_ACCOUNT_TYPE},
//                false,
//                null, null, null, null);
//        startActivityForResult(accountIntent, CHOOSE_ACCOUNT_REQUEST_CODE);
//    }
//
//    Account mAccount;
//    private void onChooseAccount() {
//
//        AccountManager accountManager = AccountManager.get(this);
//        Bundle options = new Bundle();
//
//        accountManager.getAuthToken(
//                mAccount,
//                "mail",
//                options,
//                this,
//                new OnTokenAcquired(),
//                null
//        );
//
//
//    }
//
//    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
//        @Override
//        public void run(AccountManagerFuture<Bundle> result) {
//            // Get the result of the operation from the AccountManagerFuture.
//
//            try {
//                Bundle bundle = result.getResult();
//                // The token is a named value in the bundle. The name of the value
//                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
//                String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
//                Log.d(ACCOUNT_TEST_TAG, authToken);
//
//
//                RVCloudSync.getInstance().requestDataSyncIfLoggedIn(MapActivity.this, authToken);
//
//
//            } catch (IOException | OperationCanceledException | AuthenticatorException e) {
//                e.printStackTrace();
//            }
//        }
//    }




}
