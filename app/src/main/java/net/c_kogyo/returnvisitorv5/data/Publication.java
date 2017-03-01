package net.c_kogyo.returnvisitorv5.data;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class Publication extends DataItem {

    public static final String PUBLICATION = "publication";
    public Publication() {
        super(PUBLICATION);
    }

    public Publication(Record record) {
        super(record.getDataJSON());
    }

//    public Publication(JSONObject object) {
//
//        setJSON(this, object);
//    }
//
//    public static Publication setJSON(Publication publication, JSONObject object) {
//        return (Publication) DataItem.setJSON(publication, object);
//    }
}
