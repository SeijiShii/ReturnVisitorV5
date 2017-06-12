package net.c_kogyo.returnvisitorv5;

//import net.c_kogyo.returnvisitor.R;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2016/07/24.
 */

public class Constants {

    // Shared Preferences用のタグ類
    public static final class SharedPrefTags{

        public static final String RETURN_VISITOR_SHARED_PREFS = "return_visitor_shared_prefs";
        public static final String ZOOM_LEVEL = "zoom_level";

        public static final String COUNTING_WORK_ID = "counting_work_id";
        public static final String IS_COUNTING_TIME = "is_counting_time";

        public static final String PUBLISHER_NAME = "publisher_name";


        public static final String WEEK_START_DAY = "week_start_day";



        public static final String LAST_DEVICE_SYNC_TIME = "last_device_sync_time";
    }

    public static final String DATA_ARRAY_LATER_THAN_TIME = "data_array_later_than_time";
    public static final String LOADED_DATA_ARRAY = "loaded_data_array";
//    public static final String FAILED_DATA_ARRAY = "failed_data_array";

    public static final int[] buttonRes = {
            R.mipmap.button_marker_gray,
            R.mipmap.button_marker_red,
            R.mipmap.button_marker_purple,
            R.mipmap.button_marker_blue,
            R.mipmap.button_marker_emerald,
            R.mipmap.button_marker_green,
            R.mipmap.button_marker_yellow,
            R.mipmap.button_maker_orange
    } ;

    public static final int[] markerRes = {
            R.mipmap.pin_maker_gray,
            R.mipmap.pin_marker_red,
            R.mipmap.pin_maker_purple,
            R.mipmap.pin_marker_blue,
            R.mipmap.pin_maker_emerald,
            R.mipmap.pin_maker_green,
            R.mipmap.pin_maker_yellow,
            R.mipmap.pin_maker_orange
    };

    public static final int[] complexRes = {
            R.mipmap.square_marker_gray,
            R.mipmap.square_marker_red,
            R.mipmap.square_marker_purple,
            R.mipmap.square_marker_blue,
            R.mipmap.square_marker_emerald,
            R.mipmap.square_marker_green,
            R.mipmap.square_marker_yellow,
            R.mipmap.square_marker_orange
    };

    public class WorkFragmentConstants{
        public static final String WORK_FRAGMENT_ARGUMENT = "work_fragment_argument";
        public static final String ADDED_WORK_ID = "added_work_id";
        public static final String TO_EXTRACT_WORK_VIEW = "to_extract_work_view";

    }

    public static final String DATE_LONG = "date_long";
    public static final String MONTH_LONG = "month_long";
    public static final String START_DAY = "start_day";
    public static final String CALENDAR_FRAGMENT_ARGUMENT = "calendar_fragment_argument";


    public static final String REQUEST_CODE = "Request Code";

//    public class PersonCode {
//
//        public static final int ADD_PERSON_REQUEST_CODE = 1000;
//        public static final int EDIT_PERSON_REQUEST_CODE = 1001;
//
//        public static final int PERSON_ADDED_RESULT_CODE = 1002;
//        public static final int PERSON_EDITED_RESULT_CODE = 1003;
//        public static final int PERSON_CANCELED_RESULT_CODE = 1004;
//
//    }
//
//    public class PlacementCode {
//
//        public static final int PLACEMENT_REQUEST_CODE = 2000;
//        public static final int PLACEMENT_ADDED_RESULT_CODE = 2010;
//        public static final int PLACEMENT_CANCELED_RESULT_CODE = 2020;
//    }
//
//    public class LogInCode {
//
//        public static final int GOOGLE_SIGN_IN_RC = 3000;
////        public static final int FACEBOOK_LOG_IN_RC = 3001;
//
//
//    }

    public class RecordVisitActions {

        public static final String NEW_HOUSE_ACTION = "new_house_action";
        public static final String NEW_VISIT_ACTION_WITH_PLACE  = "new_visit_action_with_place";
        public static final String EDIT_VISIT_ACTION            = "edit_visit_action";
        public static final String NEW_VISIT_ACTION_NO_PLACE    = "new_visit_action_no_place";
        public static final String NEW_VISIT_ACTION_NO_PLACE_WITH_DATE    = "new_visit_action_no_place_with_date";

        public static final int EDIT_VISIT_REQUEST_CODE = 4000;
        public static final int NEW_VISIT_REQUEST_CODE = 4001;

        public static final int DELETE_VISIT_RESULT_CODE = 4010;
        public static final int VISIT_EDITED_RESULT_CODE = 4020;
        public static final int VISIT_ADDED_RESULT_CODE = 4030;

    }

//    public class CalendarActions {
//
//        public static final String START_CALENDAR_FROM_WORK_ACTION = "start_calendar_from_work_action";
//        public static final String START_CALENDAR_FROM_MAP_ACTION = "start_calendar_from_map_action";
//        public static final int START_CALENDAR_REQUEST_CODE = 5000;
//        public static final int PRESS_DATE_RESULT_CODE = 5010;
//    }
//
    public class WorkPagerActivityActions {

        public static final String START_WITH_NEW_WORK = "start_with_new_work";
    }

}
