package net.c_kogyo.returnvisitorv5.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/05/24.
 */

public class SimpleContentFragment extends Fragment {

    private FrameLayout frameLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.simple_content_fragment, container);

        frameLayout = (FrameLayout) view.findViewById(R.id.frame);

        return view;

    }

    public void setContent(View view) {
        frameLayout.addView(view);
    }


}
