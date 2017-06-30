package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/06/29.
 */

public class LoginButtonBase extends BaseAnimateView {

    public LoginButtonBase(Context context, int initialHeight) {
        super(context, initialHeight, R.layout.login_button_base);
    }

    public LoginButtonBase(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.login_button_base);
    }

    @Override
    public void setLayoutParams(BaseAnimateView view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


}
