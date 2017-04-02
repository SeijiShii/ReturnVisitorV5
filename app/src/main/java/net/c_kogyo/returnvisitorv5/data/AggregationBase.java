package net.c_kogyo.returnvisitorv5.data;

/**
 * Created by SeijiShii on 2016/09/25.
 */

public class AggregationBase {

    protected long time;
    protected int placementCount;
    protected int showVideoCount;
    protected int rvCount;

    AggregationBase() {

        time = 0;
        placementCount = 0;
        showVideoCount = 0;
        rvCount = 0;
    }


    public long getTime() {
        return time;
    }

    public int getPlacementCount() {
        return placementCount;
    }

    public int getShowVideoCount() {
        return showVideoCount;
    }

    public int getRvCount() {
        return rvCount;
    }

}
