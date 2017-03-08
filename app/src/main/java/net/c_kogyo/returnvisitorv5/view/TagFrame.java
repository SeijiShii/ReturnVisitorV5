package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
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

public class TagFrame extends LinearLayout implements View.OnTouchListener {

    private final static int LINE_HEIGHT = 35;
    private final static int MARGIN = 5;

    private int lineHeight, frameHeight, frameWidth, margin;
    private ArrayList<String> mTagIds;
    private ArrayList<Tag> mTags;
    private ArrayList<SmallTagView> mTagViews;
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

    public void setTagIdsAndInitialize(ArrayList<String> tagIds,
                                       TagFrameCallback callback) {
        this.mTagIds = tagIds;

        this.mCallback = callback;
        if (mCallback != null) {
            this.setOnTouchListener(this);
        }

        this.mCallback = callback;

        initCommon();
    }

    private void initCommon() {

        this.removeAllViews();

        lineHeight = (int) (getContext().getResources().getDisplayMetrics().density * LINE_HEIGHT);
        margin = (int) (getContext().getResources().getDisplayMetrics().density * MARGIN);

        this.setOrientation(VERTICAL);
        this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        frameHeight = 0;

        setTags();
        if (mTags.size() <= 0) {
            if (mCallback != null) {
                mCallback.onSetHeight(0);
            }
            return;
        }

        startDrawAndMeasure();

    }

    private LinearLayout generateNewLine() {

        LinearLayout newLine = new LinearLayout(getContext());
        newLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight));
        newLine.setOrientation(HORIZONTAL);

        return newLine;
    }

    private void startDrawAndMeasure() {

        final FrameLayout testFrame = new FrameLayout(getContext());
        testFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight));
        this.addView(testFrame);

        mTagViews = new ArrayList<>();

        for (Tag tag : mTags) {
            SmallTagView tagView = new SmallTagView(getContext(), tag);
            mTagViews.add(tagView);
            testFrame.addView(tagView);
        }

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mTagViews.get(mTagViews.size() - 1).getWidth() <= 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        frameWidth = TagFrame.this.getWidth();

                        for (SmallTagView tagView : mTagViews) {
                            tagView.setViewWidth(tagView.getWidth());
                        }

                        testFrame.removeAllViews();
                        TagFrame.this.removeAllViews();

                        startDrawingLines();
                    }
                });
            }
        }).start();
    }

    private void startDrawingLines() {

        this.addView(generateNewLine());
        frameHeight = lineHeight;

        int widthSum = 0;

        for (SmallTagView tagView : mTagViews) {

            if (tagView.getViewWidth() + margin + widthSum > frameWidth) {

                // これから追加するタグの幅とすでにセットされているタグの幅を足したとき、フレームの幅を超えるようなら行を追加する
                this.addView(generateNewLine());
                widthSum = 0;
                frameHeight += lineHeight;
            }
            // 最後の行にタグを追加する
            ((LinearLayout)(this.getChildAt(this.getChildCount() - 1))).addView(tagView);
            widthSum += (tagView.getViewWidth() + margin);
        }

        if (mCallback == null) return;
        mCallback.onSetHeight(frameHeight);
    }

    private void setTags() {

        mTags = new ArrayList<>();

        for (String id : mTagIds) {
            Tag tag = RVData.getInstance().getTagList().getById(id);
            if (tag != null) {
                mTags.add(tag);
            }
        }

        // ここからはテスト部分
//        addTestTags();

    }

    private void addTestTags() {

        String[] texts = {"コンテンツの本文",
                "とかはこれでいい",
                "のですが、",
                "ヘッダー部分など", "" +
                "システム", "全体の文字スケールに", "" +
                "影響されたくない",
                "部分もあります。 ", "そういう場合に ",
                "dp など sp 以外の",
                "単位で指定するには、",
                "引数を2つとる",
                "setTextSize()",
                "を使います"};

        for (String text : texts) {
            mTags.add(new Tag(text));
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.setAlpha(0.3f);
                return true;

            case MotionEvent.ACTION_UP:
                this.setAlpha(1f);
                if (mCallback != null) {
                    mCallback.onClickFrame();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                this.setAlpha(1f);
                return true;

        }

        return false;
    }

//    public int getFrameHeight() {
//        return frameHeight;
//    }

    interface TagFrameCallback {

        void onSetHeight(int frameHeight);

        void onClickFrame();
    }

    // TODO: 2017/03/07 タグの内容が変更されたときの高さの変更がうまくいっていない
}
