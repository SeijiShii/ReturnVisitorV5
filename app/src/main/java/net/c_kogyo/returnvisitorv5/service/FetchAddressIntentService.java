package net.c_kogyo.returnvisitorv5.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.c_kogyo.returnvisitorv5.data.Place.LATITUDE;
import static net.c_kogyo.returnvisitorv5.data.Place.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.data.Place.PLACE;

/**
 * Created by SeijiShii on 2017/02/23.
 * isUsingMapLocaleがtrueなら地図上の緯度経度をもとにその場所の言語で住所を表示する。
 * falseならデバイスのロケール（言語）で住所を表示する
 */

public class FetchAddressIntentService extends IntentService {

    public static final String FETCH_ADDRESS_INTENT_SERVICE = "fetch_address_intent_service";
    public static final String IS_USING_MAP_LOCALE = "is_using_map_locale";
    public static final String SEND_FETCED_ADDRESS_ACTION = "send_fetched_address_action";
    public static final String ADDRESS_FETCHED = "address_fetched";

    private static final String TAG = "Reverse Geocoder Tag";

    private boolean mIsUsingMapLocale, doneSecondInquiry;
    private LatLng mLatLng;
    private String mPlaceId;

    public FetchAddressIntentService() {
        super(FETCH_ADDRESS_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        double lat = intent.getDoubleExtra(LATITUDE, 1000);
        double lng = intent.getDoubleExtra(LONGITUDE, 1000);
        mLatLng = new LatLng(lat, lng);

        mPlaceId = intent.getStringExtra(PLACE);

        mIsUsingMapLocale = intent.getBooleanExtra(IS_USING_MAP_LOCALE, false);


        requestAddress(Locale.getDefault());
    }

    // 再帰的に呼ばれるメソッド
    private void requestAddress(Locale locale) {

        Geocoder geocoder = new Geocoder(this, locale);

        String errorMessage = "";

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + mLatLng.latitude +
                    ", Longitude = " + mLatLng.longitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.address_found));

            if (!mIsUsingMapLocale) {

                String addressString = TextUtils.join(" ", addressFragments);
                sendAddress(addressString);

            } else {

                if (!doneSecondInquiry) {
                    String countryCode = address.getCountryCode();
                    String langCode = null;

                    Locale[] locales = Locale.getAvailableLocales();
                    for (Locale localeIn : locales) {
                        if (countryCode.equalsIgnoreCase(localeIn.getCountry())) {
                            langCode = localeIn.getLanguage();
                            break;
                        }
                    }

                    doneSecondInquiry = true;
                    requestAddress(new Locale(langCode, countryCode));

                } else {
                    String addressString = TextUtils.join(" ", addressFragments);
                    sendAddress(addressString);

                }
            }
        }
     }

    private void sendAddress(String address) {

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        Intent sendAddressIntent = new Intent(SEND_FETCED_ADDRESS_ACTION);
        sendAddressIntent.putExtra(ADDRESS_FETCHED, address);
        sendAddressIntent.putExtra(PLACE, mPlaceId);

        manager.sendBroadcast(sendAddressIntent);

    }

    static public void inquireAddress(Place place, Context context) {

        if (!place.needsAddressRequest()) return;

        Intent addressServiceIntent = new Intent(context, FetchAddressIntentService.class);

        addressServiceIntent.putExtra(LATITUDE, place.getLatLng().latitude);
        addressServiceIntent.putExtra(LONGITUDE, place.getLatLng().longitude);
        addressServiceIntent.putExtra(PLACE, place.getId());
        addressServiceIntent.putExtra(FetchAddressIntentService.IS_USING_MAP_LOCALE, true);

        context.startService(addressServiceIntent);
    }

}
