package net.c_kogyo.returnvisitorv5.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.PlaceMarkers;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.PersonCell;
import net.c_kogyo.returnvisitorv5.view.PlaceCell;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static net.c_kogyo.returnvisitorv5.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS;
import static net.c_kogyo.returnvisitorv5.Constants.SharedPrefTags.ZOOM_LEVEL;
import static net.c_kogyo.returnvisitorv5.activity.MapActivity.MY_LOCATION_TAG;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class AddPersonDialog extends DialogFragment
                                implements OnMapReadyCallback,
                                            GoogleMap.OnMarkerClickListener{

    private static AddPersonDialogListener mListener;

    private static AddPersonDialog instance;
    
    public static AddPersonDialog getInstance(AddPersonDialogListener listener) {
        
        mListener = listener;
        
        if (instance == null) {
            instance = new AddPersonDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                
        initCommon(savedInstanceState);
        builder.setView(view);
        
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listFrame.getVisibility() == VISIBLE) {
                    fadeOutListFrame();
                } else {
                    dismiss();
                }                
            }
        });
        
        return builder.create();
    }

    private View view;
    private void initCommon(Bundle savedInstanceState) {
        
        view = View.inflate(getActivity(), R.layout.add_work_dialog, null);

        initDataFrame();
        initSearchText();
        initMapFrame(savedInstanceState);

        initPersonListView();
        initNoItemMessageText();

    }

    private FrameLayout dataFrame;
    private void initDataFrame() {
        dataFrame = (FrameLayout) view.findViewById(R.id.data_frame);
    }

    private EditText searchText;
    private void initSearchText() {
        searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    fadeInListFrameBySearchWord(s.toString());
                } else {
                    fadeOutListFrame();
                }
            }
        });
    }

    private MapView mMapView;
    private void initMapFrame(Bundle savedInstanceState) {

        mMapView = (MapView) view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    private ListView personListView;
    private RelativeLayout listFrame;
    private void initPersonListView() {
        listFrame = (RelativeLayout) view.findViewById(R.id.list_frame);
        personListView = (ListView) view.findViewById(R.id.person_list_view);

        listFrame.setVisibility(INVISIBLE);
        listFrame.setAlpha(0f);

        personListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onSetPerson((Person) personListAdapter.getItem(position));
                }
            }
        });

    }

    private TextView noItemMessageText;
    private void initNoItemMessageText() {
        noItemMessageText = (TextView) view.findViewById(R.id.no_item_message_text);
        noItemMessageText.setVisibility(INVISIBLE);
    }

    private GoogleMap mMap;
    private PlaceMarkers placeMarkers;

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
//        int padding = (int) (getResources().getDisplayMetrics().density * 40);
//        mMap.setPadding(0, padding, 0, padding);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        placeMarkers = new PlaceMarkers(googleMap);

        googleMap.setOnMarkerClickListener(this);

        loadCameraPosition();
    }

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 718;
    private void setMyLocationButton() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(MY_LOCATION_TAG, "Permissions yet given.");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale((Activity)getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                Log.d(MY_LOCATION_TAG, "Should show Explanation.");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity)getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Place place = placeMarkers.getPlace(marker);
        if (place != null) {
            // DONE: 2017/05/25 place action
            fadeInListFrameByPlace(place);
        }
        saveCameraPosition();
        return false;
    }

    private void loadCameraPosition() {
        SharedPreferences prefs = getActivity().getSharedPreferences(RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        float zoomLevel = prefs.getFloat(ZOOM_LEVEL, 0f);
        double lat = Double.valueOf(prefs.getString(LATITUDE, "1000"));
        double lng = Double.valueOf(prefs.getString(LONGITUDE, "1000"));

        if (lat >= 1000 || lng >= 1000) return;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
    }

    private void saveCameraPosition() {

        SharedPreferences prefs = getActivity().getSharedPreferences(RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float zoomLevel = mMap.getCameraPosition().zoom;
        String latSt = String.valueOf(mMap.getCameraPosition().target.latitude);
        String lngSt = String.valueOf(mMap.getCameraPosition().target.longitude);

        editor.putFloat(ZOOM_LEVEL, zoomLevel);
        editor.putString(LATITUDE, latSt);
        editor.putString(LONGITUDE, lngSt);

        editor.apply();
    }

    private PersonListAdapter personListAdapter;
    private void fadeInListFrameByPlace(Place place) {
        ArrayList<Person> persons = RVData.getInstance().personList.getPersonsInPlace(place);
        fadeInPersonList(persons);
        fadeInPlaceCell(place);
    }

    private PlaceCell placeCell;
    private void fadeInPlaceCell(Place place) {
        placeCell = new PlaceCell(getActivity(), place, null, false);
        placeCell.setAlpha(0f);
        dataFrame.addView(placeCell);
        ViewUtil.fadeView(placeCell,
                true,
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                }, null, 500);
    }

    private void fadeInPersonList(ArrayList<Person> persons) {
        personListAdapter = new PersonListAdapter(persons);
        personListView.setAdapter(personListAdapter);

        if (personListAdapter.getCount() <= 0) {
            noItemMessageText.setVisibility(VISIBLE);
            personListView.setVisibility(INVISIBLE);
        } else {
            noItemMessageText.setVisibility(INVISIBLE);
            personListView.setVisibility(VISIBLE);
        }

        listFrame.setVisibility(VISIBLE);
        ViewUtil.fadeView(listFrame,
                true,
                new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        fadeOutListFrame();
                        return true;
                    }
                }, null, 500 );
    }

    private void fadeInListFrameBySearchWord(String searchWord) {
        ArrayList<Person> persons = new ArrayList<>(RVData.getInstance().personList.getSearchedItems(searchWord, getActivity()));
        fadeInPersonList(persons);
    }

    private void fadeOutListFrame() {

        if (listFrame.getAlpha() <= 0)
            return;

        ViewUtil.fadeView(listFrame, false, null,
            new ViewUtil.PostFadeViewListener() {
                @Override
                public void postFade(View view) {
                    listFrame.setVisibility(INVISIBLE);
                    noItemMessageText.setVisibility(INVISIBLE);
                    personListView.setVisibility(INVISIBLE);
                    personListView.setAdapter(null);
                    searchText.setText("");
                }
            }, 500);

        if (placeCell != null) {
            ViewUtil.fadeView(placeCell, false, null,
                new ViewUtil.PostFadeViewListener() {
                    @Override
                    public void postFade(View view) {
                        placeCell.setVisibility(INVISIBLE);
                        placeCell = null;
                    }
                }, 500);
        }
    }

    public interface AddPersonDialogListener {

        void onSetPerson(Person person);

        void onCloseDialog();
    }

    class PersonListAdapter extends BaseAdapter{

        private ArrayList<Person> mPersons;

        public PersonListAdapter(ArrayList<Person> persons) {
            this.mPersons = new ArrayList<>(persons);
        }

        @Override
        public int getCount() {
            return mPersons.size();
        }

        @Override
        public Object getItem(int position) {
            return mPersons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new PersonCell(getActivity(), (Person) getItem(position), false, null);
            } else {
                ((PersonCell) convertView).refreshData((Person) getItem(position));
            }
            return convertView;
        }
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

    @Override
    public void onResume() {
        super.onResume();
        isShowing.set(false);
        mMapView.onResume();
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
        mMapView.onDestroy();
    }
}
