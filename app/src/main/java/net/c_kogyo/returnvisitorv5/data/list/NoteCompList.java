package net.c_kogyo.returnvisitorv5.data.list;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.NoteCompItem;
import net.c_kogyo.returnvisitorv5.data.RVRecord;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/03/08.
 */

public class NoteCompList {

    private static ArrayList<NoteCompItem> loadList(RVDBHelper helper) {
        ArrayList<NoteCompItem> itemList = new ArrayList<>();
        for (RVRecord record : helper.loadRecords(NoteCompItem.class)) {
            itemList.add(new Gson().fromJson(record.getDataJSON(), NoteCompItem.class));
        }
        return itemList;
    }

    static public void addIfNoSameName(String name, RVDBHelper helper) {

        for (NoteCompItem item : loadList(helper)) {
            if (item.getName().equals(name)) {
                return;
            }
        }
        helper.save(new NoteCompItem(name));
    }
}
