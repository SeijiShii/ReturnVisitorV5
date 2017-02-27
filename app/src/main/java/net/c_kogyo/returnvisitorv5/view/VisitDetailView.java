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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class VisitDetailView extends BaseAnimateView {

    private VisitDetail mVisitDetail;
    private Person mPerson;
    public VisitDetailView(Context context, VisitDetail visitDetail, Person person, InitialHeightCondition condition) {
        super(context, 700, condition, R.layout.visit_detail_view);
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
        initPriorityFrame();
        initPriorityText();
        initNoteText();
        initRVSwitch();
        initStudySwitch();

        // TODO: exHeightをセットする


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
    
    private RelativeLayout priorityFrame;
    private void initPriorityFrame() {
        priorityFrame = (RelativeLayout) getViewById(R.id.priority_frame);
        // TODO: 2017/02/27 Implement priorityFrame
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
    
}
