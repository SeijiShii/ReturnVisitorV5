package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.view.RightTextSwitch;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class LoginDialog extends FrameLayout {

    private LoginDialogListener mListener;

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

        initUserIdText();
        initPasswordTextView();
        initShowPasswordSwitch();
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
    }

    private void initShowPasswordSwitch() {
        RightTextSwitch showPasswordSwitch = (RightTextSwitch) view.findViewById(R.id.show_password_switch);
        showPasswordSwitch.setOnCheckChangeListener(new RightTextSwitch.RightTextSwitchOnCheckChangeListener() {
            @Override
            public void onCheckChange(boolean checked) {

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
    }

    private Button loginButton;
    private void initLoginButton() {
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

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

    public interface LoginDialogListener {

        void onCancel();

    }
}
