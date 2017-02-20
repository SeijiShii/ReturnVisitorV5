package net.c_kogyo.returnvisitorv5.activity;

<<<<<<< HEAD
import android.animation.Animator;
=======
import android.animation.AnimatorSet;
>>>>>>> b91ea7e7d113c1ad449f81b102a9a97b7e70dc62
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.Button;
<<<<<<< HEAD
import android.widget.RelativeLayout;
=======
import android.widget.LinearLayout;
>>>>>>> b91ea7e7d113c1ad449f81b102a9a97b7e70dc62
import android.widget.TextView;
import android.widget.Toast;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.view.BaseAnimateView;
import net.c_kogyo.returnvisitorv5.view.ClearEditText;

/**
 * Created by SeijiShii on 2017/02/16.
 */

public class RecordVisitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record_visit_activity);

        initAddressText();
        initPlaceNameText();
<<<<<<< HEAD
        initTwoButtonsFrame();
=======
        initRecordPersonButton();
        initPersonFrame();
>>>>>>> b91ea7e7d113c1ad449f81b102a9a97b7e70dc62
        initCancelButton();
    }

    private TextView addressText;
    private void initAddressText() {
        addressText = (TextView) findViewById(R.id.address_text_view);
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeNameText.changeViewHeight(BaseAnimateView.AnimateCondition.FROM_0_TO_EX_HEIGHT, true, null, null);
            }
        });
    }

    private ClearEditText placeNameText;
    private void initPlaceNameText() {
        placeNameText = (ClearEditText) findViewById(R.id.place_name_text_view);
    }

<<<<<<< HEAD
    private RelativeLayout twoButtonsFrame;
    private void initTwoButtonsFrame() {
        twoButtonsFrame = (RelativeLayout) findViewById(R.id.two_buttons_frame);

        initVisitDetailButton();
    }

    private Button visitDetailButton;
    private void initVisitDetailButton() {
        visitDetailButton = (Button) findViewById(R.id.visit_detail_button);
        visitDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutTwoButtonsFrame();
=======
    private Button recordPersonButton;
    private void initRecordPersonButton(){
        recordPersonButton = (Button) findViewById(R.id.record_person_button);
        recordPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadePersonButtonAndFrame();
                recordPersonButton.setOnClickListener(null);
>>>>>>> b91ea7e7d113c1ad449f81b102a9a97b7e70dc62
            }
        });
    }

<<<<<<< HEAD
    private void fadeOutTwoButtonsFrame() {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                twoButtonsFrame.setAlpha((float) valueAnimator.getAnimatedValue());
                twoButtonsFrame.requestLayout();
            }
        });
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                twoButtonsFrame.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
=======
    private LinearLayout personFrame;
    private void initPersonFrame() {
        personFrame = (LinearLayout) findViewById(R.id.person_frame);

    }

    private boolean isPersonButtonVisible = true;
    private void fadePersonButtonAndFrame(){
        float originAlpha, targetAlpha;
        int originHeight, targetHeight;

        //TODO: personFrameの合計の高さをゲットできるように
        if (isPersonButtonVisible) {
            originAlpha = 1f;
            targetAlpha = 0f;
            originHeight = 0;
            targetHeight = 300;
        } else {
            originAlpha = 0f;
            targetAlpha = 1f;

            originHeight = 300;
            targetHeight = 0;
        }

        isPersonButtonVisible = !isPersonButtonVisible;

        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(originAlpha, targetAlpha);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                recordPersonButton.setAlpha(value);
                recordPersonButton.requestLayout();

                personFrame.setAlpha(1f - value);
                personFrame.requestLayout();
            }
        });

        ValueAnimator heightAnimator = ValueAnimator.ofInt(originHeight, targetHeight);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                personFrame.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                personFrame.requestLayout();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alphaAnimator, heightAnimator);
        set.setDuration(500);
        set.start();

>>>>>>> b91ea7e7d113c1ad449f81b102a9a97b7e70dc62
    }

    private Button cancelButton;
    private void initCancelButton(){
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "CANCEL", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
