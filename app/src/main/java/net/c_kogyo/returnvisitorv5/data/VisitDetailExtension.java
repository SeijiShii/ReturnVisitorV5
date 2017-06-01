package net.c_kogyo.returnvisitorv5.data;

import android.support.annotation.Nullable;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/05/31.
 */

public class VisitDetailExtension {

    VisitDetail visitDetail;
    Calendar dateTime;

    public VisitDetailExtension(@Nullable String personId, @Nullable Visit visit) {

        try {
            if (visit != null) {
                VisitDetail visitDetail = visit.getVisitDetail(personId);
                if (visitDetail != null) {
                    this.visitDetail = (VisitDetail) visitDetail.clone();
                }
            }
        } catch (CloneNotSupportedException e) {

        }
        this.dateTime = (Calendar) dateTime.clone();
    }
}
