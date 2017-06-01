package net.c_kogyo.returnvisitorv5.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.DismissedSuggestion;
import net.c_kogyo.returnvisitorv5.data.Filter;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitSuggestion;
import net.c_kogyo.returnvisitorv5.dialog.ShowInMapDialog;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.PriorityFilterPane;
import net.c_kogyo.returnvisitorv5.view.SearchFilterPane;
import net.c_kogyo.returnvisitorv5.view.SuggestionCell;
import net.c_kogyo.returnvisitorv5.view.TagFilterPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class VisitSuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filter = new Filter();

        setContentView(R.layout.visit_suggestion_activity);

        AdMobHelper.setAdView(this);

        initFilterFrame();

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
    private FrameLayout filterContentFrame;
    private void initFilterFrame() {
        filterFrame = (LinearLayout) findViewById(R.id.filter_frame);
        filterContentFrame = (FrameLayout) findViewById(R.id.filter_content_frame);

        initFilterBar();
    }

    private void initFilterBar() {
        RelativeLayout filterToggleBar = (RelativeLayout) findViewById(R.id.filter_toggle_bar);

        ViewUtil.setOnClickListener(filterToggleBar, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                closeFilterPane(null);
            }
        });
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
                    case R.id.filter_on_priority:
                        filterOnPriority();
                        return true;
                    case R.id.filter_on_tags:
                        filterOnTag();
                        return true;
                    case R.id.search_filter:
                        filterBySearch();
                        return true;
                    case R.id.reset_filter:
                        resetFilter();
                        return true;
                }
                return false;
            }
        });
        if (isPriorityFilterOpen) {
            popupMenu.getMenu().getItem(1).setEnabled(false);
        }
        if (isTagFilterOpen) {
            popupMenu.getMenu().getItem(2).setEnabled(false);
        }
        if (isSearchFilterOpen) {
            popupMenu.getMenu().getItem(3).setEnabled(false);
        }

        popupMenu.show();
    }

    private void reloadDismissedSuggestions() {
        dismissedSuggestions.clear();
        saveDismissedSuggestions();
//        refreshListByFilter(true);
    }

    private Filter filter;
    private PriorityFilterPane priorityFilterPane;
    private boolean isPriorityFilterOpen;
    private void filterOnPriority() {

        priorityFilterPane
                = new PriorityFilterPane(this,
                new PriorityFilterPane.PriorityFilterListener() {
                    @Override
                    public void onSetFilter(ArrayList<Visit.Priority> priorities) {
                        filter.setPriorities(priorities);
                        refreshListByFilter(true);
                    }
                }, filter.getPriorities());
        filterContentFrame.removeAllViews();
        filterContentFrame.addView(priorityFilterPane);

        final int paneHeight = getResources().getDimensionPixelSize(R.dimen.priority_filter_height);
        isPriorityFilterOpen = true;

        if (filterFrame.getHeight() <= 0) {
            openFilterPane(paneHeight);
        } else {
            closeFilterPane(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    openFilterPane(paneHeight);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }



    }

    private TagFilterPane tagFilterPane;
    private boolean isTagFilterOpen;
    private void filterOnTag() {

        tagFilterPane =
                new TagFilterPane(this,
                        filter.getTagIds(),
                        new TagFilterPane.TagFilterPaneListener() {
                            @Override
                            public void onTagSelectChanged(ArrayList<String> selectedTagIds) {
                                filter.setTagIds(selectedTagIds);
                                refreshListByFilter(true);
                            }
                        });
        filterContentFrame.removeAllViews();
        filterContentFrame.addView(tagFilterPane);

        final int paneHeight = getResources().getDimensionPixelSize(R.dimen.tag_filter_height);
        isTagFilterOpen = true;

        if (filterFrame.getHeight() <= 0) {
            openFilterPane(paneHeight);
        } else {
            closeFilterPane(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    openFilterPane(paneHeight);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    private SearchFilterPane searchFilterPane;
    private boolean isSearchFilterOpen;
    private void filterBySearch() {

        searchFilterPane = new SearchFilterPane(this, new SearchFilterPane.SearchFilterPaneListener() {
            @Override
            public void afterTextChanged(String s) {
                String[] words = s.split(" ");

                filter.setSearchWords(new ArrayList<String>(Arrays.asList(words)));
                refreshListByFilter(true);
            }
        });

        filterContentFrame.removeAllViews();
        filterContentFrame.addView(searchFilterPane);

        isSearchFilterOpen = true;
        final int height = getResources().getDimensionPixelSize(R.dimen.search_filter_height);

        if (filterFrame.getHeight() <= 0) {
            openFilterPane(height);
        } else {
            closeFilterPane(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    openFilterPane(height);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

    }

    private void openFilterPane(int paneHeight) {

        int barHeight = getResources().getDimensionPixelSize(R.dimen.filter_bar_height);
        int targetHeight = paneHeight + barHeight;

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) animation.getAnimatedValue());
                filterFrame.setLayoutParams(params);
                filterFrame.requestLayout();
            }
        });

        int duration =  (int) (targetHeight * getResources().getDisplayMetrics().density);
        animator.setDuration(duration);
        animator.start();

    }

    private void closeFilterPane(@Nullable Animator.AnimatorListener listener) {

        if (filterFrame.getHeight() <= 0) return;

        ValueAnimator animator = ValueAnimator.ofInt(filterFrame.getHeight(), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) animation.getAnimatedValue());
                filterFrame.setLayoutParams(params);
                filterFrame.requestLayout();
            }
        });

        int duration =  (int) (filterFrame.getHeight() * getResources().getDisplayMetrics().density);
        animator.setDuration(duration);

        if (listener != null) {
            animator.addListener(listener);
        }

        animator.start();

        isPriorityFilterOpen = false;
        isTagFilterOpen = false;
        isSearchFilterOpen = false;

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

    private void refreshListByFilter(boolean blink) {

        if (blink) {
            ViewUtil.fadeView(suggestionList, false, null,
                    new ViewUtil.PostFadeViewListener() {
                        @Override
                        public void postFade(View view) {
                            SuggestionListAdapter mAdapter = new SuggestionListAdapter(filter);
                            suggestionList.setAdapter(mAdapter);

                            ViewUtil.fadeView(suggestionList, true, null, null, 300);
                        }
                    }, 300);

        } else {
            SuggestionListAdapter mAdapter = new SuggestionListAdapter(filter);
            suggestionList.setAdapter(mAdapter);
        }
    }

    private void resetFilter() {
        filter = new Filter();
        closeFilterPane(null);
        refreshListByFilter(true);
    }

    private class SuggestionListAdapter extends BaseAdapter {

        ArrayList<VisitSuggestion> mSuggestions;
        SuggestionListAdapter(Filter filter) {
            mSuggestions = VisitSuggestion.getFilteredSuggestions(filter, dismissedSuggestions, VisitSuggestionActivity.this);
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
