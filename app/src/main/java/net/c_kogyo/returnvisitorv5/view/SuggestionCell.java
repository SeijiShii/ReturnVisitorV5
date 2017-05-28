package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitSuggestion;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class SuggestionCell extends FrameLayout {

    private VisitSuggestion mVisitSuggestion;

    public SuggestionCell(@NonNull Context context, VisitSuggestion visitSuggestion) {
        super(context);

        mVisitSuggestion = visitSuggestion;

        initCommon();
    }

    public SuggestionCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private ImageView marker;
    private TextView dataText, lastVisitText;
    private void initCommon() {
        view = View.inflate(getContext(), R.layout.suggestion_cell, this);

        marker = (ImageView) view.findViewById(R.id.marker);
        dataText = (TextView) view.findViewById(R.id.data_text);
        lastVisitText = (TextView) view.findViewById(R.id.last_visit_date_text);

        refreshData(null);

        initMenuButton();
    }

    public void refreshData(@Nullable VisitSuggestion visitSuggestion) {

        if (visitSuggestion != null) {
            mVisitSuggestion = visitSuggestion;
        }

        marker.setBackgroundResource(Constants.buttonRes[mVisitSuggestion.getLatestVisit().getPriority().num()]);

        if (mVisitSuggestion.getPerson() != null) {
            dataText.setText(mVisitSuggestion.getPerson().toString(getContext()));
        } else {
            dataText.setText(R.string.not_home);
        }
        final DateFormat format = android.text.format.DateFormat.getMediumDateFormat(getContext());
        String lastVisitDate = getContext().getString(R.string.last_visit_date,
                format.format(mVisitSuggestion.getLatestVisit().getDatetime().getTime()),
                CalendarUtil.daysPast(mVisitSuggestion.getLatestVisit().getDatetime(), Calendar.getInstance()));
        lastVisitText.setText(lastVisitDate);
    }

    private Button menuButton;
    private void initMenuButton() {
        menuButton = (Button) view.findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
