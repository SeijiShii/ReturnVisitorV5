package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sayjey on 2015/06/18.
 */
public class Person extends DataItem implements Cloneable{

    public static final String PERSON       = "person";

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

    public enum Priority {

        NONE(0),
        NEGATIVE(1),
        FOR_NEXT(2),
        NOT_HOME(3),
        BUSY(4),
        LOW(5),
        MIDDLE(6),
        HIGH(7);

        private final int num;

        Priority(int num) {
            this.num = num;
        }

        public static Priority getEnum (int num) {

            Priority[] enumArray = Priority.values();

            for (Priority priority : enumArray) {

                if (priority.num() == num) return priority;

            }
            return null;
        }
        public int num(){
            return num;
        }

    }

    private Sex sex;
    private Age age;
    private Priority priority;

    private ArrayList<String> placeIds;

    public Person(){}

    public Person(String placeId) {
        super(PERSON);
        initCommon(placeId);
    }

    private void initCommon(String placeId) {
        this.sex = Sex.SEX_UNKNOWN;
        this.age = Age.AGE_UNKNOWN;
        this.placeIds = new ArrayList<>();
        this.priority = Priority.NONE;

        if (placeId == null) return;
        this.placeIds.add(placeId);
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

    private String getSexString(Context context) {

        String[] sexStringArray = context.getResources().getStringArray(R.array.sex_array);
        return sexStringArray[sex.num()];

    }

    private String getAgeString(Context context) {

        String[] ageStringArray = context.getResources().getStringArray(R.array.age_array);
        return ageStringArray[age.num()];

    }

    private String getPriorityString(Context context) {

        String[] priorityStringArray = context.getResources().getStringArray(R.array.priority_array);
        return priorityStringArray[priority.num()];
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        Person person = (Person) super.clone();

        person.sex  = this.sex;
        person.age  = this.age;
        person.placeIds = new ArrayList<>(this.placeIds);
        person.priority = this.priority;

        return person;
    }

    @Override
    public String toStringForSearch(Context context) {

        StringBuilder builder = new StringBuilder(super.toStringForSearch(context));
        builder.append(getSexString(context)).append(" ");
        builder.append(getAgeString(context)).append(" ");
        builder.append(getPriorityString(context)).append(" ");

        // DONE: 2017/05/26 タグも対象とするか
        Visit visit = VisitList.getInstance().getLatestVisitToPerson(id);
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

        if (sex != Sex.SEX_UNKNOWN) {
            builder.append(getSexString(context)).append(" ");
        }

        builder.append(getAgeString(context)).append(" ");
        builder.append(note);

        return builder.toString();
    }

    public ArrayList<String> getPlaceIds() {
        return placeIds;
    }

    public void setPlaceIds(ArrayList<String> placeIds) {
        this.placeIds = placeIds;
        
    }

    public Priority getPriority() {

        return priority;
    }

    /**
     *
     * @return 結果的に優先度が変更になっていればTRUE
     */
    public boolean setPriorityFromLatestVisitDetail() {

        Priority oldPriority = priority;

        Visit visit = VisitList.getInstance().getLatestVisitToPerson(id);

        if (visit != null) {

            VisitDetail visitDetail = visit.getVisitDetail(id);
            if (visitDetail != null) {
                priority = visitDetail.getPriority();
            }
        }
        // 変更になっていればTRUE
        return priority != oldPriority;
    }

    // DONE: 2017/06/02 Priorityを人に属するようにする

}
