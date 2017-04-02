package net.c_kogyo.returnvisitorv5.data.list;

import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/03/30.
 */

public class WorkList extends DataList<Work> {

    public ArrayList<Work> getWorksInDay(Calendar date) {

        ArrayList<Work> works = new ArrayList<>();
        for (Work work : getList()) {
            if (CalendarUtil.isSameDay(work.getStart(), date)) {
                works.add(work);
            }
        }
        return works;
    }

    public ArrayList<Calendar> getDates() {

        ArrayList<Calendar> dates = new ArrayList<>();

        for (Work work : getList()) {
            dates.add(work.getStart());
        }

        ArrayList<Calendar> datesToRemove = new ArrayList<>();

        for (int i = 0 ; i < dates.size() - 1 ; i++ ) {

            Calendar date0 = dates.get(i);

            for ( int j = i + 1 ; j < dates.size() ; j++ ) {

                Calendar date1 = dates.get(j);

                if (CalendarUtil.isSameDay(date0, date1)) {

                    datesToRemove.add(date1);
                }
            }
        }
        dates.removeAll(datesToRemove);
        return dates;
    }
}
