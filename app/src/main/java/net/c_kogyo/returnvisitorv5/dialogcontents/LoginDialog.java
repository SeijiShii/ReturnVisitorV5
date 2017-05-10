package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

    public LoginDialog(@NonNull Context context, LoginDialogListener listener) {
        super(context);

        mListener = listener;

        initCommon();
    }

    public LoginDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.login_dialog, this);

        mShowPassword = false;

        initUserIdText();
        initShowPasswordSwitch();
        initPasswordTextView();
        initMessageText();
        initProgressBar();
        initLoginButton();
        initCancelButton();
    }

    private EditText userIdTextView;
    private void initUserIdText() {
        userIdTextView = (EditText) view.findViewById(R.id.user_id_text);
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

    private boolean mShowPassword;
    private void initShowPasswordSwitch() {
        RightTextSwitch showPasswordSwitch = (RightTextSwitch) view.findViewById(R.id.show_password_switch);
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
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogInClick();
            }
        });
    }

    private Button cancelButton;
    private void initCancelButton() {
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    private void onLogInClick() {

        InputUtil.hideSoftKeyboard((Activity) getContext());

        String userId = userIdTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        if (validateTexts(userId, password)) {
            try {
                RVCloudSync.getInstance().inquireLogin(userId, password);
                messageTextView.setText(R.string.start_login);
                progressBar.setVisibility(VISIBLE);
                loginButton.setClickable(false);
                loginButton.setAlpha(0.5f);
                view.requestLayout();
            } catch (RVCloudSync.RVCloudSyncException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateTexts(String userId, String password) {

        StringBuilder builder = new StringBuilder();

        if (userId.length() < TEXT_LENGTH ) {
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

        void onCancel();
    }



}
