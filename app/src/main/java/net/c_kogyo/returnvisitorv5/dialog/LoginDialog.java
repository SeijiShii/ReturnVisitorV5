package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import net.c_kogyo.returnvisitorv5.activity.MapActivity;
import net.c_kogyo.returnvisitorv5.cloudsync.LoginState;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.util.InputUtil;
import net.c_kogyo.returnvisitorv5.view.RightTextSwitch;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class LoginDialog extends DialogFragment {

    private static LoginDialogListener mListener;
    private static final int TEXT_LENGTH = 8;

    private static LoginDialog instance;
    
    public static LoginDialog getInstance(LoginDialogListener listener) {
        
        mListener = listener;
        
        if (instance == null) {
            instance = new LoginDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        initCommon();
        builder.setView(view);
        
        builder.setTitle(R.string.login_title);
        return builder.create();
    }

    private View view;
    private void initCommon() {
        view = View.inflate(getActivity(), R.layout.login_dialog, null);

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
            userNameTextView.setAlpha(1f);
            userNameTextView.setEnabled(true);
        } else {
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
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginState.getInstance().isLoggedIn()) {
                    onClickLogout();
                } else {
                    onLogInClick();
                }
            }
        });
    }

    private void refreshLoginButton() {
        if (LoginState.getInstance().isLoggedIn()) {
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
        createAccountButton = (Button) view.findViewById(R.id.create_user_button);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCreateAccount();
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
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null) {
                    mListener.onCloseDialog();
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

        setCancelable(false);

        InputUtil.hideSoftKeyboard(getActivity());

        String userName = userNameTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        if (validateTexts(userName, password)) {
            try {
                RVCloudSync.getInstance()
                        .startSendingUserData(userName,
                                                password,
                                                RVCloudSync.RVCloudSyncMethod.LOGIN,
                                                getActivity(),
                                                false);
                messageTextView.setText(R.string.start_login);

                if (mListener != null) {
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

        setCancelable(false);

        InputUtil.hideSoftKeyboard(getActivity());

        String userName = userNameTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        if (validateTexts(userName, password)) {
            try {
                RVCloudSync.getInstance().
                        startSendingUserData(userName,
                                                password,
                                                RVCloudSync.RVCloudSyncMethod.CREATE_USER,
                                                getActivity(),
                                                false);
                messageTextView.setText(R.string.creating_user);

                if (mListener != null) {

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
            builder.append(getActivity().getString(R.string.short_user_name, userName));

            if (password.length() < TEXT_LENGTH) {
                builder.append("\n").append(getActivity().getString(R.string.short_password));
            }
            messageTextView.setText(builder.toString());
            return false;

        } else if (password.length() < TEXT_LENGTH) {
            builder.append(getActivity().getString(R.string.short_password));
            messageTextView.setText(builder.toString());
            return false;
        }
        return true;
    }

    public interface LoginDialogListener {

        void onLogoutClick();

        void onCloseDialog();

    }

    public void onLoginResult(RVCloudSync.RequestResult result){
        // DONE: 2017/05/11  onRequestResult(RVCloudSync.ResultStatus statusCode)

        setCancelable(true);

        progressBar.setVisibility(INVISIBLE);
        enableCloseButton(true);

        String message = "";
        switch (result.statusCode) {
            case STATUS_202_AUTHENTICATED:
                message = getActivity().getString(R.string.login_success, result.userData.userName);
                break;

            case STATUS_401_UNAUTHORIZED:
                message = getActivity().getString(R.string.login_failed) + "\n"
                        + getActivity().getString(R.string.wrong_password, result.userData.userName);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case STATUS_404_NOT_FOUND:
                message = getActivity().getString(R.string.login_failed) + "\n"
                        + getActivity().getString(R.string.user_not_found, result.userData.userName);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case STATUS_201_CREATED:
                message = getActivity().getString(R.string.create_user_success, result.userData.userName);
                break;

            case STATUS_400_DUPLICATE_USER_NAME:
                message = getActivity().getString(R.string.create_user_failed) + "\n"
                        + getActivity().getString(R.string.duplicate_user, result.userData.userName);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case STATUS_400_SHORT_PASSWORD:
                message = getActivity().getString(R.string.create_user_failed) + "\n"
                        + getActivity().getString(R.string.short_password);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case STATUS_400_SHORT_USER_NAME:
                message = getActivity().getString(R.string.create_user_failed) + "\n"
                        + getActivity().getString(R.string.short_user_name, result.userData.userName);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case REQUEST_TIME_OUT:
                message = getActivity().getString(R.string.login_failed) + "\n"
                        + getActivity().getString(R.string.request_time_out);
                enableAccountButton(true);
                enableUserNameText(true);
                enablePasswordText(true);
                break;

            case SERVER_NOT_AVAILABLE:
                message = getActivity().getString(R.string.login_failed) + "\n"
                        + getActivity().getString(R.string.server_not_available);
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

    public void postLogout() {
        enableUserNameText(true);
        enablePasswordText(true);
        enableLoginButton(true);
        refreshLoginButton();
        enableAccountButton(true);
        enableCloseButton(true);

        userNameTextView.setText("");
        passwordTextView.setText("");
        messageTextView.setText("");
    }

    // DONE: 2017/05/11 userName to userName
    // DONE: 2017/05/13 ログインダイアログのUIの動きについてはまだいろいろ考える必要がある。
    // DONE: 2017/05/13  editTextの活性

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
    }

}
