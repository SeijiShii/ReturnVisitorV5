package net.c_kogyo.returnvisitorv5.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
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
        initTwoButtonsFrame();
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
            }
        });
    }

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
