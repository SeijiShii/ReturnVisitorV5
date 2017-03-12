package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/03/09.
 */

public class PlacementDialog extends FrameLayout {

    private OnButtonClickListener mButtonClickListener;
    private Placement mPlacement;
    private String mParentId;

    public PlacementDialog(@NonNull Context context, String parentId) {
        super(context);

        mParentId = parentId;
        initCommon();
    }

    public PlacementDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initCommon();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.mButtonClickListener = listener;
    }

    private View view;
    private void initCommon() {

        view = LayoutInflater.from(getContext()).inflate(R.layout.placement_dialog, this);

        initPlacementList();
        initCancelButton();

        initGeneralPlcFrame();

        initMagazineFrame();
    }

    private ListViewCompat placementList;
    private String[] plcArray;
    private void initPlacementList() {

        placementList = (ListViewCompat) view.findViewById(R.id.placement_list_view);

        plcArray = getContext().getResources().getStringArray(R.array.placement_array);
        ArrayAdapter<String>  adapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, plcArray);
        placementList.setAdapter(adapter);
        placementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 3) {
                    // Magazine
                    mPlacement = new Placement(Placement.Category.MAGAZINE);
                    fadeInMagazineFrame();

                } else {

                    mPlacement = new Placement(Placement.Category.getEnum(i));
                    fadeInGeneralFrame();
                }
            }
        });
    }

    private void initCancelButton() {

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mButtonClickListener != null) {
                    mButtonClickListener.onCancelClick();
                }
            }
        });
    }

    private LinearLayout generalPlcFrame;
    private void initGeneralPlcFrame() {
        generalPlcFrame = (LinearLayout) view.findViewById(R.id.general_placement_frame);

        initCategoryText();
        initGeneralCloseButton();
        initGeneralNameText();
        initGeneralOKButton();
    }

    private TextView categoryText;
    private void initCategoryText() {
        categoryText = (TextView) view.findViewById(R.id.category_text);
    }

    private void initGeneralCloseButton() {

        Button generalCloseButton = (Button) view.findViewById(R.id.general_close_button);
        generalCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutFrame(generalPlcFrame);
            }
        });
    }

    private AutoCompleteTextView generalNameText;
    private void initGeneralNameText() {
        generalNameText = (AutoCompleteTextView) view.findViewById(R.id.general_name_text);
    }

    private void initGeneralOKButton() {
        Button generalOKButton = (Button) view.findViewById(R.id.general_ok_button);
        generalOKButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlacement.setName(generalNameText.getText().toString());
                if (mButtonClickListener != null) {
                    mButtonClickListener.onOkClick(mPlacement, mParentId);
                }
            }
        });
    }

    private void fadeInGeneralFrame() {

        generalPlcFrame.setVisibility(VISIBLE);
        categoryText.setText(plcArray[mPlacement.getCategory().num()]);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                generalPlcFrame.setAlpha((float) valueAnimator.getAnimatedValue());
                generalPlcFrame.requestLayout();
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private void fadeOutFrame(final LinearLayout frame) {

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                frame.setAlpha((float) valueAnimator.getAnimatedValue());
                frame.requestLayout();
            }
        });
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                frame.setVisibility(INVISIBLE);
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

    private LinearLayout magazineFrame;
    private void initMagazineFrame() {
        magazineFrame = (LinearLayout) view.findViewById(R.id.magazine_frame);

        initNumberSpinner();
        initMagCategorySpinner();

        initMagazineCloseButton();
        initMagazineOKButton();
    }

    private void fadeInMagazineFrame() {

        magazineFrame.setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                magazineFrame.setAlpha((float) valueAnimator.getAnimatedValue());
                magazineFrame.requestLayout();
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private AppCompatSpinner magCategorySpinner;
    private void initMagCategorySpinner() {
        magCategorySpinner = (AppCompatSpinner) view.findViewById(R.id.magazine_category_spinner);
        String[] magCatArray = getContext().getResources().getStringArray(R.array.magazine_array);
        ArrayAdapter<String> magCatAdapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, magCatArray);
        magCategorySpinner.setAdapter(magCatAdapter);

        magCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                refreshNumberSpinner(Placement.MagazineCategory.getEnum(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private AppCompatSpinner numberSpinner;
    private void initNumberSpinner() {
        numberSpinner = (AppCompatSpinner) view.findViewById(R.id.number_spinner);

    }

    private ArrayList<Pair<Calendar, String>> numbers;
    private void refreshNumberSpinner(Placement.MagazineCategory magCategory) {
        numbers = Placement.getMagazineNumberArrayList(magCategory, getContext());
        ArrayList<String> numList = new ArrayList<>();
        for (Pair<Calendar, String> pair : numbers) {
            numList.add(pair.second);
        }

        ArrayAdapter<String> numAdapter
                = new ArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, numList);
        numberSpinner.setAdapter(numAdapter);
        numberSpinner.setSelection(numList.size() - 3);

    }

    private void initMagazineCloseButton() {
        Button magazineCloseButton = (Button) view.findViewById(R.id.magazine_close_button);
        magazineCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutFrame(magazineFrame);
            }
        });
    }

    private void initMagazineOKButton() {
        Button magazineOKButton = (Button) view.findViewById(R.id.magazine_ok_button);
        magazineOKButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                mPlacement.setMagCategory(Placement.MagazineCategory.getEnum(magCategorySpinner.getSelectedItemPosition()));
                mPlacement.setNumber(numbers.get(numberSpinner.getSelectedItemPosition()).first);

                if (mButtonClickListener != null) {
                    mButtonClickListener.onOkClick(mPlacement, mParentId);
                }
            }
        });
    }



    public interface OnButtonClickListener {

        void onCancelClick();

        void onOkClick(Placement placement, String parentId);

    }

}
