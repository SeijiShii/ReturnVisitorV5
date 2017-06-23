package net.c_kogyo.returnvisitorv5.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.dialog.AddWorkDialog;
import net.c_kogyo.returnvisitorv5.dialog.DayAggregationDialog;
import net.c_kogyo.returnvisitorv5.fragment.WorkFragment;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.InputUtil;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.Constants.DATE_LONG;
import static net.c_kogyo.returnvisitorv5.Constants.LATITUDE;
import static net.c_kogyo.returnvisitorv5.Constants.LONGITUDE;
import static net.c_kogyo.returnvisitorv5.util.ViewUtil.setOnClickListener;

/**
 * Created by SeijiShii on 2016/09/17.
 */

public class WorkPagerActivity extends AppCompatActivity {


    // DONE: 2017/04/05 Dialog Overlay
    // DONE: 2017/05/06 Pagerの左右ステートが変わったときだけリフレッシュ

    private enum PagerState {
        HAS_RIGHT_AND_NO_LEFT,
        HAS_LEFT_AND_NO_RIGHT,
        HAS_BOTH,
        NO_EITHER
    }

    private PagerState mPagerState, mOldState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.work_pager_activity);

        AdMobHelper.setAdView(this);

        setAddedWork();

        initHeaderRow();
        initPager();

        initLeftButton();
        initRightButton();
        initDateText();
        refreshPagerState();

        initMenuButton();
        initLogoButton();

    }

    private ViewPager mPager;
    private DatePagerAdapter mDatePagerAdapter;
    private void initPager() {

        mPager = (ViewPager) findViewById(R.id.view_pager);

        Calendar date = getDateToOpen();
        mDatePagerAdapter = new DatePagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mDatePagerAdapter);
        mPager.setCurrentItem(mDatePagerAdapter.getClosestPosition(date));

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                refreshPagerState();
                refreshButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    // 日付指定で遷移するためのプロセス
    private Calendar getDateToOpen() {

        Calendar date = Calendar.getInstance();

        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals(Constants.WorkPagerActivityActions.START_WITH_NEW_WORK)) {
                return addedWork.getStart();
            } else {
                return date;
            }
        } else {
            long dLong = getIntent().getLongExtra(Constants.DATE_LONG, 0);
            if (dLong != 0) {
                date.setTimeInMillis(dLong);
            }
            return date;
        }
    }

    private void initHeaderRow() {
        LinearLayout headerRow = (LinearLayout) findViewById(R.id.header_row);
        headerRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private ImageView leftButton;
    private void initLeftButton() {
        leftButton = (ImageView) findViewById(R.id.left_button);
        setOnClickListener(leftButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                onClickLeftButton();
            }
        });
        if (mPager.getCurrentItem() <= 0) {
            fadeLeftButton(false);
        }
    }

    private void onClickLeftButton() {
        mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
        refreshButtons();
    }

    // DONE: 2017/05/06 ボタンrefreshのアニメーション

    private void fadeLeftButton(boolean fadeIn) {

        float originAlpha, targetAlpha;

        if (fadeIn) {

            originAlpha = 0f;
            targetAlpha = 1f;

            leftButton.setClickable(true);
        } else {

            originAlpha = 1f;
            targetAlpha = 0f;

            leftButton.setClickable(false);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                leftButton.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private ImageView rightButton;
    private void initRightButton() {
        rightButton = (ImageView) findViewById(R.id.right_button);
        setOnClickListener(rightButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                onClickRightButton();
            }
        });

        if (mPager.getCurrentItem() >= mDatePagerAdapter.getCount() - 1) {
            fadeRightButton(false);
        }
    }

    private void onClickRightButton() {
        mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        refreshButtons();
    }

    private void fadeRightButton(boolean fadeIn) {

        float originAlpha, targetAlpha;

        if (fadeIn) {

            originAlpha = 0f;
            targetAlpha = 1f;

            rightButton.setClickable(true);
        } else {

            originAlpha = 1f;
            targetAlpha = 0f;

            rightButton.setClickable(false);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rightButton.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) findViewById(R.id.date_text);
        setOnClickListener(dateText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                onClickDateText();
            }
        });
        refreshDateText(null);
    }

    private void onClickDateText() {
        startCalendarActivity(mDatePagerAdapter.getDayItem(mPager.getCurrentItem()));
    }

    // DONE: 2017/04/05 カレンダーアクティビティと遷移
    private void startCalendarActivity(Calendar date) {
        Intent calendarActivityIntent = new Intent(this, CalendarPagerActivity.class);
        calendarActivityIntent.putExtra(Constants.MONTH_LONG, date.getTimeInMillis());
        startActivity(calendarActivityIntent);
        finish();
    }

    private void refreshDateText(@Nullable Calendar date) {
        DateFormat format = android.text.format.DateFormat.getDateFormat(this);
        String dateString;

        if (date != null) {
            dateString = format.format(date.getTime());
        } else {
            dateString = format.format(mDatePagerAdapter.getDayItem(mPager.getCurrentItem()).getTime());
        }
        dateText.setText(dateString);
    }

    private void refreshPagerState() {

        mOldState = mPagerState;
        if (mPager.getCurrentItem() <= 0
                && mPager.getCurrentItem() >= mDatePagerAdapter.getCount() - 1) {
            mPagerState = PagerState.NO_EITHER;
        } else if (mPager.getCurrentItem() <= 0) {
            mPagerState = PagerState.HAS_RIGHT_AND_NO_LEFT;
        } else if (mPager.getCurrentItem() >= mDatePagerAdapter.getCount() - 1) {
            mPagerState = PagerState.HAS_LEFT_AND_NO_RIGHT;
        }else {
            mPagerState = PagerState.HAS_BOTH;
        }
    }

    private void refreshButtons() {

        refreshDateText(null);

        if (mPagerState == mOldState)
            return;

        switch (mOldState) {
            case NO_EITHER:
                switch (mPagerState) {
                    case HAS_RIGHT_AND_NO_LEFT:
                        fadeRightButton(true);
                        break;
                    case HAS_LEFT_AND_NO_RIGHT:
                        fadeLeftButton(true);
                        break;
                    case HAS_BOTH:
                        fadeLeftButton(true);
                        fadeRightButton(true);
                        break;
                }
                break;
            case HAS_RIGHT_AND_NO_LEFT:
                switch (mPagerState) {
                    case NO_EITHER:
                        fadeRightButton(false);
                        break;
                    case HAS_LEFT_AND_NO_RIGHT:
                        fadeLeftButton(true);
                        fadeRightButton(false);
                        break;
                    case HAS_BOTH:
                        fadeLeftButton(true);
                        break;
                }
                break;
            case HAS_LEFT_AND_NO_RIGHT:
                switch (mPagerState) {
                    case NO_EITHER:
                        fadeLeftButton(false);
                        break;
                    case HAS_RIGHT_AND_NO_LEFT:
                        fadeRightButton(true);
                        fadeLeftButton(false);
                        break;
                    case HAS_BOTH:
                        fadeRightButton(true);
                        break;
                }
                break;
            case HAS_BOTH:
                switch (mPagerState) {
                    case NO_EITHER:
                        fadeLeftButton(false);
                        fadeRightButton(false);
                        break;
                    case HAS_RIGHT_AND_NO_LEFT:
                        fadeLeftButton(false);
                        break;
                    case HAS_LEFT_AND_NO_RIGHT:
                        fadeRightButton(false);
                        break;
                }
                break;
        }
    }

    private ImageView menuButton;
    private void initMenuButton() {
        menuButton = (ImageView) findViewById(R.id.work_menu_button);
        setOnClickListener(menuButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                showPopupMenu();
            }
        });
    }

    private void initLogoButton() {
        ImageView logoButton = (ImageView) findViewById(R.id.logo_button);
        setOnClickListener(logoButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                returnToMapActivity();
            }
        });
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.work_pager_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_work:
                        onClickAddWorkMenu();
                        return true;
                    case R.id.add_visit:
                        onClickAddVisitMenu();
                        return true;
                    case R.id.aggregation:
                        onClickAggregationMenu();
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void onClickAddWorkMenu() {
        // DONE: 2017/04/02 Add Work Action
        showAddWorkDialog();
    }

    private void onClickAddVisitMenu() {
        // DONE: 2017/04/02 RVRecord Visit Action with no Place
        Calendar date = Calendar.getInstance();
        Calendar pagerDate = mDatePagerAdapter.getDayItem(mPager.getCurrentItem());

        date.set(Calendar.YEAR, pagerDate.get(Calendar.YEAR));
        date.set(Calendar.MONTH, pagerDate.get(Calendar.MONTH));
        date.set(Calendar.DAY_OF_MONTH, pagerDate.get(Calendar.DAY_OF_MONTH));

        Intent intent = new Intent(this, RecordVisitActivity.class);
        intent.setAction(Constants.RecordVisitActions.NEW_VISIT_ACTION_NO_PLACE_WITH_DATE);
        intent.putExtra(DATE_LONG, date.getTimeInMillis());

        startActivityForResult(intent, Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE);

    }

    private void onClickAggregationMenu() {
        // DONE: 2017/04/02 Aggregation Action
        showDayAggregationDialog();
    }

    private void showDayAggregationDialog() {
        DayAggregationDialog.getInstance(mDatePagerAdapter.getDayItem(mPager.getCurrentItem())).show(getFragmentManager(), null);
    }

    private void returnToMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void moveToMapWithPosition(Visit visit) {
        String placeId = visit.getPlaceId();
        Place place = PlaceList.getInstance().getById(placeId);
        if (place == null) return;

        Intent intent = new Intent(WorkPagerActivity.this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(LATITUDE, place.getLatLng().latitude);
        intent.putExtra(LONGITUDE, place.getLatLng().longitude);

        startActivity(intent);
    }

    private void showAddWorkDialog() {
        AddWorkDialog.getInstance(new AddWorkDialog.AddWorkDialogListener() {
            @Override
            public void onOkClick(final Work work) {
                onAddWork(work);
            }

              @Override
              public void onCloseDialog() {
                  InputUtil.hideSoftKeyboard(WorkPagerActivity.this);
              }
          }, false,
                mDatePagerAdapter.getDayItem(mPager.getCurrentItem())).show(getFragmentManager(), null);
    }

    private void onAddWork(Work work) {
        RVData.getInstance().workList.setOrAdd(work);
        ArrayList<Work> worksRemoved = RVData.getInstance().workList.onChangeTime(work);
        ArrayList<Visit> visitsSwallowed = VisitList.getInstance().getVisitsInWork(work);

        int pos = mDatePagerAdapter.getPosition(work.getStart());
        mPager.setCurrentItem(pos);

        WorkFragment fragment = (WorkFragment) mDatePagerAdapter.instantiateItem(mPager, pos);
        fragment.removeWorkViews(worksRemoved);
        fragment.removeVisitCells(visitsSwallowed);
        fragment.addWorkViewAndExtract(work);

        RVData.getInstance().saveData(WorkPagerActivity.this);

        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(WorkPagerActivity.this);
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // DONE: 2017/05/06 AdView to Real

    private Work addedWork;
    private void setAddedWork() {
        if (getIntent().getAction() == null)
            return;

        if (!getIntent().getAction().equals(Constants.WorkPagerActivityActions.START_WITH_NEW_WORK))
            return;

        String workId = getIntent().getStringExtra(Work.WORK);
        if (workId == null)
            return;

        addedWork = RVData.getInstance().workList.getById(workId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RecordVisitActions.NEW_VISIT_REQUEST_CODE) {
            if (resultCode == Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE) {
                String visitId = data.getStringExtra(Visit.VISIT);
                if (visitId != null) {
                    Visit visit = VisitList.getInstance().getById(visitId);
                    if (visit != null) {
                        getCurrentFragment().insertVisitCell(visit);
                    }
                }
            }
        }
    }

    private WorkFragment getCurrentFragment() {
        int pos = mPager.getCurrentItem();
        return (WorkFragment) mDatePagerAdapter.instantiateItem(mPager, pos);
    }

    class DatePagerAdapter extends FragmentStatePagerAdapter {

        String TAG = "DatePagerAdapter_TAG";

        private boolean toExtractAddedWorkView;
        public DatePagerAdapter(FragmentManager fm) {
            super(fm);
            toExtractAddedWorkView = true;
        }

        @Override
        public Fragment getItem(int position) {

//            Log.d(TAG, "Get Work Fragment, position: " + position);
            return WorkFragment.newInstance(RVData.getInstance().getDatesWithData().get(position),
                    addedWork,
                    toExtractAddedWorkView,
                    new WorkFragment.WorkFragmentListener() {

                        @Override
                        public void postExtractAddedWorkView() {
                            toExtractAddedWorkView = false;
                        }

                        @Override
                        public void postRemoveWorkView(Work work) {
                            RVData.getInstance().workList.deleteById(work.getId());
                            RVData.getInstance().saveData(WorkPagerActivity.this);
                            notifyDataSetChanged();

                            RVCloudSync.getInstance().requestDataSyncIfLoggedIn(WorkPagerActivity.this);
                        }

                        @Override
                        public void onAllItemRemoved(Calendar date) {
                            removeDay(date);
                        }

                        @Override
                        public void moveToMap(Visit visit) {
                            moveToMapWithPosition(visit);
                            WorkPagerActivity.this.finish();
                        }
                    });
        }

        @Override
        public int getCount() {
            return RVData.getInstance().getDatesWithData().size();
        }

        public int getPosition(Calendar date) {

            for ( int i = 0 ; i < getCount() ; i++ ) {

                Calendar dateWithData = RVData.getInstance().getDatesWithData().get(i);

                if (CalendarUtil.isSameDay(date, dateWithData)) {
                    return i;
                }
            }

            return -1;
        }

        public Calendar getDayItem(int pos) {
            return RVData.getInstance().getDatesWithData().get(pos);
        }


//        public int getPositionForAddedWork(Work work) {
//
//            return getPosition(work.getStart());
//        }

        private Calendar getClosestDate(Calendar date) {
            if (getCount() <= 0) return date ;

            if (getPosition(date) >= 0) {
                return date;
            }

            Calendar dateFuture = (Calendar) date.clone();
            Calendar datePast = (Calendar) date.clone();

            while (true) {

                dateFuture.add(Calendar.DAY_OF_MONTH, 1);
                datePast.add(Calendar.DAY_OF_MONTH, -1);

                if (getPosition(datePast) >= 0) {
                    return datePast;
                }

                if (getPosition(dateFuture) >= 0) {
                    return dateFuture;
                }

            }
        }

        public int getClosestPosition(Calendar date) {

            return getPosition(getClosestDate(date));

        }

        private void removeDay(Calendar date) {

            // DONE: 2017/05/04 挙動要検証

            notifyDataSetChanged();
            mPager.setCurrentItem(getClosestPosition(date), true);
            refreshDateText(getClosestDate(date));
            refreshPagerState();
            refreshButtons();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    interface DialogPostAnimationListener {
        void onFinishAnimation();
    }

}
