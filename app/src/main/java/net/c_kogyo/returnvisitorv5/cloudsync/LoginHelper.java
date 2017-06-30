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

    private static final String LOGIN_PROVIDER = "login_provider";
    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    public static final String LAST_SYNC_TIME = "_last_sync_time";

    public static final String FB_TAG = "facebook_login";

    public enum LoginProvider {
        USER_NAME,
        FACEBOOK
    }

    private static LoginProvider mLoginProvider;
    private static String mUserName, mPassword;
    private static boolean mIsLoggedIn;

    public static void onLogin(LoginProvider loginProvider,
                               @Nullable String userName,
                               @Nullable String password,
                               Context context) {
        mLoginProvider = loginProvider;
        mIsLoggedIn = true;
        mUserName = userName;
        mPassword = password;
        onUpdate(context);
    }

    public static void onLogin(LoginProvider loginProvider,
                               Context context) {

        onLogin(loginProvider, null, null, context);
    }

    private static void onUpdate(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (mLoginProvider != null) {
            editor.putString(LOGIN_PROVIDER, mLoginProvider.toString());
        } else {
            editor.putString(LOGIN_PROVIDER, null);
        }
        editor.putBoolean(IS_LOGGED_IN, mIsLoggedIn);
        editor.putString(USER_NAME, mUserName);
        editor.putString(PASSWORD, mPassword);

        editor.apply();
    }

    private static void loadState(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        mLoginProvider = LoginProvider.valueOf(preferences.getString(LOGIN_PROVIDER, LoginProvider.USER_NAME.toString()));
        mUserName = preferences.getString(USER_NAME, null);
        mPassword = preferences.getString(PASSWORD, null);
        mIsLoggedIn = preferences.getBoolean(IS_LOGGED_IN, false);
    }

    public static void onLoggedOut(Context context) {

        mLoginProvider = null;
        mUserName = null;
        mPassword = null;
        mIsLoggedIn = false;

        onUpdate(context);
    }

    public static boolean isLoggedIn(Context context) {
        loadState(context);
        return mIsLoggedIn;
    }

    public static String getUserName(Context context) {
        loadState(context);
        return mUserName;
    }

    public static String getPassword(Context context) {
        loadState(context);
        return mPassword;
    }

    public static LoginProvider getLoginProvider(Context context) {
        loadState(context);
        return mLoginProvider;
    }

    public static void setUserName(String userName, Context context) {
        mUserName = userName;
        onUpdate(context);
    }

    public static void saveLastSyncDate(long lastSyncTime, Context context) {

        loadState(context);

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(mLoginProvider.toString() + LAST_SYNC_TIME, lastSyncTime);
        editor.apply();
    }

    static long loadLastSyncDate(Context context) {

        loadState(context);

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getLong(mLoginProvider.toString() + LAST_SYNC_TIME, 0);
    }


}
