package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.facebook.AccessToken;

import net.c_kogyo.returnvisitorv5.Constants;

/**
 * Created by SeijiShii on 2017/06/02.
 */

public class LoginHelper {

    private static final String ACCOUNT_NAME = "account_name";
    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String LAST_SYNC_TIME = "_last_sync_time";
    private static final String AUTH_TOKEN = "auth_token";
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";


    public static final String GOOGLE_ACCOUNT_NAME = "Google";


    public static void onLogin(Context context, String accountName, String authToken) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(ACCOUNT_NAME, accountName);
        editor.putString(AUTH_TOKEN, authToken);

        editor.apply();
    }

    public static void onLogOut(Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(IS_LOGGED_IN, false);
        editor.putString(ACCOUNT_NAME, null);
        editor.putString(AUTH_TOKEN, null);

        editor.apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getBoolean(IS_LOGGED_IN, false);
    }

    public static String getAccountName(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(ACCOUNT_NAME, null);
    }

    public static void saveLastSyncDate(long lastSyncTime, Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(LAST_SYNC_TIME, lastSyncTime);

        editor.apply();
    }

    static long loadLastSyncDate(Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getLong(LAST_SYNC_TIME, 0);
    }

    @Nullable
    public static String getAuthToken(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(AUTH_TOKEN, null);
    }


}
