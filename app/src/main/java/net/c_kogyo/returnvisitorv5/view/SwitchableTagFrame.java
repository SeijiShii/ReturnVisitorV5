package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.data.list.TagList;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/06/01.
 */

public class SwitchableTagFrame extends TagLinesFrame {

    private ArrayList<String> mAllTagIds;
    private ArrayList<String> mSelectedTagIds;
    private ArrayList<Tag> mAllTags;
    private SwitchableTagFrameListener mListener;

    public SwitchableTagFrame(Context context) {
        super(context);

    }

    public SwitchableTagFrame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setLayoutParams(TagLinesFrame frame) {
        frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setTagIdsAndInitialize(ArrayList<String> allTagIds,
                                       ArrayList<String> selectedTagIds,
                                       SwitchableTagFrameListener callback) {

        this.mAllTagIds = new ArrayList<>(allTagIds);
        this.mSelectedTagIds = new ArrayList<>(selectedTagIds);
        this.mListener = callback;

        if (mAllTagIds.size() <= 0) {
            return;
        }

        setAllTags();
        if (mAllTags.size() <= 0) {
            return;
        }

        ArrayList<View> tagViews = new ArrayList<>();
        for (Tag tag : mAllTags) {

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
            tagViews.add(tagToggleButton);
        }

        super.setViewsAndInitialize(tagViews);
    }

    private void setAllTags() {

        mAllTags = new ArrayList<>();

        for (String id : mAllTagIds) {
            Tag tag = TagList.getInstance().getById(id);
            if (tag != null) {
                mAllTags.add(tag);
            }
        }

    }

    public interface SwitchableTagFrameListener {

        void onTagSelectChanged(ArrayList<String> selectedTagIds);

    }
}
