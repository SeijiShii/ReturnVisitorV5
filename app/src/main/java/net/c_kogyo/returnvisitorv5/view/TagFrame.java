package net.c_kogyo.returnvisitorv5.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Tag;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class TagFrame extends LinearLayout{

    private final static int LINE_HEIGHT = 35;
    private final static int MARGIN = 5;

    private int lineHeight, frameWidth, margin;
    private ArrayList<String> mTagIds;
    private ArrayList<Tag> mTags;
    private TagFrameCallback mCallback;

    public TagFrame(Context context,
                    ArrayList<String> tags,
                    TagFrameCallback callback) {
        super(context);

        this.mTagIds = tags;
        this.mCallback = callback;

        initCommon();

    }

    public TagFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTagIdsAndInitialize(ArrayList<String> tagIds, TagFrameCallback callback) {
        this.mTagIds = tagIds;
        this.mCallback = callback;

        initCommon();
    }

    private void initCommon() {

        this.removeAllViews();

        lineHeight = (int) (getContext().getResources().getDisplayMetrics().density * LINE_HEIGHT);
        margin = (int) (getContext().getResources().getDisplayMetrics().density * MARGIN);

        this.setOrientation(VERTICAL);
        this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (mTagIds.size() <= 0) {
            return;
        }

        setTags();
        if (mTags.size() <= 0) {
            return;
        }

        waitAndSetWidth();
    }

    private LinearLayout generateNewLine() {

        LinearLayout newLine = new LinearLayout(getContext());
        newLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight));
        newLine.setOrientation(HORIZONTAL);

        return newLine;
    }

    private void waitAndSetWidth() {

        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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
                        frameWidth = getWidth();
                        putTagsInLine();
                    }
                });
            }
        }).start();

    }

    private void putTagsInLine() {

        this.removeAllViews();
        this.addView(generateNewLine());

        int widthSum = 0;

        for (Tag tag: mTags) {

            SmallTagView tagView = new SmallTagView(getContext(), tag);
            if (tagView.getViewWidth() + margin + widthSum > frameWidth) {

                // これから追加するタグの幅とすでにセットされているタグの幅を足したとき、フレームの幅を超えるようなら行を追加する
                this.addView(generateNewLine());
                widthSum = 0;
            }
            // 最後の行にタグを追加する
            ((LinearLayout)(this.getChildAt(this.getChildCount() - 1))).addView(tagView);
            widthSum += (tagView.getViewWidth() + margin);
        }

        if (mCallback != null) {
            mCallback.postDrawn();
        }
    }

    private void setTags() {

        mTags = new ArrayList<>();

        for (String id : mTagIds) {
            Tag tag = RVData.getInstance().tagList.getById(id);
            if (tag != null) {
                mTags.add(tag);
            }
        }

    }

    public interface TagFrameCallback {
        void postDrawn();
    }

}
