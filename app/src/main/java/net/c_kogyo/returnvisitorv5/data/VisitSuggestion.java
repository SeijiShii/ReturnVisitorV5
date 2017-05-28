package net.c_kogyo.returnvisitorv5.data;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class VisitSuggestion {

    private Person person;
    private Visit latestVisit;

    public VisitSuggestion(Person person, Visit latestVisit) {
        this.person = person;
        this.latestVisit = latestVisit;
    }

    public Person getPerson() {
        return person;
    }

    public Visit getLatestVisit() {
        return latestVisit;
    }
}
