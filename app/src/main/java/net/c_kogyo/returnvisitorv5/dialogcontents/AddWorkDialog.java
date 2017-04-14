package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/04/14.
 */

public class AddWorkDialog extends FrameLayout {

    private AddWorkDialogListener mListener;
    private boolean mIsDateChangeable;
    private Work mWork;

    public AddWorkDialog(@NonNull Context context,
                         AddWorkDialogListener listener,
                         boolean isDateChangeable) {
        super(context);

        mListener = listener;
        mIsDateChangeable = isDateChangeable;

        initCommon();
    }

    public AddWorkDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {

        mWork = new Work(Calendar.getInstance());

        view = LayoutInflater.from(getContext()).inflate(R.layout.add_work_dialog, this);
        initDateText();
        initStartTimeText();
        initEndTimeText();
        initDurationText();
        initOkButton();
        initCancelButton();
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) view.findViewById(R.id.date_text);
        DateFormat format = android.text.format.DateFormat.getMediumDateFormat(getContext());
        dateText.setText(format.format(mWork.getStart().getTime()));
        if (mIsDateChangeable) {
            ViewUtil.setOnClickListener(dateText, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick() {
                    showDatePicker();
                }
            });
        } else {
            dateText.setAlpha(0.5f);
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mWork.getStart().set(Calendar.YEAR, year);
                mWork.getEnd().set(Calendar.YEAR, year);

                mWork.getStart().set(Calendar.MONTH, month);
                mWork.getEnd().set(Calendar.MONTH, month);

                mWork.getStart().set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mWork.getEnd().set(Calendar.DAY_OF_MONTH, dayOfMonth);

                DateFormat format = android.text.format.DateFormat.getMediumDateFormat(getContext());
                dateText.setText(format.format(mWork.getStart().getTime()));

            }
        },
                mWork.getStart().get(Calendar.YEAR),
                mWork.getStart().get(Calendar.MONTH),
                mWork.getStart().get(Calendar.DAY_OF_MONTH)).show();
    }

    private TextView startTimeText;
    private void initStartTimeText() {
        startTimeText = (TextView) view.findViewById(R.id.start_time_text);
        startTimeText.setText(DateTimeText.getTimeText(mWork.getStart()));
        ViewUtil.setOnClickListener(startTimeText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                showStartTimePicker();
            }
        });
    }

    private void showStartTimePicker() {
        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                Calendar setTime = Calendar.getInstance();
                setTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                setTime.set(Calendar.MINUTE, minute);

                if (setTime.after(mWork.getEnd())) {
                    return;
                }

                mWork.getStart().set(Calendar.HOUR_OF_DAY, hourOfDay);
                mWork.getStart().set(Calendar.MINUTE, minute);

                startTimeText.setText(DateTimeText.getTimeText(mWork.getStart()));
                refreshDurationText();
            }
        },
                mWork.getStart().get(Calendar.HOUR_OF_DAY),
                mWork.getStart().get(Calendar.MINUTE),
                true).show();
    }

    private TextView endTimeText;
    private void initEndTimeText() {
        endTimeText = (TextView) view.findViewById(R.id.end_time_text);
        endTimeText.setText(DateTimeText.getTimeText(mWork.getEnd()));
        ViewUtil.setOnClickListener(endTimeText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                showEndTimePicker();
            }
        });
    }

    private void showEndTimePicker() {
        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                Calendar setTime = Calendar.getInstance();
                setTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                setTime.set(Calendar.MINUTE, minute);

                if (setTime.before(mWork.getStart())) {
                    return;
                }

                mWork.getEnd().set(Calendar.HOUR_OF_DAY, hourOfDay);
                mWork.getEnd().set(Calendar.MINUTE, minute);

                endTimeText.setText(DateTimeText.getTimeText(mWork.getEnd()));
                refreshDurationText();
            }
        },
                mWork.getEnd().get(Calendar.HOUR_OF_DAY),
                mWork.getEnd().get(Calendar.MINUTE),
                true).show();
    }

    private TextView durationText;
    private void initDurationText() {
        durationText = (TextView) view.findViewById(R.id.duration_text);
        refreshDurationText();
    }

    private void refreshDurationText() {
        String durationString = getContext().getString(R.string.duration_string, DateTimeText.getDurationString(mWork.getDuration(), false));
        durationText.setText(durationString);
    }

    private void initOkButton() {
        Button okButton = (Button) view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onOkClick(mWork);
                }
            }
        });
    }

    private void initCancelButton() {
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelClick();
                }
            }
        });
    }

    public interface AddWorkDialogListener {

        void onOkClick(Work work);

        void onCancelClick();
    }
}
