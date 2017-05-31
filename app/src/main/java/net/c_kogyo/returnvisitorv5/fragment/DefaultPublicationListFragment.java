package net.c_kogyo.returnvisitorv5.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Publication;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class DefaultPublicationListFragment extends Fragment {

    private static DefaultPublicationListListener mListener;

    public static DefaultPublicationListFragment newInstance(DefaultPublicationListListener listener) {

        mListener = listener;

        return new DefaultPublicationListFragment();
    }

    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.default_publication_list_fragment, container, false);

        initDefaultListView();

        return view;
    }

    private void initDefaultListView() {
        String[] defaultArray = getActivity().getResources().getStringArray(R.array.placement_array);
        ArrayAdapter<String> defaultListAdapter
                = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, defaultArray);

        ListView defaultListView = (ListView) view.findViewById(R.id.list_view);
//        defaultListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        defaultListView.setAdapter(defaultListAdapter);
        defaultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Publication publication;
                if (position == 3) {
                    // Magazine
                    publication = new Publication(Publication.Category.MAGAZINE);
                } else {
                    publication = new Publication(Publication.Category.getEnum(position));
                }
                if (mListener != null) {
                    mListener.onTouchListItem(publication);
                }
            }
        });
    }

    public interface DefaultPublicationListListener {

        void onTouchListItem(Publication publication);
    }
}
