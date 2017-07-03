package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
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
import net.c_kogyo.returnvisitorv5.data.AggregationOfMonth;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/07.
 */

public class MonthAggregationDialog extends DialogFragment {

    private static Calendar mMonth;
    private static MonthAggregationDialogListener mListener;

    private static MonthAggregationDialog instance;

    public static MonthAggregationDialog getInstance(Calendar month, MonthAggregationDialogListener listener) {
        
        mMonth = (Calendar) month.clone();
        mListener = listener;
        
        if (instance == null) {
            instance = new MonthAggregationDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        builder.setTitle(R.string.month_aggregation);
        
        initCommon();
        builder.setView(view);

        builder.setNeutralButton(R.string.report_mail, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onClickMailButton(mMonth);
                }
            }
        });

        builder.setNegativeButton(R.string.close, null);
        
        return builder.create();
    }

    private View view;
    private void initCommon() {
        
        view = View.inflate(getActivity(), R.layout.month_aggregation_dialog, null);

        initMonthTextView();
        initPlacementCountText();
        initVideoCountText();
        initTimeText();
        initRVCountText();
        initStudyCountText();
        initCarryOverText();

    }

    private void initMonthTextView() {
        TextView monthTextView = (TextView) view.findViewById(R.id.month_text);
        String monthText = DateTimeText.getMonthText(mMonth, getActivity());
        monthTextView.setText(monthText);
    }

    private void initPlacementCountText() {
        TextView placementCountText = (TextView) view.findViewById(R.id.placement_count_text);
        int plcCount = AggregationOfMonth.placementCount(mMonth);
        placementCountText.setText(String.valueOf(plcCount));
    }

    private void initVideoCountText() {
        TextView videoCountText = (TextView) view.findViewById(R.id.video_count_text);
        int videoCount = AggregationOfMonth.showVideoCount(mMonth);
        videoCountText.setText(String.valueOf(videoCount));
    }

    private void initTimeText() {
        TextView timeText = (TextView) view.findViewById(R.id.time_text);
        int time = AggregationOfMonth.hour(mMonth);
        timeText.setText(String.valueOf(time));
    }

    private void initRVCountText() {
        TextView rvCountText = (TextView) view.findViewById(R.id.rv_count_text);
        int rvCount = AggregationOfMonth.rvCount(mMonth);
        rvCountText.setText(String.valueOf(rvCount));
    }

    private void initStudyCountText() {
        TextView studyCountText = (TextView) view.findViewById(R.id.study_count_text);
        int studyCount = AggregationOfMonth.bsCount(mMonth);
        studyCountText.setText(String.valueOf(studyCount));
    }

    private void initCarryOverText() {
        TextView carryOverTextView = (TextView) view.findViewById(R.id.carry_over_text);
        long carryOver = AggregationOfMonth.getCarryOver(AggregationOfMonth.time(mMonth));
        int minute = (int)(carryOver / (60 * 1000));

        String carryOverText = getActivity().getString(R.string.carry_over, String.valueOf(minute));
        carryOverTextView.setText(carryOverText);
    }

    public interface MonthAggregationDialogListener {

        void onClickMailButton(Calendar month);
    }

    // DONE: 2017/05/08 Mail Action
    // DONE: 2017/05/07 月名を表示
    // Fraction minute
}
