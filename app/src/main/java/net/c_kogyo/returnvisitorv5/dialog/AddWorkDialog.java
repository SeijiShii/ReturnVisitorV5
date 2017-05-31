package net.c_kogyo.returnvisitorv5.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SeijiShii on 2017/04/14.
 */

public class AddWorkDialog extends DialogFragment {

    // DONE: 2017/05/08 日付を設定できるかできないか
    private static AddWorkDialogListener mListener;
    private static boolean mIsDateChangeable;
    private Work mWork;
    private static Calendar mDate;
    
    private static AddWorkDialog instance;


    public static AddWorkDialog getInstance(AddWorkDialogListener listener,
                                             boolean isDateChangeable,
                                             Calendar date) {
        
        mListener = listener;
        mIsDateChangeable = isDateChangeable;
        mDate = (Calendar) date.clone();
        
        
        if (instance == null) {
            instance = new AddWorkDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        initCommon();
        builder.setView(view);

        builder.setTitle(R.string.add_work_title);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onOkClick(mWork);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
        
    }

    private View view;
    private void initCommon() {

        mWork = new Work(Calendar.getInstance());
        mWork.setDate(mDate);

        view = View.inflate(getActivity(), R.layout.add_work_dialog ,null);
        initDateText();
        initStartTimeText();
        initEndTimeText();
        initDurationText();
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) view.findViewById(R.id.date_text);
        DateFormat format = android.text.format.DateFormat.getMediumDateFormat(getActivity());
        dateText.setText(format.format(mWork.getStart().getTime()));
        if (mIsDateChangeable) {
            ViewUtil.setOnClickListener(dateText, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View v) {
                    showDatePicker();
                }
            });
        } else {
            dateText.setAlpha(0.5f);
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mWork.getStart().set(Calendar.YEAR, year);
                mWork.getEnd().set(Calendar.YEAR, year);

                mWork.getStart().set(Calendar.MONTH, month);
                mWork.getEnd().set(Calendar.MONTH, month);

                mWork.getStart().set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mWork.getEnd().set(Calendar.DAY_OF_MONTH, dayOfMonth);

                DateFormat format = android.text.format.DateFormat.getMediumDateFormat(getActivity());
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
        startTimeText.setText(DateTimeText.getTimeText(mWork.getStart(), false));
        ViewUtil.setOnClickListener(startTimeText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                showStartTimePicker();
            }
        });
    }

    private void showStartTimePicker() {
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                mWork.getStart().set(Calendar.HOUR_OF_DAY, hourOfDay);
                mWork.getStart().set(Calendar.MINUTE, minute);
                startTimeText.setText(DateTimeText.getTimeText(mWork.getStart(), false));

                if (mWork.getStart().after(mWork.getEnd())) {
                    mWork.getEnd().set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mWork.getEnd().set(Calendar.MINUTE, minute);
                    mWork.getEnd().add(Calendar.MINUTE, 1);
                    endTimeText.setText(DateTimeText.getTimeText(mWork.getEnd(), false));
                }

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
        endTimeText.setText(DateTimeText.getTimeText(mWork.getEnd(), false));
        ViewUtil.setOnClickListener(endTimeText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                showEndTimePicker();
            }
        });
    }

    private void showEndTimePicker() {
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                mWork.getEnd().set(Calendar.HOUR_OF_DAY, hourOfDay);
                mWork.getEnd().set(Calendar.MINUTE, minute);
                endTimeText.setText(DateTimeText.getTimeText(mWork.getEnd(), false));

                if (mWork.getEnd().before(mWork.getStart())) {
                    mWork.getStart().set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mWork.getStart().set(Calendar.MINUTE, minute);
                    mWork.getStart().add(Calendar.MINUTE, -1);
                    startTimeText.setText(DateTimeText.getTimeText(mWork.getStart(), false));
                }
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
        String durationString = getActivity().getString(R.string.duration_string, DateTimeText.getDurationString(mWork.getDuration(), false));
        durationText.setText(durationString);
    }

    public interface AddWorkDialogListener {

        void onOkClick(Work work);
    }

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
