package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import net.c_kogyo.returnvisitorv5.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SeijiShii on 2017/06/02.
 */

public class TermOfUseDialog extends DialogFragment {

    private static TermOfUseDialog instance;
    public static TermOfUseDialog getInstance() {
        if (instance == null) {
            instance = new TermOfUseDialog();
        }
        return instance;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        WebView webView = new WebView(getActivity());
        webView.loadUrl(getString(R.string.term_of_use_url));
        builder.setView(webView);

        builder.setNegativeButton(R.string.close, null);

        return builder.create();

    }

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
