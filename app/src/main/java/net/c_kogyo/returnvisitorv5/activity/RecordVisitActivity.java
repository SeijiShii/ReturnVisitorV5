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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.dialogcontents.HousingComplexDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.PersonDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.PlacementDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.SetPlaceDialog;
import net.c_kogyo.returnvisitorv5.dialogcontents.TagDialog;
import net.c_kogyo.returnvisitorv5.service.FetchAddressIntentService;
import net.c_kogyo.returnvisitorv5.util.DateTimeText;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;
import net.c_kogyo.returnvisitorv5.view.ClearEditText;
import net.c_kogyo.returnvisitorv5.view.PlacementCell;
import net.c_kogyo.returnvisitorv5.view.PriorityRater;
import net.c_kogyo.returnvisitorv5.view.VisitDetailView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.EDIT_VISIT_ACTION;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_HOUSE_ACTION;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_VISIT_ACTION_NO_PLACE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_VISIT_ACTION_NO_PLACE_WITH_DATE;
import static net.c_kogyo.returnvisitorv5.activity.Constants.RecordVisitActions.NEW_VISIT_ACTION_WITH_PLACE;
import static net.c_kogyo.returnvisitorv5.data.Place.PLACE;
import static net.c_kogyo.returnvisitorv5.data.Visit.VISIT;

/**
 * Created by SeijiShii on 2017/02/16.
 */

public class RecordVisitActivity extends AppCompatActivity {

    // DONE: 2017/03/24 会えた会えないがうまく反映されていない
    // DONE: 2017/04/05 logoButton

    private Place mPlace;
    private Visit mVisit;
    private ArrayList<Person> mAddedPersons;
    private ArrayList<Person> mRemovedPersons;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initBroadcastManager();

        setContentView(R.layout.record_visit_activity);

        initMapViewForDialog(savedInstanceState);

        initAddressText();
        initPlaceNameText();
        initDateText();
        initTimeText();
        initAddPersonButton();

        initRecordPlacementButton();
        initPlacementContainer();

        initVisitDetailFrame();
        initDialogOverlay();
        initOkButton();
        initCancelButton();
        initDeleteButton();
        initPriorityRater();

        initScrollView();
        initLogoButton();
    }

    private void initData() {

        mAddedPersons = new ArrayList<>();
        mRemovedPersons = new ArrayList<>();

        Intent intent = getIntent();
        switch (intent.getAction()) {
            case NEW_HOUSE_ACTION:

                double lat = intent.getDoubleExtra(Constants.LATITUDE, 0);
                double lng = intent.getDoubleExtra(Constants.LONGITUDE, 0);

                mPlace = new Place(new LatLng(lat, lng), Place.Category.HOUSE);
                mVisit = new Visit(mPlace);
                mVisit.setPlaceId(mPlace.getId());

                break;

            case EDIT_VISIT_ACTION:
                String visitId = intent.getStringExtra(VISIT);

                Visit visit = RVData.getInstance().visitList.getById(visitId);
                Place place = RVData.getInstance().placeList.getById(visit.getPlaceId());

                try {
                    mVisit = (Visit) visit.clone();
                    mPlace = (Place) place.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                // DONE: 2017/03/16 Edit Visit で遷移してきたときの UI描画

                break;
            case NEW_VISIT_ACTION_WITH_PLACE:
                String placeId = intent.getStringExtra(PLACE);
                Place place1 = RVData.getInstance().placeList.getById(placeId);
                try {
                    mPlace = (Place) place1.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                Visit lastVisit = RVData.getInstance().visitList.getLatestVisitToPlace(placeId);
                if (lastVisit != null) {
                    mVisit = new Visit(lastVisit);
                } else {
                    mVisit = new Visit(mPlace);
                }
                break;

            case NEW_VISIT_ACTION_NO_PLACE:

                mVisit = new Visit();

                // Place = null;

                break;

            case NEW_VISIT_ACTION_NO_PLACE_WITH_DATE:

                mVisit = new Visit();
                long dLong = getIntent().getLongExtra(Constants.DATE_LONG, 0);
                if (dLong > 0) {
                    mVisit.getDatetime().setTimeInMillis(dLong);
                }
                break;
        }
    }

    private TextView addressText;
    private void initAddressText() {
        addressText = (TextView) findViewById(R.id.address_text_view);
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlace == null) {
                    onSetPlaceClick();
                } else {
                    placeNameText.extract();
                }
            }
        });

        if (mPlace == null) {
            addressText.setHint(R.string.set_place);

        } else {
            refreshAddressText();
        }
    }

    private void refreshAddressText() {
        if (mPlace.getAddress() != null && !mPlace.getAddress().equals("")) {
            addressText.setText(mPlace.getAddress());
        } else {
            inquireAddress();
        }
    }

    private ClearEditText placeNameText;
    private void initPlaceNameText() {
        placeNameText = (ClearEditText) findViewById(R.id.place_name_text_view);
        if (mPlace != null) {
           refreshPlaceNameText();
        }
    }

    private void refreshPlaceNameText() {
        if (mPlace.getName() != null && !mPlace.getName().equals("")) {
            placeNameText.setText(mPlace.getName());
            placeNameText.extract();
        }
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
        timeText.setText(DateTimeText.getTimeText(mVisit.getDatetime(), false));
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(RecordVisitActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                mVisit.getDatetime().set(Calendar.HOUR_OF_DAY, i);
                                mVisit.getDatetime().set(Calendar.MINUTE, i1);

                                timeText.setText(DateTimeText.getTimeText(mVisit.getDatetime(), false));
                            }
                        },
                        mVisit.getDatetime().get(Calendar.HOUR_OF_DAY),
                        mVisit.getDatetime().get(Calendar.MINUTE),
                        true).show();
            }
        });
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
                        mAddedPersons.add(person);
                        final VisitDetail visitDetail = new VisitDetail(person.getId(), mVisit.getId());
                        // 新しく人を追加したということは会えたということでしょう。
                        visitDetail.setSeen(true);
                        mVisit.addVisitDetail(visitDetail);
                        fadeDialogOverlay(false, new DialogPostAnimationListener() {
                            @Override
                            public void onFinishAnimation() {
                                // Person Dialogが消えたら実行するアニメーション
                                addVisitDetailView(visitDetail,
                                        person,
                                        VisitDetailView.DrawCondition.EXTRACT_POST_DRAWN_FROM_0);
                            }
                        });
                    }

                    @Override
                    public void onCancelClick() {
                        fadeDialogOverlay(false, null);
                    }

                    @Override
                    public void onDeleteClick(Person person) {
                        // Newの時は削除ボタンが表示されないので呼ばれることはない
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

        hideSoftKeyboard();

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
                    mPlace.setAddress(address);
                    addressText.setText(mPlace.getAddress());
                    break;
            }

        }
    };

    private void initBroadcastManager() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(receiver, new IntentFilter(FetchAddressIntentService.SEND_FETCED_ADDRESS_ACTION));
    }

    private void inquireAddress() {

        if (!mPlace.getAddress().equals("") && mPlace.getAddress() != null) return;

        double lat, lng;

        if (mPlace.getLatLng() == null) {
            lat = getIntent().getDoubleExtra(Constants.LATITUDE, 1000);
            lng = getIntent().getDoubleExtra(Constants.LONGITUDE, 1000);
            if (lat >= 1000 & lng >= 1000) return;

        } else {
            lat = mPlace.getLatLng().latitude;
            lng = mPlace.getLatLng().longitude;
        }

        Intent addressServiceIntent = new Intent(this, FetchAddressIntentService.class);

        addressServiceIntent.putExtra(Constants.LATITUDE, lat);
        addressServiceIntent.putExtra(Constants.LONGITUDE, lng);
        addressServiceIntent.putExtra(FetchAddressIntentService.IS_USING_MAP_LOCALE, true);

        startService(addressServiceIntent);
    }

    private LinearLayout visitDetailFrame;
    private void initVisitDetailFrame() {
        visitDetailFrame = (LinearLayout) findViewById(R.id.visit_detail_frame);
        for (VisitDetail visitDetail : mVisit.getVisitDetails()) {
            Person person = RVData.getInstance().personList.getById(visitDetail.getPersonId());
            if (person != null) {
                addVisitDetailView(visitDetail,
                        person,
                        VisitDetailView.DrawCondition.COLLAPSE);
            }
        }
    }

    private void addVisitDetailView(VisitDetail visitDetail,
                                    Person person,
                                    final VisitDetailView.DrawCondition condition){

        // Personが削除されていたら表示しない
        if (person == null) return;

        VisitDetailView detailView
                = new VisitDetailView(this,
                visitDetail,
                person,
                condition,
                new VisitDetailView.VisitDetailViewListener() {
                    @Override
                    public void onPrioritySet(Visit.Priority priority) {
                        mVisit.refreshPriority();
                        visitPriorityRater.setPriority(mVisit.getPriority());
                    }

                    @Override
                    public void onEditPersonClick(Person person) {
                        showPersonDialogForEdit(person);
                    }

                    @Override
                    public void onTagButtonClick(VisitDetail visitDetail1) {
                        showTagDialog(visitDetail1);
                    }

                    @Override
                    public void onPlacementButtonClick(VisitDetail visitDetail) {
                showPlacementDialog(visitDetail.getId());
            }

                    @Override
                    public void postExtractView(VisitDetailView visitDetailView) {
                        if (condition == VisitDetailView.DrawCondition.EXTRACT_POST_DRAWN_FROM_0) {
                            ViewUtil.scrollToView(scrollView, visitDetailView);
                        }
                    }
                });
        visitDetailFrame.addView(detailView);
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

                    @Override
                    public void onDeleteClick(Person person) {
                        fadeDialogOverlay(false, null);
                        // DONE: 2017/03/08 削除動作テスト 削除時のUIの動きを実装
                        VisitDetail visitDetail = mVisit.getVisitDetail(person);
                        if (visitDetail == null) return;

                        removeVisitDetailView(visitDetail);
                        mVisit.getVisitDetails().remove(visitDetail);
                        mRemovedPersons.add(person);
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

    private void removeVisitDetailView(VisitDetail visitDetail) {
        final VisitDetailView detailView = getVisitDetailView(visitDetail);
        if (detailView == null) return;

        detailView.changeViewHeight(0, true, null, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                visitDetailFrame.removeView(detailView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private PriorityRater visitPriorityRater;
    private void initPriorityRater() {
        visitPriorityRater = (PriorityRater) findViewById(R.id.visit_priority_rater);
        visitPriorityRater.setPriority(mVisit.getPriority());
        visitPriorityRater.setOnPrioritySetListener(new PriorityRater.OnPrioritySetListener() {
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

                RVData.getInstance().visitList.setOrAdd(mVisit);
                RVData.getInstance().personList.addList(mAddedPersons);
                RVData.getInstance().personList.removeList(mRemovedPersons);

                if (mPlace != null) {
                    mPlace.setName(placeNameText.getText());
                    RVData.getInstance().placeList.setOrAdd(mPlace);
                }

                // PENDING: 2017/03/08 要動作検証 noteがAutoCompListについかされたかどうか
                for (VisitDetail visitDetail : mVisit.getVisitDetails()) {
                    RVData.getInstance().noteCompList.addIfNoSameName(visitDetail.getNote());
                }

                switch (getIntent().getAction()) {
                    case NEW_HOUSE_ACTION:
                    case NEW_VISIT_ACTION_WITH_PLACE:
                    case NEW_VISIT_ACTION_NO_PLACE:
                    case NEW_VISIT_ACTION_NO_PLACE_WITH_DATE:

                        Intent newPlaceReturnIntent = new Intent();
                        newPlaceReturnIntent.putExtra(VISIT, mVisit.getId());
                        setResult(Constants.RecordVisitActions.VISIT_ADDED_RESULT_CODE, newPlaceReturnIntent);

                        break;

                    case EDIT_VISIT_ACTION:
                        // DONE: 2017/03/16 MapActivityへの戻り処理

                        Intent editVisitReturnIntent = new Intent();
                        editVisitReturnIntent.putExtra(VISIT, mVisit.getId());
                        setResult(Constants.RecordVisitActions.VISIT_EDITED_RESULT_CODE, editVisitReturnIntent);

                        break;
                }

                RVData.getInstance().saveData(getApplicationContext(), null);
                finish();

            }
        });
    }

    private Button cancelButton;
    private void initCancelButton(){
        // DONE: 2017/03/24 キャンセル時も本データが変更されてしまうのはクローンして編集していないから
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
        if (RVData.getInstance().visitList.contains(mVisit)) {
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

    private void showPlacementDialog(String parentId) {

        PlacementDialog placementDialog = new PlacementDialog(this, parentId);
        placementDialog.setOnButtonClickListener(new PlacementDialog.OnButtonClickListener() {
            @Override
            public void onCancelClick() {
                fadeDialogOverlay(false, null);
            }

            @Override
            public void onOkClick(Placement placement, String parentId) {

                addPlacement(placement, parentId);
                addPlacementCell(placement, parentId, false);
                fadeDialogOverlay(false, null);
            }
        });
        dialogFrame.addView(placementDialog);
        fadeDialogOverlay(true, null);

    }

    private void addPlacement(Placement placement, String parentId) {
        if (parentId.equals(mVisit.getId())) {
            // Visitに配布物を追加する場合
            mVisit.addPlacement(placement);

        } else {
            // VisitDetailに追加する場合

            for (VisitDetail visitDetail : mVisit.getVisitDetails()) {

                if (visitDetail.getId().equals(parentId)) {
                    visitDetail.getPlacements().add(placement);
                }
            }
        }
    }

    private void addPlacementCell(Placement placement, String parentId, boolean extracted){

        if (parentId.equals(mVisit.getId())) {
            // Visitに配布物を追加する場合
            PlacementCell placementCell = new PlacementCell(this,
                    placement,
                    extracted,
                    new PlacementCell.PlacementCellListener() {
                @Override
                public void postExtract(PlacementCell cell) {

                }

                @Override
                public void postCompress(PlacementCell cell) {

                    mVisit.getPlacements().remove(cell.getPlacement());
                    placementContainer.removeView(cell);
                }
            });
            placementContainer.addView(placementCell);

        } else {
            // VisitDetailに追加する場合

            for (VisitDetail visitDetail : mVisit.getVisitDetails()) {

                if (visitDetail.getId().equals(parentId)) {
                    VisitDetailView visitDetailView = getVisitDetailView(visitDetail);
                    if (visitDetailView != null) {
                        visitDetailView.addPlacementCell(placement, extracted);
                    }
                }
            }
        }

    }

    private void initRecordPlacementButton() {
        Button recordPlcButton = (Button) findViewById(R.id.record_placement_button);
        recordPlcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlacementDialog(mVisit.getId());
            }
        });
    }

    private LinearLayout placementContainer;
    private void initPlacementContainer() {
        placementContainer = (LinearLayout) findViewById(R.id.placement_container);
        for (Placement placement : mVisit.getPlacements()) {
            addPlacementCell(placement, mVisit.getId(), true);
        }
    }

    private ScrollView scrollView;
    private void initScrollView() {
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
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

    // DONE: 2017/05/08 場所無し訪問機能
    private void onSetPlaceClick() {
        showSetPlaceDialog();
    }

    private MapView mMapView;
    private void initMapViewForDialog(Bundle bundle) {
        mMapView = new MapView(this);
        mMapView.onCreate(bundle);
    }

    private void showSetPlaceDialog() {
        final SetPlaceDialog setPlaceDialog = new SetPlaceDialog(this,
                new SetPlaceDialog.SetPlaceDialogListener() {
                    @Override
                    public void onSetPlace(Place place) {
                        if (place.getCategory() == Place.Category.HOUSE) {
                            setPlace(place);
                            fadeDialogOverlay(false, null);
                        } else if (place.getCategory() == Place.Category.HOUSING_COMPLEX) {
                            onHousingComplexSelected(place);
                        }
                    }

                    @Override
                    public void onSetLatLng(LatLng latLng) {
                        fadeDialogOverlay(false, null);
                        setPlace(new Place(latLng, Place.Category.HOUSE));
                    }

                    @Override
                    public void onCancel() {
                        fadeDialogOverlay(false, null);
                    }
                },
                mMapView);
        dialogFrame.addView(setPlaceDialog);
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fadeDialogOverlay(false, null);
                return true;
            }
        });
        fadeDialogOverlay(true, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    private void setPlace(Place place) {
        mPlace = place;
        refreshAddressText();
        refreshPlaceNameText();

        Calendar oldDateTime = (Calendar) mVisit.getDatetime().clone();

        Visit lastVisit = RVData.getInstance().visitList.getLatestVisitToPlace(place.getId());
        if (lastVisit != null) {
            mVisit = new Visit(lastVisit);

            for (VisitDetail visitDetail : mVisit.getVisitDetails()) {
                Person person = RVData.getInstance().personList.getById(visitDetail.getPersonId());
                if (person != null) {
                    addVisitDetailView(visitDetail,
                            person,
                            VisitDetailView.DrawCondition.EXTRACT_POST_DRAWN_FROM_0);
                }
            }

            visitPriorityRater.setPriority(mVisit.getPriority());
        }
        mVisit.setDatetime(oldDateTime);


    }

    private void onHousingComplexSelected(final Place housingComplex) {
        fadeDialogOverlay(false, new DialogPostAnimationListener() {
            @Override
            public void onFinishAnimation() {
                showHousingComplexDialog(housingComplex);
            }
        });
    }

    private void showHousingComplexDialog(Place housingComplex) {

        HousingComplexDialog housingComplexDialog
                = new HousingComplexDialog(this,
                housingComplex,
                new HousingComplexDialog.HousingComplexDialogListener() {
                    @Override
                    public void onClickAddRoomButton(Place newRoom) {
                        fadeDialogOverlay(false, null);
                        setPlace(newRoom);
                    }

                    @Override
                    public void onClickRoomCell(final Place room) {
                        fadeDialogOverlay(false, null);
                        setPlace(room);
                    }

                    @Override
                    public void onClickOkButton(Place housingComplex) {
//
                    }

                    @Override
                    public void onClickCancelButton() {
                        fadeDialogOverlay(false, null);
                    }

                    @Override
                    public void onDeleteHousingComplex(Place housingComplex) {

                    }
                }, false, false);
        dialogFrame.addView(housingComplexDialog);
        fadeDialogOverlay(true, null);
        dialogOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                fadeDialogOverlay(false, null);
                return true;
            }
        });

    }

    // DONE: 2017/03/26 PriorityRaterの挙動がいまいち
    // DONE: 2017/04/05 Activity起動時にスクロール位置が一番上にない
    // TODO: 2017/05/06 再訪問時に人visit detailが閉じているようにしたい。


}
