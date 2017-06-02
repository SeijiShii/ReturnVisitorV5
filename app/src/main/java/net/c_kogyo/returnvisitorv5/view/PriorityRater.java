package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

import static net.c_kogyo.returnvisitorv5.Constants.buttonRes;

/**
 * Created by SeijiShii on 2017/03/06.
 */

public class PriorityRater extends FrameLayout {

    private Person.Priority mPriority;
    private OnPrioritySetListener mListener;

    public PriorityRater(@NonNull Context context) {
        super(context);

        initCommon();
    }

    public PriorityRater(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initCommon();
    }

    public void setPriority(Person.Priority priority) {
        this.mPriority = priority;
        waitAndUpdateUI(mPriority);
    }

    public void setOnPrioritySetListener(OnPrioritySetListener listener) {
        this.mListener = listener;
    }


    private View view;
//    private int measuredWidth = 0;
    private void initCommon() {

        view = LayoutInflater.from(getContext()).inflate(R.layout.priority_rater, this);

//        measure(0, 0);
//        measuredWidth = getMeasuredWidth();

//        initPriorityFrame();
//        initPriorityText();

        waitAndDraw();
    }

    private void waitAndDraw() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getWidth() <= 0) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {

                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initPriorityFrame();
                        initPriorityText();
                    }
                });
            }
        }).start();
    }

    private LinearLayout priorityFrame;
    private RaterButton[] raterButtons;
//    private Button[] raterButtons;
    private void initPriorityFrame() {
        priorityFrame = (LinearLayout) view.findViewById(R.id.priority_frame);

//        final int raterSize = (int) (getContext().getResources().getDisplayMetrics().density * 25);
//        final int buttonMargin = (int) (getContext().getResources().getDisplayMetrics().density * 10);

        // 2017/02/27 Implement priorityFrame
        priorityFrame.removeAllViews();

//        int buttonWith = measuredWidth / 8;
        int buttonWith = getWidth() / 8;


        raterButtons = new RaterButton[Person.Priority.values().length];
        for ( int i = 0 ; i < Person.Priority.values().length ; i++ ) {
            raterButtons[i] = new RaterButton(getContext(), buttonRes[0]);
            raterButtons[i].setTag(i);
            raterButtons[i].setLayoutParams(new LinearLayout.LayoutParams(buttonWith, ViewGroup.LayoutParams.MATCH_PARENT));

            priorityFrame.addView(raterButtons[i]);

            ViewUtil.setOnClickListener(raterButtons[i], new ViewUtil.OnViewClickListener() {
                @Override
                public void onViewClick(View v) {
                    int tag = Integer.parseInt(v.getTag().toString());
                    Person.Priority priority = Person.Priority.getEnum(tag);

                    if (priority != mPriority) {
                        // 変化したか
                        mPriority = priority;
                        if (mListener != null) {
                            mListener.onPriorityChanged(priority);
                        }
                        updateUI(priority);
                    }
                }
            });
        }
    }

    private void waitAndUpdateUI(final Person.Priority priority) {

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (priorityText == null || raterButtons == null) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {

                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(priority);
                    }
                });
            }
        }).start();



    }

    private void updateUI(Person.Priority priority) {

        int num = priority.num();

        priorityText.setText(getResources().getStringArray(R.array.priority_array)[priority.num()]);

        for (int i = 0 ; i <= num ; i++) {
            raterButtons[i].setBackgroundResource(Constants.buttonRes[num]);
        }

        for (int i = num + 1 ; i < Person.Priority.values().length ; i++ ) {
            raterButtons[i].setBackgroundResource(Constants.buttonRes[0]);
        }
    }

    private TextView priorityText;
    private void initPriorityText() {
        priorityText = (TextView) view.findViewById(R.id.priority_state_text);
        // DONE priorityText
    }

    public interface OnPrioritySetListener {
        void onPriorityChanged(Person.Priority priority);
    }

    private class RaterButton extends RelativeLayout{

        int mResId;

        public RaterButton(Context context, int resId) {
            super(context);

            mResId = resId;

            initCommon();
        }

        public RaterButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        ImageView buttonImage;

        private void initCommon() {
            int dp35 = (int) (getContext().getResources().getDisplayMetrics().density * 35);
            setLayoutParams(new ViewGroup.LayoutParams(dp35, dp35));

            buttonImage = new ImageView(getContext());
            int dp25 = (int)(getContext().getResources().getDisplayMetrics().density * 25);
            RelativeLayout.LayoutParams params = new LayoutParams(dp25, dp25);
            params.addRule(CENTER_IN_PARENT);
            buttonImage.setLayoutParams(params);

            addView(buttonImage);

            buttonImage.setBackgroundResource(mResId);
        }

        public void setBackgroundResource(int resId) {
            buttonImage.setBackgroundResource(resId);
        }


    }

    // DONE: 2017/05/31 改善
}
