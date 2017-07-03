package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

/**
 * Created by SeijiShii on 2017/05/31.
 */

public class VisitDetailCell extends FrameLayout {

    private VisitDetail mVisitDetail;

    public VisitDetailCell(@NonNull Context context, VisitDetail visitDetail) {
        super(context);

        mVisitDetail = visitDetail;

        initCommon();
    }

    public VisitDetailCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private TextView textView;
    private TagFrame tagFrame;
    private void initCommon() {

        view = View.inflate(getContext(), R.layout.visit_detail_cell, this);
        textView = (TextView) view.findViewById(R.id.text_view);
        tagFrame = (TagFrame) view.findViewById(R.id.tag_frame);

        refreshVisitDetail(null);
    }

    public void refreshVisitDetail(@Nullable VisitDetail visitDetail) {

        if (visitDetail != null) {
            mVisitDetail = visitDetail;
        }

        textView.setText(mVisitDetail.toString(getContext(), 0, false));
        tagFrame.setTagIdsAndInitialize(mVisitDetail.getTagIds(),
                new TagFrame.TagFrameCallback() {
                    @Override
                    public void postDrawn() {

                    }
                });
    }
}
