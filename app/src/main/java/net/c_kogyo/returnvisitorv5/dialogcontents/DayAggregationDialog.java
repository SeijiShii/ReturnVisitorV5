package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.AggregationOfDay;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/04/16.
 */

public class DayAggregationDialog extends FrameLayout {

    private Calendar mDate;

    public DayAggregationDialog(@NonNull Context context, Calendar date) {
        super(context);

        mDate = (Calendar) date.clone();

        initCommon();
    }

    public DayAggregationDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.day_aggregation_dialog, this);
        initPlacementCountText();
        initVideoCountText();
        initTimeText();
        initRVCountText();
        initStudyCountText();
    }

    private void initPlacementCountText() {
        TextView placementCountText = (TextView) view.findViewById(R.id.placement_count_text);
        int plcCount = AggregationOfDay.placementCount(mDate);
        placementCountText.setText(String.valueOf(plcCount));
    }

    private void initVideoCountText() {
        TextView videoCountText = (TextView) view.findViewById(R.id.video_count_text);
        int videoCount = AggregationOfDay.showVideoCount(mDate);
        videoCountText.setText(String.valueOf(videoCount));
    }

    private void initTimeText() {
        TextView timeText = (TextView) view.findViewById(R.id.time_text);
        long time = AggregationOfDay.time(mDate);
        timeText.setText(DateTimeText.getDurationString(time, false));
    }

    private void initRVCountText() {
        TextView rvCountText = (TextView) view.findViewById(R.id.rv_count_text);
        int rvCount = AggregationOfDay.rvCount(mDate);
        rvCountText.setText(String.valueOf(rvCount));
    }

    private void initStudyCountText() {
        TextView studyCountText = (TextView) view.findViewById(R.id.study_count_text);
        int studyCount = AggregationOfDay.bsVisitCount(mDate);
        studyCountText.setText(String.valueOf(studyCount));
    }
}
