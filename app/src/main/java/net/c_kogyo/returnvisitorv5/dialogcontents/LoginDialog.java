package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.util.InputUtil;
import net.c_kogyo.returnvisitorv5.view.RightTextSwitch;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class LoginDialog extends FrameLayout {

    private LoginDialogListener mListener;
    private static final int TEXT_LENGTH = 8;
    private boolean mIsLoggedIn;
    private Handler handler;

    public LoginDialog(@NonNull Context context,
                       boolean isLoggedIn,
                       LoginDialogListener listener) {
        super(context);

        mIsLoggedIn = isLoggedIn;
        mListener = listener;
        handler = new Handler();

        initCommon();
    }

    public LoginDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.login_dialog, this);

        mShowPassword = false;

        initUserNameText();
        initShowPasswordSwitch();
        initPasswordTextView();
        initMessageText();
        initProgressBar();
        initLoginButton();
        initCreateAccountButton();
        initCloseButton();
    }

    private EditText userNameTextView;
    private void initUserNameText() {
        userNameTextView = (EditText) view.findViewById(R.id.user_name_text);
    }

    private void enableUserNameText(boolean enabled) {
        if (enabled) {
//            userNameTextView.setFocusable(true);
            userNameTextView.setAlpha(1f);
            userNameTextView.setEnabled(true);
        } else {
//            userNameTextView.setFocusable(false);
            userNameTextView.setAlpha(0.5f);
            userNameTextView.setEnabled(false);
        }
    }

    private EditText passwordTextView;
    private void initPasswordTextView() {
        passwordTextView = (EditText) view.findViewById(R.id.password_text);
        if (mShowPassword) {
            passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    private void enablePasswordText(boolean enabled) {
        if (enabled) {
//            passwordTextView.setFocusable(true);
            passwordTextView.setAlpha(1f);
            passwordTextView.setEnabled(true);

            showPasswordSwitch.setClickable(true);
            showPasswordSwitch.setAlpha(1f);

            if (mShowPassword) {
                passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

        } else {
//            passwordTextView.setFocusable(false);
            passwordTextView.setAlpha(0.5f);
            passwordTextView.setEnabled(false);

            showPasswordSwitch.setClickable(false);
            showPasswordSwitch.setAlpha(0.5f);

            passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    private boolean mShowPassword;
    private RightTextSwitch showPasswordSwitch;
    private void initShowPasswordSwitch() {
        showPasswordSwitch = (RightTextSwitch) view.findViewById(R.id.show_password_switch);
        showPasswordSwitch.setOnCheckChangeListener(new RightTextSwitch.RightTextSwitchOnCheckChangeListener() {
            @Override
            public void onCheckChange(boolean checked) {
                mShowPassword = checked;
                if (mShowPassword) {
                    passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    passwordTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    private TextView messageTextView;
    private void initMessageText() {
        messageTextView = (TextView) view.findViewById(R.id.message_text);
    }

    private ProgressBar progressBar;
    private void initProgressBar() {
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        progressBar.setAlpha(0.7f);
        progressBar.setVisibility(INVISIBLE);
    }

    private Button loginButton;
    private void initLoginButton() {
        loginButton = (Button) view.findViewById(R.id.login_button);
        refreshLoginButton();
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsLoggedIn) {
                    onClickLogout();
                } else {
                    onLogInClick();
                }
            }
        });
    }

    private void refreshLoginButton() {
        if (mIsLoggedIn) {
            loginButton.setText(R.string.logout_button_small);
        } else {
            loginButton.setText(R.string.login_button);
        }
    }

    private void enableLoginButton(boolean enabled) {
        if (enabled) {
            loginButton.setClickable(true);
            loginButton.setAlpha(1f);
        } else {
            loginButton.setClickable(false);
            loginButton.setAlpha(0.5f);
        }
    }

    private Button createAccountButton;
    private void initCreateAccountButton() {
        createAccountButton = (Button) view.findViewById(R.id.create_account_button);

        createAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                onClickCreateAccount();
            }
        });
    }

    private void enableAccountButton(boolean enabled) {
        if (enabled) {
            createAccountButton.setClickable(true);
            createAccountButton.setAlpha(1f);
        } else {
            createAccountButton.setClickable(false);
            createAccountButton.setAlpha(0.5f);
        }
    }

    private Button closeButton;
    private void initCloseButton() {
        closeButton = (Button) view.findViewById(R.id.close_button);
        // DONE: 2017/05/11 change layout to CLOSE
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickClose();
                }
            }
        });
    }

    private void enableCloseButton(boolean enabled) {
        if (enabled) {
            closeButton.setClickable(true);
            closeButton.setAlpha(1f);
        } else {
            closeButton.setClickable(false);
            closeButton.setAlpha(0.5f);
        }
    }

    private void onLogInClick() {

        InputUtil.hideSoftKeyboard((Activity) getContext());

        String userName = userNameTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        // test
//        try {
//            RVCloudSync.getInstance().startLogin("seijishii", "fugafuga");
//        } catch (RVCloudSync.RVCloudSyncException e) {
//            e.printStackTrace();
//        }

        if (validateTexts(userName, password)) {
            try {
                RVCloudSync.getInstance().startLogin(userName, password);
                messageTextView.setText(R.string.start_login);

                if (mListener != null) {
                    mListener.onStartLogin();

                    progressBar.setVisibility(VISIBLE);

                    enableLoginButton(false);
                    enableAccountButton(false);
                    enableCloseButton(false);

                    enableUserNameText(false);
                    enablePasswordText(false);

                }
            } catch (RVCloudSync.RVCloudSyncException e) {
                e.printStackTrace();
            }
        }
    }

    private void onClickCreateAccount() {
        InputUtil.hideSoftKeyboard((Activity) getContext());

        String userName = userNameTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        // test
//        try {
//            RVCloudSync.getInstance().startLogin("seijishii", "fugafuga");
//        } catch (RVCloudSync.RVCloudSyncException e) {
//            e.printStackTrace();
//        }

        if (validateTexts(userName, password)) {
            try {
                RVCloudSync.getInstance().startLogin(userName, password);
                messageTextView.setText(R.string.start_create_account);

                if (mListener != null) {
                    mListener.onStartCreateAccount();

                    progressBar.setVisibility(VISIBLE);

                    enableLoginButton(false);
                    enableAccountButton(false);
                    enableCloseButton(false);
                }
            } catch (RVCloudSync.RVCloudSyncException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateTexts(String userName, String password) {

        StringBuilder builder = new StringBuilder();

        if (userName.length() < TEXT_LENGTH ) {
            builder.append(getContext().getString(R.string.short_user_id_message));

            if (password.length() < TEXT_LENGTH) {
                builder.append("\n").append(getContext().getString(R.string.short_password_message));
            }
            messageTextView.setText(builder.toString());
            return false;

        } else if (password.length() < TEXT_LENGTH) {
            builder.append(getContext().getString(R.string.short_password_message));
            messageTextView.setText(builder.toString());
            return false;
        }
        return true;
    }

    public interface LoginDialogListener {

        void onStartLogin();

        void onStartCreateAccount();

        void onLogoutClick();

        void onClickClose();

    }

    public void onLoginResult(String userName, RVCloudSync.LoginStatusCode statusCode){
        // DONE: 2017/05/11  onLoginResult(RVCloudSync.LoginStatusCode statusCode)

        progressBar.setVisibility(INVISIBLE);
        enableCloseButton(true);

        String message = "";
        switch (statusCode) {
            case AUTHENTICATED_202:
                mIsLoggedIn = true;
                message = getContext().getString(R.string.login_success, userName);
                break;

            case UNAUTHORIZED_401:
                mIsLoggedIn = false;
                message = getContext().getString(R.string.login_failed) + "\n"
                        + getContext().getString(R.string.wrong_password, userName);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case NOT_FOUND_404:
                mIsLoggedIn = false;
                message = getContext().getString(R.string.login_failed) + "\n"
                        + getContext().getString(R.string.account_not_found, userName);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case REQUEST_TIME_OUT:
                mIsLoggedIn = false;
                message = getContext().getString(R.string.login_failed) + "\n"
                        + getContext().getString(R.string.request_time_out);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            default:
        }

        messageTextView.setText(message);
        refreshLoginButton();
        enableLoginButton(true);
    }

    private void onClickLogout() {
        if (mListener != null) {
            mListener.onLogoutClick();
        }
    }

    // DONE: 2017/05/11 userName to userName
    // TODO: 2017/05/13 ログインダイアログのUIの動きについてはまだいろいろ考える必要がある。
    // TODO: 2017/05/13  editTextの活性


}
