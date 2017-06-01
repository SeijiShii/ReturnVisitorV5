package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.util.ViewUtil;

/**
 * Created by SeijiShii on 2017/06/01.
 */

public class SearchFilterPane extends FrameLayout {

    private SearchFilterPaneListener mListener;

    public SearchFilterPane(@NonNull Context context, SearchFilterPaneListener listener) {
        super(context);

        mListener = listener;
        initCommon();
    }

    public SearchFilterPane(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void initCommon() {
        View view = View.inflate(getContext(), R.layout.search_filter_pane, this);
        final EditText searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mListener != null) {
                    mListener.afterTextChanged(s.toString());
                }
            }
        });

        ImageView clearButton = (ImageView) view.findViewById(R.id.clear_button);
        ViewUtil.setOnClickListener(clearButton, new ViewUtil.OnViewClickListener() {
            @Override
            public void onViewClick(View view) {
                searchText.setText("");
            }
        });
    }

    public interface SearchFilterPaneListener {
        void afterTextChanged(String s);
    }
}
