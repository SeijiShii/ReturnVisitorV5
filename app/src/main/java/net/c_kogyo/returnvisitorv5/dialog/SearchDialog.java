package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.activity.VisitSuggestionActivity;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.Place;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.util.InputUtil;
import net.c_kogyo.returnvisitorv5.view.SearchCell;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SeijiShii on 2017/05/26.
 */

public class SearchDialog extends DialogFragment {

    private static SearchDialogListener mListener;
    private static String mInitWord;
    private static SearchDialog instance;
    
    public static SearchDialog getInstance (SearchDialogListener listener,
                                            String initialSearchWord) {

        mListener = listener;
        mInitWord = initialSearchWord;

        if (instance == null) {
            instance = new SearchDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        initCommon();
        builder.setView(view);

        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onCloseDialog();
                }
                dismiss();
            }
        });
        
        return builder.create();
    }

    private View view;
    private void initCommon() {
        view = View.inflate(getActivity(), R.layout.search_dialog, null);

        initSearchText();
        initNoItemText();
        initSearchListView();
    }

    private void initSearchText() {
        EditText searchText = (EditText) view.findViewById(R.id.search_text);
        searchText.setText(mInitWord);
        searchText.requestFocus();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                searchListAdapter = new SearchListAdapter(s.toString(), getActivity());
//                searchListView.setAdapter(null);
                searchListView.setAdapter(searchListAdapter);

                refreshNoItemText();

                if (mListener != null) {
                    mListener.onTextChanged(s.toString());
                }
            }
        });
    }

    private ListView searchListView;
    private SearchListAdapter searchListAdapter;
    private void initSearchListView() {
        searchListView = (ListView) view.findViewById(R.id.search_list_view);
        searchListAdapter = new SearchListAdapter(mInitWord, getActivity());
        searchListView.setAdapter(searchListAdapter);
        searchListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    InputUtil.hideSoftKeyboard(getActivity());
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        refreshNoItemText();
    }

//    public void changeSearchWord(String searchWord, Context context) {
//
//        searchListAdapter = new SearchListAdapter(searchWord, context);
//        searchListView.setAdapter(null);
//        searchListView.setAdapter(searchListAdapter);
//
//       refreshNoItemText();
//    }

    private TextView noItemText;
    private void initNoItemText() {
        noItemText = (TextView) view.findViewById(R.id.no_item_message_text);
    }

    private void refreshNoItemText() {
        if (searchListAdapter.getCount() > 0) {
            noItemText.setVisibility(View.INVISIBLE);
        } else {
            noItemText.setVisibility(View.VISIBLE);
        }
    }

    private class SearchListAdapter extends BaseAdapter {

        ArrayList<Place> places;
        ArrayList<Person> persons;

        public SearchListAdapter(String searchWord, Context context) {
            places = new ArrayList<>(RVData.getInstance().placeList.getSearchedItems(searchWord, context));
            persons = new ArrayList<>(RVData.getInstance().personList.getSearchedItems(searchWord, context));
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
                convertView = new SearchCell(getActivity(), object,
                        new SearchCell.SearchCellListener() {

                            @Override
                            public void onClickShowInMap(Object data) {
                                SearchDialog.this.onClickShowInMap(object);
                            }

                            @Override
                            public void onClickEditPerson(Person person) {
                                if (mListener != null) {
                                    mListener.onClickEditPerson(person);
                                }
                                dismiss();
                            }
                        });
            } else {
                ((SearchCell) convertView).refreshData(object);
            }
            return convertView;
        }
    }

    private void onClickShowInMap(Object object) {

        if (mListener == null)
            return;

        if (object instanceof Place) {
            mListener.onClickShowPlaceInMap((Place) object);
        } else if (object instanceof Person) {
            mListener.onClickShowPersonInMap((Person) object);
        }
        dismiss();
    }

    public interface SearchDialogListener{

        void onCloseDialog();

        void onClickShowPersonInMap(Person person);

        void onClickShowPlaceInMap(Place place);

        void onTextChanged(String text);

        void onClickEditPerson(Person person);
    }

    public static AtomicBoolean isShowing = new AtomicBoolean(false);

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShowing.getAndSet(true)) return;

        try {
            super.show(manager, tag);
        } catch (Exception e) {
            isShowing.set(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowing.set(false);
    }
}
