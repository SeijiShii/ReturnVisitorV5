package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.list.TagList;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/06/01.
 */

public class TagFilterPane extends FrameLayout {

    private ArrayList<String> mVisibleTagIds;
    private ArrayList<String> mSelectedTagIds;

    private TagFilterPaneListener mListener;

    public TagFilterPane(@NonNull Context context,
                         ArrayList<String> selectedTagIds,
                         TagFilterPaneListener listener) {
        super(context);

        mVisibleTagIds = new ArrayList<>(TagList.getInstance().getAllIds());
        mSelectedTagIds = new ArrayList<>(selectedTagIds);

        mListener = listener;

        initCommon();
    }

    public TagFilterPane(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;

    private void initCommon() {
        view = View.inflate(getContext(), R.layout.tag_filter_pane, this);

        initSearchText();
        initTagFrame();
    }

    private EditText searchText;
    private void initSearchText() {
        searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mVisibleTagIds = TagList.getInstance().getSearchedTagIds(s.toString(), getContext());
                initTagFrame();
            }
        });
    }

    private SwitchableTagFrame tagFrame;
    private void initTagFrame() {
        tagFrame = (SwitchableTagFrame) view.findViewById(R.id.tag_frame);
        tagFrame.setTagIdsAndInitialize(mVisibleTagIds, mSelectedTagIds, new SwitchableTagFrame.SwitchableTagFrameListener() {
            @Override
            public void onTagSelectChanged(ArrayList<String> selectedTagIds) {
                if (mListener != null) {
                    mListener.onTagSelectChanged(selectedTagIds);
                    mSelectedTagIds = selectedTagIds;
                }
            }
        });
    }

    public interface TagFilterPaneListener {
        void onTagSelectChanged(ArrayList<String> selectedTagIds);
    }
}
