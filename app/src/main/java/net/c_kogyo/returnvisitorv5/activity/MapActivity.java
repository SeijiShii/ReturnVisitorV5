package net.c_kogyo.returnvisitorv5.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.PlaceMarkers;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.dialogcontents.AddWorkDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.HousingComplexDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.LoginDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.MapLongClickDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.PlaceDialog;
import net.c_kogyo.returnvisitorv5.service.TimeCountService;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.CountTimeFrame;

import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.EDIT_VISIT_ACTION;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.EDIT_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_HOUSE_ACTION;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_VISIT_ACTION_WITH_PLACE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.IS_LOGGED_IN;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.PASSWORD;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.USER_NAME;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.ZOOM_LEVEL;
import static net.c_kogyo.returnvisitorv5.data.Place.PLACE;
import static net.c_kogyo.returnvisitorv5.data.Visit.VISIT;

public class MapActivity extends AppCompatActivity
                            implements OnMapReadyCallback,
                                        GoogleMap.OnMapLongClickListener,
                                        GoogleMap.OnMarkerClickListener,
                                        GoogleMap.OnMarkerDragListener,
                                        RVData.RVDataStoreCallback{

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private static boolean isForeground;
    private static boolean mIsLoggedIn;
    private String userName;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        loginDialogHandler = new Handler();
        RVCloudSync.getInstance().setCallback(new RVCloudSync.RVCloudSyncCallback() {
            @Override
            public void onLoginResult(RVCloudSync.LoginResult result) {

                switch (result.statusCode) {
                    case STATUS_202_AUTHENTICATED:
                    case STATUS_201_CREATED:
                        mIsLoggedIn = true;
                        MapActivity.this.userName = result.userData.userName;
                        MapActivity.this.password = result.userData.password;
                        break;

                    case STATUS_401_UNAUTHORIZED:
                    case STATUS_404_NOT_FOUND:
                    case STATUS_400_DUPLICATE_USER_NAME:
                    case STATUS_400_SHORT_PASSWORD:
                    case STATUS_400_SHORT_USER_NAME:
                        mIsLoggedIn = false;
                        break;

                    case REQUEST_TIME_OUT:
                        mIsLoggedIn = false;
                        break;
                }

                if (loginDialog != null) {
                    loginDialog.onLoginResult(result);
                    dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            fadeOutDialogOverlay(normalFadeOutListener);
                            return true;
                        }
                    });
                }

                refreshLoginButton();


            }
        }, new Handler());

        // 初期化のために一回ゲットする
        RVData.getInstance().loadData(this, this);

        initLocalBroadcast();

        setContentView(R.layout.map_activity);

        AdMobHelper.setAdView(this);

        initWaitScreen();
        initLogoButton();
        initMapView(savedInstanceState);
        initDialogOverlay();
        initDrawerOverlay();

        loadLoginState();

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

    public static final String MY_LOCATION_TAG = "my_location";
    private GoogleMap mMap;
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
        int topPadding = (int) (getResources().getDisplayMetrics().density * 50);
        mMap.setPadding(0, topPadding, 0, 0);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        loadCameraPosition();

        waitForDataLoadedAndRefreshUI();
    }

    private PlaceMarkers placeMarkers;
    private void waitForDataLoadedAndRefreshUI() {
        
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
                        placeMarkers = new PlaceMarkers(mMap);

                        refreshLogoButton();
                        refreshWorkButton();
                        refreshCalendarButton();
                        fadeoutWaitScreen();
                    }
                });
                
            }
        }).start();
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideSoftKeyboard();
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

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        saveCameraPosition();
        saveLoginState();
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

        final Place tmpPlace = new Place(latLng, Place.Category.HOUSE);
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
                // DONE: 2017/03/17 record complex action
                placeMarkers.removeByPlace(tmpPlace);
                // 同じダイアログを使用するのでアニメータリスナエンド後に表示メソッドを起動する
                fadeOutDialogOverlay(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dialogFrame.removeAllViews();
                        showHousingComplexDialog(null, tmpPlace.getLatLng());
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
                // DONE: 2017/03/17 record not home action
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
        fadeInDialogOverlay(mapLongClickDialog);

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

        Place place = placeMarkers.getPlace(marker);
        if (place == null) return false;

        if (place.getCategory() == Place.Category.HOUSE) {
            showPlaceDialog(place);
        } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
            showHousingComplexDialog(place, null);
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

        RVData.getInstance().saveData(this, null);

    }

    private RelativeLayout dialogOverlay;
    private void initDialogOverlay() {
        dialogOverlay = (RelativeLayout) findViewById(R.id.dialog_overlay);
        initDialogFrame();
    }

    private void fadeInDialogOverlay(View dialogView) {

        if (dialogOverlay.getVisibility() == View.VISIBLE) return;

        dialogFrame.addView(dialogView);

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
                        Visit visit = RVData.getInstance().visitList.getById(visitId);
                        if (visit != null) {
                            String placeId = visit.getPlaceId();
                            Place place = RVData.getInstance().placeList.getById(placeId);
                            if (place != null) {

                                if (place.getCategory() == Place.Category.HOUSE) {
                                    placeMarkers.addMarker(place);
                                } else if (place.getCategory() == Place.Category.ROOM) {
                                    String parentId = place.getParentId();
                                    Place parent = RVData.getInstance().placeList.getById(parentId);
                                    placeMarkers.refreshMarker(parent);
                                }
                            }
                        }
                    }
                }
                break;
            case EDIT_VISIT_REQUEST_CODE:
                if (resultCode == VISIT_EDITED_RESULT_CODE) {
                    String visitId = data.getStringExtra(VISIT);
                    if (visitId != null) {
                        Visit visit = RVData.getInstance().visitList.getById(visitId);
                        if (visit != null) {
                            String placeId = visit.getPlaceId();
                            Place place = RVData.getInstance().placeList.getById(placeId);
                            if (place != null) {
                                placeMarkers.refreshMarker(place);
                            }
                        }
                    }
                }
        }
    }

    private ImageView logoButton;
    private void initLogoButton() {
        logoButton = (ImageView) findViewById(R.id.logo_button);
        refreshLogoButton();
    }

    // DONE: 2017/05/06 データ読み込み前は半透明に
    private void refreshLogoButton() {

        float originAlpha, targetAlpha;

        if (!isDataLoaded) {
            originAlpha = 1f;
            targetAlpha = 0.5f;
            ViewUtil.setOnClickListener(logoButton, null);
        } else {
            originAlpha = 0.5f;
            targetAlpha = 1f;
            ViewUtil.setOnClickListener(logoButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick() {
                    openCloseDrawer();
                }
            });
        }
        ValueAnimator animator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                logoButton.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(300);
        animator.start();
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
                                placeMarkers.removeByPlace(place);
                                RVData.getInstance().placeList.deleteById(place.getId());
                                RVData.getInstance().saveData(MapActivity.this, null);
                            }

                            @Override
                            public void onEditVisitClick(Visit visit) {
                                fadeOutDialogOverlay(normalFadeOutListener);
                                startRecordVisitActivityToEditVisit(visit);
                            }
                        });
        fadeInDialogOverlay(placeDialog);
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
        recordVisitIntent.setAction(NEW_HOUSE_ACTION);
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

    private void showHousingComplexDialog(@Nullable Place housingComplex, @Nullable LatLng latLng) {

        Place mComplex;

        if (housingComplex == null) {
            mComplex =  new Place(latLng, Place.Category.HOUSING_COMPLEX);
        } else {
            mComplex = housingComplex;
        }

        HousingComplexDialog housingComplexDialog
                = new HousingComplexDialog(this,
                        mComplex,
                        new HousingComplexDialog.HousingComplexDialogListener() {
                            @Override
                            public void onClickAddRoomButton(Place newRoom) {
                                fadeOutDialogOverlay(normalFadeOutListener);
                                startRecordVisitActivityForNewVisit(newRoom);
                            }

                            @Override
                            public void onClickRoomCell(final Place room) {
                                fadeOutDialogOverlay(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        dialogOverlay.setVisibility(View.INVISIBLE);
                                        dialogFrame.removeAllViews();
                                        showPlaceDialog(room);
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
                            public void onClickOkButton(Place housingComplex) {
                                placeMarkers.refreshMarker(housingComplex);
                                fadeOutDialogOverlay(normalFadeOutListener);
                            }

                            @Override
                            public void onClickCancelButton() {
                                fadeOutDialogOverlay(normalFadeOutListener);
                            }

                            @Override
                            public void onDeleteHousingComplex(Place housingComplex) {
                                fadeOutDialogOverlay(normalFadeOutListener);
                                placeMarkers.removeByPlace(housingComplex);
                                RVData.getInstance().placeList.deleteById(housingComplex.getId());
                                RVData.getInstance().saveData(MapActivity.this, null);
                            }
                        }, true, true);
        fadeInDialogOverlay(housingComplexDialog);
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
            loginDialog = null;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


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
        RVData.getInstance().placeList.setOrAdd(place);
        RVData.getInstance().visitList.setOrAdd(visit);
        RVData.getInstance().saveData(this, null);

        placeMarkers.addMarker(place);
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

        initLoginButton();
        initCountTimeFrame();
        initWorkButton();
        initCalendarButton();
        initAddWorkButton();
        initAddVisitButton();
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

    private CountTimeFrame countTimeFrame;
    private void initCountTimeFrame() {
        countTimeFrame = (CountTimeFrame) findViewById(R.id.count_time_frame);
        countTimeFrame.setExtracted(TimeCountService.isTimeCounting());
        countTimeFrame.setListener(new CountTimeFrame.CountTimeFrameListener() {
            @Override
            public void onClickStartButton() {
                Intent startTimeCountIntent = new Intent(getApplicationContext(), TimeCountService.class);
                startTimeCountIntent.setAction(TimeCountService.START_COUNTING_ACTION_TO_SERVICE);
                startService(startTimeCountIntent);
            }

            @Override
            public void onClickStopButton() {
                TimeCountService.stopTimeCount();
            }

            @Override
            public void onChangeStart(Calendar calendar) {
                Intent changeStartIntent = new Intent(TimeCountService.CHANGE_START_ACTION_TO_SERVICE);
                changeStartIntent.putExtra(TimeCountService.START_TIME, calendar.getTimeInMillis());
                LocalBroadcastManager.getInstance(MapActivity.this).sendBroadcast(changeStartIntent);
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           if (intent.getAction().equals(TimeCountService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY)) {
               countTimeFrame.updateUI(false, 0, null, null);

               SharedPreferences preferences = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
               SharedPreferences.Editor editor = preferences.edit();
               editor.putBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, false);
               editor.apply();

           } else if (intent.getAction().equals(TimeCountService.TIME_COUNTING_ACTION_TO_ACTIVITY)) {

               long startTime = intent.getLongExtra(TimeCountService.START_TIME, 0);
               Calendar start = Calendar.getInstance();
               start.setTimeInMillis(startTime);
               String startText = getString(R.string.start_time_text, DateTimeText.getTimeText(start, false));

               long duration = intent.getLongExtra(TimeCountService.DURATION, 0);
               String durationText = getString(R.string.duration_string, DateTimeText.getDurationString(duration, true));

               countTimeFrame.updateUI(true, startTime, startText, durationText);

               String countingWorkId = intent.getStringExtra(TimeCountService.COUNTING_WORK_ID);
               if (countingWorkId != null) {
                   SharedPreferences preferences = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
                   SharedPreferences.Editor editor = preferences.edit();
                   editor.putBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, true);
                   editor.putString(Constants.SharedPrefTags.COUNTING_WORK_ID, countingWorkId);
                   editor.apply();
               }
           }
        }
    };

    private void initLocalBroadcast() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        manager.registerReceiver(receiver, new IntentFilter(TimeCountService.TIME_COUNTING_ACTION_TO_ACTIVITY));
        manager.registerReceiver(receiver, new IntentFilter(TimeCountService.STOP_TIME_COUNT_ACTION_TO_ACTIVITY));
    }

    private void restartTimeCountIfNeeded() {
        SharedPreferences preferences
                = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        boolean isCounting = preferences.getBoolean(Constants.SharedPrefTags.IS_COUNTING_TIME, false);
        if (isCounting) {
            String workId = preferences.getString(Constants.SharedPrefTags.COUNTING_WORK_ID, null);

            if (workId == null) return;

            Intent restartCountIntent = new Intent(this, TimeCountService.class);
            restartCountIntent.setAction(TimeCountService.RESTART_COUNTING_ACTION_TO_SERVICE);
            restartCountIntent.putExtra(TimeCountService.COUNTING_WORK_ID, workId);
            startService(restartCountIntent);
        }

    }

    private Button workButton;
    private void initWorkButton() {
        workButton = (Button) findViewById(R.id.work_button);
    }

    private void refreshWorkButton() {
        if (RVData.getInstance().hasWorkOrVisit()) {
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

        if (RVData.getInstance().hasWorkOrVisit()) {
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
        AddWorkDialog addWorkDialog
                = new AddWorkDialog(this,
                new AddWorkDialog.AddWorkDialogListener() {
                    @Override
                    public void onOkClick(Work work) {
                        startWorkPagerActivityWithNewWork(work);
                        fadeOutDialogOverlay(normalFadeOutListener);
                    }

                    @Override
                    public void onCancelClick() {
                        fadeOutDialogOverlay(normalFadeOutListener);
                    }
                },
                true,
                Calendar.getInstance());
        fadeInDialogOverlay(addWorkDialog);
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fadeOutDialogOverlay(normalFadeOutListener);
                return true;
            }
        });
    }

    private void startWorkPagerActivityWithNewWork(Work work) {

        RVData.getInstance().workList.setOrAdd(work);
        RVData.getInstance().saveData(this, null);

        Intent withNewWorkIntent = new Intent(this, WorkPagerActivity.class);
        withNewWorkIntent.setAction(Constants.WorkPagerActivityActions.START_WITH_NEW_WORK);
        withNewWorkIntent.putExtra(Work.WORK, work.getId());
        startActivity(withNewWorkIntent);
    }

    // DONE: 2017/04/01 集合住宅のマークがでかすぎる

    // DONE: 2017/05/05 データ読み込みまでボタンを押せなくする
    // DONE: 2017/05/08 データ読み込みwait画面
    private RelativeLayout waitScreen;
    private void initWaitScreen() {
        waitScreen = (RelativeLayout) findViewById(R.id.wait_screen);
        waitScreen.setVisibility(View.VISIBLE);
        waitScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void fadeoutWaitScreen() {

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waitScreen.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                waitScreen.setVisibility(View.INVISIBLE);
                waitScreen.setOnTouchListener(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(500);
        animator.start();
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

    private Button loginButton;
    private void initLoginButton() {
        loginButton = (Button) findViewById(R.id.login_button);

        refreshLoginButton();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLoggedIn) {
                    confirmLogout();
                } else {
                    openCloseDrawer();
                    onLoginClicked();
                }
            }
        });
    }

    private void refreshLoginButton() {
        if (mIsLoggedIn) {
            String s = getString(R.string.logout_button, userName);
            loginButton.setText(s);
        } else {
            loginButton.setText(R.string.login_button);
        }
    }

    private void onLoginClicked() {
        showLoginDialog();
    }

    private LoginDialog loginDialog;
    private void showLoginDialog() {
        loginDialog = new LoginDialog(this,
                new LoginDialog.LoginDialogListener() {

            @Override
            public void onStartLogin() {
                // DONE: 2017/05/11  onStartLogin()
                // DONE: 2017/05/13 ログイン通信中はダイアログを消せないようにする
                dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }

            @Override
            public void onStartCreateAccount() {

            }

            @Override
            public void onClickClose() {
                fadeOutDialogOverlay(normalFadeOutListener);
            }

            @Override
            public void onLogoutClick() {
                // DONE: 2017/05/13 onLogoutClick
                confirmLogout();
            }
        });
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fadeOutDialogOverlay(normalFadeOutListener);
                return true;
            }
        });
        fadeInDialogOverlay(loginDialog);
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.logout_button_small, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void logout() {
        mIsLoggedIn = false;
        userName = null;
        password = null;
        refreshLoginButton();
        if (loginDialog != null) {
            loginDialog.postLogout();
        }
    }

    public static boolean isLoggedIn() {
        return mIsLoggedIn;
    }

    public static void setIsLoggedIn(boolean login) {
        mIsLoggedIn = login;
    }

    // TODO: 2017/05/05 データがないときにWORKやカレンダーに遷移しないようにする(実装済み、要検証)
    // TODO: 2017/05/06 AdView to Real
    // DONE: 2017/05/08 開始時間に秒はいらない
    // DONE: 2017/05/08 ログイン画面
    // TODO: 2017/05/08 データ同期まわり

    // TODO: 2017/05/16 save login state

    private void saveLoginState() {

        SharedPreferences prefs
                = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IS_LOGGED_IN, mIsLoggedIn);
        editor.putString(USER_NAME, userName);
        editor.putString(PASSWORD, password);

        editor.apply();
    }

    private void loadLoginState() {

        SharedPreferences prefs
                = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        mIsLoggedIn = prefs.getBoolean(IS_LOGGED_IN, false);
        userName = prefs.getString(USER_NAME, null);
        password = prefs.getString(PASSWORD, null);

        if (mIsLoggedIn){
            try {
                RVCloudSync.getInstance().startSendingUserData(userName, password, RVCloudSync.RVCloudSyncMethod.LOGIN);
            } catch (RVCloudSync.RVCloudSyncException e){
                Log.e(RVCloudSync.TAG, e.getMessage());
            }
        }

    }



}
