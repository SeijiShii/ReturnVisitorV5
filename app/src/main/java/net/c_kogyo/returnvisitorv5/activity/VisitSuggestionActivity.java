package net.c_kogyo.returnvisitorv5.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitSuggestion;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.SuggestionCell;
import net.c_kogyo.returnvisitorv5.view.ToggleColorButton;

import java.util.ArrayList;

import static net.c_kogyo.returnvisitorv5.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.LONGITUDE;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class VisitSuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_suggestion_activity);

        initFilterFrame();
        initFilterButtonFrame();

        initLogoButton();

        initSuggestionList();
    }

    private LinearLayout filterFrame;
    private boolean mIsFilterOpen = false;
    private void initFilterFrame() {
        filterFrame = (LinearLayout) findViewById(R.id.filter_frame);

        initFilterToggleBar();
    }

    private RelativeLayout filterToggleBar;
    private void initFilterToggleBar() {
        filterToggleBar = (RelativeLayout) findViewById(R.id.filter_toggle_bar);

        ViewUtil.setOnClickListener(filterToggleBar, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                openCloseFilter();
            }
        });
    }

    private void openCloseFilter() {

        ImageView toggleArrow = (ImageView) findViewById(R.id.toggle_arrow);

        int closeHeight = getResources().getDimensionPixelSize(R.dimen.filter_frame_height_close);
        int openHeight = getResources().getDimensionPixelSize(R.dimen.filter_frame_height_open);

        int origin, target;

        if (mIsFilterOpen) {
            origin = openHeight;
            target = closeHeight;

            toggleArrow.setBackgroundResource(R.drawable.white_upper_arrow);

        } else {
            origin = closeHeight;
            target = openHeight;

            toggleArrow.setBackgroundResource(R.drawable.white_down_arrow);
        }

        ValueAnimator animator = ValueAnimator.ofInt(origin, target);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                filterFrame.getLayoutParams().height = (int) animation.getAnimatedValue();
                filterFrame.requestLayout();
            }
        });
        animator.setDuration(200);
        animator.start();

        mIsFilterOpen = !mIsFilterOpen;
    }

    private void initLogoButton() {
        final ImageView logoButton = (ImageView) findViewById(R.id.logo_button);
        ViewUtil.setOnClickListener(logoButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                returnToMapActivity();
            }
        });
    }

    private void returnToMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void moveToMapWithPlace(Visit visit) {
        String placeId = visit.getPlaceId();

        Intent intent = new Intent(VisitSuggestionActivity.this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Place.PLACE, placeId);

        startActivity(intent);
    }

    private ListView suggestionList;
    private void initSuggestionList() {
        suggestionList = (ListView) findViewById(R.id.suggestion_list_view);
        refreshFilter();
    }


    private ToggleColorButton[] filterButtons;
    private void initFilterButtonFrame() {

        FrameLayout filterButtonFrame = (FrameLayout) findViewById(R.id.filter_button_frame);

        LinearLayout filterButtonBase = new LinearLayout(this);
        filterButtonBase.setOrientation(LinearLayout.HORIZONTAL);

        filterButtons = new ToggleColorButton[5];
        for (int i = 0 ; i < 5 ; i++ ) {
            filterButtons[i] = new ToggleColorButton(this,
                    Constants.buttonRes[i + 3],
                    Constants.buttonRes[0],
                    true);
            filterButtons[i].setCheckChangeListener(mCheckChangeListener);
            filterButtonBase.addView(filterButtons[i]);
        }
        filterButtonFrame.addView(filterButtonBase);

    }

    ToggleColorButton.CheckChangeListener mCheckChangeListener = new ToggleColorButton.CheckChangeListener() {
        @Override
        public void onCheckChange(boolean checked) {
            refreshFilter();
        }
    };

    private void refreshFilter() {
        ArrayList<Visit.Priority> priorities = new ArrayList<>();
        for ( int i = 0 ; i < 5 ; i++ ) {
            if (filterButtons[i].isChecked()) {
                priorities.add(Visit.Priority.getEnum(i + 3));
            }
        }

        SuggestionListAdapter mAdapter = new SuggestionListAdapter(priorities);
        suggestionList.setAdapter(mAdapter);

    }

    private class SuggestionListAdapter extends BaseAdapter {

        ArrayList<VisitSuggestion> mSuggestions;
        SuggestionListAdapter(ArrayList<Visit.Priority> priorities) {
            mSuggestions = VisitSuggestion.getFilteredSuggestions(priorities);
        }

        @Override
        public int getCount() {
            return mSuggestions.size();
        }

        @Override
        public Object getItem(int position) {
            return mSuggestions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = new SuggestionCell(VisitSuggestionActivity.this,
                        (VisitSuggestion) getItem(position),
                        new SuggestionCell.SuggestionCellListener() {
                            @Override
                            public void onDismiss(VisitSuggestion suggestion) {
                                mSuggestions.remove(suggestion);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onClickShowInMap(VisitSuggestion suggestion) {
                                moveToMapWithPlace(suggestion.getLatestVisit());
                            }
                        });
            } else {
                ((SuggestionCell) convertView).refreshData((VisitSuggestion) getItem(position));
            }

            return convertView;
        }
    }
}
