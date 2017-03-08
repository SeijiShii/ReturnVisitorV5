package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.NoteCompItem;

/**
 * Created by SeijiShii on 2017/03/08.
 */

public class NoteCompList extends DataList<NoteCompItem> {

    public void addIfNoSameName(String name) {

        for (NoteCompItem item : list) {
            if (item.getName().equals(name)) {
                return;
            }
        }
        list.add(new NoteCompItem(name));
    }
}
