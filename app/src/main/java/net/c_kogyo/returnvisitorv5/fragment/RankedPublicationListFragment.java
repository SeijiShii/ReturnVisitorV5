package net.c_kogyo.returnvisitorv5.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.view.ViewContent;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class RankedPublicationListFragment extends SwitchablePagerBaseFragment {

    private static RankedPublicationListListener mListener;

    public static RankedPublicationListFragment newInstance(RankedPublicationListListener listener) {

        mListener = listener;

        return new RankedPublicationListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        title = context.getString(R.string.history_title);
    }

    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.ranked_publication_list_fragment, null);

        initSearchText();
        initRankedListView();

        return view;
    }

    private TextView searchText;
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
                refreshList(s.toString());
            }
        });
    }

    private ListView rankedListView;
    private void initRankedListView() {

        rankedListView = (ListView) view.findViewById(R.id.list_view);
        refreshList("");
    }

    public interface RankedPublicationListListener {
        void onClickItem(Placement placement);
    }

    public void refreshList(String searchWord) {
        final ArrayList<Publication> rankedList
                = RVData.getInstance().publicationList.getSearchedAndRankedList(Calendar.getInstance(), searchWord, getContext());

        ArrayAdapter<String> rankedListAdapter;
        ArrayList<String> rankedDataList = new ArrayList<>();
        for (Publication publication : rankedList) {
            rankedDataList.add(publication.toString(getContext()));
        }

        rankedListAdapter
                = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                rankedDataList);

        rankedListAdapter.notifyDataSetChanged();

        rankedListView.setAdapter(rankedListAdapter);
        rankedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Placement placement = new Placement(rankedList.get(position), getContext());

                if (mListener != null) {
                    mListener.onClickItem(placement);
                }
            }
        });
    }
}
