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
    private Visit latestVisit, latestSeenVisit;

    public VisitSuggestion(@Nullable Person person, Visit latestVisit, @Nullable Visit latestSeenVisit) {
        this.person = person;
        this.latestVisit = latestVisit;
        this.latestSeenVisit = latestSeenVisit;
    }

    @Nullable
    public Person getPerson() {
        return person;
    }

    public Visit getLatestVisit() {
        return latestVisit;
    }

    public Visit.Priority getPriority() {

        for (VisitDetail visitDetail : latestVisit.getVisitDetails()) {
            if (visitDetail.getPersonId().equals(person.getId())) {
                return visitDetail.getPriority();
            }
        }
        return latestVisit.getPriority();
    }

    public static ArrayList<VisitSuggestion> getFilteredSuggestions(ArrayList<Visit.Priority> priorities) {

        ArrayList<Visit.Priority> noDoubledPriorities = new ArrayList<>();
        for (Visit.Priority priority : priorities) {
            if (!noDoubledPriorities.contains(priority)) {
                noDoubledPriorities.add(priority);
            }
        }

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>();
        for (Visit.Priority priority : noDoubledPriorities) {
            suggestions.addAll(getSuggestionByPriority(priority));
        }

        Collections.sort(suggestions, new Comparator<VisitSuggestion>() {
            @Override
            public int compare(VisitSuggestion o1, VisitSuggestion o2) {
                return o2.getPriority().num() - o1.getPriority().num();
            }
        });

        return suggestions;
    }

    private static ArrayList<VisitSuggestion> getSuggestionByPriority(Visit.Priority priority) {

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>();

        if (priority != Visit.Priority.NOT_HOME) {
            for (Person person : RVData.getInstance().personList) {

                if (person.getPriority() == priority) {
                    Visit latestVisit = RVData.getInstance().visitList.getLatestVisitToPerson(person.getId());
                    Visit latestSeenVisit = RVData.getInstance().visitList.getLatestVisitSeenToPerson(person.getId());
                    if (latestVisit != null) {
                        switch (priority) {
                            case HIGH:
                                if (CalendarUtil.daysPast(latestVisit.getDatetime(), Calendar.getInstance()) > 4) {
                                    if (latestSeenVisit == null) {
                                        // 一度も会えていないなら
                                        suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                    } else {
                                        if (CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance()) > 2
                                                && !CalendarUtil.isSameDay(latestSeenVisit.getDatetime(), Calendar.getInstance())) {
                                            suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                        }
                                    }
                                }
                                break;
                            case MIDDLE:
                                if (CalendarUtil.daysPast(latestVisit.getDatetime(), Calendar.getInstance()) > 10) {
                                    if (latestSeenVisit == null) {
                                        // 一度も会えていないなら
                                        suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                    } else {
                                        if (CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance()) > 5
                                                && !CalendarUtil.isSameDay(latestSeenVisit.getDatetime(), Calendar.getInstance())) {
                                            suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                        }
                                    }
                                }
                                break;
                            case LOW:
                                if (CalendarUtil.daysPast(latestVisit.getDatetime(), Calendar.getInstance()) > 20) {
                                    if (latestSeenVisit == null) {
                                        // 一度も会えていないなら
                                        suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                    } else {
                                        if (CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance()) > 10
                                                && !CalendarUtil.isSameDay(latestSeenVisit.getDatetime(), Calendar.getInstance())) {
                                            suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                        }
                                    }
                                }
                                break;
                            case BUSY:
                                suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                break;

                            default:
                        }
                    }
                }
            }
        } else {
            for (Visit visit : RVData.getInstance().visitList.getAllNotHomeVisitsInOneMonth()) {
                suggestions.add(new VisitSuggestion(null, visit, null));
            }
        }

        return suggestions;
    }
}
