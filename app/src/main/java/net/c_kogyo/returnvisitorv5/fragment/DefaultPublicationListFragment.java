package net.c_kogyo.returnvisitorv5.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.view.ViewContent;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class DefaultPublicationListFragment extends SwitchablePagerBaseFragment {

    private static DefaultPublicationListListener mListener;

    public static DefaultPublicationListFragment newInstance(DefaultPublicationListListener listener) {

        mListener = listener;

        DefaultPublicationListFragment fragment = new DefaultPublicationListFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getContext().getString(R.string.default_title);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        initDefaultListView();

        return defaultListView;
    }

    private ListView defaultListView;
    private void initDefaultListView() {
        String[] defaultArray = getContext().getResources().getStringArray(R.array.placement_array);
        ArrayAdapter<String> defaultListAdapter
                = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, defaultArray);

        defaultListView = new ListView(getContext());
        defaultListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
