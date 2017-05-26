package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.view.PersonCell;
import net.c_kogyo.returnvisitorv5.view.PlaceCell;
import net.c_kogyo.returnvisitorv5.view.SearchCell;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/05/26.
 */

public class SearchDialog extends FrameLayout {

    private SearchDialogListener mListener;
    private String mInitWord;

    public SearchDialog(@NonNull Context context,
                        SearchDialogListener listener,
                        String initialSearchWord) {
        super(context);

        mListener = listener;
        mInitWord = initialSearchWord;

        initCommon();
    }

    public SearchDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private View view;
    private void initCommon() {
        view = View.inflate(getContext(), R.layout.search_dialog, this);

        initNoItemText();
        initSearchListView();
        initCloseButton();
    }

    private ListView searchListView;
    private SearchListAdapter searchListAdapter;
    private void initSearchListView() {
        searchListView = (ListView) findViewById(R.id.search_list_view);
        searchListAdapter = new SearchListAdapter(mInitWord);
        searchListView.setAdapter(searchListAdapter);

        refreshNoItemText();
    }

    private void initCloseButton() {
        Button closeButton = (Button) view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    public void changeSearchWord(String searchWord) {

        searchListAdapter = new SearchListAdapter(searchWord);
        searchListView.setAdapter(null);
        searchListView.setAdapter(searchListAdapter);

       refreshNoItemText();
    }

    private TextView noItemText;
    private void initNoItemText() {
        noItemText = (TextView) view.findViewById(R.id.no_item_message_text);
    }

    private void refreshNoItemText() {
        if (searchListAdapter.getCount() > 0) {
            noItemText.setVisibility(INVISIBLE);
        } else {
            noItemText.setVisibility(VISIBLE);
        }
    }

    private class SearchListAdapter extends BaseAdapter {

        ArrayList<Place> places;
        ArrayList<Person> persons;

        public SearchListAdapter(String searchWord) {
            places = new ArrayList<>(RVData.getInstance().placeList.getSearchedItems(searchWord, getContext()));
            persons = new ArrayList<>(RVData.getInstance().personList.getSearchedItems(searchWord, getContext()));
        }

        @Override
        public int getCount() {
            return places.size() + persons.size();
        }

        @Override
        public Object getItem(int position) {

            if (position < places.size()) {
                return places.get(position);
            } else {
                return persons.get(position - places.size());
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Object object = getItem(position);

            if (convertView == null) {
                convertView = new SearchCell(getContext(), object,
                        new SearchCell.SearchCellListener() {
                            @Override
                            public void onClick(Object data) {
                                onClickSearchItem(object);
                            }
                        });
            } else {
                ((SearchCell) convertView).refreshData(object);
            }
            return convertView;
        }
    }

    private void onClickSearchItem(Object object) {

        if (mListener == null)
            return;

        if (object instanceof Place) {
            mListener.onClickPlace((Place) object);
        } else if (object instanceof Person) {
            mListener.onClickPerson((Person) object);
        }
    }

    public interface SearchDialogListener{

        void onCancel();

        void onClickPerson(Person person);

        void onClickPlace(Place place);
    }
}
