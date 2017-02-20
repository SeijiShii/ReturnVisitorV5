package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 56255 on 2016/07/19.
 */
public class Visit extends TimePeriodDataItem{

    public static final String VISIT = "visit";
    public static final String PLACE_ID = "place_id";
    public static final String PERSON_IDS = "person_ids";
    public static final String PLACEMENTS = "placements";
    public static final String RV_COUNT = "rv_count";
    public static final String IS_BS = "is_bs";

    private String placeId;
    private ArrayList<String> personIds;
    private ArrayList<Placement> placements;
    private int rvCount;
    private boolean isBS;

    public Visit() {
        super();

        this.placeId = null;
        this.personIds = new ArrayList<>();
        this.placements = new ArrayList<>();
        this.rvCount = 0;
        this.isBS = false;
    }

    @Override
    public String getIdHeader() {
        return VISIT;
    }

    @Override
    public String toStringForSearch(Context context) {

        // Visitをsearchすることはあるのだろうか
        return null;
    }

    @Override
    public HashMap<String, Object> toMap() {

        HashMap<String, Object> map = super.toMap();

        // NULLなら特に何も記録しない
        map.put(PLACE_ID, placeId);
        map.put(PERSON_IDS, personIds);
        map.put(RV_COUNT, rvCount);
        map.put(IS_BS, isBS);

        ArrayList<HashMap<String, Object>> mapList = new ArrayList<>();
        for ( int i = 0 ; i < placements.size() ; i++ ) {
            mapList.add(placements.get(i).toMap());
        }
        map.put(PLACEMENTS, mapList);

        return map;
    }

    public void addPlacement(Placement placement) {
        placements.add(placement);
    }

    public void removePlacement(Placement placement) {
        placements.remove(placement);
    }

    public int getRvCount() {
        return rvCount;
    }

    public void setRvCount(int rvCount) {
        this.rvCount = rvCount;
    }

    public int refreshRVCount(Context context) {

        ArrayList<Person> persons = RVData.getInstance().personList.getList(personIds);

        int count = 0;

        for (Person person : persons) {
            if (person.isRV(context)) {
                count++;
            }
        }
        rvCount = count;
        return count;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void addPersonId(String personId) {

        if (!this.personIds.contains(personId))
            personIds.add(personId);
    }

    public ArrayList<String> getPersonIds() {
        return personIds;
    }

    public void setPersonIds(ArrayList<String> personIds) {
        this.personIds = personIds;
    }

    public ArrayList<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(ArrayList<Placement> placements) {
        this.placements = placements;
    }

    @Override
    public void setMap(HashMap<String, Object> map) {
        super.setMap(map);

        // NULL場所訪問もあるからね
        if (map.containsKey(PLACE_ID)) {
            this.placeId = map.get(PLACE_ID).toString();
        }

        if (map.containsKey(RV_COUNT)) {
            this.rvCount = Integer.valueOf(map.get(RV_COUNT).toString());
        }

        if (map.containsKey(IS_BS)) {
            this.isBS = Boolean.valueOf(map.get(IS_BS).toString());
        }

        // HashMap to ArrayList
        Object o = map.get(PERSON_IDS);
        if ( o != null ) {

            this.personIds = (ArrayList<String>) o;
            o = null;
        }

        // placementsのHashMapからのデータ回収
        Object o1 = map.get(PLACEMENTS);
        if (o1 != null) {

            this.placements = new ArrayList<>();

            ArrayList<Object> oList = (ArrayList<Object>) o1;
            for (Object o2 : oList) {

                HashMap<String, Object> map1 = (HashMap<String, Object>) o2;
                this.placements.add(new Placement(map1));
            }

        }

    }

    public Person.Interest getInterest() {

        ArrayList<Person> persons = RVData.getInstance().personList.getPersons(personIds);

        Person.Interest interest = Person.Interest.NONE;

        for (Person person : persons) {

            if (person.getInterest().num() > interest.num()){
                interest = person.getInterest();
            }
        }
        return interest;
    }

    public Person getBestPerson() {

        ArrayList<Person> persons = RVData.getInstance().personList.getPersons(personIds);

        if (persons.size() <= 0) {
            return null;
        }

        Person person = persons.get(0);

        for (Person person1 : persons) {

            if (person1.getInterest().num() > person.getInterest().num()){
                person = person1;
            }
        }
        return person;

    }

    public String toDataString(Context context) {

        StringBuilder builder = new StringBuilder();
        Person person = getBestPerson();


        if (person != null) {

            if (person.getName() != null && !person.getName().equals("")){
                builder.append(person.getName()).append("");
            }
        }

        return builder.toString();

    }

    // そう考えると場所無き訪問もあるよね

    public int getPlacementCount() {

        int count = 0;
        for (Placement plc : placements) {
            if (plc.getCategory() != Placement.Category.SHOW_VIDEO
                    && plc.getCategory() != Placement.Category.OTHER) {
                count++;
            }
        }
        return count;
    }

    public int getShowVideoCount() {

        int count = 0;
        for (Placement plc : placements) {
            if (plc.getCategory() == Placement.Category.SHOW_VIDEO) {
                count++;
            }
        }
        return count;
    }

    public boolean isBS() {
        return isBS;
    }

    public void setBS(boolean bs) {
        isBS = bs;
    }

    // BSに挨拶で立ち寄ることもあるので
    public boolean canBeBS(Context context) {

        for (String id  : personIds) {

            Person person = RVData.getInstance().personList.getById(id);
            if (person != null) {
                if (person.isBS(context)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasPersonId (String id) {

        for (String id1 : this.personIds) {
            if (id.equals(id1)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSamePersonIds(Visit visit) {

        ArrayList<String> ids1 = new ArrayList<>(this.personIds);
        ArrayList<String> ids2 = new ArrayList<>(visit.personIds);

        ids1.removeAll(visit.personIds);
        ids2.removeAll(this.personIds);

        return ids1.size() <= 0 && ids2.size() <= 0;

    }
}
