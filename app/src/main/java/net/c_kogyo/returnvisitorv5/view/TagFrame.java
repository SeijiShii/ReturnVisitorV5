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
import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.data.list.TagList;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class TagFrame extends TagLinesFrame{

    private ArrayList<String> mTagIds;
    private ArrayList<Tag> mTags;
    private TagFrameCallback mCallback;

    public TagFrame(Context context) {
        super(context);
    }

    public TagFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setLayoutParams(TagLinesFrame frame) {
        frame.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    public void setTagIdsAndInitialize(ArrayList<String> tagIds, TagFrameCallback callback) {
        this.mTagIds = tagIds;
        this.mCallback = callback;
        removeAllViews();

        if (mTagIds.size() <= 0) {
            return;
        }

        setTags();
        if (mTags.size() <= 0) {
            return;
        }

        ArrayList<View> tagViews = new ArrayList<>();
        for (Tag tag : mTags) {
            SmallTagView tagView = new SmallTagView(getContext(), tag);
            tagViews.add(tagView);
        }

        super.setViewsAndInitialize(tagViews);

        if (mCallback != null) {
            mCallback.postDrawn();
        }
    }

    private void setTags() {

        mTags = new ArrayList<>();

        for (String id : mTagIds) {
            Tag tag = TagList.getInstance().getById(id);
            if (tag != null) {
                mTags.add(tag);
            }
        }

    }

    public interface TagFrameCallback {
        void postDrawn();
    }

}
