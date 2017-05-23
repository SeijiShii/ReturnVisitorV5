package net.c_kogyo.returnvisitorv5.data.list;

import android.content.Context;

import net.c_kogyo.returnvisitorv5.data.Placement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by SeijiShii on 2017/05/23.
 */

public class PlacementList extends DataList<Placement> {


    /**
     * first Placement
     * second Integer: ranking weight;
     */
    private class RankedPlacement {

        private Placement placement;
        private int weight;

        public RankedPlacement(Placement placement) throws CloneNotSupportedException{

            this.placement = placement.clone(true);
            this.weight = 0;
        }
    }

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
    private ArrayList<RankedPlacement> getWeightedList(Calendar today) {

        ArrayList<Placement> undoubledList = new ArrayList<>(list);
        ArrayList<Placement> listToRemove = new ArrayList<>();
        ArrayList<RankedPlacement> weightedList = new ArrayList<>();

        // まずidダブりのないweightedListを抽出する。

        for ( int i = 0 ; i < undoubledList.size() - 1 ; i++ ) {
            for ( int j = i + 1 ; j < undoubledList.size() ; j++ ) {
                if (undoubledList.get(i).equals(undoubledList.get(j))) {
                    listToRemove.add(undoubledList.get(j));
                }
            }
        }
        undoubledList.removeAll(listToRemove);

        for (Placement placement : undoubledList) {
            try {
                weightedList.add(new RankedPlacement(placement));
            } catch (CloneNotSupportedException e) {
                //
            }
        }

        // 重みづけをする
        for (RankedPlacement rankedPlacement : weightedList) {
            for (Placement placement : list) {
                if (rankedPlacement.placement.getId().equals(placement.getId())) {
                    long diff = today.getTimeInMillis() - placement.getPlacedDate().getTimeInMillis();
                    if (diff <= ONE_WEEK)
                        rankedPlacement.weight += 16;
                    else if (diff <= ONE_MONTH)
                        rankedPlacement.weight += 8;
                    else if (diff <= THREE_MONTH)
                        rankedPlacement.weight += 4;
                    else if (diff <= SIX_MONTH)
                        rankedPlacement.weight += 2;
                    else
                        rankedPlacement.weight += 1;
                }
            }
        }

        Collections.sort(weightedList, new Comparator<RankedPlacement>() {
            @Override
            public int compare(RankedPlacement o1, RankedPlacement o2) {
                return o1.weight - o2.weight;
            }
        });

        return weightedList;
    }

    private ArrayList<Placement> getRankedList(Calendar today) {

        ArrayList<Placement> rankedList = new ArrayList<>();
        ArrayList<RankedPlacement> weightedList = new ArrayList<>(getWeightedList(today));

        for (RankedPlacement rankedPlacement : weightedList) {
            try {
                rankedList.add(rankedPlacement.placement.clone(true));
            } catch (CloneNotSupportedException e) {
                //
            }
        }
        return rankedList;
    }

    // TODO: 2017/05/23 getSearchedAndRankedList
    public ArrayList<Placement> getSearchedAndRankedList(Calendar today, String searchWord, Context context) {

        if (searchWord.length() <= 0)
            return getRankedList(today);

        String[] searchedWords = searchWord.split(" ");
        ArrayList<Placement> rankedList = new ArrayList<>(getRankedList(today));
        ArrayList<Placement> listToRemove = new ArrayList<>();

        for (Placement placement : rankedList) {
            for (String word : searchedWords) {
                if (placement.toStringForSearch(context).contains(word)) {
                    break;
                }
            }
            listToRemove.add(placement);
        }
        rankedList.removeAll(listToRemove);

        return rankedList;
    }

}
