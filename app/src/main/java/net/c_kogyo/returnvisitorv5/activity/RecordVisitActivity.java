package net.c_kogyo.returnvisitorv5.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.dialogcontents.PersonDialog;
import net.c_kogyo.returnvisitorv5.service.FetchAddressIntentService;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
import net.c_kogyo.returnvisitorv5.view.ClearEditText;
import net.c_kogyo.returnvisitorv5.view.VisitDetailView;

import java.util.ArrayList;

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
        initTwoButtonsFrame();
        initAddPersonButton();
        initVisitDetailFrame();
        initDialogOverlay();
        initCancelButton();
        initBroadcastManager();
        inquireAddress();
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
                placeNameText.changeViewHeight(BaseAnimateView.AnimateCondition.FROM_0_TO_EX_HEIGHT, true, null, null);
            }
        });
    }

    private ClearEditText placeNameText;
    private void initPlaceNameText() {
        placeNameText = (ClearEditText) findViewById(R.id.place_name_text_view);
    }

    private RelativeLayout twoButtonsFrame;
    private void initTwoButtonsFrame() {
        twoButtonsFrame = (RelativeLayout) findViewById(R.id.two_buttons_frame);

        initVisitDetailButton();
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

        PersonDialog personDialog = new PersonDialog(this, new Person(mPlace.getId()), new PersonDialog.OnPersonEditFinishListener() {
            @Override
            public void onFinishEdit(final Person person) {

                hideSoftKeyboard();
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

    private Button cancelButton;
    private void initCancelButton(){
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
                                BaseAnimateView.InitialHeightCondition.EXTRACT_POST_DRAWN);
        visitDetailFrame.addView(detailView);
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
