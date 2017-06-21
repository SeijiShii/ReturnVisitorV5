package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.db.RVDBHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by SeijiShii on 2017/05/23.
 */

public class PublicationList {

    /**
     *
     * @return Weighted list
     */
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
    private static final long ONE_WEEK = ONE_DAY * 7;          // weight: 16
    private static final long ONE_MONTH = ONE_DAY * 30;        // weight: 8
    private static final long THREE_MONTH = ONE_MONTH * 3;     // weight: 4
    private static final long SIX_MONTH = ONE_MONTH * 6;       // weight: 2
    private static final long ONE_YEAR = ONE_MONTH * 12;       // weight: 1

    private static ArrayList<Publication> loadList(RVDBHelper helper) {
        return helper.loadList(Publication.class, false);
    }

    private static ArrayList<Publication> getRankedList(Calendar today, RVDBHelper helper) {

        ArrayList<Publication> weightedList = new ArrayList<>(loadList(helper));

        // 重みづけをする
        for (Visit visit : VisitList.loadList(helper)) {
            long diff = today.getTimeInMillis() - visit.getDatetime().getTimeInMillis();

            for (Placement placement : visit.getAllPlacements()) {
                for (Publication publication : weightedList) {

                    if (placement.getPublicationId() != null) {
                        if (placement.getPublicationId().equals(publication.getId())) {

                            if (diff <= ONE_WEEK)
                                publication.setWeight(publication.getWeight() + 16);
                            else if (diff <= ONE_MONTH)
                                publication.setWeight(publication.getWeight() + 8);
                            else if (diff <= THREE_MONTH)
                                publication.setWeight(publication.getWeight() + 4);
                            else if (diff <= SIX_MONTH)
                                publication.setWeight(publication.getWeight() + 2);
                            else
                                publication.setWeight(publication.getWeight() + 1);
                        }
                    }
                }
            }
        }

        Collections.sort(weightedList, new Comparator<Publication>() {
            @Override
            public int compare(Publication o1, Publication o2) {
                return o1.getWeight() - o2.getWeight();
            }
        });

        return weightedList;
    }


    // DONE: 2017/05/23 getSearchedAndRankedList
    public static ArrayList<Publication> getSearchedAndRankedList(Calendar today,
                                                                  String searchWord,
                                                                  Context context,
                                                                  RVDBHelper helper) {

        if (searchWord.length() <= 0)
            return getRankedList(today, helper);

        String[] searchedWords = searchWord.split(" ");
        ArrayList<Publication> rankedList = new ArrayList<>(getRankedList(today, helper));
        ArrayList<Publication> listToRemove = new ArrayList<>();

        for (Publication publication : rankedList) {
            for (String word : searchedWords) {
                if (publication.toStringForSearch(context).contains(word)) {
                    break;
                }
            }
            listToRemove.add(publication);
        }
        rankedList.removeAll(listToRemove);

        return rankedList;
    }

    public static  Publication getCorrespondingData(Publication publication, RVDBHelper helper) {
        if (indexOf(publication, helper) < 0) {
            return publication;
        } else {
            return loadList(helper).get(indexOf(publication, helper));
        }
    }

    public static void addIfNotExits(Publication data, RVDBHelper helper) {

        if (!contains(data, helper)) {
            helper.save(data);
        }
    }

    public static boolean contains(Publication data, RVDBHelper helper) {
        return indexOf(data, helper) >= 0;
    }


    public static int indexOf(Publication data, RVDBHelper helper) {

        ArrayList<Publication> list = helper.loadList(Publication.class, false);
        for (int i = 0; i < list.size() ; i++ ) {

            Publication data1 = list.get(i);

            if (data.equals(data1)) {
                return i;
            }
        }
        return -1;
    }
}
