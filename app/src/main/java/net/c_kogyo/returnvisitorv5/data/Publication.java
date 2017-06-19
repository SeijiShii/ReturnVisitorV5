package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;
import android.support.v4.util.Pair;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by 56255 on 2016/07/19.
 */
public class Publication extends DataItem implements Cloneable{

    public enum Category {

        BIBLE(0),
        BOOK(1),
        TRACT(2),
        MAGAZINE(3),
        WEB_LINK(4),
        SHOW_VIDEO(5),
        OTHER(6);

        private final int num;

        Category(int num) {
            this.num = num;
        }

        public static Category getEnum (int num) {

            Category[] enumArray = Category.values();

            for (Category category : enumArray) {

                if (category.num() == num) return category;

            }
            return null;
        }
        public int num(){
            return num;
        }
    }

    public enum MagazineCategory {

        WATCHTOWER(0),
        STUDY_WATCHTOWER(1),
        AWAKE(2);

        private final int num;

        MagazineCategory(int num) {
            this.num = num;
        }

        public static MagazineCategory getEnum(int num) {

            MagazineCategory[] enumArray = MagazineCategory.values();

            for (MagazineCategory magCategory : enumArray) {

                if (magCategory.num() == num) return magCategory;
            }
            return null;
        }

        public int num(){
            return num;
        }
    }

    public static final String PUBLICATION = "Publication";

    public static final String CATEGORY = "category";
    public static final String MAGAZINE_CATEGORY = "magazine_category";
    public static final String NUMBER = "number";

    private Category category;
    private MagazineCategory magCategory;
    private Calendar number;
    private int weight;

    public Publication() {
        super(PUBLICATION);

        this.category = Category.OTHER;
        this.magCategory = MagazineCategory.WATCHTOWER;
        this.number = Calendar.getInstance();

        this.weight = 0;

    }

    public Publication(Category category) {

        this();

        this.category = category;
    }

    public Publication(RVRecord RVRecord) {
        this(RVRecord.getDataJSON());
    }

    public Publication(JSONObject object) {
        super(object);

        try {
            if (object.has(CATEGORY))
                this.category = Category.valueOf(object.getString(CATEGORY));
            if (object.has(MAGAZINE_CATEGORY))
                this.magCategory = MagazineCategory.valueOf(object.getString(MAGAZINE_CATEGORY));
            if (object.has(NUMBER)) {
                this.number = Calendar.getInstance();
                this.number.setTimeInMillis(object.getLong(NUMBER));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject jsonObject() {

        JSONObject object = super.jsonObject();

        try {
            object.put(CATEGORY, this.category);
            object.put(MAGAZINE_CATEGORY, this.magCategory);
            object.put(NUMBER, this.number.getTimeInMillis());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

//    public Publication(HashMap<String, Object> map) {
//
//        super();
//        setMap(map);
//    }

    public String toString(Context context) {

        String[] catArray = context.getResources().getStringArray(R.array.placement_array);
        String[] magArray = context.getResources().getStringArray(R.array.magazine_array);

        StringBuilder builder = new StringBuilder();

        if (category != Category.MAGAZINE) {

            builder.append(catArray[category.num()]);

        } else {

            builder.append(magArray[magCategory.num()])
                    .append(" ")
                    .append(getNumberString(number, magCategory, context));
        }

        if (name.length() > 0) {
            builder.append(" ").append(name);
        }

        return builder.toString();
    }

    @Override
    public String toStringForSearch(Context context) {
        return toString(context);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        onUpdate();
    }

    public MagazineCategory getMagCategory() {
        return magCategory;
    }

    public void setMagCategory(MagazineCategory magCategory) {
        this.magCategory = magCategory;
        onUpdate();
    }

    public Calendar getNumber() {

        return number;

    }

    public void setNumber(Calendar number) {
        this.number = number;
        onUpdate();
    }

    public static String getNumberString(Calendar number, MagazineCategory magCategory, Context context) {

        String magNumString;

        SimpleDateFormat yFormat = new SimpleDateFormat("yyyy");
        String yearString = yFormat.format(number.getTime());

        if (magCategory == MagazineCategory.STUDY_WATCHTOWER) {

            SimpleDateFormat mFormat = new SimpleDateFormat("MMMM");
            String monthString = mFormat.format(number.getTime());

            magNumString = context.getString(R.string.magazine_number_month, monthString, yearString);

        } else {

            String numString = String.valueOf((number.get(Calendar.MONTH) + 2 ) / 2);

            magNumString = context.getString(R.string.magazine_number_number, numString, yearString);
        }
        return magNumString;

    }

    static public ArrayList<Pair<Calendar, String>> getMagazineNumberArrayList(MagazineCategory magCategory, Context context) {

        ArrayList<Pair<Calendar, String>> list = new ArrayList<>();
        Calendar numberCounter = Calendar.getInstance();
        numberCounter.add(Calendar.MONTH, -11);

        if (magCategory == MagazineCategory.STUDY_WATCHTOWER) {
            // 現在月が12(#11)番目（1年前まで指定可能）　現在月より3つ先まで表示

            for ( int i = 0; i < 15 ; i++ ) {

                Calendar clonedNumber = (Calendar) numberCounter.clone();
                String str = getNumberString(numberCounter, magCategory, context);
                Pair<Calendar, String> pair = new Pair<>(clonedNumber, str);
                list.add(pair);


                numberCounter.add(Calendar.MONTH, 1);
            }

        } else {
            // 現在月が6(#5)番目（1年前まで指定可能）　現在月より3つ先まで表示

            for ( int i = 0; i < 9 ; i++ ) {

                Calendar clonedNumber = (Calendar) numberCounter.clone();
                String str = getNumberString(numberCounter, magCategory, context);
                Pair<Calendar, String> pair = new Pair<>(clonedNumber, str);
                list.add(pair);

                numberCounter.add(Calendar.MONTH, 2);
            }

        }

        return list;
    }

    public boolean equals(Publication pub) {

        if (super.equals(pub))
            return true;

        if (this.category == Category.MAGAZINE) {

            return this.magCategory == pub.magCategory && CalendarUtil.isSameNumber(this.number, pub.number);

        } else {

            return this.category == pub.category && this.name.equals(pub.name);

        }
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
        onUpdate();
    }

    //    public Publication clone (boolean withPlacedDate)  throws CloneNotSupportedException{
//
//        Publication clonedPlc = (Publication) super.clone();
//
//        clonedPlc.category = this.category;
//        clonedPlc.magCategory = this.magCategory;
//        clonedPlc.number = (Calendar) this.number.clone();
//
//        if (withPlacedDate) {
//            clonedPlc.placedDate = (Calendar) this.placedDate.clone();
//        }else {
//            clonedPlc.placedDate = Calendar.getInstance();
//        }
//
//        return clonedPlc;
//    }

}
