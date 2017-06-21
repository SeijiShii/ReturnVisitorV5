package net.c_kogyo.returnvisitorv5.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.NoteCompItem;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;
import net.c_kogyo.returnvisitorv5.data.list.NoteCompList;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class VisitDetailView extends BaseAnimateView {

    private VisitDetail mVisitDetail;
    private Person mPerson;
    private VisitDetailViewListener mListener;
    private DrawCondition mCondition;

    private int mCollapseHeight;

    public enum DrawCondition{
        EXTRACT_POST_DRAWN_FROM_0,
        COLLAPSE,
        EXTRACT
    }

    public VisitDetailView(Context context,
                           VisitDetail visitDetail,
                           Person person,
                           DrawCondition condition,
                           VisitDetailViewListener listener) {
        super(context,
                0,
                R.layout.visit_detail_view);
        mVisitDetail = visitDetail;
        mPerson = person;
        mListener = listener;
        mCondition = condition;

        initCommon();
        super.setListener(new BaseAnimateViewListener() {
            @Override
            public void onUpdateHeight() {

            }

            @Override
            public void postInitialExtract(BaseAnimateView view) {
                if (mListener!= null) {
                    mListener.postInitialExtract(VisitDetailView.this);
                }
            }
        });
    }

    public VisitDetailView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.visit_detail_view);
    }

    @Override
    public void setLayoutParams(BaseAnimateView view) {
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    private void initCommon() {

        // 新規追加の時はたいてい会えたから。
        mVisitDetail.setSeen(true);

        isViewOpen = true;
        mCollapseHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                + (int) (getContext().getResources().getDisplayMetrics().density * 5);

        initFirstRow();
        initEditButton();
        initDataText();
        initOpenCloseButton();
        initSeenSwitch();
        initPlacementButton();
        initPlacementContainer();
        initTagButton();
        initPriorityRater();
        initNoteText();
        initRVSwitch();
        initStudySwitch();

        initTagFrame();
        setPlacementCells();

        switch (mCondition) {
            case EXTRACT_POST_DRAWN_FROM_0:
                isViewOpen = true;
//                extractPostDrawn(getExtractHeight(), null);
                extractPostDrawn(ViewGroup.LayoutParams.WRAP_CONTENT);
                break;
            case EXTRACT:
                isViewOpen = true;
//                this.getLayoutParams().height = getExtractHeight();
                this.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                break;
            case COLLAPSE:
                isViewOpen = false;
                this.getLayoutParams().height = mCollapseHeight;
                break;
        }

    }

    private void initFirstRow() {
        LinearLayout firstRow = (LinearLayout) getViewById(R.id.first_row);
        ViewUtil.setOnClickListener(firstRow, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                // 開閉アニメーション DONE
                if (isViewOpen) {
                    VisitDetailView.this.changeViewHeight(mCollapseHeight, true, false, null);
                } else {
                    VisitDetailView.this.changeViewHeight(ViewGroup.LayoutParams.WRAP_CONTENT, true, false, null);
                }

                rotateOpenCloseButton();

                isViewOpen = !isViewOpen;
            }
        });
    }

    private TextView dataText;
    private void initDataText() {
        dataText = (TextView) getViewById(R.id.data_text);
        dataText.setText(mPerson.toString(getContext()));

    }

    private ImageView openCloseButton;
    private boolean isViewOpen;
    private void initOpenCloseButton() {
        openCloseButton = (ImageView) getViewById(R.id.open_close_button);
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
    
    private void initSeenSwitch() {
        RightTextSwitch seenSwitch = (RightTextSwitch) getViewById(R.id.seen_switch);
        seenSwitch.setChecked(mVisitDetail.isSeen());
        seenSwitch.setOnCheckChangeListener(new RightTextSwitch.RightTextSwitchOnCheckChangeListener() {
            @Override
            public void onCheckChange(boolean checked) {
                mVisitDetail.setSeen(checked);
            }
        });

    }

    private void initPlacementButton() {
        Button placementButton = (Button) getViewById(R.id.add_placement_button);
        placementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/02/27 placementDialogへの遷移
                if (mListener != null) {
                    mListener.onPlacementButtonClick(mVisitDetail);
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
                extract();
            }

            @Override
            public void postCompress(PlacementCell cell) {
                placementContainer.removeView(cell);
                mVisitDetail.getPlacements().remove(cell.getPlacement());
                extract();
            }
        });
        placementContainer.addView(placementCell);
    }
    
    private void initTagButton() {
        Button tagButton = (Button) getViewById(R.id.tag_button);
        tagButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/02/27 tagDialogへの遷移
                if (mListener != null) {
                    mListener.onTagButtonClick(mVisitDetail);
                }
            }
        });
    }

    private void initPriorityRater() {
        PriorityRater priorityRater = (PriorityRater) getViewById(R.id.priority_rater);
        priorityRater.setPriority(mVisitDetail.getPriority());
        priorityRater.setOnPrioritySetListener(new PriorityRater.OnPrioritySetListener() {
            @Override
            public void onPriorityChanged(Person.Priority priority) {
                mVisitDetail.setPriority(priority);
                mPerson.setPriority(priority);
                if (mListener != null) {
                    mListener.onPrioritySet(priority);
                }
            }
        });
    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {
        noteText = (AutoCompleteTextView) getViewById(R.id.note_text);
//        noteLineHeight = (int) (noteText.getPaint().getFontMetrics().bottom - noteText.getPaint().getFontMetrics().top);

        // DONE: 2017/02/27 AutoCompleteTextViewアダプタ
        // PENDING: 2017/03/08 要動作検証

        ArrayList<String> noteCompList = new ArrayList<>();
        for (NoteCompItem compItem : NoteCompList.loadList(new RVDBHelper(getContext()))){
            noteCompList.add(compItem.getName());
        }
        String[] compArray = noteCompList.toArray(new String[0]);

        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, compArray);
        noteText.setAdapter(adapter);

        noteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                extract();

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mVisitDetail.setNote(editable.toString());
            }
        });
    }

    private void extract() {

        measure(0, 0);
        int height = getMeasuredHeight();

        VisitDetailView.this.changeViewHeight(height, true, false, new PostAnimationListener() {
            @Override
            public void postAnimate(BaseAnimateView view) {
                VisitDetailView.this.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        });
    }

    private void initRVSwitch() {
        RightTextSwitch rvSwitch = (RightTextSwitch) getViewById(R.id.rv_switch);
        rvSwitch.setChecked(mVisitDetail.isRV());
        rvSwitch.setOnCheckChangeListener(new RightTextSwitch.RightTextSwitchOnCheckChangeListener() {
            @Override
            public void onCheckChange(boolean checked) {
                mVisitDetail.setIsRv(checked);
            }
        });
    }

    private void initStudySwitch() {
        RightTextSwitch studySwitch = (RightTextSwitch) getViewById(R.id.study_switch);
        studySwitch.setChecked(mVisitDetail.isStudy());
        studySwitch.setOnCheckChangeListener(new RightTextSwitch.RightTextSwitchOnCheckChangeListener() {
            @Override
            public void onCheckChange(boolean checked) {
                mVisitDetail.setIsStudy(checked);
            }
        });
    }

    private TagFrame tagFrame;
    private void initTagFrame() {

        tagFrame = (TagFrame) getViewById(R.id.tag_frame);
        tagFrame.setTagIdsAndInitialize(mVisitDetail.getTagIds(),
                new TagFrame.TagFrameCallback() {
                    @Override
                    public void postDrawn() {
                        extract();
                    }
                });
        ViewUtil.setOnClickListener(tagFrame, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View v) {
                if (mListener != null) {
                    mListener.onTagButtonClick(mVisitDetail);
                }
            }
        });
    }


    public VisitDetail getVisitDetail() {
        return mVisitDetail;
    }

    public void refreshPersonData() {
        dataText.setText(mPerson.toString(getContext()));
    }

    public void refreshTagFrame() {
        initTagFrame();
        extract();
    }

//    public int getExtractHeight() {
//
//        // DONE: 2017/03/12 高さが合わない
//        int rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
//        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding_normal);
//
//        fixedHeight = rowHeight * 9 + padding * 2;
//
//        int lineCount;
//        if (noteText.getLineCount() <= 0) {
//            lineCount = 1;
//        } else {
//            lineCount = noteText.getLineCount();
//        }
//        int noteHeight = noteLineHeight * lineCount;
//        int plcHeight = rowHeight * mVisitDetail.getPlacements().size();
//
//        return fixedHeight + plcHeight + noteHeight + tagFrame.getFrameHeight();
//
//    }

    private Button editButton;
    private void initEditButton() {
        editButton = (Button) getViewById(R.id.edit_button);
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopup();
            }
        });
    }

    private void showMenuPopup() {
        PopupMenu popupMenu = new PopupMenu(getContext(), editButton);
        popupMenu.getMenuInflater().inflate(R.menu.visit_detail_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_person:
                        if (mListener != null) {
                            mListener.onEditPersonClick(mPerson);
                        }
                        return true;
                    case R.id.remove_person:

                        VisitDetailView.this.compress(new PostAnimationListener() {
                            @Override
                            public void postAnimate(BaseAnimateView view) {
                                if (mListener != null) {
                                    mListener.postRemoveVisitDetail(mVisitDetail);
                                }
                            }
                        });

                        return true;
                    case R.id.delete_person:
                        ConfirmDialog.confirmAndDeletePerson(getContext(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                VisitDetailView.this.compress(new PostAnimationListener() {
                                    @Override
                                    public void postAnimate(BaseAnimateView view) {
                                        if (mListener != null) {
                                            mListener.postDeletePerson(mPerson);
                                        }
                                    }
                                });
                            }
                        }, mPerson);
                        return true;
                }
                return false;
            }
        });
        if (!mVisitDetail.belongsToMultiplePlace(new RVDBHelper(getContext()))) {
            popupMenu.getMenu().removeItem(R.id.remove_person);
        }
        popupMenu.show();
    }

    public interface VisitDetailViewListener {

        void onEditPersonClick(Person person);

        void onTagButtonClick(VisitDetail visitDetail);

        void onPlacementButtonClick(VisitDetail visitDetail);

        void onPrioritySet(Person.Priority priority);

        void postInitialExtract(VisitDetailView visitDetailView);

        void postRemoveVisitDetail(VisitDetail visitDetail);

        void postDeletePerson(Person person);
    }

    // DONE: 2017/03/20 mExtractによる振り分け
    // DONE: 2017/05/06 描画時に高さ0, collapse, extractを選べるようにする


}
