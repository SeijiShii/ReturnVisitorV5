package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;

/**
 * Created by SeijiShii on 2017/03/12.
 */

public class PlacementCell extends BaseAnimateView {

    private Placement mPlacement;
    private PlacementCellListener mCellListener;
    private boolean mExtracted;

    public PlacementCell(Context context,
                         Placement placement,
                         boolean extracted,
                         PlacementCellListener listener) {
        super(context,
                0,
                R.layout.placement_cell);

        mExtracted = extracted;
        mPlacement = placement;
        mCellListener = listener;

        initCommon();
    }

    public PlacementCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    @Override
    public void setLayoutParams() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    private void initCommon() {

        int mExtractHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);

        initPlacementText();
        initDeleteButton();

        if (!mExtracted) {
            extractPostDrawn(mExtractHeight, new Animator.AnimatorListener() {
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
            this.getLayoutParams().height = mExtractHeight;
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
        // DONE: 2017/03/23 クリックイベントが起きたり起きなかったり
        final Button deleteButton = (Button) getViewById(R.id.plc_delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacementCell.this.changeViewHeight(0,
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
