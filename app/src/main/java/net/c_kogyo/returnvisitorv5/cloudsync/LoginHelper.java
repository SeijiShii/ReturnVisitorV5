package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import net.c_kogyo.returnvisitorv5.Constants;

import static net.c_kogyo.returnvisitorv5.cloudsync.LoginHelper.LoginProvider.FACEBOOK;

/**
 * Created by SeijiShii on 2017/06/02.
 */

public class LoginHelper {

    private static final String LOGIN_PROVIDER = "login_provider";
    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";

    public static final String FB_TAG = "facebook_login";

    public enum LoginProvider {
        USER_NAME,
        FACEBOOK
    }

//    private static LoginProvider loginProvider;

    public static void onSuccessLogin(LoginProvider loginProvider,
                                      @Nullable String userName,
                                      @Nullable String password,
                                      Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(LOGIN_PROVIDER, loginProvider.toString());
        editor.putString(USER_NAME, userName);

        if (loginProvider == LoginProvider.USER_NAME) {
            editor.putBoolean(IS_LOGGED_IN, true);
            editor.putString(PASSWORD, password);
        }
        editor.apply();

    }

    public static void onLoggedOut(Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(LOGIN_PROVIDER, null);
        editor.putString(USER_NAME, null);
        editor.putBoolean(IS_LOGGED_IN, false);
        editor.putString(PASSWORD, null);

        editor.apply();

    }

    public static String getUserName(Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(USER_NAME, "");


    }

    @Nullable
    static String getPassword(Context context) {

        switch (getLoginProvider(context)) {
            case USER_NAME:
                SharedPreferences preferences
                        = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
                return preferences.getString(PASSWORD, "");
            case FACEBOOK:
                return null;
        }
        return null;
    }

    public static boolean isLoggedIn(Context context) {

        switch (getLoginProvider(context)) {
            case USER_NAME:
                SharedPreferences preferences
                        = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
                return preferences.getBoolean(IS_LOGGED_IN, false);
            case FACEBOOK:
                return AccessToken.getCurrentAccessToken() != null;
        }
        return false;
    }

    static LoginProvider getLoginProvider(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        return LoginProvider.valueOf(preferences.getString(LOGIN_PROVIDER, LoginProvider.USER_NAME.toString()));
    }
}
