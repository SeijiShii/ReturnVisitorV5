package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.DataItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by SeijiShii on 2016/07/24.
 */


public class DataList<T extends DataItem> implements Iterable<T>{

    protected ArrayList<T> list;

    public DataList() {

        this.list = new ArrayList<>();

    }

    private void setOrAdd(T data) {

        if (contains(data)) {
            list.set(indexOf(data), data);
        } else {
            list.add(data);
        }

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

    public void add(T item) {
        list.add(item);
    }

    public int size() {
        return list.size();
    }

    public T get(int index) {
        return list.get(index);
    }

    public boolean containsDataWithName(String data) {

        for (T item : list) {
            if (item.getName().equals(data)) {
                return true;
            }
        }
        return false;
    }
}

