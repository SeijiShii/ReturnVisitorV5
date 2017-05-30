package net.c_kogyo.returnvisitorv5.dialog;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.fragment.DefaultPublicationListFragment;
import net.c_kogyo.returnvisitorv5.fragment.RankedPublicationListFragment;
import net.c_kogyo.returnvisitorv5.fragment.SwitchablePagerBaseFragment;
import net.c_kogyo.returnvisitorv5.view.SwitchablePager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by SeijiShii on 2017/03/09.
 */

public  class  PlacementDialog<T extends SwitchablePagerBaseFragment> extends DialogFragment {

    private static PlacementDialogListener mListener;
    private static Publication mPublication;
    private static String mParentId;
 
    private static PlacementDialog instance;
    public static PlacementDialog getInstance(String parentId, PlacementDialogListener listener) {
        
        mParentId = parentId;
        mListener = listener;
        
        if (instance == null) {
            instance = new PlacementDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        initCommon();
        builder.setView(view);

        builder.setNegativeButton(R.string.cancel, null);
        
        return builder.create();
    }

    private View view;
    private void initCommon() {

        view = View.inflate(getActivity(), R.layout.placement_dialog, null);

        initSwitchablePager();

        initGeneralPlcFrame();

        initMagazineFrame();
    }



    private SwitchablePager switchablePager;
    private RankedPublicationListFragment rankedPublicationListFragment;
    private void initSwitchablePager() {
        switchablePager = (SwitchablePager) view.findViewById(R.id.switchable_pager);

        List<Object> contents = new ArrayList<>();
        rankedPublicationListFragment
                = RankedPublicationListFragment.newInstance(new RankedPublicationListFragment.RankedPublicationListListener() {
            @Override
            public void onClickItem(Placement placement) {
                if (mListener != null) {
                    mListener.onDecidePlacement(placement, mParentId);
                }
            }
        });

        contents.add(rankedPublicationListFragment);
        contents.add(DefaultPublicationListFragment.newInstance(new DefaultPublicationListFragment.DefaultPublicationListListener() {
            @Override
            public void onTouchListItem(Publication publication) {
                mPublication = publication;
                if (publication.getCategory() == Publication.Category.MAGAZINE) {
                    fadeInMagazineFrame();
                } else {
                    fadeInGeneralFrame();
                }

            }
        }));

        switchablePager.setContents(contents, getChildFragmentManager());
    }


//    private ViewContent rankedContent;
//    private ArrayList<Publication> rankedList;
//    private void initRankedListView(String searchWord) {
//
//        ListView rankedListView = new ListView(getActivity());
//        rankedListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        rankedList = RVData.getInstance().publicationList.getSearchedAndRankedList(Calendar.getInstance(), searchWord, getActivity());
//
//        ArrayAdapter<String> rankedListAdapter;
//        ArrayList<String> rankedDataList = new ArrayList<>();
//        for (Publication publication : rankedList) {
//            rankedDataList.add(publication.toString(getActivity()));
//        }
//
//        rankedListAdapter
//                = new ArrayAdapter<>(getActivity(),
//                android.R.layout.simple_list_item_1,
//                rankedDataList);
//
//        rankedListAdapter.notifyDataSetChanged();
//
//        rankedListView.setAdapter(rankedListAdapter);
//        rankedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                Placement placement = new Placement(rankedList.get(position), getActivity());
//
//                if (mListener != null) {
//                    mListener.onDecidePlacement(placement, mParentId);
//                }
//            }
//        });
//
//        rankedContent = new ViewContent(rankedListView, getActivity().getString(R.string.history_title));
//    }

//    private ViewContent defaultContent;
//    private String[] defaultArray;
//    private void initDefaultListView() {
//        defaultArray = getActivity().getResources().getStringArray(R.array.placement_array);
//        ArrayAdapter<String> defaultListAdapter
//                = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, defaultArray);
//
//        ListView defaultListView = new ListView(getActivity());
//        defaultListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        defaultListView.setAdapter(defaultListAdapter);
//        defaultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//            }
//        });
//
//        defaultContent = new ViewContent(defaultListView, getActivity().getString(R.string.default_title));
//
//    }

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
        generalCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutFrame(generalPlcFrame);
            }
        });
    }

    private EditText generalNameText;
    private void initGeneralNameText() {
        generalNameText = (EditText) view.findViewById(R.id.general_name_text);
    }

    private void initGeneralOKButton() {
        Button generalOKButton = (Button) view.findViewById(R.id.general_ok_button);
        generalOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPublication.setName(generalNameText.getText().toString());

                RVData.getInstance().publicationList.setOrAdd(mPublication);
                Publication publication = RVData.getInstance().publicationList.getCorrespondingData(mPublication);
                Placement placement = new Placement(publication, getActivity());

                if (mListener != null) {
                    mListener.onDecidePlacement(placement, mParentId);
                }
            }
        });
    }

    private void fadeInGeneralFrame() {

        String defaultArray[] = getActivity().getResources().getStringArray(R.array.placement_array);

        generalPlcFrame.setVisibility(VISIBLE);
        categoryText.setText(defaultArray[mPublication.getCategory().num()]);
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
        initMagazineNameText();

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
        String[] magCatArray = getActivity().getResources().getStringArray(R.array.magazine_array);
        ArrayAdapter<String> magCatAdapter
                = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, magCatArray);
        magCategorySpinner.setAdapter(magCatAdapter);

        magCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                refreshNumberSpinner(Publication.MagazineCategory.getEnum(i));
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
    private void refreshNumberSpinner(Publication.MagazineCategory magCategory) {
        numbers = Publication.getMagazineNumberArrayList(magCategory, getActivity());
        ArrayList<String> numList = new ArrayList<>();
        for (Pair<Calendar, String> pair : numbers) {
            numList.add(pair.second);
        }

        ArrayAdapter<String> numAdapter
                = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, numList);
        numberSpinner.setAdapter(numAdapter);
        numberSpinner.setSelection(numList.size() - 3);

    }

    private EditText magazineNameText;
    private void initMagazineNameText() {
        magazineNameText = (EditText) view.findViewById(R.id.magazine_name_text);
    }

    private void initMagazineCloseButton() {
        Button magazineCloseButton = (Button) view.findViewById(R.id.magazine_close_button);
        magazineCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutFrame(magazineFrame);
            }
        });
    }

    private void initMagazineOKButton() {
        Button magazineOKButton = (Button) view.findViewById(R.id.magazine_ok_button);
        magazineOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPublication.setMagCategory(Publication.MagazineCategory.getEnum(magCategorySpinner.getSelectedItemPosition()));
                mPublication.setNumber(numbers.get(numberSpinner.getSelectedItemPosition()).first);
                mPublication.setName(magazineNameText.getText().toString());

                RVData.getInstance().publicationList.setOrAdd(mPublication);
                Publication publication = RVData.getInstance().publicationList.getCorrespondingData(mPublication);
                Placement placement = new Placement(publication, getActivity());

                if (mListener != null) {
                    mListener.onDecidePlacement(placement, mParentId);
                }
            }
        });
    }

    public interface PlacementDialogListener {

        void onDecidePlacement(Placement placement, String parentId);

    }

    // DONE: 2017/05/22 よく使う配布物を表示

//    private class RankedPlacementListAdapter extends BaseAdapter {
//
//        private ArrayList<Publication> mRankedList;
//        public RankedPlacementListAdapter(Calendar today, String searchWord) {
//
//            mRankedList = RVData.getInstance().publicationList.getSearchedAndRankedList(today, searchWord, getActivity());
//        }
//
//        @Override
//        public int getCount() {
//
//            return mRankedList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mRankedList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            if (convertView == null) {
//
//                convertView = new PlacementCell(getActivity(), (Publication) getItem(position), true, null, false) {
//                    @Override
//                    public void setLayoutParams(BaseAnimateView view) {
//                        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                    }
//                };
//            } else {
//
//                ((PlacementCell) convertView).refreshData((Publication) getItem(position));
//            }
//            return convertView;
//        }
//    }

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShowing.getAndSet(true)) return;

        try {
            super.show(manager, tag);
        } catch (Exception e) {
            isShowing.set(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowing.set(false);
    }

}
