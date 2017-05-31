package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sayjey on 2015/06/18.
 */
public class Person extends DataItem implements Cloneable{

    public static final String PERSON       = "person";

    public static final String SEX      = "sex";
    public static final String AGE      = "age";
    public static final String PLACE_IDS  = "place_ids";

    public enum Sex {
        SEX_UNKNOWN(0),
        MALE(1),
        FEMALE(2);

        private final int num;

        Sex(int num) {
            this.num = num;
        }

        public static Sex getEnum (int num) {

            Sex[] enumArray = Sex.values();

            for (Sex sex : enumArray) {

                if (sex.num() == num) return sex;

            }

            return null;
        }

        public int num(){
            return num;
        }
    }

    public enum Age {

        AGE_UNKNOWN(0),
        AGE__10(1),
        AGE_11_20(2),
        AGE_21_30(3),
        AGE_31_40(4),
        AGE_41_50(5),
        AGE_51_60(6),
        AGE_61_70(7),
        AGE_71_80(8),
        AGE_80_(9);

        final int num;

        Age(int num) {
            this.num = num;
        }

        public static Age getEnum(int num) {

            Age[] enumArray = Age.values();

            for (Age age : enumArray) {

                if (age.num() == num) return age;

            }

            return null;
        }

        public int num() {return num;}

    }

//    public enum Interest {
//
//        NONE(0),
//        REFUSED(1),
//        INDIFFERENT(2),
//        FAIR(3),
//        KIND(4),
//        INTERESTED(5),
//        STRONGLY_INTERESTED(6);
//
//        final int num;
//
//        Interest(int num) {
//            this.num = num;
//        }
//
//        public static Interest getEnum(int num) {
//
//            Interest[] enumArray = Interest.values();
//
//            for (Interest interest : enumArray) {
//
//                if (interest.num() == num) return interest;
//
//            }
//
//            return null;
//        }
//
//        public int num() {return num;}
//
//    }

    private Sex sex;
    private Age age;

    private ArrayList<String> placeIds;

    public Person(){};

    public Person(String placeId) {
        super(PERSON);
        initCommon(placeId);
    }

    private void initCommon(String placeId) {
        this.sex = Sex.SEX_UNKNOWN;
        this.age = Age.AGE_UNKNOWN;
//        this.interest = Interest.NONE;
//        this.tagIds = new ArrayList<>();
        this.placeIds = new ArrayList<>();

        if (placeId == null) return;
        this.placeIds.add(placeId);
    }

    public Person(JSONObject object) {
        super(object);
        initCommon(null);
        setJSON(this, object);

//        try {
//            if (object.has(SEX))            this.sex         = Sex.valueOf(object.get(SEX).toString());
//            if (object.has(AGE))            this.age         = Age.valueOf(object.get(AGE).toString());
//
//            if (object.has(PLACE_IDS)) {
//                this.placeIds = new ArrayList<>();
//                JSONArray array = object.getJSONArray(PLACE_IDS);
//                for ( int i = 0 ; i < array.length() ; i++ ) {
//                    this.placeIds.add(array.getString(i));
//                }
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    public Person(Record record) {
        this(record.getDataJSON());
    }

    public Sex getSex() {
        return sex;
    }

    public Age getAge() {
        return age;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public String getSexString(Context context) {

        String[] sexStringArray = context.getResources().getStringArray(R.array.sex_array);
        return sexStringArray[sex.num()];

    }

    public String getAgeString(Context context) {

        String[] ageStringArray = context.getResources().getStringArray(R.array.age_array);
        return ageStringArray[age.num()];

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        Person person = (Person) super.clone();

        person.sex  = this.sex;
        person.age  = this.age;
        person.placeIds = new ArrayList<>(this.placeIds);

        return person;
    }

    @Override
    public JSONObject jsonObject() {

        JSONObject object = super.jsonObject();

        try {
            object.put(SEX, sex);
            object.put(AGE, age);

            JSONArray array = new JSONArray();
            for ( int i = 0 ; i < this.placeIds.size() ; i++ ) {
                array.put(this.placeIds.get(i));
            }
            object.put(PLACE_IDS, array);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @Override
    public String toStringForSearch(Context context) {

        StringBuilder builder = new StringBuilder(super.toStringForSearch(context));
        builder.append(context.getResources().getStringArray(R.array.sex_array)[this.sex.num()]).append(" ");
        builder.append(context.getResources().getStringArray(R.array.age_array)[this.age.num()]).append(" ");

        // DONE: 2017/05/26 タグも対象とするか
        Visit visit = RVData.getInstance().visitList.getLatestVisitToPerson(id);
        if (visit != null) {
            VisitDetail visitDetail = visit.getVisitDetail(id);
            if (visitDetail != null) {
                ArrayList<Tag> tags = RVData.getInstance().tagList.getList(visitDetail.getTagIds());
                for (Tag tag : tags) {
                    builder.append(" ").append(tag.getName());
                }
            }
        }

        return builder.toString();
    }

    public String toString(Context context) {

        StringBuilder builder = new StringBuilder();

        if (name.length() > 0) {
            builder.append(name).append(" ");
        }

        String[] sexArray = context.getResources().getStringArray(R.array.sex_array);
        if (sex != Sex.SEX_UNKNOWN) {
            builder.append(sexArray[sex.num()]).append(" ");
        }

        String[] ageArray = context.getResources().getStringArray(R.array.age_array);
        builder.append(ageArray[age.num()]).append(" ");
        builder.append(note);

        return builder.toString();
    }

    public ArrayList<String> getPlaceIds() {
        return placeIds;
    }

    public void setPlaceIds(ArrayList<String> placeIds) {
        this.placeIds = placeIds;
    }

    public static Person setJSON(Person person, JSONObject object) {
        try {
            if (object.has(SEX))            person.sex         = Sex.valueOf(object.get(SEX).toString());
            if (object.has(AGE))            person.age         = Age.valueOf(object.get(AGE).toString());

            if (object.has(PLACE_IDS)) {
                person.placeIds = new ArrayList<>();
                JSONArray array = object.getJSONArray(PLACE_IDS);
                for ( int i = 0 ; i < array.length() ; i++ ) {
                    person.placeIds.add(array.getString(i));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return person;
    }

    public Visit.Priority getPriority() {

        Visit.Priority priority = Visit.Priority.NONE;
        Visit visit = RVData.getInstance().visitList.getLatestVisitToPerson(id);

        if (visit != null) {

            VisitDetail visitDetail = visit.getVisitDetail(id);
            if (visitDetail != null) {
                priority = visitDetail.getPriority();
            }
        }
        return priority;
    }

}
