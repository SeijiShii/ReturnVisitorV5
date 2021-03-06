package net.c_kogyo.returnvisitorv5.fragment;

import android.app.Fragment;
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
import net.c_kogyo.returnvisitorv5.data.list.PublicationList;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class RankedPublicationListFragment extends Fragment {

    private static RankedPublicationListListener mListener;
    private static PublicationList mPublicationList;

    public static RankedPublicationListFragment newInstance(RankedPublicationListListener listener, PublicationList publicationList) {

        mListener = listener;
        mPublicationList = publicationList;

        return new RankedPublicationListFragment();
    }

    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.ranked_publication_list_fragment, container, false);

        initSearchText();
        initRankedListView();

        return view;
    }

    private void initSearchText() {
        EditText searchText = (EditText) view.findViewById(R.id.search_text);
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
                = mPublicationList.getSearchedAndRankedList(Calendar.getInstance(), searchWord, getActivity());

        ArrayAdapter<String> rankedListAdapter;
        ArrayList<String> rankedDataList = new ArrayList<>();
        for (Publication publication : rankedList) {
            rankedDataList.add(publication.toString(getActivity()));
        }

        rankedListAdapter
                = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                rankedDataList);

        rankedListAdapter.notifyDataSetChanged();

        rankedListView.setAdapter(rankedListAdapter);
        rankedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Placement placement = new Placement(rankedList.get(position), getActivity());

                if (mListener != null) {
                    mListener.onClickItem(placement);
                }
            }
        });
    }
}
