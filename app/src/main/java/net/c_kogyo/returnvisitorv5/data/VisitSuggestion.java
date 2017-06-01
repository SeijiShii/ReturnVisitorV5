package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;
import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public Visit getLatestSeenVisit() {
        return latestSeenVisit;
    }

    public Visit.Priority getPriority() {

        for (VisitDetail visitDetail : latestVisit.getVisitDetails()) {
            if (visitDetail.getPersonId().equals(person.getId())) {
                return visitDetail.getPriority();
            }
        }
        return latestVisit.getPriority();
    }

    public ArrayList<String> getTagIds() {
        if (person == null) {
            return new ArrayList<>();
        }

        if (latestSeenVisit != null) {
            VisitDetail detail = latestSeenVisit.getVisitDetail(person.getId());
            if (detail != null) {
                return detail.getTagIds();
            }
        }

        VisitDetail detail = latestVisit.getVisitDetail(person.getId());
        if (detail != null) {
            return detail.getTagIds();
        }

        return new ArrayList<>();
    }

    public int getPassedDaysFromLastSeen(){
        if (latestSeenVisit != null) {
            return CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance());
        } else {
            return -1;
        }
    }

    public String toStringForSearch(Context context) {
        StringBuilder builder = new StringBuilder();

        builder.append(latestVisit.toStringForSearch(context));

        if (latestSeenVisit != null ) {
            builder.append(" ").append(latestSeenVisit.toStringForSearch(context));
        }

        if (person != null) {
            builder.append(" ").append(person.toStringForSearch(context));
        }

        return builder.toString();
    }

    public static ArrayList<VisitSuggestion> getFilteredSuggestions(Filter filter,
                                                                    ArrayList<DismissedSuggestion> dismissedSuggestions,
                                                                    Context context) {

        ArrayList<VisitSuggestion> suggestions = getSuggestionsByPriorities(filter.priorities);

        suggestions = filterSuggestionsByTag(filter.tagIds, suggestions);
        suggestions = filterSuggestionsByWords(filter.searchWords, suggestions, context);


        ArrayList<VisitSuggestion> deleteList = new ArrayList<>();
        for (DismissedSuggestion dismissedSuggestion : dismissedSuggestions) {
            for (VisitSuggestion suggestion : suggestions) {
                if (suggestion.getLatestVisit().getId().equals(dismissedSuggestion.getLatestVisitId())) {
                    deleteList.add(suggestion);
                }
            }
        }
        suggestions.removeAll(deleteList);

        return suggestions;
    }

    private static ArrayList<VisitSuggestion> getSuggestionsByPriority(Visit.Priority priority) {

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>();

        if (priority != Visit.Priority.NOT_HOME) {
            for (Person person : RVData.getInstance().personList) {

                if (person.getPriority() == priority) {
                    Visit latestVisit = RVData.getInstance().visitList.getLatestVisitToPerson(person.getId());
                    Visit latestSeenVisit = RVData.getInstance().visitList.getLatestVisitSeenToPerson(person.getId());
                    if (latestVisit != null) {
                        switch (priority) {
                            case HIGH:

                                if (latestSeenVisit != null) {
                                    if (CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance()) > 4
                                            && !CalendarUtil.isSameDay(latestSeenVisit.getDatetime(), Calendar.getInstance())) {
                                        suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                    }
                                } else {
                                    // 一度も会えていないなら
                                    suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                }

                                break;

                            case MIDDLE:

                                if (latestSeenVisit != null) {
                                    if (CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance()) > 7
                                            && !CalendarUtil.isSameDay(latestSeenVisit.getDatetime(), Calendar.getInstance())) {
                                        suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                    }
                                } else {
                                    // 一度も会えていないなら
                                    suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                }

                                break;

                            case LOW:

                                if (latestSeenVisit != null) {
                                    if (CalendarUtil.daysPast(latestSeenVisit.getDatetime(), Calendar.getInstance()) > 15
                                            && !CalendarUtil.isSameDay(latestSeenVisit.getDatetime(), Calendar.getInstance())) {
                                        suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
                                    }
                                } else {
                                    // 一度も会えていないなら
                                    suggestions.add(new VisitSuggestion(person, latestVisit, latestSeenVisit));
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

    private static ArrayList<VisitSuggestion> getSuggestionsByPriorities(ArrayList<Visit.Priority> priorities) {
        ArrayList<Visit.Priority> noDoubledPriorities = new ArrayList<>();
        for (Visit.Priority priority : priorities) {
            if (!noDoubledPriorities.contains(priority)) {
                noDoubledPriorities.add(priority);
            }
        }

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>();
        for (Visit.Priority priority : noDoubledPriorities) {
            suggestions.addAll(getSuggestionsByPriority(priority));
        }

        Collections.sort(suggestions, new Comparator<VisitSuggestion>() {
            @Override
            public int compare(VisitSuggestion o1, VisitSuggestion o2) {
                return o2.getPriority().num() - o1.getPriority().num();
            }
        });

        return suggestions;
    }

    private static ArrayList<VisitSuggestion> filterSuggestionsByTag(ArrayList<String> tagIds, ArrayList<VisitSuggestion> givenSuggestions) {

        if (tagIds.size() <= 0) {
            return givenSuggestions;
        }

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>();

        for (VisitSuggestion suggestion : givenSuggestions) {

            if (suggestion.person != null) {
                if (suggestion.latestSeenVisit != null) {
                    VisitDetail visitDetail = suggestion.latestSeenVisit.getVisitDetail(suggestion.person.getId());
                    if (visitDetail != null) {
                        if (hasSame(tagIds, visitDetail.getTagIds())) {
                            suggestions.add(suggestion);
                        }
                    }
                } else {
                    VisitDetail visitDetail = suggestion.latestVisit.getVisitDetail(suggestion.person.getId());
                    if (visitDetail != null) {
                        if (hasSame(tagIds, visitDetail.getTagIds())) {
                            suggestions.add(suggestion);
                        }
                    }
                }
            }
        }
        return suggestions;
    }

    private static ArrayList<VisitSuggestion> filterSuggestionsByWords(ArrayList<String> searchWords,
                                                                        ArrayList<VisitSuggestion> givenSuggestions,
                                                                       Context context) {

        if (searchWords.size() <= 0) {
            return givenSuggestions;
        }

        ArrayList<VisitSuggestion> suggestions = new ArrayList<>(givenSuggestions);
        ArrayList<VisitSuggestion> deleteList = new ArrayList<>();
        for (String word : searchWords) {
            for (VisitSuggestion suggestion : suggestions) {
                if (!suggestion.toStringForSearch(context).contains(word)) {
                    deleteList.add(suggestion);
                }
            }
            suggestions.removeAll(deleteList);
        }
        return suggestions;
    }

    private static boolean hasSame(ArrayList<String> ids1, ArrayList<String> ids2) {

        for (String id1 : ids1) {
            for (String id2 : ids2) {
                if (id1.equals(id2)) {
                    return true;
                }
            }
        }
        return false;
    }
}
