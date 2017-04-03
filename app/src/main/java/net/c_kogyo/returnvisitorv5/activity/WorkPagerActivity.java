package net.c_kogyo.returnvisitorv5.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.AggregationDay;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.fragment.WorkFragment;
import net.c_kogyo.returnvisitorv5.util.AdMobHelper;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.util.ViewUtil.setOnClickListener;

/**
 * Created by SeijiShii on 2016/09/17.
 */

public class WorkPagerActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.work_pager_activity);

        AdMobHelper.setAdView(this);

        initHeaderRow();
        initPager();
        initButtons();

    }

    private ViewPager pager;
    private DatePagerAdapter datePagerAdapter;
    private void initPager() {

        pager = (ViewPager) findViewById(R.id.view_pager);

        Calendar date = getDate();
        datePagerAdapter = new DatePagerAdapter(getSupportFragmentManager());

        pager.setAdapter(datePagerAdapter);
        pager.setCurrentItem(datePagerAdapter.getClosestPosition(date));

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                refreshButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    // 日付指定で遷移するためのプロセス あったわ…
    private Calendar getDate() {

        Calendar date = Calendar.getInstance();

        long dLong = getIntent().getLongExtra(Constants.DATE_LONG, 0);
        if (dLong != 0) {
            date.setTimeInMillis(dLong);
        }
        return date;

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
            public void onViewClick() {
                onClickLeftButton();
            }
        });
        refreshLeftButton();
    }

    private void onClickLeftButton() {
        pager.setCurrentItem(pager.getCurrentItem() - 1, true);
        refreshButtons();
    }

    private void refreshLeftButton() {
        if (pager.getCurrentItem() > 0) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setClickable(true);
        } else {
            leftButton.setVisibility(View.INVISIBLE);
            leftButton.setClickable(false);
        }
    }

    private ImageView rightButton;
    private void initRightButton() {
        rightButton = (ImageView) findViewById(R.id.right_button);
        setOnClickListener(rightButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                onClickRightButton();
            }
        });
        refreshRightButton();
    }

    private void onClickRightButton() {
        pager.setCurrentItem(pager.getCurrentItem() + 1, true);
        refreshButtons();
    }

    private void refreshRightButton() {
        if (pager.getCurrentItem() < datePagerAdapter.getCount() - 1) {
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setClickable(true);
        } else {
            rightButton.setVisibility(View.INVISIBLE);
            rightButton.setClickable(false);
        }
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) findViewById(R.id.date_text);
        setOnClickListener(dateText, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                onClickDateText();
            }
        });
        refreshDateText();
    }

    private void onClickDateText() {

    }

    private void refreshDateText() {
        DateFormat format = android.text.format.DateFormat.getDateFormat(this);
        String dateString = format.format(datePagerAdapter.getDay(pager.getCurrentItem()).getDate().getTime());

        dateText.setText(dateString);
    }

    private void initButtons() {

        initLeftButton();
        initRightButton();
        initDateText();
        initMenuButton();
        initLogoButton();
     }

    private void refreshButtons() {

        refreshLeftButton();
        refreshRightButton();
        refreshDateText();
    }

    private ImageView menuButton;
    private void initMenuButton() {
        menuButton = (ImageView) findViewById(R.id.work_menu_button);
        setOnClickListener(menuButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {
                showPopupMenu();
            }
        });
    }

    private void initLogoButton() {
        ImageView logoButton = (ImageView) findViewById(R.id.logo_button);
        setOnClickListener(logoButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick() {

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
                    case R.id.record_visit:
                        onClickRecordVisitMenu();
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
        // TODO: 2017/04/02 Add Work Action
    }

    private void onClickRecordVisitMenu() {
        // TODO: 2017/04/02 Record Visit Action with no Place
    }

    private void onClickAggregationMenu() {
        // TODO: 2017/04/02 Aggregation Action
    }

//        dateText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {

//                CalendarDialog.newInstance(datePagerAdapter.getDate(pager.getCurrentItem())).show(getFragmentManager(), null);
//                Intent calendarIntent = new Intent(WorkPagerActivity.this, CalendarActivity.class);
//                calendarIntent.setAction(Constants.CalendarActions.START_CALENDAR_FROM_WORK_ACTION);
//                calendarIntent.putExtra(Constants.DATE_LONG,
//                        datePagerAdapter.getDay(pager.getCurrentItem()).getDate().getTimeInMillis());
//
//                WorkPagerActivity.this.startActivityForResult(calendarIntent, Constants.CalendarActions.START_CALENDAR_REQUEST_CODE);


//                Calendar date = datePagerAdapter.getDate(pager.getCurrentItem());
//                new DatePickerDialog(WorkPagerActivity.this, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
//
//                        Calendar daySet = Calendar.getInstance();
//                        daySet.set(Calendar.YEAR, i);
//                        daySet.set(Calendar.MONTH, i1);
//                        daySet.set(Calendar.DAY_OF_MONTH, i2);
//
//                        if (datePagerAdapter.containsDate(daySet)) {
//
//                            pager.setCurrentItem(datePagerAdapter.getPosition(daySet), true);
//                            refreshButtons();
//                        }
//                    }
//                }, date.get(Calendar.YEAR),
//                        date.get(Calendar.MONTH),
//                        date.get(Calendar.DAY_OF_MONTH)).show();

//            }
//        });



//    private void initAddButton() {
//
//        Button addButton = (Button) findViewById(R.id.add_button);
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                AddSelectDialog.newInstance(datePagerAdapter.getDay(pager.getCurrentItem()).getDate(),
//                        new AddWorkDialog.OnWorkSetListener() {
//                    @Override
//                    public void onWorkSet(Work work) {
//
//                        // ここがUIの一番浅い場所なので原初データをいじる
//                        RVData.getInstance().workList.addOrSet(work);
//                        RVData.getInstance().workList.onChangeTime(work);
//
//                        pager.setCurrentItem(datePagerAdapter.onAddWork(work), true);
//                        refreshButtons();
//
//                        WorkFragment fragment = (WorkFragment) datePagerAdapter.instantiateItem(pager, datePagerAdapter.getPosition(work.getStart()));
//                        fragment.addWorkViewAndExtract(work);
//
//                    }
//                }).show(getFragmentManager(), null);
//
//            }
//        });
//
//    }


    class DatePagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<AggregationDay> mAggregationOfDays;

        public DatePagerAdapter(FragmentManager fm) {
            super(fm);

            setDays();
        }

        @Override
        public Fragment getItem(int position) {

            return WorkFragment.newInstance(mAggregationOfDays.get(position).getDate(),
                    new WorkFragment.WorkFragmentListener() {
                @Override
                public void onAllItemRemoved(Calendar date) {
                    removeDay(date);
                }
            });
        }

        @Override
        public int getCount() {
            return mAggregationOfDays.size();
        }

        public int getPosition(Calendar date) {

            for ( int i = 0 ; i < mAggregationOfDays.size() ; i++ ) {

                AggregationDay day = mAggregationOfDays.get(i);

                if (CalendarUtil.isSameDay(date, day.getDate())) {
                    return i;
                }
            }

            return -1;
        }

        private void setDays() {

            mAggregationOfDays = RVData.getInstance().getAggregatedDays();
        }

        public AggregationDay getDay(int pos) {
            return mAggregationOfDays.get(pos);
        }


        public int onAddWork(Work work) {

            // Workが追加された時点ですでにmDatesにある日付かどうか
            int datePos = getPosition(work.getStart());

            if (datePos >= 0) {
                // 日付がすでにある

            } else {
                // 日付が存在しない(その日にはまだ何のデータもなかった)
                // この日には削除されるWorkも存在しない
                setDays();

                // 気を取り直して…
                datePos = getPosition(work.getStart());
                notifyDataSetChanged();
            }

            return datePos;
        }

        public int getClosestPosition(Calendar date) {

            if (getCount() <= 0) return 0;

            if (getPosition(date) >= 0) {
                return getPosition(date);
            }

            Calendar dateFuture = (Calendar) date.clone();
            Calendar datePast = (Calendar) date.clone();

            while (true) {

                dateFuture.add(Calendar.DAY_OF_MONTH, 1);
                datePast.add(Calendar.DAY_OF_MONTH, -1);

                if (getPosition(datePast) >= 0) {
                    return getPosition(datePast);
                }

                if (getPosition(dateFuture) >= 0) {
                    return getPosition(dateFuture);
                }

            }
        }

        private void removeDay(Calendar date) {

            for (AggregationDay day : mAggregationOfDays) {
                if (CalendarUtil.isSameDay(date, day.getDate())){
                    mAggregationOfDays.remove(day);
                    break;
                }
            }
            notifyDataSetChanged();
            pager.setCurrentItem(getClosestPosition(date), true);
            refreshButtons();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


}
