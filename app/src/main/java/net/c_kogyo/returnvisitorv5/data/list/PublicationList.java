package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;
import android.view.ViewDebug;

import net.c_kogyo.returnvisitorv5.data.Placement;
import net.c_kogyo.returnvisitorv5.data.Publication;
import net.c_kogyo.returnvisitorv5.data.Visit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by SeijiShii on 2017/05/23.
 */

public class PublicationList extends DataList<Publication> {




    /**
     *
     * @return Weighted list
     */
    private final long ONE_DAY = 1000 * 60 * 60 * 24;
    private final long ONE_WEEK = ONE_DAY * 7;          // weight: 16
    private final long ONE_MONTH = ONE_DAY * 30;        // weight: 8
    private final long THREE_MONTH = ONE_MONTH * 3;     // weight: 4
    private final long SIX_MONTH = ONE_MONTH * 6;       // weight: 2
    private final long ONE_YEAR = ONE_MONTH * 12;       // weight: 1

    public PublicationList() {
        super(Publication.class);
    }


    private ArrayList<Publication> getRankedList(Calendar today) {

        ArrayList<Publication> weightedList = new ArrayList<>(list);

        // 重みづけをする
        for (Visit visit : VisitList.getInstance()) {
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
    public ArrayList<Publication> getSearchedAndRankedList(Calendar today, String searchWord, Context context) {

        if (searchWord.length() <= 0)
            return getRankedList(today);

        String[] searchedWords = searchWord.split(" ");
        ArrayList<Publication> rankedList = new ArrayList<>(getRankedList(today));
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

    public Publication getCorrespondingData(Publication publication) {
        if (indexOf(publication) < 0) {
            return publication;
        } else {
            return get(indexOf(publication));
        }
    }

    @Override
    synchronized public void setOrAdd(Publication data) {

        if (contains(data)) {
//            list.set(indexOf(data), data);
        } else {
            list.add(data);
        }
    }

    @Override
    synchronized public boolean contains(Publication data) {
        return indexOf(data) >= 0;
    }


    @Override
    public synchronized int indexOf(Publication data) {

        for (int i = 0; i < list.size() ; i++ ) {

            Publication data1 = list.get(i);

            if (data.equals(data1)) {
                return i;
            }
        }
        return -1;
    }
}
