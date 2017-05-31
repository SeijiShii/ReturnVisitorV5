package net.c_kogyo.returnvisitorv5.data;

import android.app.Fragment;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class FragmentTitlePair{

    public Fragment fragment;
    public String title;

    public FragmentTitlePair(Fragment fragment, String title) {
        this.fragment = fragment;
        this.title = title;
    }
}
