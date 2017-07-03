package net.c_kogyo.returnvisitorv5.data;

import net.c_kogyo.returnvisitorv5.data.Visit;

import java.util.ArrayList;

/**
 * Created by SeijiShii on 2017/05/31.
 */

public class Filter {

    ArrayList<Person.Priority> priorities;
    ArrayList<String> tagIds;
    ArrayList<String> searchWords;

    public Filter() {
        priorities = new ArrayList<>();

        for (int i = 0 ; i < 5 ;i++ ) {
            priorities.add(Person.Priority.values()[i + 3]);
        }

        tagIds = new ArrayList<>();
        searchWords = new ArrayList<>();
    }

    public Filter(ArrayList<Person.Priority> priorities,
                  ArrayList<String> tagIds,
                  ArrayList<String> searchWords) {

        this();

        this.priorities = priorities;
        this.tagIds = tagIds;
        this.searchWords = searchWords;
    }

    public ArrayList<Person.Priority> getPriorities() {
        return priorities;
    }

    public void setPriorities(ArrayList<Person.Priority> priorities) {
        this.priorities = priorities;
    }

    public ArrayList<String> getTagIds() {
        return tagIds;
    }

    public void setTagIds(ArrayList<String> tagIds) {
        this.tagIds = tagIds;
    }

    public ArrayList<String> getSearchWords() {
        return searchWords;
    }

    public void setSearchWords(ArrayList<String> searchWords) {
        this.searchWords = searchWords;
    }
}
