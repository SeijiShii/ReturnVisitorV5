package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class PersonDialog extends FrameLayout {

    public PersonDialog(Context context) {
        this(context, null);
    }

    public PersonDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        initCommon();
    }

    private View view;
    private void initCommon(){

        view = LayoutInflater.from(getContext()).inflate(R.layout.person_dialog, this);

    }



}
