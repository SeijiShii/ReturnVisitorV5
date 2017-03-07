package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Tag;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/06.
 */

public class TagDialog extends FrameLayout {

    private ArrayList<String> mSelectedIds;

    public TagDialog(@NonNull Context context, ArrayList<String> selectedIds) {
        super(context);

        this.mSelectedIds = new ArrayList<>(selectedIds);

        initCommon();
    }

    public TagDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initCommon();
    }

    private View view;
    private void initCommon() {

        view = LayoutInflater.from(getContext()).inflate(R.layout.tag_dialog, this);

        initSearchText();
        initClearButton();
        initAddButton();
        initTagListView();
        initOkButton();
        initCancelButton();
    }

    public void setSelectedIds(ArrayList<String> selectedIds) {
        this.mSelectedIds = new ArrayList<>(selectedIds);
    }

    private EditText searchText;
    private void initSearchText() {
        searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                // TODO: 2017/03/06 インクリメンタルサーチ
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private Button clearButton;
    private void initClearButton() {
        clearButton = (Button) view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
            }
        });
    }

    private Button addButton;
    private void initAddButton() {
        addButton = (Button) view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/03/06 タグ追加処理

                String data = searchText.getText().toString();
                searchText.setText("");

                if (RVData.getInstance().getTagList().containsDataWithName(data)){
                    return;
                }
                Tag newTag = new Tag(data);
                RVData.getInstance().getTagList().add(newTag);
                mSelectedIds.add(newTag.getId());

                mAdapter.notifyDataSetChanged();
                setListViewHeight();

                RVData.getInstance().saveData(null);
            }
        });
    }

    private ListViewCompat tagListView;
    private TagListAdapter mAdapter;
    private void initTagListView() {

        tagListView = (ListViewCompat) view.findViewById(R.id.tag_list_view);
        // TODO: 2017/03/06 tag list adapter
        mAdapter = new TagListAdapter();
        tagListView.setAdapter(mAdapter);

        setListViewHeight();
    }

    private void setListViewHeight() {

        int rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
        int count = mAdapter.getCount();
        if (count > 6) {
            count = 5;
        }
        int height = rowHeight * count;

        tagListView.getLayoutParams().height = height;

    }

    private Button okButton;
    private void initOkButton() {
        okButton = (Button) view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/03/06 OK
            }
        });
    }

    private Button cancelButton;
    private void initCancelButton() {

        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2017/03/06 Cancel
            }
        });
    }

    class TagListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return RVData.getInstance().getTagList().size();
        }

        @Override
        public Object getItem(int i) {
            return RVData.getInstance().getTagList().get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            Tag tag = (Tag) getItem(i);

            if (view == null) {
                view = new TagListCell(getContext(), tag, mSelectedIds.contains(tag.getId()));
            } else {
                ((TagListCell) view).refreshData(tag, mSelectedIds.contains(tag.getId()));
            }
            return view;
        }
    }
    
    class TagListCell extends FrameLayout {

        private Tag mTag;
        private boolean mIsSelected;

        public TagListCell(@NonNull Context context, Tag tag, boolean isSelected) {
            super(context);

            this.mTag = tag;
            this.mIsSelected = isSelected;

            initCommon();
        }

        // Forbidden to use!! No tag set
        public TagListCell(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initCommon();
        }
        
        private View mView;
        private TextView textView;
        private Button editButton;
        private void initCommon() {
            mView = LayoutInflater.from(getContext()).inflate(R.layout.tag_list_cell, this);
            
            textView = (TextView) mView.findViewById(R.id.text_view);
            
            editButton = (Button) mView.findViewById(R.id.edit_button);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: 2017/03/06 ポップアップで削除を尋ねる 
                }
            });

            refreshData(mTag, mIsSelected);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIsSelected = !mIsSelected;
                    refreshData(mTag, mIsSelected);
                }
            });
        }
        
        private void refreshData(Tag tag, boolean isSelected) {
            // TODO: 2017/03/06 情報の更新

            mTag = tag;
            mIsSelected = isSelected;

            this.textView.setText(mTag.getName());

            if (mIsSelected) {
                this.setBackgroundResource(R.drawable.green_grade_circle);
            } else {
                this.setBackgroundResource(0);
            }
        }
    }

}
