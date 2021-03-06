package net.c_kogyo.returnvisitorv5.data.list;

import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.Visit;
import net.c_kogyo.returnvisitorv5.data.Work;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/03/30.
 */

public class WorkList extends DataList<Work> {

    private static WorkList instance;
    private WorkList() {
        super(Work.class);
    }

    public static WorkList getInstance() {
        if (instance == null) {
            instance = new WorkList();
        }
        return instance;
    }

    synchronized public ArrayList<Work> getWorksInDay(Calendar date) {

        ArrayList<Work> works = new ArrayList<>();
        for (Work work : list) {
            if (CalendarUtil.isSameDay(work.getStart(), date)) {
                works.add(work);
            }
        }
        return works;
    }

    synchronized public ArrayList<Calendar> getDates() {

        ArrayList<Calendar> dates = new ArrayList<>();

        for (Work work : list) {
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

    //要素の時間が変更されたとき前後に向かって調整するメソッド

    /**
     *
     * @param workChanged 時間の変更された
     * @return 調整の結果削除されたWorkのリスト
     */
    synchronized public ArrayList<Work> onChangeTime(Work workChanged) {

        ArrayList<Work> worksRemoved = new ArrayList<>();

        // 念のため存在チェック
        if (!list.contains(workChanged)) return worksRemoved;

        // すべてのリストを開始時間で整列
        ArrayList<Work> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, new Comparator<Work>() {
            @Override
            public int compare(Work work, Work t1) {
                return work.getStart().compareTo(t1.getStart());
            }
        });

        // 対象の要素のindexを取得
        int index = sortedList.indexOf(workChanged);

        // 過去に向かってさかのぼり
        for ( int i = index - 1 ; i >= 0 ; i-- ) {

            Work work = sortedList.get(i);

            if(work.equals(workChanged))
                break;

            if (workChanged.getStart().before(work.getStart())) {
                worksRemoved.add(work);
            } else if (workChanged.getStart().before(work.getEnd())) {
                workChanged.setStart(work.getStart());
                worksRemoved.add(work);
            } else {
                break;
            }
        }

        // 未来にむかって!!
        for (int i = index + 1 ; i < sortedList.size() ; i++ ) {

            Work work = sortedList.get(i);

            if(work.equals(workChanged))
                break;

            if (workChanged.getEnd().after(work.getEnd())) {
                worksRemoved.add(work);
            } else if (workChanged.getEnd().after(work.getStart())) {
                workChanged.setEnd(work.getEnd());
                worksRemoved.add(work);
            } else {
                break;
            }
        }

        deleteAll(worksRemoved);

        return worksRemoved;
    }

    @Nullable
    synchronized public Work getByVisit(Visit visit) {
        for (Work work : list) {
            if (visit.getDatetime().after(work.getStart()) && visit.getDatetime().before(work.getEnd())) {
                return work;
            }
        }
        return null;
    }

    synchronized public ArrayList<Work> getWorksInMonth(Calendar month) {
        ArrayList<Work> works = new ArrayList<>();
        for (Work work : list) {
            if (CalendarUtil.isSameMonth(work.getStart(), month)) {
                works.add(work);
            }
        }
        return works;
    }
}
