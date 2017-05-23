package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.Placement;

/**
 * Created by SeijiShii on 2017/03/12.
 */

public abstract class PlacementCell extends BaseAnimateView {

    private Placement mPlacement;
    private PlacementCellListener mCellListener;
    private boolean mExtracted;
    private boolean mShowDeleteButton;

    public PlacementCell(Context context,
                         Placement placement,
                         boolean extracted,
                         @Nullable PlacementCellListener listener,
                         boolean showDeleteButton) {
        super(context,
                0,
                R.layout.placement_cell);

        mExtracted = extracted;
        mPlacement = placement;
        mCellListener = listener;
        mShowDeleteButton = showDeleteButton;

        initCommon();
    }

    public PlacementCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
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

    private TextView placementText;
    private void initPlacementText() {
        placementText = (TextView) getViewById(R.id.placement_text);
        refreshData(null);
    }

    private void initDeleteButton() {
        // DONE: 2017/03/23 クリックイベントが起きたり起きなかったり
        final Button deleteButton = (Button) getViewById(R.id.plc_delete_button);
        if (mShowDeleteButton) {
            deleteButton.setVisibility(VISIBLE);
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
        } else {
            deleteButton.setVisibility(INVISIBLE);
        }
    }

    public void refreshData(@Nullable Placement placement) {
        if (placement != null) {
            mPlacement = placement;
        }

        placementText.setText(mPlacement.toString(getContext()));
    }

    public Placement getPlacement() {
        return mPlacement;
    }

    public interface PlacementCellListener {

        void postExtract(PlacementCell cell);

        void postCompress(PlacementCell cell);
    }

    @Override
    public void postViewExtract(BaseAnimateView view) {

    }
}
