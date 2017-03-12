package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;

/**
 * Created by SeijiShii on 2017/03/12.
 */

public class PlacementCell extends BaseAnimateView {

    private Placement mPlacement;
    private PlacementCellListener mCellListener;

    public PlacementCell(Context context,
                         Placement placement,
                         InitialHeightCondition initCondition,
                         PlacementCellListener listener) {
        super(context,
                context.getResources().getDimensionPixelSize(R.dimen.ui_height_small),
                initCondition,
                R.layout.placement_cell);
        mPlacement = placement;
        mCellListener = listener;

        initCommon();
    }

    public PlacementCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    private void initCommon() {
        initPlacementText();
        initDeleteButton();

        if (mInitCondition == InitialHeightCondition.ZERO) {
            extractPostDrawn(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mCellListener != null) {
                        mCellListener.postExtract(PlacementCell.this);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else {
            if (mCellListener != null) {
                mCellListener.postExtract(this);
            }
        }


    }

    private void initPlacementText() {
        TextView placementText = (TextView) getViewById(R.id.placement_text);
        placementText.setText(mPlacement.toString(getContext()));
    }

    private void initDeleteButton() {
        Button deleteButton = (Button) getViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacementCell.this.changeViewHeight(AnimateCondition.FROM_EX_HEIGHT_TO_ZERO,
                        0,
                        true,
                        null,
                        new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        if (mCellListener != null) {
                            mCellListener.postCompress(PlacementCell.this);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });
    }

    public Placement getPlacement() {
        return mPlacement;
    }

    public interface PlacementCellListener {

        void postExtract(PlacementCell cell);

        void postCompress(PlacementCell cell);
    }
}
