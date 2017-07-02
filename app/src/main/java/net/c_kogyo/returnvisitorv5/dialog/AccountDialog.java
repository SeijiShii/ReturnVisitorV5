package net.c_kogyo.returnvisitorv5.dialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.util.ArrayList;
import java.util.Arrays;

import static net.c_kogyo.returnvisitorv5.Constants.AccountType.FACEBOOK_ACCOUNT_NAME;
import static net.c_kogyo.returnvisitorv5.Constants.AccountType.FACEBOOK_ACCOUNT_TYPE;
import static net.c_kogyo.returnvisitorv5.Constants.AccountType.GOOGLE_ACCOUNT_NAME;
import static net.c_kogyo.returnvisitorv5.Constants.AccountType.GOOGLE_ACCOUNT_TYPE;

/**
 * Created by SeijiShii on 2017/07/02.
 */

public class AccountDialog extends DialogFragment {

    public static final String ACCOUNT_TEST_TAG = "AccountTest";

    private ArrayList<Account> mAccounts;
    private static AccountDialogListener mListener;

    public static AccountDialog newInstance(AccountDialogListener listener) {

        mListener = listener;

        return new AccountDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager accountManager = AccountManager.get(getActivity());

        mAccounts = new ArrayList<>();
        mAccounts.addAll(Arrays.asList(accountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE)));
        mAccounts.addAll(Arrays.asList(accountManager.getAccountsByType(FACEBOOK_ACCOUNT_TYPE)));

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.account);
        builder.setMessage(R.string.account_message);

        builder.setView(generateAccountListView());

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    private ListView generateAccountListView() {
        ListView accountListView = new ListView(getActivity());
        accountListView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AccountListAdapter accountListAdapter = new AccountListAdapter();
        accountListView.setAdapter(accountListAdapter);

        return accountListView;
    }

    private Drawable getIconForAccount(Account account, AccountManager manager) {
        AuthenticatorDescription[] descriptions =  manager.getAuthenticatorTypes();
        for (AuthenticatorDescription description: descriptions) {
            if (description.type.equals(account.type)) {
                PackageManager pm = getActivity().getPackageManager();
                return pm.getDrawable(description.packageName, description.iconId, null);
            }
        }
        return null;
    }

    private void confirmAccount(final Account account) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.account_confirm);
        builder.setView(new AccountCell(getActivity(), account));

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AccountDialog.this.dismiss();
                if (mListener != null) {
                    mListener.onClickAccount(account);
                }
            }
        });
        builder.create().show();
    }

    public interface AccountDialogListener {
        void onClickAccount(Account account);
    }

    private class AccountListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAccounts.size();
        }

        @Override
        public Object getItem(int position) {
            return mAccounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new AccountCell(getActivity(), (Account) getItem(position));
            } else {
                ((AccountCell) convertView).refresh((Account) getItem(position));
            }
            return convertView;
        }
    }

    private class AccountCell extends FrameLayout {

        private Account mAccount;

        public AccountCell(@NonNull Context context, final Account account) {
            super(context);

            mAccount = account;
            initCommon();

            ViewUtil.setOnClickListener(this, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View view) {
                    if (mListener != null) {
                        confirmAccount(account);
                    }
                }
            });
        }

        public AccountCell(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        private View view;
        private ImageView accountIcon;
        private TextView accountText;
        private void initCommon() {

            view = View.inflate(getActivity(), R.layout.account_cell, this);
            accountIcon = (ImageView) view.findViewById(R.id.account_icon);
            accountText = (TextView) view.findViewById(R.id.account_text);

            refresh(null);

        }

        public void refresh(@Nullable Account account) {
            if (account != null) {
                mAccount = account;
            }

            accountIcon.setImageDrawable(getIconForAccount(mAccount, AccountManager.get(getActivity())));
            accountText.setText(mAccount.name);
        }
    }
}
