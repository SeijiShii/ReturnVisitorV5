package net.c_kogyo.returnvisitorv5.activity;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.Button;
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
        initRecordPersonButton();
        initPersonFrame();
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

    private Button recordPersonButton;
    private void initRecordPersonButton(){
        recordPersonButton = (Button) findViewById(R.id.record_person_button);
        recordPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadePersonButtonAndFrame();
                recordPersonButton.setOnClickListener(null);
            }
        });
    }

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
