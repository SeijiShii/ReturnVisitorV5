package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.data.DataItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by SeijiShii on 2016/07/24.
 */


public class DataList<T extends DataItem> implements Iterable<T>{

    // DONE: 2017/03/16 さくじょしたものが拾われないようにする
    private ArrayList<T> list;

    public DataList() {

        this.list = new ArrayList<>();

    }

    public void setOrAdd(T data) {

        if (contains(data)) {
            list.set(indexOf(data), data);
        } else {
            list.add(data);
        }
    }

    public void addList(ArrayList<T> list) {
        for (T item : list) {
            setOrAdd(item);
        }
    }

    public void removeList(ArrayList<T> list) {
        this.list.removeAll(list);
    }

    private ArrayList<T> safeList() {
        ArrayList<T> safeList = new ArrayList<>();
        for (T data : list) {
            if (!data.isDeleted()) {
                safeList.add(data);
            }
        }
        return safeList;
    }

    public int indexOf(T data) {

        for (int i = 0; i < safeList().size() ; i++ ) {

            T data1 = safeList().get(i);

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

        for ( T data : safeList() ) {
            if (data.getId().equals(id)) {
                return data;
            }
        }
        return null;
    }

    private void delete(T data) {
        data.setDeleted(true);
    }

    public void deleteAll(ArrayList<T> dataList) {
        for ( T data : dataList ) {
            delete(data);
        }
    }

    public void deleteById(String id) {

        T data = getById(id);
        if ( data == null ) return;

        delete(data);
    }

    private void removeByIdIfContained(String id) {

        T data = getById(id);
        if ( data == null ) return;

        list.remove(data);
    }

    @Override
    public Iterator<T> iterator() {
        return safeList().iterator();
    }

    public ArrayList<T> getList(ArrayList<String> ids) {

        ArrayList<T> arrayList = new ArrayList<>();
        for ( T item : safeList() ) {

            if (ids.contains(item.getId())) {
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    public ArrayList<T> getList() {
        return safeList();
    }

    private void add(T item) {
        list.add(item);
    }

    public int size() {

        return safeList().size();
    }

    public T get(int index) {
        return safeList().get(index);
    }

    public boolean containsDataWithName(String data) {

        for (T item : safeList()) {
            if (item.getName().equals(data)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<T> getSearchedItems(String searchWord, Context context) {

        String[] words = searchWord.split(" ");

        ArrayList<T> searchResultItems = safeList();

        for (String word : words) {
            List<T> listToRemove = new ArrayList<>();

            for (T t: searchResultItems) {
                if (!t.toStringForSearch(context).contains(word)){
                    listToRemove.add(t);
                }
            }
            searchResultItems.removeAll(listToRemove);
        }
        return searchResultItems;
    }


}

