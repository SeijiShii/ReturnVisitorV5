package net.c_kogyo.returnvisitorv5.view;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;

/**
 * Created by SeijiShii on 2017/03/12.
 */

public class PlacementCell extends BaseAnimateView {

    private Placement mPlacement;
    private PlacementCellListener mListener;
    private boolean mExtracted;

    public PlacementCell(Context context,
                         Placement placement,
                         boolean extracted,
                         @Nullable PlacementCellListener listener) {
        super(context,
                0,
                R.layout.placement_cell);

        mExtracted = extracted;
        mPlacement = placement;
        mListener = listener;

        initCommon();
        super.setListener(new BaseAnimateViewListener() {
            @Override
            public void onUpdateHeight() {

            }

            @Override
            public void postInitialExtract(BaseAnimateView view) {
                if (mListener != null) {
                    mListener.postExtract(PlacementCell.this);
                }
            }
        });
    }

    public PlacementCell(Context context, AttributeSet attrs, int resId) {
        super(context, attrs, resId);
    }

    @Override
    public void setLayoutParams(BaseAnimateView view) {
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void initCommon() {

        int mExtractHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);

        initPlacementText();
        initDeleteButton();

        if (!mExtracted) {
            extractPostDrawn(mExtractHeight);

        } else {
            this.getLayoutParams().height = mExtractHeight;
            if (mListener != null) {
                mListener.postExtract(this);
            }
        }
    }

    private TextView placementText;
    private void initPlacementText() {
        placementText = (TextView) getViewById(R.id.placement_text);
        placementText.setText(mPlacement.getPublicationData());
    }

    private void initDeleteButton() {
        // DONE: 2017/03/23 クリックイベントが起きたり起きなかったり
        final Button deleteButton = (Button) getViewById(R.id.plc_delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacementCell.this.compress(new PostAnimationListener() {
                    @Override
                    public void postAnimate(BaseAnimateView view) {
                        if (mListener != null) {
                            mListener.postCompress(PlacementCell.this);
                        }
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
