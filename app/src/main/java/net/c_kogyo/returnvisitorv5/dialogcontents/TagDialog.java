package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.cloudsync.RVCloudSync;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.data.VisitDetail;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/06.
 */

public class TagDialog extends FrameLayout {

    // DONE: 2017/05/08 名前でソート
    private VisitDetail mVisitDetail;
    private OnButtonsClickListener mButtonsClickListener;

    public TagDialog(@NonNull Context context, VisitDetail visitDetail) {
        super(context);

        this.mVisitDetail = visitDetail;

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

    private EditText searchText;
    private void initSearchText() {
        searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                // DONE: 2017/03/06 インクリメンタルサーチ

                String s = charSequence.toString();
                s = trimWhitespace(s);

                if (s.length() <= 0) {
                    mAdapter = new TagListAdapter(RVData.getInstance().tagList.getList());
                } else {
                    mAdapter = new TagListAdapter(RVData.getInstance().tagList.getSearchedItems(s, getContext()));
                }
                tagListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public static String trimWhitespace(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int st = 0;
        int len = str.length();
        char[] val = str.toCharArray();
        while ((st < len) && ((val[st] <= '\u0020') || (val[st] == '\u00A0') || (val[st] == '\u3000'))) {
            st++;
        }
        while ((st < len) && ((val[len - 1] <= '\u0020') || (val[len - 1] == '\u00A0') || (val[len - 1] == '\u3000'))) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
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
                // DONE: 2017/03/06 タグ追加処理

                String data = searchText.getText().toString();
                searchText.setText("");

                if (data.equals("") || data.length() <= 0) {
                    return;
                }

                if (RVData.getInstance().tagList.containsDataWithName(data)){
                    return;
                }

                Tag newTag = new Tag(data);
                RVData.getInstance().tagList.setOrAdd(newTag);

                mVisitDetail.getTagIds().add(newTag.getId());

                mAdapter = new TagListAdapter(RVData.getInstance().tagList.getList());
                tagListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                setListViewHeight();

                RVData.getInstance().saveData(getContext());

                RVCloudSync.syncDataIfLoggedIn(getContext());
            }
        });
    }

    private ListViewCompat tagListView;
    private TagListAdapter mAdapter;
    private void initTagListView() {

        tagListView = (ListViewCompat) view.findViewById(R.id.tag_list_view);
        // DONE: 2017/03/06 tag list adapter
        mAdapter = new TagListAdapter(RVData.getInstance().tagList.getList());
        tagListView.setAdapter(mAdapter);

        setListViewHeight();
    }

    private void setListViewHeight() {

        int rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small)
                 + (int) (getContext().getResources().getDisplayMetrics().density * 10);
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
                // DONE: 2017/03/06 OK
                if (mButtonsClickListener != null) {
                    mButtonsClickListener.onOkClick(mVisitDetail);
                }
            }
        });
    }

    private Button cancelButton;
    private void initCancelButton() {

        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE: 2017/03/06 Cancel
                if (mButtonsClickListener != null) {
                    mButtonsClickListener.onCancelClick();
                }
            }
        });
    }

    public void setOnButtonsClickListener(OnButtonsClickListener listener) {
        this.mButtonsClickListener = listener;
    }

    public interface OnButtonsClickListener {

        void onOkClick(VisitDetail visitDetail);

        void onCancelClick();
    }

    private class TagListAdapter extends BaseAdapter{

        private ArrayList<Tag> mTags;
        TagListAdapter(ArrayList<Tag> tags) {
            mTags = new ArrayList<>(tags);
        }

        @Override
        public int getCount() {
            return mTags.size();
        }

        @Override
        public Object getItem(int i) {
            return mTags.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            Tag tag = (Tag) getItem(i);

            if (view == null) {
                view = new TagListCell(getContext(), tag, mVisitDetail.getTagIds().contains(tag.getId()));
            } else {
                ((TagListCell) view).refreshData(tag, mVisitDetail.getTagIds().contains(tag.getId()));
            }

            ((TagListCell) view).setOnTagSelectChangeListener(new OnTagListCellListener() {
                @Override
                public void onTagSelectChange(Tag tag, boolean selected) {
                    if (selected) {
                        mVisitDetail.getTagIds().add(tag.getId());
                    } else {
                        mVisitDetail.getTagIds().remove(tag.getId());
                    }
                }

                @Override
                public void onDeleteTag(Tag tag) {
                    RVData.getInstance().tagList.deleteById(tag.getId());
                    mVisitDetail.getTagIds().remove(tag.getId());

                    mAdapter = new TagListAdapter(RVData.getInstance().tagList.getList());
                    tagListView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();

                    setListViewHeight();

                    RVData.getInstance().saveData(getContext());

                    RVCloudSync.syncDataIfLoggedIn(getContext());
                }
            });
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
        private OnTagListCellListener mListener;

        private void initCommon() {
            mView = LayoutInflater.from(getContext()).inflate(R.layout.tag_list_cell, this);
            
            textView = (TextView) mView.findViewById(R.id.text_view);
            
            editButton = (Button) mView.findViewById(R.id.edit_button);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // DONE: 2017/03/06 ポップアップで削除を尋ねる
                    PopupMenu popupMenu = new PopupMenu(getContext(), editButton);
                    MenuInflater inflater = popupMenu.getMenuInflater();
                    inflater.inflate(R.menu.delete_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == R.id.delete) {
                                if (mListener != null) {
                                    mListener.onDeleteTag(mTag);
                                }
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

            refreshData(mTag, mIsSelected);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIsSelected = !mIsSelected;
                    refreshData(mTag, mIsSelected);
                    if (mListener != null) {
                        mListener.onTagSelectChange(mTag, mIsSelected);
                    }
                }
            });
        }

        public void setOnTagSelectChangeListener(OnTagListCellListener listener) {
            mListener = listener;
        }

        private void refreshData(Tag tag, boolean isSelected) {
            // DONE: 2017/03/06 情報の更新

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

    interface OnTagListCellListener {

        void onTagSelectChange(Tag tag, boolean selected);

        void onDeleteTag(Tag tag);
    }


}
