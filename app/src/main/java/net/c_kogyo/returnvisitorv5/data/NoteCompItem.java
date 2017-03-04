package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONObject;

import java.net.PortUnreachableException;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class NoteCompItem extends DataItem {

    public static final String NOTE_COMP_ITEM = "note_comp_item";

    public NoteCompItem(String name) {
        super(NOTE_COMP_ITEM);
        this.name = name;
    }

    public NoteCompItem(Record record) {
        super(record.getDataJSON());
    }

//    public NoteCompItem(JSONObject object) {
//        super(object);
//        setJSON(this, object);
//    }
//
//    public static NoteCompItem setJSON(NoteCompItem item, JSONObject object) {
//        return (NoteCompItem) DataItem.setJSON(item, object);
//    }
}
