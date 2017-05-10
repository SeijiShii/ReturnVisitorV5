package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.DataItem;
import net.c_kogyo.returnvisitorv5.data.DeletedData;
import net.c_kogyo.returnvisitorv5.data.Record;

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


    public <T extends DataItem> void onDelete(T data) {
        list.add(new DeletedData(data));
    }

    public void onLoadData(Record record) {
        list.add(new DeletedData(record));
    }

    @Override
    public Iterator<DeletedData> iterator() {
        return list.iterator();
    }
}
