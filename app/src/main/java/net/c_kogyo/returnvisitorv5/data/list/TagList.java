package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import net.c_kogyo.returnvisitorv5.data.RVRecord;
import net.c_kogyo.returnvisitorv5.data.Tag;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by SeijiShii on 2017/05/08.
 */

public class TagList {

    @Nullable
    public static Tag loadTag(String tagId, RVDBHelper helper) {
        RVRecord record = helper.loadRecord(tagId, false);
        if (record == null) return null;

        return new Gson().fromJson(record.getDataJSON(), Tag.class);
    }

    private static ArrayList<Tag> loadList(RVDBHelper helper) {
        return helper.loadList(Tag.class, false);
    }

    public static ArrayList<Tag> getSortedList(RVDBHelper helper) {
        ArrayList<Tag> list = loadList(helper);
        Collections.sort(list, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }

    public static ArrayList<String> getAllIds(RVDBHelper helper) {
        ArrayList<String> ids = new ArrayList<>();
        for (Tag tag : loadList(helper)) {
            ids.add(tag.getId());
        }
        return ids;
    }

    public static ArrayList<String> getSearchedTagIds(String searchWord,
                                                      Context context,
                                                      RVDBHelper helper) {
        if (searchWord.length() <= 0)
            return getAllIds(helper);

        ArrayList<String> ids = new ArrayList<>();
        for (Tag tag : helper.getSearchedItems(Tag.class, searchWord, context)) {
            ids.add(tag.getId());
        }
        return ids;
    }


}
