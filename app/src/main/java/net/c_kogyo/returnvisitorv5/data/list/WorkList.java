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

    //要素の時間が変更されたとき前後に向かって調整するメソッド

    /**
     *
     * @param work 時間の変更された
     * @return 調整の結果削除されたWorkのリスト
     */
    public ArrayList<Work> onChangeTime(Work work) {

        ArrayList<Work> worksRemoved = new ArrayList<>();

        // 念のため存在チェック
        if (!getList().contains(work)) return worksRemoved;

        // すべてのリストを開始時間で整列
        Collections.sort(getList(), new Comparator<Work>() {
            @Override
            public int compare(Work work, Work t1) {
                return work.getStart().compareTo(t1.getStart());
            }
        });

        // 対象の要素のindexを取得
        int index = getList().indexOf(work);

        // 過去に向かってさかのぼり
        for ( int i = index - 1 ; i >= 0 ; i-- ) {

            Work work1 = getList().get(i);

            if (work.getStart().before(work1.getStart())) {
                worksRemoved.add(work1);
            } else if (work.getStart().before(work1.getEnd())) {
                work.setStart(work1.getStart());
                worksRemoved.add(work1);
            } else {
                break;
            }
        }

        // 未来にむかって!!
        for (int i = index + 1 ; i < getList().size() ; i++ ) {

            Work work1 = getList().get(i);

            if (work.getEnd().after(work1.getEnd())) {
                worksRemoved.add(work1);
            } else if (work.getEnd().after(work1.getStart())) {
                work.setEnd(work1.getEnd());
                worksRemoved.add(work1);
            } else {
                break;
            }
        }

        deleteAll(worksRemoved);

        return worksRemoved;
    }

    @Nullable
    public Work getByVisit(Visit visit) {
        for (Work work : getList()) {
            if (visit.getDatetime().after(work.getStart()) && visit.getDatetime().before(work.getEnd())) {
                return work;
            }
        }
        return null;
    }
}
