package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class VisitDetailView extends BaseAnimateView {

    private VisitDetail mVisitDetail;
    private Person mPerson;
    private OnButtonClickListener mButtonClickListener;
    private boolean mExtracted;

    private int mCollapseHeight,
//            mExtractHeight,
            fixedHeight, noteLineHeight;

    public VisitDetailView(Context context,
                           VisitDetail visitDetail,
                           Person person,
                           boolean extracted) {
        super(context,
                0,
                R.layout.visit_detail_view);
        mVisitDetail = visitDetail;
        mPerson = person;
        mExtracted = extracted;

        initCommon();
    }

    public VisitDetailView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.visit_detail_view);
    }

    @Override
    public void setLayoutParams(BaseAnimateView view) {
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    private void initCommon() {

        isViewOpen = true;
        mCollapseHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                + (int) (getContext().getResources().getDisplayMetrics().density * 5);

        initDataText();
        initOpenCloseButton();
        initSeenSwitch();
        initEditPersonButton();
        initPlacementButton();
        initPlacementContainer();
        initTagButton();
        initPriorityRater();
        initNoteText();
        initRVSwitch();
        initStudySwitch();

        initTagFrame();
        setPlacementCells();

        if (mExtracted) {
            this.getLayoutParams().height = getExtractHeight();
        } else {
            extractPostDrawn(getExtractHeight(), null);
        }

    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.mButtonClickListener = listener;
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
                    VisitDetailView.this.changeViewHeight(mCollapseHeight, true, null, null);
                } else {
                    VisitDetailView.this.changeViewHeight(getExtractHeight(), true, null, null);
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
        seenSwitch.setChecked(mVisitDetail.isSeen());
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
                if (mButtonClickListener != null){
                    mButtonClickListener.onEditPersonClick(mPerson);
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
                // DONE: 2017/02/27 placementDialogへの遷移
                if (mButtonClickListener != null) {
                    mButtonClickListener.onPlacementButtonClick(mVisitDetail);
                }
            }
        });
    }

    private LinearLayout placementContainer;
    private void initPlacementContainer() {
        placementContainer = (LinearLayout) getViewById(R.id.placement_container);

    }

    private void setPlacementCells() {
        for (Placement placement : mVisitDetail.getPlacements()) {
            addPlacementCell(placement, false);
        }
    }

    public void addPlacementCell(Placement placement, boolean extracted) {

        PlacementCell placementCell
                = new PlacementCell(getContext(),
                        placement,
                        extracted,
                        new PlacementCell.PlacementCellListener() {
            @Override
            public void postExtract(PlacementCell cell) {
                changeToTheHeight();
            }

            @Override
            public void postCompress(PlacementCell cell) {
                placementContainer.removeView(cell);
                mVisitDetail.getPlacements().remove(cell.getPlacement());
                changeToTheHeight();
            }
        });
        placementContainer.addView(placementCell);

    }
    
    private Button tagButton;
    private void initTagButton() {
        tagButton = (Button) getViewById(R.id.tag_button);
        tagButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/02/27 tagDialogへの遷移
                if (mButtonClickListener != null) {
                    mButtonClickListener.onTagButtonClick(mVisitDetail);
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
                if (mButtonClickListener != null) {
                    mButtonClickListener.onPrioritySet(priority);
                }
            }
        });
    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {
        noteText = (AutoCompleteTextView) getViewById(R.id.note_text);
        noteLineHeight = (int) (noteText.getPaint().getFontMetrics().bottom - noteText.getPaint().getFontMetrics().top);

        // DONE: 2017/02/27 AutoCompleteTextViewアダプタ
        // PENDING: 2017/03/08 要動作検証

        ArrayList<String> pubNameList = new ArrayList<>();
        for (Publication pub : RVData.getInstance().pubList){
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

                changeToTheHeight();

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mVisitDetail.setNote(editable.toString());
            }
        });
    }

    private void changeToTheHeight() {

        VisitDetailView.this.changeViewHeight(getExtractHeight(), true, null, null);
    }

    private SwitchCompat rvSwitch;
    private void initRVSwitch() {
        rvSwitch = (SwitchCompat) getViewById(R.id.rv_switch);
        rvSwitch.setChecked(mVisitDetail.isRV());
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
        studySwitch.setChecked(mVisitDetail.isStudy());
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
            public void onClickFrame() {

                if (mButtonClickListener != null) {
                    mButtonClickListener.onTagButtonClick(mVisitDetail);
                }
            }
        }, 40);
    }


    public VisitDetail getVisitDetail() {
        return mVisitDetail;
    }

    public void refreshPersonData() {
        dataText.setText(mPerson.toString(getContext()));
    }

    public void refreshTagFrame() {
        initTagFrame();
        changeToTheHeight();
    }

    public int getExtractHeight() {

        // DONE: 2017/03/12 高さが合わない
        int rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_normal);

        fixedHeight = rowHeight * 10 + padding * 2;

        int lineCount;
        if (noteText.getLineCount() <= 0) {
            lineCount = 1;
        } else {
            lineCount = noteText.getLineCount();
        }
        int noteHeight = noteLineHeight * lineCount;
        int plcHeight = rowHeight * mVisitDetail.getPlacements().size();

        return fixedHeight + plcHeight + noteHeight + tagFrame.getFrameHeight();

    }

    public interface OnButtonClickListener {

        void onEditPersonClick(Person person);

        void onTagButtonClick(VisitDetail visitDetail);

        void onPlacementButtonClick(VisitDetail visitDetail);

        void onPrioritySet(Visit.Priority priority);
    }

    // DONE: 2017/03/20 mExtractによる振り分け
    // TODO: 2017/05/06 描画時に高さ0, collapse, extractを選べるようにする

}
