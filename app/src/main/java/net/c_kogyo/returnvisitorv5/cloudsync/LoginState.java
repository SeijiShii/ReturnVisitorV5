package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.content.SharedPreferences;

import net.c_kogyo.returnvisitorv5.Constants;

/**
 * Created by SeijiShii on 2017/06/02.
 */

public class LoginState {

    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";

    private String userName, password;
    private boolean isLoggedIn;
    private static LoginState instance = new LoginState();

    private LoginState(){}

    public static LoginState getInstance() {
        return instance;
    }

    public static LoginState loadLoginState(Context context) {

        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        instance.isLoggedIn = preferences.getBoolean(IS_LOGGED_IN, false);
        if (instance.isLoggedIn) {
            instance.userName = preferences.getString(USER_NAME, null);
            instance.password = preferences.getString(PASSWORD, null);
        } else {
            instance.userName = null;
            instance.password = null;
        }
        return instance;
    }

    private static void saveLoginState(Context context) {
        SharedPreferences preferences
                = context.getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(IS_LOGGED_IN, instance.isLoggedIn);
        if (instance.isLoggedIn) {
            editor.putString(USER_NAME, instance.userName);
            editor.putString(PASSWORD, instance.password);
        } else {
            editor.putString(USER_NAME, null);
            editor.putString(PASSWORD, null);
        }
        editor.apply();
    }

    public static void onSuccessLogin(String userName, String password, Context context) {

        instance.isLoggedIn = true;
        instance.userName = userName;
        instance.password = password;

        saveLoginState(context);
    }

    public static void onLoggedOut(Context context) {

        instance.isLoggedIn = false;
        instance.userName = null;
        instance.password = null;
        saveLoginState(context);
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
