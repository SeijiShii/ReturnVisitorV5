package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.data.Tag;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/06/01.
 */

public abstract class TagLinesFrame extends LinearLayout {

    private final static int LINE_HEIGHT = 35;
    private final static int MARGIN = 5;

    private ArrayList<View> mViews;
    private int mFrameWidth, mLineHeight, mMargin;

    public abstract void setLayoutParams(TagLinesFrame frame);

    public TagLinesFrame(Context context) {
        super(context);
        mFrameWidth = 0;
    }

    public TagLinesFrame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mFrameWidth = 0;
    }

    protected void setViewsAndInitialize(ArrayList<View> views) {

        mViews = new ArrayList<>(views);

        initCommon();
    }

    private void initCommon() {
        this.removeAllViews();

        mLineHeight = (int) (getContext().getResources().getDisplayMetrics().density * LINE_HEIGHT);
        mMargin = (int) (getContext().getResources().getDisplayMetrics().density * MARGIN);

        if (mViews.size() <= 0) {
            return;
        }

        this.setOrientation(VERTICAL);
        setLayoutParams(this);


        if (mFrameWidth <= 0) {
            waitAndSetWidth();
        } else {
            putViewsInLine();
        }
    }

    private LinearLayout generateNewLine() {

        LinearLayout newLine = new LinearLayout(getContext());
        newLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLineHeight));
        newLine.setOrientation(HORIZONTAL);

        return newLine;
    }

    private void waitAndSetWidth() {

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFrameWidth = getWidth();
                        putViewsInLine();
                    }
                });
            }
        }).start();

    }

    private void putViewsInLine() {

        this.removeAllViews();
        this.addView(generateNewLine());

        int widthSum = 0;

        for (View view : mViews) {

            view.measure(0, 0);
            int viewWidth = view.getMeasuredWidth();

            if (viewWidth + mMargin + widthSum > mFrameWidth) {
                // これから追加するタグの幅とすでにセットされているタグの幅を足したとき、フレームの幅を超えるようなら行を追加する
                this.addView(generateNewLine());
                widthSum = 0;
            }
            // 最後の行にタグを追加する
            ((LinearLayout)(this.getChildAt(this.getChildCount() - 1))).addView(view);
            widthSum += (viewWidth + mMargin);

        }
    }
}
