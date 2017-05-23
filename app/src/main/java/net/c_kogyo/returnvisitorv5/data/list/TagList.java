package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/05/08.
 */

public class TagList extends DataList<Tag> {

    @Override
    synchronized public ArrayList<Tag> getList() {
        ArrayList<Tag> list = new ArrayList<>(super.list);
        Collections.sort(list, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }
}
