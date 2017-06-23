package net.c_kogyo.returnvisitorv5.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;
import net.c_kogyo.returnvisitorv5.db.RVRecord;
import net.c_kogyo.returnvisitorv5.dialog.AddWorkDialog;
import net.c_kogyo.returnvisitorv5.dialog.MonthAggregationDialog;
import net.c_kogyo.returnvisitorv5.fragment.CalendarFragment;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.InputUtil;
import net.c_kogyo.returnvisitorv5.util.MailReport;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.util.ViewUtil.setOnClickListener;

/**
 * Created by SeijiShii on 2017/05/04.
 */

public class CalendarPagerActivity extends AppCompatActivity {

    public enum StartDay {
        SUNDAY,
        MONDAY
    }

    private enum PagerState {
        HAS_RIGHT_AND_NO_LEFT,
        HAS_LEFT_AND_NO_RIGHT,
        HAS_BOTH,
        NO_EITHER
    }

    private PagerState mPagerState, mOldState;
    private StartDay mStartDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_pager_activity);

        AdMobHelper.setAdView(this);

        loadPrefs();
        initPager();

        initMonthTextView();
        initLeftButton();
        initRightButton();

        refreshPagerState();

        scrollToMonth();
        initLogoButton();

        initMenuButton();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePrefs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // メール画面からもどったときのため
        InputUtil.hideSoftKeyboard(this);
    }

    private ViewPager mPager;
    private CalendarPagerAdapter mAdapter;
    private void initPager() {
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new CalendarPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshMonthText();
                refreshPagerState();
                refreshButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private TextView monthTextView;
    private void initMonthTextView() {

        monthTextView = (TextView) findViewById(R.id.month_text);
        refreshMonthText();
    }

    private void refreshMonthText() {

        Calendar currentMonth = mAdapter.getMonth(mPager.getCurrentItem());
        String monthText = DateTimeText.getMonthText(currentMonth, this);
        monthTextView.setText(monthText);
    }

    private ImageView leftButton;
    private void initLeftButton() {
        leftButton = (ImageView) findViewById(R.id.left_button);
        if (mPager.getCurrentItem() <= 0) {
            fadeLeftButton(false);
        }
    }

    private void fadeLeftButton(boolean fadeIn) {

        float originAlpha, targetAlpha;

        if (fadeIn) {

            originAlpha = 0f;
            targetAlpha = 1f;
            ViewUtil.setOnClickListener(leftButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View v) {
                    mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
                    refreshPagerState();
                    refreshButtons();
                }
            });
        } else {
            originAlpha = 1f;
            targetAlpha = 0f;
            ViewUtil.setOnClickListener(leftButton, null);
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
        if (mPager.getCurrentItem() >= mAdapter.getCount() - 1) {
            fadeRightButton(false);
        }
    }

    private void fadeRightButton(boolean fadeIn) {

        float originAlpha, targetAlpha;

        if (fadeIn) {
            originAlpha = 0f;
            targetAlpha = 1f;
            ViewUtil.setOnClickListener(rightButton, new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View v) {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
                    refreshPagerState();
                    refreshButtons();
                }
            });
        } else {
            originAlpha = 1f;
            targetAlpha = 0f;
            ViewUtil.setOnClickListener(rightButton, null);
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

    private void refreshPagerState() {

        mOldState = mPagerState;
        if (mPager.getCurrentItem() <= 0
                && mPager.getCurrentItem() >= mAdapter.getCount() - 1) {
            mPagerState = PagerState.NO_EITHER;
        } else if (mPager.getCurrentItem() <= 0) {
            mPagerState = PagerState.HAS_RIGHT_AND_NO_LEFT;
        } else if (mPager.getCurrentItem() >= mAdapter.getCount() - 1) {
            mPagerState = PagerState.HAS_LEFT_AND_NO_RIGHT;
        }else {
            mPagerState = PagerState.HAS_BOTH;
        }
    }

    private void refreshButtons() {

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

    private void scrollToMonth() {

        Calendar month = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent == null) return;

        long monthLong = intent.getLongExtra(Constants.MONTH_LONG, 0);
        if (monthLong > 0) {
            month.setTimeInMillis(monthLong);
        }

        // Test
        // month.set(Calendar.MONTH, 10);

        int pos = mAdapter.getClosestPositionByMonth(month);
        mPager.setCurrentItem(pos, false);
    }

    private void startWorkPagerActivity(Calendar date) {
        Intent workActivityIntent = new Intent(this, WorkPagerActivity.class);
        workActivityIntent.putExtra(Constants.DATE_LONG, date.getTimeInMillis());
        startActivity(workActivityIntent);
        finish();
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

    private void returnToMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // DONE: 2017/05/06 Aggregation Dialog

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

    private PopupMenu popupMenu;
    private void showPopupMenu() {
        popupMenu = new PopupMenu(this, menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.calendar_pager_menu, popupMenu.getMenu());

        if (mStartDay == StartDay.MONDAY) {
            popupMenu.getMenu().getItem(3).setTitle(R.string.to_sunday_start);
        } else {
            popupMenu.getMenu().getItem(3).setTitle(R.string.to_monday_start);
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.aggregation:
                        onClickAggregationMenu();
                        return true;
                    case R.id.report_mail:
                        onClickMailReport();
                        return true;
                    case R.id.add_work:
                        showAddWorkDialog();
                        return true;
                    case R.id.change_start_day:
                        onClickChangeStartDay();
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void onClickAggregationMenu() {
        // DONE: 2017/04/02 Aggregation Action
        showMonthAggregationDialog();
    }

    private void showMonthAggregationDialog() {
        MonthAggregationDialog.getInstance(mAdapter.getMonth(mPager.getCurrentItem()),
                new MonthAggregationDialog.MonthAggregationDialogListener() {
            @Override
            public void onClickMailButton(Calendar month) {
                // DONE: 2017/05/07 Mail Action
                MailReport.exportToMail(CalendarPagerActivity.this, month);
            }
        }).show(getFragmentManager(), null);
    }

    // DONE: 2017/05/09 mail action
    private void onClickMailReport() {
        MailReport.exportToMail(this, mAdapter.getMonth(mPager.getCurrentItem()));
    }

    private void loadPrefs() {
        SharedPreferences preferences
                = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        mStartDay = StartDay.valueOf(preferences.getString(Constants.SharedPrefTags.WEEK_START_DAY, StartDay.MONDAY.toString()));
    }

    private void savePrefs() {
        SharedPreferences preferences
                = getSharedPreferences(Constants.SharedPrefTags.RETURN_VISITOR_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.SharedPrefTags.WEEK_START_DAY, mStartDay.toString());
        editor.apply();
    }

    // DONE: 2017/05/07 週の開始日を切り替える
    private void onClickChangeStartDay() {
        if (mStartDay == StartDay.MONDAY) {
            mStartDay = StartDay.SUNDAY;
            popupMenu.getMenu().getItem(3).setTitle(R.string.to_sunday_start);
        } else if (mStartDay == StartDay.SUNDAY) {
            mStartDay = StartDay.MONDAY;
            popupMenu.getMenu().getItem(3).setTitle(R.string.to_monday_start);
        }

        mAdapter.notifyDataSetChanged();

    }

    // DONE: 2017/05/06 月で遷移
    // DONE: 2017/05/06 getClosestPosition




    // DONE: 2017/05/06 AdView to Real
    // DONE: 2017/05/08 Add Work
    private void showAddWorkDialog() {

        AddWorkDialog.getInstance(new AddWorkDialog.AddWorkDialogListener() {
                    @Override
                    public void onOkClick(Work work) {
                        startWorkPagerActivityWithNewWork(work);
                    }

                  @Override
                  public void onCloseDialog() {
                        InputUtil.hideSoftKeyboard(CalendarPagerActivity.this);
                  }
              }, true,
                Calendar.getInstance()).show(getFragmentManager(), null);
    }

    private void startWorkPagerActivityWithNewWork(Work work) {

        WorkList.getInstance().setOrAdd(work);

        RVCloudSync.getInstance().requestDataSyncIfLoggedIn(this);

        Intent withNewWorkIntent = new Intent(this, WorkPagerActivity.class);
        withNewWorkIntent.setAction(Constants.WorkPagerActivityActions.START_WITH_NEW_WORK);
        withNewWorkIntent.putExtra(Work.WORK, work.getId());
        startActivity(withNewWorkIntent);

        finish();
    }

    private class CalendarPagerAdapter extends FragmentStatePagerAdapter {

        public CalendarPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarFragment.newInstance(getMonth(position),
                    mStartDay,
                    new CalendarFragment.CalendarCellListener() {
                        @Override
                        public void onTouch(Calendar date) {
                            startWorkPagerActivity(date);
                        }
                    });
        }

        @Override
        public int getCount() {
            return RVDBHelper.getInstance().getMonthsWithData().size();
        }

        private Calendar getMonth(int position) {
            return RVDBHelper.getInstance().getMonthsWithData().get(position);
        }

        private int getClosestPositionByMonth(Calendar month) {

            int pos;
            // 指定した月が存在するればそのindexを返す。
            pos = getPositionByMonth(month);
            if (pos >= 0) {
                return pos;
            }

            // 存在しなければ
            Calendar forwardCal, backwardCal;
            forwardCal = (Calendar) month.clone();
            backwardCal = (Calendar) month.clone();

            while (true) {

                forwardCal.add(Calendar.MONTH, 1);
                int forwardPos = getPositionByMonth(forwardCal);
                if (forwardPos >= 0) {
                    return forwardPos;
                }

                backwardCal.add(Calendar.MONTH, -1);
                int backwardPos = getPositionByMonth(backwardCal);
                if (backwardPos >= 0) {
                    return backwardPos;
                }

                if (backwardCal.get(Calendar.YEAR) <= 2000) {
                    return -1;
                }
            }
        }

        private int getPositionByMonth(Calendar month) {
            ArrayList<Calendar> monthsWithData = RVDBHelper.getInstance().getMonthsWithData();

            for ( int i = 0 ; i < monthsWithData.size() ; i++ ) {
                if (CalendarUtil.isSameMonth(month, monthsWithData.get(i))) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
 }
