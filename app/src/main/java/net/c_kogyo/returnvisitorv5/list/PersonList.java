package net.c_kogyo.returnvisitorv5.list;

import net.c_kogyo.returnvisitorv5.data.Person;

import org.json.JSONObject;

/**
 * Created by SeijiShii on 2017/02/21.
 */

public class PersonList extends DataList<Person> {

    public static final String PERSON_LIST = "person_list";

    public PersonList() {
        super(Person.class, PERSON_LIST);
    }

    public PersonList(JSONObject object) {
        super(Person.class, PERSON_LIST, object);
    }

    @Override
    public Person getInstance(JSONObject object) {
        return new Person(object);
    }
}
