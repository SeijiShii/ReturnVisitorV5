package net.c_kogyo.returnvisitorv5.list;

import net.c_kogyo.returnvisitorv5.data.DataItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by SeijiShii on 2016/07/24.
 */


public abstract class DataList<T extends DataItem> implements Iterable<T>{

    public static final String DATA_LIST_TAG = "DataList";

    protected ArrayList<T> list;
    private Class<T> klass;
    private String mListHeader;

    DataList(final Class<T> clazz, String listHeader) {

        this.mListHeader = listHeader;
        this.klass = clazz;
        this.list = new ArrayList<>();

    }

    DataList(final Class<T> clazz, String listHeader, JSONObject object) {
        this(clazz, listHeader);

        try {
            if (object.has(mListHeader)) {
                JSONArray array = object.getJSONArray(mListHeader);
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    this.list.add(getInstance(array.getJSONObject(i)));
                }
            }
        } catch (JSONException  e) {
            e.printStackTrace();
        }
    }

    public abstract T getInstance(JSONObject object);

    private void setToList(T data) {

        list.set(indexOf(data), data);
    }

    private int indexOf(T data) {

        for ( int i = 0 ; i < list.size() ; i++ ) {

            T data1 = list.get(i);

            if (data.equals(data1)) {
                return i;
            }
        }
        return -1;

    }

    public boolean contains(T data) {
        return indexOf(data) >= 0;
    }

    public T getById(String id) {

        for ( T data : list ) {
            if (data.getId().equals(id)) {
                return data;
            }
        }
        return null;
    }

    private void remove(T data) {
        list.remove(data);
    }

    public void removeById(String id) {

        T data = getById(id);
        if ( data == null ) return;

        remove(data);
    }

    private void removeByIdIfContained(String id) {

        T data = getById(id);
        if ( data == null ) return;

        list.remove(data);
        onDataChanged(data);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public ArrayList<T> getList(ArrayList<String> ids) {

        ArrayList<T> arrayList = new ArrayList<>();
        for ( T item : list ) {

            if (ids.contains(item.getId())) {
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    public void clearFromLocal() {
        list.clear();

    }

    public ArrayList<T> getList() {
        return list;
    }

    public abstract void onDataChanged(T data);


}

