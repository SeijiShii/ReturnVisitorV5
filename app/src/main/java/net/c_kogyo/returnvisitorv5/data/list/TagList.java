package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.data.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by SeijiShii on 2017/05/08.
 */

public class TagList extends DataList<Tag> {

    private static TagList instance;
    private TagList() {
        super(Tag.class);
    }

    public static TagList getInstance() {
        if (instance == null) {
            instance = new TagList();
        }
        return instance;
    }

    public ArrayList<Tag> getSortedList() {
        ArrayList<Tag> list = new ArrayList<>(super.list);
        Collections.sort(list, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }

    public ArrayList<String> getAllIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (Tag tag : list) {
            ids.add(tag.getId());
        }
        return ids;
    }

    public ArrayList<String> getSearchedTagIds(String searchWord, Context context) {

        if (searchWord.length() <= 0)
            return getAllIds();

        ArrayList<String> ids = new ArrayList<>();
        for (Tag tag : getSearchedItems(searchWord, context)) {
            ids.add(tag.getId());
        }
        return ids;
    }
}
