package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class VisitDetailView extends BaseAnimateView {

    private VisitDetail mVisitDetail;
    public VisitDetailView(Context context, VisitDetail visitDetail, InitialHeightCondition condition) {
        super(context, 500, condition, R.layout.visit_detail_view);
        mVisitDetail = visitDetail;

        initCommon();
    }

    public VisitDetailView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.visit_detail_view);
    }

    private void initCommon() {

    }
}
