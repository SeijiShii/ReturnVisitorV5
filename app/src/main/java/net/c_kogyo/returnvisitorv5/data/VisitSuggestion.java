package net.c_kogyo.returnvisitorv5.data;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/05/27.
 */

public class VisitSuggestion {

    private Person person;
    private Visit latestVisit;

    public VisitSuggestion(@Nullable Person person, Visit latestVisit) {
        this.person = person;
        this.latestVisit = latestVisit;
    }

    @Nullable
    public Person getPerson() {
        return person;
    }

    public Visit getLatestVisit() {
        return latestVisit;
    }

    public static ArrayList<VisitSuggestion> getSuggestions() {

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>();

        // すべての人の最新の訪問情報
        for (Person person : RVData.getInstance().personList) {
            Visit latestVisit = RVData.getInstance().visitList.getLatestVisitToPerson(person.getId());
            if (latestVisit != null) {
                suggestions.add(new VisitSuggestion(person, latestVisit));
            }
        }

        // 1年以上古いものを削除
        ArrayList<VisitSuggestion> deleteList = new ArrayList<>();
        for (VisitSuggestion suggestion : suggestions) {
            if (CalendarUtil.daysPast(suggestion.getLatestVisit().getDatetime(), Calendar.getInstance()) > 365) {
                deleteList.add(suggestion);
            }
        }
        suggestions.removeAll(deleteList);

        // 3日以内のものを削除
        // TODO: 2017/05/28 会いたい人に会えているか
        deleteList = new ArrayList<>();
        for (VisitSuggestion suggestion : suggestions) {
            if (CalendarUtil.daysPast(suggestion.getLatestVisit().getDatetime(), Calendar.getInstance()) < 3) {
                deleteList.add(suggestion);
            }
        }
        suggestions.removeAll(deleteList);

        ArrayList<VisitSuggestion> nhSuggestions = new ArrayList<>();
        // すべての留守宅をゲット
        for (Visit visit : RVData.getInstance().visitList.getAllNotHomeVisits()) {
            nhSuggestions.add(new VisitSuggestion(null, visit));
        }

        // 3か月以上たった留守宅を削除
        deleteList = new ArrayList<>();
        for (VisitSuggestion suggestion : nhSuggestions) {
            if (CalendarUtil.daysPast(suggestion.getLatestVisit().getDatetime(), Calendar.getInstance()) > 90) {
                deleteList.add(suggestion);
            }
        }
        nhSuggestions.removeAll(deleteList);

        suggestions.addAll(nhSuggestions);

        // 日時で整列
        Collections.sort(suggestions, new Comparator<VisitSuggestion>() {
            @Override
            public int compare(VisitSuggestion o1, VisitSuggestion o2) {
                return o2.getLatestVisit().getDatetime().compareTo(o1.getLatestVisit().getDatetime());
            }
        });

        // 優先度で整列
        Collections.sort(suggestions, new Comparator<VisitSuggestion>() {
            @Override
            public int compare(VisitSuggestion o1, VisitSuggestion o2) {
                return o2.getLatestVisit().getPriority().num() - o1.getLatestVisit().getPriority().num();
            }
        });

        return suggestions;
    }
}
