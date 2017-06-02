package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Visit;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/05/31.
 */

public class PriorityFilterPane extends RelativeLayout {

    private PriorityFilterListener mListener;
    private ArrayList<Person.Priority> mInitPriorities;

    public PriorityFilterPane(Context context,
                              PriorityFilterListener listener,
                              ArrayList<Person.Priority> initPriorities) {

        // TODO: 2017/06/01 Priority反映
        super(context);

        mListener = listener;
        mInitPriorities = new ArrayList<>(initPriorities);

        initCommon();
    }

    public PriorityFilterPane(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private LinearLayout buttonFrame;
    private void initCommon() {

        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getContext().getResources().getDimensionPixelSize(R.dimen.priority_filter_height)));

        buttonFrame = new LinearLayout(getContext());

        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        buttonFrame.setLayoutParams(params);

        addView(buttonFrame);

        initButtons();
    }

    private ToggleColorButton[] buttons;
    private void initButtons() {

        buttons = new ToggleColorButton[5];

        for ( int i = 0 ; i < 5 ; i++ ) {
            buttons[i] = new ToggleColorButton(getContext(),
                    Constants.buttonRes[i + 3],
                    Constants.buttonRes[0],
                    mInitPriorities.contains(Person.Priority.values()[i + 3]));
            buttonFrame.addView(buttons[i]);

            buttons[i].setCheckChangeListener(checkChangeListener);
        }

        for (Person.Priority priority : mInitPriorities) {
            buttons[priority.num() - 3].setChecked(true);
        }

    }

    private ToggleColorButton.CheckChangeListener checkChangeListener
            = new ToggleColorButton.CheckChangeListener() {
        @Override
        public void onCheckChange(boolean checked) {
            ArrayList<Person.Priority> priorities = new ArrayList<>();
            for ( int i = 0 ; i < 5 ; i++ ) {
                if (buttons[i].isChecked()) {
                    priorities.add(Person.Priority.getEnum(i + 3));
                }
            }
            if (mListener != null) {
                mListener.onSetFilter(priorities);
            }
        }
    };

    public interface PriorityFilterListener {
        void onSetFilter(ArrayList<Person.Priority> priorities);
    }
}
