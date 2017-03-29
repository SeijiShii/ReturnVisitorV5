package net.c_kogyo.returnvisitorv5.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.service.TimeCountService;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/03/29.
 */

public class CountTimeFrame extends BaseAnimateView {

    private int mCollapseHeight, mExtractHeight;
    private CountTimeFrameListener mListener;
    private boolean isCounting;
    private long mStartTimeLong;

    public CountTimeFrame(Context context, int initialHeight) {
        super(context, initialHeight, R.layout.count_time_frame);

        initCommon();
    }

    public CountTimeFrame(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.count_time_frame);

        initCommon();
    }

    @Override
    public void setLayoutParams() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    public void setListener(CountTimeFrameListener listener) {
        mListener = listener;
    }

    private void initCommon() {

        mCollapseHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
        mExtractHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small) * 3
                            + (int) (getContext().getResources().getDisplayMetrics().density * 15);

        initCountTimeButton();
        initStartTimeText();
        initDurationText();
    }

    private Button countTimeButton;
    private void initCountTimeButton() {
        countTimeButton = (Button) findViewById(R.id.time_count_button);
        countTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    if (isCounting) {
                        mListener.onClickStopButton();
                    } else {
                        mListener.onClickStartButton();
                    }
                }
            }
        });
    }

    private TextView startTimeText;
    private void initStartTimeText() {
        startTimeText = (TextView) getViewById(R.id.start_time_text);
        startTimeText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTimeText.setAlpha(0.5f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        startTimeText.setAlpha(1f);
                        showTimePickerDialog();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        startTimeText.setAlpha(1f);
                        return true;
                }
                return false;
            }
        });
    }

    private void showTimePickerDialog() {

        final Calendar start = Calendar.getInstance();
        start.setTimeInMillis(mStartTimeLong);

        new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar start2 = Calendar.getInstance();
                        start2.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        start2.set(Calendar.MINUTE, minute);

                        Calendar now = Calendar.getInstance();
                        if (start2.before(now)) {
                            if (mListener != null) {
                                mListener.onChangeStart(start2);
                            }
                        }
                    }
                },
                start.get(Calendar.HOUR_OF_DAY),
                start.get(Calendar.MINUTE),
                true).show();

    }

    private TextView durationText;
    private void initDurationText() {
        durationText = (TextView) getViewById(R.id.duration_text);
    }

    public void setExtracted(boolean extracted) {
        isExtracted = extracted;
        if (isExtracted) {
            getLayoutParams().height = mExtractHeight;
        } else {
            getLayoutParams().height = mCollapseHeight;
        }
    }

    public boolean isExtracted() {
        return isExtracted;
    }
    private boolean isExtracted;
    public void extract() {
        changeViewHeight(mExtractHeight, true, null, null);
        isExtracted = true;
    }

    public void collapse() {
        changeViewHeight(mCollapseHeight, true, null, null);
        isExtracted = false;
    }

    public void updateUI(boolean isCounting, long startTimeLong, @Nullable String startText, @Nullable String durationText) {

        this.isCounting = isCounting;
        this.mStartTimeLong = startTimeLong;

        if (this.isCounting) {
            if (!isExtracted) {
                extract();
            }
            countTimeButton.setText(R.string.stop_time_count);
            if (startText != null)
                this.startTimeText.setText(startText);
            if (durationText != null)
                this.durationText.setText(durationText);
        } else {
            if (isExtracted) {
                collapse();
            }
            countTimeButton.setText(R.string.count_time);
        }
    }

    public interface CountTimeFrameListener {

        void onClickStartButton();

        void onClickStopButton();

        void onChangeStart(Calendar calendar);
    }
}
