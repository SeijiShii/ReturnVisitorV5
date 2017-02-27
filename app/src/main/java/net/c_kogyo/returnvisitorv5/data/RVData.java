package net.c_kogyo.returnvisitorv5.data;

import net.c_kogyo.returnvisitorv5.data.list.DataList;
import net.c_kogyo.returnvisitorv5.data.list.PersonList;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class RVData {

    public static final String TAG_LIST = "tag_list";
    public static final String NOTE_COMP_LIST = "note_comp_list";
    public static final String PUB_LIST = "pub_list";

    private PlaceList placeList;
    private PersonList personList;
    private VisitList visitList;
    private DataList tagList;
    private DataList noteCompList;
    private DataList pubList;

    private static RVData instance = new RVData();
    private RVData() {

        placeList = new PlaceList();
        personList = new PersonList();
        visitList = new VisitList();

        tagList = new DataList(TAG_LIST);
        noteCompList = new DataList(NOTE_COMP_LIST);
        pubList = new DataList(PUB_LIST);

    }

    public static RVData getInstance() {return instance;}

    public PersonList getPersonList() {
        return personList;
    }

    public PlaceList getPlaceList() {
        return placeList;
    }

    public VisitList getVisitList() {
        return visitList;
    }

    public DataList getTagList() {
        return tagList;
    }

    public DataList getNoteCompList() {
        return noteCompList;
    }

    public DataList getPubList() {
        return pubList;
    }
}
