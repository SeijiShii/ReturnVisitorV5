package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.NoteCompItem;

/**
 * Created by SeijiShii on 2017/03/08.
 */

public class NoteCompList extends DataList<NoteCompItem> {

    public NoteCompList() {
        super(NoteCompItem.class);
    }

    synchronized public void addIfNoSameName(String name) {

        for (NoteCompItem item : this) {
            if (item.getName().equals(name)) {
                return;
            }
        }
        setOrAdd(new NoteCompItem(name));
    }
}
