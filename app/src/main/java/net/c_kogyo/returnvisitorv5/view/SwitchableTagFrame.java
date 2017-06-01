package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Tag;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/06/01.
 */

public class SwitchableTagFrame extends LinearLayout {

    private final static int LINE_HEIGHT = 35;
    private final static int MARGIN = 5;

    private int lineHeight, frameWidth, margin;

    private ArrayList<String> mAllTagIds;
    private ArrayList<String> mSelectedTagIds;
    private ArrayList<Tag> mAllTags;
    private SwitchableTagFrameListener mListener;

    public SwitchableTagFrame(Context context,
                              ArrayList<String> allTagIds,
                              ArrayList<String> selectedTagIds,
                              SwitchableTagFrameListener callback) {
        super(context);

        this.mAllTagIds = new ArrayList<>(allTagIds);
        this.mSelectedTagIds = new ArrayList<>(selectedTagIds);
        this.mListener = callback;

        initCommon();
    }

    public SwitchableTagFrame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTagIdsAndInitialize(ArrayList<String> allTagIds,
                                       ArrayList<String> selectedTagIds,
                                       SwitchableTagFrameListener callback) {

        this.mAllTagIds = new ArrayList<>(allTagIds);
        this.mSelectedTagIds = new ArrayList<>(selectedTagIds);
        this.mListener = callback;

        initCommon();
    }

    private void initCommon() {

        this.removeAllViews();

        lineHeight = (int) (getContext().getResources().getDisplayMetrics().density * LINE_HEIGHT);
        margin = (int) (getContext().getResources().getDisplayMetrics().density * MARGIN);

        this.setOrientation(VERTICAL);
        this.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (mAllTagIds.size() <= 0) {
            return;
        }

        setAllTags();
        waitAndSetWidth();
    }

    private LinearLayout generateNewLine() {

        LinearLayout newLine = new LinearLayout(getContext());
        newLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight));
        newLine.setOrientation(HORIZONTAL);

        return newLine;
    }

    private void waitAndSetWidth() {

//        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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

        this.addView(generateNewLine());

        int widthSum = 0;

        for (Tag tag: mAllTags) {

            SmallTagToggleButton tagToggleButton
                    = new SmallTagToggleButton(getContext(),
                                tag,
                                mSelectedTagIds.contains(tag.getId()),
                                new SmallTagToggleButton.CheckChangeListener() {
                        @Override
                        public void onCheckChange(Tag tag1, boolean checked) {
                            if (mSelectedTagIds.contains(tag1.getId())) {
                                if (!checked) {
                                    mSelectedTagIds.remove(tag1.getId());
                                }
                            } else {
                                if (checked) {
                                    mSelectedTagIds.add(tag1.getId());
                                }
                            }
                            if (mListener != null) {
                                mListener.onTagSelectChanged(mSelectedTagIds);
                            }
                        }
                    });
            if (tagToggleButton.getViewWidth() + margin + widthSum > frameWidth) {

                // これから追加するタグの幅とすでにセットされているタグの幅を足したとき、フレームの幅を超えるようなら行を追加する
                this.addView(generateNewLine());
                widthSum = 0;
            }
            // 最後の行にタグを追加する
            ((LinearLayout)(this.getChildAt(this.getChildCount() - 1))).addView(tagToggleButton);
            widthSum += (tagToggleButton.getViewWidth() + margin);
        }

    }

    private void setAllTags() {

        mAllTags = new ArrayList<>();

        for (String id : mAllTagIds) {
            Tag tag = RVData.getInstance().tagList.getById(id);
            if (tag != null) {
                mAllTags.add(tag);
            }
        }

    }

    public interface SwitchableTagFrameListener {

        void onTagSelectChanged(ArrayList<String> selectedTagIds);

    }
}
