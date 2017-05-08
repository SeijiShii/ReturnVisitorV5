package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.AggregationOfDay;
import net.c_kogyo.returnvisitorv5.data.AggregationOfMonth;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/07.
 */

public class MonthAggregationDialog extends FrameLayout {

    private Calendar mMonth;
    private MonthAggregationDialogListener mListener;

    public MonthAggregationDialog(@NonNull Context context,
                                  Calendar month,
                                  MonthAggregationDialogListener listener) {
        super(context);

        mMonth = (Calendar) month.clone();
        mListener = listener;

        initCommon();
    }

    public MonthAggregationDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.month_aggregation_dialog, this);

        initMonthTextView();
        initPlacementCountText();
        initVideoCountText();
        initTimeText();
        initRVCountText();
        initStudyCountText();

        initCloseButton();
    }

    private void initMonthTextView() {
        TextView monthTextView = (TextView) view.findViewById(R.id.month_text);
        String monthText = DateTimeText.getMonthText(mMonth, getContext());
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

    private void initCloseButton() {
        Button closeButton = (Button) view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickCloseButton();
                }
            }
        });
    }

    public interface MonthAggregationDialogListener {
        void onClickCloseButton();

        void onClickMailButton(Calendar month);
    }

    // TODO: 2017/05/08 Mail Action 
    // DONE: 2017/05/07 月名を表示
}
