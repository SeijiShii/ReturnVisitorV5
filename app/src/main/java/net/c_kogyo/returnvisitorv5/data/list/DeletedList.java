package net.c_kogyo.returnvisitorv5.data.list;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.data.DeletedData;
import net.c_kogyo.returnvisitorv5.db.RVRecord;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by SeijiShii on 2017/05/10.
 */

public class DeletedList implements Iterable<DeletedData>{

    private ArrayList<DeletedData> list;

    public DeletedList() {
        list = new ArrayList<>();
    }


    synchronized public <T extends DataItem> void add(T data) {
        list.add(new DeletedData(data));
    }

    synchronized public void add(RVRecord record) {
        list.add(new Gson().fromJson(record.getDataJSON(), DeletedData.class));
    }

    synchronized public void clear() {
        list.clear();
    }

    @Override
    public Iterator<DeletedData> iterator() {
        return list.iterator();
    }
}
