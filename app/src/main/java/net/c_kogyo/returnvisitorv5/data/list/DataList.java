package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;
import android.util.Log;

import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.data.DeletedData;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by SeijiShii on 2016/07/24.
 */


public class DataList<T extends DataItem> implements Iterable<T>{

    private final String TAG = "DataList";

    // DONE: 2017/03/16 さくじょしたものが拾われないようにする
    // DONE: 2017/05/10 削除フラグの撤去に伴う変更
    protected CopyOnWriteArrayList<T> list;
    private Class<T> mClass;

    DataList() {
        this.list = new CopyOnWriteArrayList<>();
    }

    public DataList(Class<T> tClass) {
        this.list = new CopyOnWriteArrayList<>(RVDBHelper.getInstance().loadList(tClass, false));
        this.mClass = tClass;
    }

    synchronized public void refreshByDB() {
        this.list = new CopyOnWriteArrayList<>(RVDBHelper.getInstance().loadList(mClass, false));
//        Log.d(TAG, "After refresh list by DB, Data count: " + list.size());
    }

    synchronized public void setOrAdd(T data) {

        if (contains(data)) {
            list.set(indexOf(data), data);
        } else {
            list.add(data);
        }
        RVDBHelper.getInstance().save(data);
    }

    synchronized public void addList(ArrayList<T> list) {
        for (T item : list) {
            setOrAdd(item);
        }
    }

    // DONE: 2017/06/23 要チェック
    synchronized public void removeList(ArrayList<T> list) {
        for (T item : list) {
            delete(item);
        }
    }

    synchronized public int indexOf(T data) {

        for (int i = 0; i < list.size() ; i++ ) {

            T data1 = list.get(i);

            if (data.equals(data1)) {
                return i;
            }
        }
        return -1;
    }

    synchronized public boolean contains(T data) {
        return indexOf(data) >= 0;
    }

    synchronized public T getById(String id) {

        for ( T data : list ) {
            if (data.getId().equals(id)) {
                return data;
            }
        }
        return null;
    }

    synchronized private void delete(T data) {
        list.remove(data);
        RVDBHelper.getInstance().saveAsDeleted(data);
    }

    synchronized public void deleteAll(ArrayList<T> dataList) {
        for ( T data : dataList ) {
            delete(data);
        }
    }

    synchronized public void deleteById(String id) {

        T data = getById(id);
        if ( data == null ) return;

        delete(data);
    }

    synchronized public void deleteByDeletedData(DeletedData deletedData) {
        T data = getById(deletedData.getDataId());
        if ( data == null ) return;

        list.remove(data);
    }

    synchronized private void removeByIdIfContained(String id) {

        T data = getById(id);
        if ( data == null ) return;

        list.remove(data);
    }

    @Override
    synchronized public Iterator<T> iterator() {
        return list.iterator();
    }

    synchronized public CopyOnWriteArrayList<T> getList() {
        return list;
    }

    synchronized public int size() {

        return list.size();
    }

    synchronized public T get(int index) {
        return list.get(index);
    }

    synchronized public boolean containsDataWithName(String data) {

        for (T item : list) {
            if (item.getName().equals(data)) {
                return true;
            }
        }
        return false;
    }

    synchronized public ArrayList<T> getSearchedItems(String searchWord, Context context) {

        String[] words = searchWord.split(" ");

        ArrayList<T> searchResultItems = new ArrayList<>(list);

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

//    synchronized public ArrayList<T> getListLaterThanTime(long dataTimeInMills) {
//        ArrayList<T> laterList = new ArrayList<>();
//        for ( T data : list ) {
//            if (data.getUpdatedAt() >= dataTimeInMills) {
//                laterList.add(data);
//            }
//        }
//        return laterList;
//    }

    public ArrayList<T> getList(ArrayList<String> ids) {
        ArrayList<T> items = new ArrayList<>();
        for (String id : ids) {
            if( getById(id) != null){
                items.add(getById(id));
            }
        }
        return items;
    }

    public boolean hasItem(String id) {
        return getById(id) != null;
    }

}

