package net.c_kogyo.returnvisitorv5.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.DismissedSuggestion;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitSuggestion;
import net.c_kogyo.returnvisitorv5.dialog.ShowInMapDialog;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.SuggestionCell;
import net.c_kogyo.returnvisitorv5.view.ToggleColorButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class VisitSuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_suggestion_activity);

        AdMobHelper.setAdView(this);

        initFilterFrame();
        initFilterButtonFrame();

        initLogoButton();
        initMenuButton();

        initSuggestionList();

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadDismissedSuggestions();
        refreshListByFilter(false);
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
            public void onViewClick(View v) {
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
            public void onViewClick(View v) {
                returnToMapActivity();
            }
        });
    }

    private ImageView menuButton;
    private void initMenuButton() {
        menuButton = (ImageView) findViewById(R.id.menu_button);
        ViewUtil.setOnClickListener(menuButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                showPopup();
            }
        });
    }

    private void showPopup() {
        PopupMenu popupMenu = new PopupMenu(this, menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.suggestion_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.reload_dismissed:
                        reloadDismissedSuggestions();
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void reloadDismissedSuggestions() {
        dismissedSuggestions.clear();
        saveDismissedSuggestions();
        refreshListByFilter(true);
    }

    private void returnToMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void moveToMapWithPlace(Place place) {
        String placeId = place.getId();

        Intent intent = new Intent(VisitSuggestionActivity.this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Place.PLACE, placeId);

        startActivity(intent);
    }

    private void showMapDialog(Visit visit) {

        if (!RVData.getInstance().placeList.hasItem(visit.getPlaceId())) {
            Toast.makeText(this, R.string.place_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        ShowInMapDialog mapDialog = ShowInMapDialog.newInstance(visit.getPlaceId(),
                new ShowInMapDialog.MapDialogListener() {
                    @Override
                    public void onMarkerClick(Place place) {
                        moveToMapWithPlace(place);
                    }
                });
        mapDialog.show(getFragmentManager(), null);
    }

    private ListView suggestionList;
    private void initSuggestionList() {
        suggestionList = (ListView) findViewById(R.id.suggestion_list_view);
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
            refreshListByFilter(true);
        }
    };

    private void refreshListByFilter(boolean blink) {
        final ArrayList<Visit.Priority> priorities = new ArrayList<>();
        for ( int i = 0 ; i < 5 ; i++ ) {
            if (filterButtons[i].isChecked()) {
                priorities.add(Visit.Priority.getEnum(i + 3));
            }
        }

        if (blink) {
            ViewUtil.fadeView(suggestionList, false, null,
                    new ViewUtil.PostFadeViewListener() {
                        @Override
                        public void postFade(View view) {
                            SuggestionListAdapter mAdapter = new SuggestionListAdapter(priorities);
                            suggestionList.setAdapter(mAdapter);

                            ViewUtil.fadeView(suggestionList, true, null, null, 300);
                        }
                    }, 300);

        } else {
            SuggestionListAdapter mAdapter = new SuggestionListAdapter(priorities);
            suggestionList.setAdapter(mAdapter);
        }
    }

    private class SuggestionListAdapter extends BaseAdapter {

        ArrayList<VisitSuggestion> mSuggestions;
        SuggestionListAdapter(ArrayList<Visit.Priority> priorities) {
            mSuggestions = VisitSuggestion.getFilteredSuggestions(priorities, dismissedSuggestions);
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

                                DismissedSuggestion dismissedSuggestion
                                        = new DismissedSuggestion(suggestion.getLatestVisit().getId(), Calendar.getInstance());
                                dismissedSuggestions.add(dismissedSuggestion);
                                saveDismissedSuggestions();
                            }

                            @Override
                            public void onClickShowInMap(VisitSuggestion suggestion) {
                                showMapDialog(suggestion.getLatestVisit());
                            }
                        });
            } else {
                ((SuggestionCell) convertView).refreshData((VisitSuggestion) getItem(position));
            }

            return convertView;
        }
    }

    private ArrayList<DismissedSuggestion> dismissedSuggestions;
    private void saveDismissedSuggestions() {

        JSONArray array = new JSONArray();

        for (DismissedSuggestion suggestion : dismissedSuggestions) {
            array.put(suggestion.jsonObject());
        }

        JSONObject object = new JSONObject();

        try {
            object.put(DismissedSuggestion.DISMISSED_SUGGESTIONS, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DismissedSuggestion.DISMISSED_SUGGESTIONS, object.toString());
        editor.apply();
    }

    private void loadDismissedSuggestions() {
        dismissedSuggestions = new ArrayList<>();

        SharedPreferences preferences = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        String arrayString = preferences.getString(DismissedSuggestion.DISMISSED_SUGGESTIONS, null);

        if (arrayString == null) return;

        try {
            JSONObject object = new JSONObject(arrayString);
            if (object.has(DismissedSuggestion.DISMISSED_SUGGESTIONS)) {
                JSONArray array = object.getJSONArray(DismissedSuggestion.DISMISSED_SUGGESTIONS);

                for (int i = 0 ; i < array.length() ; i++) {
                    DismissedSuggestion dismissedSuggestion = new DismissedSuggestion(array.getJSONObject(i));

                    if (CalendarUtil.isSameDay(dismissedSuggestion.getDismissedDate(), Calendar.getInstance())) {
                        // 今日dismissしたものなら再読み込み
                        dismissedSuggestions.add(dismissedSuggestion);
                    }
                }
            }

            // 内容が変更されているので再保存
            saveDismissedSuggestions();

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
}
