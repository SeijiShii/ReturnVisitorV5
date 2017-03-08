package net.c_kogyo.returnvisitorv5.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.dialogcontents.PersonDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.TagDialog;
import net.c_kogyo.returnvisitorv5.service.FetchAddressIntentService;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
import net.c_kogyo.returnvisitorv5.view.ClearEditText;
import net.c_kogyo.returnvisitorv5.view.PriorityRater;
import net.c_kogyo.returnvisitorv5.view.VisitDetailView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/02/16.
 */

public class RecordVisitActivity extends AppCompatActivity {

    private Place mPlace;
    private Visit mVisit;
    private ArrayList<Person> mPersons;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.record_visit_activity);

        initAddressText();
        initPlaceNameText();
        initDateText();
        initTimeText();
        initTwoButtonsFrame();
        initAddPersonButton();
        initVisitDetailFrame();
        initDialogOverlay();
        initOkButton();
        initCancelButton();
        initDeleteButton();
        initBroadcastManager();
        inquireAddress();
        initPriorityRater();
    }

    private void initData() {

        mPersons = new ArrayList<>();

        Intent intent = getIntent();
        switch (intent.getAction()) {
            case Constants.RecordVisitActions.NEW_PLACE_ACTION:

                double lat = intent.getDoubleExtra(Constants.LATITUDE, 0);
                double lng = intent.getDoubleExtra(Constants.LONGITUDE, 0);

                mPlace = new Place(new LatLng(lat, lng));
                mVisit = new Visit();
                mVisit.setPlaceId(mPlace.getId());

                break;
        }
    }

    private TextView addressText;
    private void initAddressText() {
        addressText = (TextView) findViewById(R.id.address_text_view);
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeNameText.changeViewHeight(BaseAnimateView.AnimateCondition.FROM_0_TO_EX_HEIGHT, 0, true, null, null);
            }
        });
    }

    private ClearEditText placeNameText;
    private void initPlaceNameText() {
        placeNameText = (ClearEditText) findViewById(R.id.place_name_text_view);
    }

    private TextView dateText;
    private void initDateText() {
        dateText = (TextView) findViewById(R.id.date_text_view);

        final DateFormat format = android.text.format.DateFormat.getMediumDateFormat(this);
        dateText.setText(format.format(mVisit.getDatetime().getTime()));
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(RecordVisitActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                mVisit.getDatetime().set(Calendar.YEAR, i);
                                mVisit.getDatetime().set(Calendar.MONTH, i1);
                                mVisit.getDatetime().set(Calendar.DAY_OF_MONTH, i2);

                                dateText.setText(format.format(mVisit.getDatetime().getTime()));
                            }
                        },
                        mVisit.getDatetime().get(Calendar.YEAR),
                        mVisit.getDatetime().get(Calendar.MONTH),
                        mVisit.getDatetime().get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private TextView timeText;
    private void initTimeText() {
        timeText = (TextView) findViewById(R.id.time_text_view);
        timeText.setText(DateTimeText.getTimeText(mVisit.getDatetime()));
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(RecordVisitActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                mVisit.getDatetime().set(Calendar.HOUR_OF_DAY, i);
                                mVisit.getDatetime().set(Calendar.MINUTE, i1);

                                timeText.setText(DateTimeText.getTimeText(mVisit.getDatetime()));
                            }
                        },
                        mVisit.getDatetime().get(Calendar.HOUR_OF_DAY),
                        mVisit.getDatetime().get(Calendar.MINUTE),
                        true).show();
            }
        });
    }

    private LinearLayout twoButtonsFrame;
    private void initTwoButtonsFrame() {
        twoButtonsFrame = (LinearLayout) findViewById(R.id.two_buttons_frame);
        // オーバーレイをタッチが透過するのを防ぐ
        twoButtonsFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        initVisitDetailButton();
        initNotHomeButton();
    }

    private Button visitDetailButton;
    private void initVisitDetailButton() {
        visitDetailButton = (Button) findViewById(R.id.visit_detail_button);
        visitDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutTwoButtonsFrame();
            }
        });
    }

    private Button notHomeButton;
    private void initNotHomeButton() {
        notHomeButton = (Button) findViewById(R.id.record_not_home_button);
        notHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordAsNotHome();
            }
        });
    }

    private void recordAsNotHome() {
        RVData.getInstance().getVisitList().setOrAdd(mVisit);
        RVData.getInstance().getPlaceList().setOrAdd(mPlace);
        Intent intent = new Intent();
        intent.putExtra(Visit.VISIT, mVisit.getId());
        setResult(Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE, intent);

        RVData.getInstance().saveData(null);
        finish();
    }

    private void fadeOutTwoButtonsFrame() {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                twoButtonsFrame.setAlpha((float) valueAnimator.getAnimatedValue());
                twoButtonsFrame.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                twoButtonsFrame.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    private Button addPersonButton;
    private void initAddPersonButton() {
        addPersonButton = (Button) findViewById(R.id.add_person_button);
        addPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPersonDialogForNew();
            }
        });
    }

    private void showPersonDialogForNew() {

        PersonDialog personDialog = new PersonDialog(this, new Person(mPlace.getId()),

                new PersonDialog.OnButtonsClickListener() {
                    @Override
                    public void onOkClick(final Person person) {
                        hideSoftKeyboard();
                        // とりあえずアクティビティ内のアレイリストに追加
                        mPersons.add(person);
                        final VisitDetail visitDetail = new VisitDetail(person.getId(), mVisit.getId());
                        mVisit.addVisitDetail(visitDetail);
                        fadeDialogOverlay(false, new DialogPostAnimationListener() {
                            @Override
                            public void onFinishAnimation() {
                                // Person Dialogが消えたら実行するアニメーション
                                addVisitDetailView(visitDetail, person);
                            }
                        });
                    }

                    @Override
                    public void onCancelClick() {
                        fadeDialogOverlay(false, null);
                    }
                });
        dialogFrame.addView(personDialog);
        fadeDialogOverlay(true, null);
    }

    private RelativeLayout dialogOverlay;
    private void initDialogOverlay() {
        dialogOverlay = (RelativeLayout) findViewById(R.id.dialog_overlay);

        initDialogFrame();
    }

    private FrameLayout dialogFrame;
    private void initDialogFrame() {
        dialogFrame = (FrameLayout) findViewById(R.id.dialog_frame);
    }

    private void fadeDialogOverlay(boolean isFadeIn, final DialogPostAnimationListener listener) {

        if (isFadeIn) {
            dialogOverlay.setVisibility(View.VISIBLE);

            ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0f, 1f);
            fadeInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    dialogOverlay.setAlpha((float) valueAnimator.getAnimatedValue());
                    dialogOverlay.requestLayout();
                }
            });
            fadeInAnimator.setDuration(500);

            if (listener != null) {
                fadeInAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        listener.onFinishAnimation();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }

            fadeInAnimator.start();

        } else {
            ValueAnimator fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f);
            fadeOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    dialogOverlay.setAlpha((float) valueAnimator.getAnimatedValue());
                    dialogOverlay.requestLayout();
                }
            });
            fadeOutAnimator.setDuration(500);
            fadeOutAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    dialogOverlay.setVisibility(View.INVISIBLE);
                    dialogFrame.removeAllViews();
                    if (listener == null) return;
                    listener.onFinishAnimation();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            fadeOutAnimator.start();
        }
    }

    interface DialogPostAnimationListener {
        void onFinishAnimation();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION:
                    String address = intent.getStringExtra(FetchAddressIntentService.ADDRESS_FETCHED);
                    addressText.setText(address);
                    break;
            }

        }
    };

    private void initBroadcastManager() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(receiver, new IntentFilter(FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION));
    }

    private void inquireAddress() {

        double lat = getIntent().getDoubleExtra(Constants.LATITUDE, 1000);
        double lng = getIntent().getDoubleExtra(Constants.LONGITUDE, 1000);

        if (lat >= 1000 & lng >= 1000) return;

        Intent addressServiceIntent = new Intent(this, FetchAddressIntentService.class);

        addressServiceIntent.putExtra(Constants.LATITUDE, lat);
        addressServiceIntent.putExtra(Constants.LONGITUDE, lng);
        addressServiceIntent.putExtra(FetchAddressIntentService.IS_USING_MAP_LOCALE, true);

        startService(addressServiceIntent);
    }

    private LinearLayout visitDetailFrame;
    private void initVisitDetailFrame() {
        visitDetailFrame = (LinearLayout) findViewById(R.id.visit_detail_frame);
    }

    private void addVisitDetailView(VisitDetail visitDetail, Person person){
        VisitDetailView detailView
                = new VisitDetailView(this,
                                visitDetail,
                                person,
                                BaseAnimateView.InitialHeightCondition.ZERO);
        visitDetailFrame.addView(detailView);
        detailView.setOnPersonPrioritySetListener(new VisitDetailView.OnPersonPrioritySetListener() {
            @Override
            public void onPersonPrioritySet(Visit.Priority priority) {
                mVisit.refreshPriority();
                priorityRater.setPriority(mVisit.getPriority());
            }
        });
        detailView.setOnEditPersonClickListener(new VisitDetailView.OnEditPersonClickListener() {
            @Override
            public void onEditPersonClick(Person person) {
                showPersonDialogForEdit(person);
            }
        });
        detailView.setOnTagButtonClickListener(new VisitDetailView.OnTagButtonClickListener() {
            @Override
            public void onTagButtonClick(VisitDetail visitDetail1) {
                showTagDialog(visitDetail1);
            }
        });
    }

    private void showPersonDialogForEdit(Person person) {

        PersonDialog personDialog
                = new PersonDialog(this,
                person,
                new PersonDialog.OnButtonsClickListener() {
                    @Override
                    public void onOkClick(Person person) {
                        VisitDetail visitDetail = mVisit.getVisitDetail(person);
                        if (visitDetail != null) {
                            VisitDetailView visitDetailView = getVisitDetailView(visitDetail);
                            if (visitDetailView != null) {
                                visitDetailView.refreshPersonData();
                            }
                        }
                        fadeDialogOverlay(false, null);
                    }

                    @Override
                    public void onCancelClick() {
                        fadeDialogOverlay(false, null);
                    }
                });
        dialogFrame.addView(personDialog);
        fadeDialogOverlay(true, null);
    }

    @Nullable
    private VisitDetailView getVisitDetailView(VisitDetail visitDetail) {

        for ( int i = 0 ; i < visitDetailFrame.getChildCount() ; i++ ) {

            VisitDetailView visitDetailView = (VisitDetailView) visitDetailFrame.getChildAt(i);
            if (visitDetailView.getVisitDetail().getId().equals(visitDetail.getId())) {
                return visitDetailView;
            }
        }
        return null;
    }

    private PriorityRater priorityRater;
    private void initPriorityRater() {
        priorityRater = (PriorityRater) findViewById(R.id.priority_rater);
        priorityRater.setPriority(mVisit.getPriority());
        priorityRater.setOnPrioritySetListener(new PriorityRater.OnPrioritySetListener() {
            @Override
            public void onPrioritySet(Visit.Priority priority) {
                mVisit.setPriority(priority);
            }
        });

    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private Button okButton;
    private void initOkButton() {

        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RVData.getInstance().getVisitList().setOrAdd(mVisit);
                RVData.getInstance().getPersonList().addList(mPersons);

                switch (getIntent().getAction()) {
                    case Constants.RecordVisitActions.NEW_PLACE_ACTION:

                        RVData.getInstance().getPlaceList().setOrAdd(mPlace);

                        Intent intent = new Intent();
                        intent.putExtra(Visit.VISIT, mVisit.getId());
                        setResult(Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE, intent);

                        break;
                }

                RVData.getInstance().saveData(null);
                finish();

            }
        });
    }

    private Button cancelButton;
    private void initCancelButton(){
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private Button deleteButton;
    private void initDeleteButton() {
        deleteButton = (Button) findViewById(R.id.delete_button);
        if (RVData.getInstance().getVisitList().contains(mVisit)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void showTagDialog(VisitDetail visitDetail) {
        TagDialog tagDialog = new TagDialog(this, visitDetail);
        tagDialog.setOnButtonsClickListener(new TagDialog.OnButtonsClickListener() {
            @Override
            public void onOkClick(VisitDetail visitDetail1) {

                VisitDetailView detailView = getVisitDetailView(visitDetail1);
                if (detailView != null) {
                    detailView.refreshTagFrame();
                }
                fadeDialogOverlay(false, null);
            }

            @Override
            public void onCancelClick() {
                fadeDialogOverlay(false, null);
            }
        });
        dialogFrame.addView(tagDialog);
        fadeDialogOverlay(true, null);
    }





}
