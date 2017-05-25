package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static net.c_kogyo.returnvisitorv5.data.Publication.CATEGORY;

/**
 * Created by SeijiShii on 2017/05/24.
 */

public class Placement extends DataItem{

    public static final String PLACEMENT = "placement";
    public static final String PUBLICATION_ID = "publication_id";
    public static final String PUBLICATION_DATA = "publication_data";
//    public static final String PLACED_DATE = "placed_date";

    private String publicationId;
//    private Calendar placedDate;  // Placementが所属するVisitから抽出するのが正しい。
    private String publicationData;
    private Publication.Category category;

    public Placement(Publication publication, Context context) {
        super(PLACEMENT);

        this.publicationId = publication.getId();
        this.publicationData = publication.toString(context);
        this.category = publication.getCategory();

    }

    public Placement(JSONObject object) {
        super(object);

        try {

            if (object.has(PUBLICATION_ID))
                this.publicationId = object.getString(PUBLICATION_ID);

            if (object.has(PUBLICATION_DATA))
                this.publicationData = object.getString(PUBLICATION_DATA);

            if (object.has(CATEGORY))
                this.category = Publication.Category.valueOf(object.getString(CATEGORY));

        } catch (JSONException e) {
            //
        }
    }

    public Placement(Record record) {
        this(record.getDataJSON());
    }

    public JSONObject jsonObject() {
        JSONObject object = super.jsonObject();

        try {
            object.put(PUBLICATION_ID, this.publicationId);
            object.put(PUBLICATION_DATA, this.publicationData);
            object.put(CATEGORY, this.category.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public String getPublicationData() {
        return publicationData;
    }

    public Publication.Category getCategory() {
        return category;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Placement clonedPlacement = (Placement) super.clone();

        clonedPlacement.publicationId = this.publicationId;
        clonedPlacement.publicationData = this.publicationData;
        clonedPlacement.category = this.category;

        return clonedPlacement;
    }
}
