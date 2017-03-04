package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.Constants;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;

import static net.c_kogyo.returnvisitorv5.activity.Constants.buttonRes;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class VisitDetailView extends BaseAnimateView implements TagFrame.OnSetHeightCallback{

    private VisitDetail mVisitDetail;
    private Person mPerson;

    private int mExHeight, fixedHeight, noteLineHeight, mTagFrameHeight;

    public VisitDetailView(Context context,
                           VisitDetail visitDetail,
                           Person person,
                           InitialHeightCondition condition) {
        super(context, 0, condition, R.layout.visit_detail_view);
        mVisitDetail = visitDetail;
        mPerson = person;

        initCommon();
    }

    public VisitDetailView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.visit_detail_view);
    }

    private void initCommon() {

        initDataText();
        initOpenCloseButton();
        initSeenSwitch();
        initEditPersonButton();
        initPlacementButton();
        initTagButton();
        initTagFrame();
        initPriorityText();
        initPriorityFrame();
        initNoteText();
        initRVSwitch();
        initStudySwitch();

        // TODO: exHeightをセットする



        // 高さをセットするのはコールバックに譲る

    }

    private TextView dataText;
    private void initDataText() {
        dataText = (TextView) getViewById(R.id.data_text);
        dataText.setText(mPerson.toString(getContext()));
    }

    private Button openCloseButton;
    private void initOpenCloseButton() {
        openCloseButton = (Button) getViewById(R.id.open_close_button);
        openCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/02/27 開閉アニメーション
            }
        });
    }
    
    private SwitchCompat seenSwitch;
    private TextView seenText;
    private void initSeenSwitch() {
        seenSwitch = (SwitchCompat) getViewById(R.id.seen_switch);
        seenText = (TextView) getViewById(R.id.seen_text);
        seenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mVisitDetail.setSeen(b);
                if (b) {
                    seenText.setText(R.string.seen);
                    seenText.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                } else {
                    seenText.setText(R.string.not_seen);
                    seenText.setTextColor(getContext().getResources().getColor(R.color.textHintGray));

                }
            }
        });
    }

    private Button editPersonButton;
    private void initEditPersonButton() {
        editPersonButton = (Button) getViewById(R.id.edit_person_button);
        editPersonButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/02/27 人編集ダイアログへの遷移
            }
        });
    }
    
    private Button placementButton;
    private void initPlacementButton() {
        placementButton = (Button) getViewById(R.id.add_placement_button);
        placementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/02/27 placementDialogへの遷移 
            }
        });
    }
    
    private Button tagButton;
    private void initTagButton() {
        tagButton = (Button) getViewById(R.id.tag_button);
        tagButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/02/27 tagDialogへの遷移 
            }
        });
    }
    
    private LinearLayout priorityFrame;
    private Button[] raterButtons;
    private void initPriorityFrame() {
        priorityFrame = (LinearLayout) getViewById(R.id.priority_frame);

        final int raterSize = (int) (getContext().getResources().getDisplayMetrics().density * 25);
        final int buttonMargin = (int) (getContext().getResources().getDisplayMetrics().density * 15);

        // 2017/02/27 Implement priorityFrame
        priorityFrame.removeAllViews();

        raterButtons = new Button[8];
        for ( int i = 0 ; i < 7 ; i++ ) {
            raterButtons[i] = new Button(getContext());
            raterButtons[i].setBackgroundResource(buttonRes[0]);
            raterButtons[i].setTag(i);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(raterSize, raterSize);

            raterButtons[i].setLayoutParams(params);
            priorityFrame.addView(raterButtons[i]);

            if ( i < 6 ) {
                View view = new View(getContext());
                FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(buttonMargin, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(params2);
                priorityFrame.addView(view);
            }

            raterButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int tag = Integer.parseInt(view.getTag().toString());
                    Visit.Priority priority = Visit.Priority.getEnum(tag);

                    mVisitDetail.setPriority(priority);

                    refreshRater(priority);

                }
            });
        }
        refreshRater(mVisitDetail.getPriority());
    }

    private void refreshRater(Visit.Priority priority) {

        int num = priority.num();

        priorityText.setText(getResources().getStringArray(R.array.priority_array)[mVisitDetail.getPriority().num()]);

        for (int i = 0 ; i <= num ; i++) {
            raterButtons[i].setBackgroundResource(Constants.buttonRes[num]);
        }

        for (int i = num + 1 ; i < 7 ; i++ ) {
            raterButtons[i].setBackgroundResource(Constants.buttonRes[0]);
        }
    }
    
    private TextView priorityText;
    private void initPriorityText() {
        priorityText = (TextView) getViewById(R.id.priority_state_text);
        // TODO: 2017/02/27 priorityText 
    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {
        noteText = (AutoCompleteTextView) getViewById(R.id.note_text);

        // TODO: 2017/02/27 AutoCompleteTextViewアダプタ

        noteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                mExHeight = noteLineHeight * noteText.getLineCount();
                VisitDetailView.this.setExHeight(fixedHeight + mExHeight + mTagFrameHeight);
                VisitDetailView.this.changeViewHeight(AnimateCondition.TO_EX_HEIGHT, true, null, null);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mVisitDetail.setNote(editable.toString());
            }
        });
    }

    private SwitchCompat rvSwitch;
    private void initRVSwitch() {
        rvSwitch = (SwitchCompat) getViewById(R.id.rv_switch);
        rvSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mVisitDetail.setIsRv(b);
            }
        });
    }

    private SwitchCompat studySwitch;
    private void initStudySwitch() {
        studySwitch = (SwitchCompat) getViewById(R.id.study_switch);
        studySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mVisitDetail.setIsStudy(b);
            }
        });
    }

    private TagFrame tagFrame;
    private void initTagFrame() {

        tagFrame = (TagFrame) getViewById(R.id.tag_frame);
        tagFrame.setTagIdsAndInitialize(new ArrayList<String>(), null, this);
    }

    @Override
    public void onSetHeight(int frameHeight) {

        mTagFrameHeight = frameHeight;

        // タグフレームの高さが渡されたらビュー全体の高さを設定しアニメーションする。
        int rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_normal);
        fixedHeight = rowHeight * 10 + padding * 2;

        noteLineHeight = (int) (noteText.getPaint().getFontMetrics().bottom - noteText.getPaint().getFontMetrics().top);

        mExHeight = fixedHeight + noteLineHeight + mTagFrameHeight;

        this.setExHeight(mExHeight);
        this.changeViewHeight(AnimateCondition.FROM_0_TO_EX_HEIGHT, true, null, null);
    }


}
