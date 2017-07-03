package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.AggregationOfDay;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SeijiShii on 2017/04/16.
 */

public class DayAggregationDialog extends DialogFragment {

    private static Calendar mDate;

    private static DayAggregationDialog instance;
    public static DayAggregationDialog getInstance(Calendar date) {
        
        mDate = (Calendar) date.clone();

        if (instance == null) {
            instance = new DayAggregationDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        initCommon();
        builder.setView(view);

        builder.setTitle(R.string.day_aggregation);
        
        builder.setNegativeButton(R.string.close, null);
        
        return builder.create();
    }

    private View view;
    private void initCommon() {
        view = View.inflate(getActivity(), R.layout.day_aggregation_dialog, null);

        initDateTextView();
        initPlacementCountText();
        initVideoCountText();
        initTimeText();
        initRVCountText();
        initStudyCountText();

    }

    private void initDateTextView() {
        TextView dateTextView = (TextView) view.findViewById(R.id.date_text);
        DateFormat format = android.text.format.DateFormat.getMediumDateFormat(getActivity());
        String dateText = format.format(mDate.getTime());
        dateTextView.setText(dateText);
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

    // DONE: 2017/05/07 日付を表示

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
