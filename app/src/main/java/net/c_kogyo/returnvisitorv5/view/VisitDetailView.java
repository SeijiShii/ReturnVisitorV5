package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class VisitDetailView extends BaseAnimateView {

    private VisitDetail mVisitDetail;
    private Person mPerson;
    private OnPersonPrioritySetListener mPriorityListener;
    private OnEditPersonClickListener mPersonClickListener;
    private OnTagButtonClickListener mTagButtonClickListener;

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

        isViewOpen = true;

        initDataText();
        initOpenCloseButton();
        initSeenSwitch();
        initEditPersonButton();
        initPlacementButton();
        initTagButton();
        initPriorityRater();
        initNoteText();
        initRVSwitch();
        initStudySwitch();

        initTagFrame();

    }

    public void setOnPersonPrioritySetListener(OnPersonPrioritySetListener listener) {
        this.mPriorityListener = listener;
    }

    public void setOnEditPersonClickListener(OnEditPersonClickListener listener) {
        this.mPersonClickListener = listener;
    }

    public void setOnTagButtonClickListener(OnTagButtonClickListener listener) {
        this.mTagButtonClickListener = listener;
    }

    private TextView dataText;
    private void initDataText() {
        dataText = (TextView) getViewById(R.id.data_text);
        dataText.setText(mPerson.toString(getContext()));
    }

    private Button openCloseButton;
    private boolean isViewOpen;
    private void initOpenCloseButton() {
        openCloseButton = (Button) getViewById(R.id.open_close_button);
        openCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 開閉アニメーション DONE
                if (isViewOpen) {

                    int padding = (int) (getContext().getResources().getDisplayMetrics().density * 5);
                    int targetHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                            + padding;
                    VisitDetailView.this.changeViewHeight(AnimateCondition.TO_TARGET_HEIGHT, targetHeight, true, null, null);

                } else {
                    VisitDetailView.this.changeViewHeight(AnimateCondition.TO_EX_HEIGHT, 0, true, null, null);
                }

                rotateOpenCloseButton();

                isViewOpen = !isViewOpen;
            }
        });
    }

    private void rotateOpenCloseButton() {

        float originAngle, targetAngle;

        if (isViewOpen) {
            originAngle = 0f;
            targetAngle = 180f;
        } else {
            originAngle = 180f;
            targetAngle = 0f;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(openCloseButton, "rotation", originAngle, targetAngle);
        animator.setDuration(300);
        animator.start();
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
//        seenText.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                seenSwitch.;
//            }
//        });
    }

    private Button editPersonButton;
    private void initEditPersonButton() {
        editPersonButton = (Button) getViewById(R.id.edit_person_button);
        editPersonButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/02/27 人編集ダイアログへの遷移
                if (mPersonClickListener != null){
                    mPersonClickListener.onEditPersonClick(mPerson);
                }
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
                // DONE: 2017/02/27 tagDialogへの遷移
                if (mTagButtonClickListener != null) {
                    mTagButtonClickListener.onTagButtonClick(mVisitDetail);
                }
            }
        });
    }

    private PriorityRater priorityRater;
    private void initPriorityRater() {
        priorityRater = (PriorityRater) getViewById(R.id.priority_rater);
        priorityRater.setPriority(mVisitDetail.getPriority());
        priorityRater.setOnPrioritySetListener(new PriorityRater.OnPrioritySetListener() {
            @Override
            public void onPrioritySet(Visit.Priority priority) {
                mVisitDetail.setPriority(priority);
                if (mPriorityListener != null) {
                    mPriorityListener.onPersonPrioritySet(priority);
                }
            }
        });
    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {
        noteText = (AutoCompleteTextView) getViewById(R.id.note_text);

        // DONE: 2017/02/27 AutoCompleteTextViewアダプタ
        // TODO: 2017/03/08 要動作検証

        ArrayList<String> pubNameList = new ArrayList<>();
        for (Publication pub : RVData.getInstance().getPubList()){
            pubNameList.add(pub.getName());
        }
        String[] pubArray = pubNameList.toArray(new String[0]);

        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, pubArray);
        noteText.setAdapter(adapter);

        noteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                mExHeight = noteLineHeight * noteText.getLineCount();
                VisitDetailView.this.setExHeight(fixedHeight + mExHeight + mTagFrameHeight);
                VisitDetailView.this.changeViewHeight(AnimateCondition.TO_EX_HEIGHT, 0, true, null, null);

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
        tagFrame.setTagIdsAndInitialize(mVisitDetail.getTagIds(),
                new TagFrame.TagFrameCallback() {
            @Override
            public void onSetHeight(int frameHeight) {
                mTagFrameHeight = frameHeight;
                extractAfterTagFrameHeightSet();
            }

            @Override
            public void onClickFrame() {
                mTagButtonClickListener.onTagButtonClick(mVisitDetail);
            }
        });
    }

    private void extractAfterTagFrameHeightSet() {

        // タグフレームの高さが渡されたらビュー全体の高さを設定しアニメーションする。
        int rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_normal);
        fixedHeight = rowHeight * 10 + padding * 2;

        noteLineHeight = (int) (noteText.getPaint().getFontMetrics().bottom - noteText.getPaint().getFontMetrics().top);

        mExHeight = fixedHeight + noteLineHeight + mTagFrameHeight;

        VisitDetailView.this.setExHeight(mExHeight);
        VisitDetailView.this.changeViewHeight(AnimateCondition.TO_EX_HEIGHT, 0, true, null, null);

    }


    public VisitDetail getVisitDetail() {
        return mVisitDetail;
    }

    public void refreshPersonData() {
        dataText.setText(mPerson.toString(getContext()));
    }

    public void refreshTagFrame() {
        initTagFrame();
    }

    public interface OnPersonPrioritySetListener {
        void onPersonPrioritySet(Visit.Priority priority);
    }

    public interface OnEditPersonClickListener {
        void onEditPersonClick(Person person);
    }

    public interface OnTagButtonClickListener {
        void onTagButtonClick(VisitDetail visitDetail);
    }


}
