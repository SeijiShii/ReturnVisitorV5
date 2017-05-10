package net.c_kogyo.returnvisitorv5.data;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import net.c_kogyo.returnvisitorv5.data.list.DataList;
import net.c_kogyo.returnvisitorv5.data.list.DeletedList;
import net.c_kogyo.returnvisitorv5.data.list.NoteCompList;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
import net.c_kogyo.returnvisitorv5.data.list.TagList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;
import net.c_kogyo.returnvisitorv5.data.list.WorkList;
import net.c_kogyo.returnvisitorv5.util.CalendarUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SeijiShii on 2017/02/27.
 */

public class RVData {

    public static final String RV_DIR_NAME = "return_visitor_data_dir";
    public static final String RV_FILE_NAME = "return_visitor_data_file";
    public static final String RV_DATA_LIST = "rv_data_list";

//    public static final String TAG_LIST = "tag_list";
//    public static final String NOTE_COMP_LIST = "note_comp_list";
//    public static final String PUB_LIST = "pub_list";

    public PlaceList placeList;
    public DataList<Person> personList;
    public VisitList visitList;
    public TagList tagList;
    public NoteCompList noteCompList;
    public DataList<Publication> pubList;
    public WorkList workList;

    public DeletedList deletedList;

    private static RVData instance = new RVData();
    private RVData() {

        placeList = new PlaceList();
        personList = new DataList<>();
        visitList = new VisitList();

        tagList = new TagList();
        noteCompList = new NoteCompList();
        pubList = new DataList<>();

        workList = new WorkList();

        deletedList = new DeletedList();

    }

    public static RVData getInstance() {return instance;}

    public void saveData(Context context, @Nullable RVDataStoreCallback callback){
        new SaveData(context, callback).execute();
    }

    public void loadData(Context context, @Nullable RVDataStoreCallback callback) {
        new LoadData(context, callback).execute();
    }

    private class LoadData extends AsyncTask<Void, Void, Void> {

        RVDataStoreCallback mCallback;
        Context mContext;
        private LoadData(Context context, RVDataStoreCallback callback) {
            mContext = context;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            jsonToData(stringToJson(loadStringFromFile()));

            if (mCallback != null) {
                mCallback.onDataLoaded();
            }

            return null;
        }

        private String loadStringFromFile() {

//            File dir = new File(Environment.getExternalStorageDirectory().toString()
//                    + "/" + RV_DIR_NAME + "/");
//            if (!dir.exists()) {
//                return "";
//            }

            File dir = mContext.getDir(RV_DIR_NAME, Context.MODE_PRIVATE);

            File file = new File(dir.getPath() + "/" + RV_FILE_NAME);
            if (!file.exists()) {
                return "";
            }

            String s = "";
            try {
                FileInputStream input = new FileInputStream(file);
                int size = input.available();
                byte[] buffer = new byte[size];
                input.read(buffer);
                input.close();

                s = new String(buffer);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }

        private JSONObject stringToJson(String string) {

            if (string.equals("") || string.length() <= 0)
                return null;

            try {
                return new JSONObject(string);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        private void jsonToData(JSONObject object) {

            if (object == null) return;

            try {

                if (object.has(RV_DATA_LIST)) {
                    JSONArray array = object.getJSONArray(RV_DATA_LIST);

                    for ( int i = 0 ; i < array.length() ; i++ ) {

                        JSONObject recordObject = array.getJSONObject(i);
                        Record record = new Record(recordObject);

                        switch (record.getClassName()) {
                            case "Place":
                                placeList.setOrAdd(new Place(record));
                                break;
                            case "Person":
                                personList.setOrAdd(new Person(record));
                                break;
                            case "Visit":
                                visitList.setOrAdd(new Visit(record));
                                break;
                            case "Tag":
                                tagList.setOrAdd(new Tag(record));
                                break;
                            case "NoteCompItem":
                                noteCompList.setOrAdd(new NoteCompItem(record));
                                break;
                            case "Publication":
                                pubList.setOrAdd(new Publication(record));
                                break;
                            case "Work":
                                workList.setOrAdd(new Work(record));
                                break;
                            case "DeletedData":
                                deletedList.onLoadData(record);
                                break;
                        }
                    }
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDataSaving = false;
    private class SaveData extends AsyncTask<Void, Void, Void> {

        RVDataStoreCallback mCallback;
        Context mContext;
        private SaveData(Context context, RVDataStoreCallback callback) {
            mContext = context;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // 他のインスタンスがデータの書き込み中だったら待つ
            while (isDataSaving) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    //
                }
            }

            // 自分インスタンスのスレッドでデータの書き込みを開始
            isDataSaving = true;

            // 書き込み処理
            saveToFile(jsonToString(jsonObject()));

            // 書き込みが終わった
            isDataSaving = false;

            if (mCallback != null) {
                mCallback.onDataSaved();
            }
            return null;
        }

        private JSONObject jsonObject() {

            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();

            for (Place place : placeList) {
                array.put(new Record(place).getFullJSON());
            }

            for (Person person : personList) {
                array.put(new Record(person).getFullJSON());
            }

            for (Visit visit : visitList) {
                array.put(new Record(visit).getFullJSON());
            }

            for (Tag tag : tagList) {
                array.put(new Record(tag).getFullJSON());
            }

            for (NoteCompItem note : noteCompList) {
                array.put(new Record(note).getFullJSON());
            }

            for (Publication pub : pubList ) {
                array.put(new Record(pub).getFullJSON());
            }

            for (Work work : workList) {
                array.put(new Record(work).getFullJSON());
            }

            for (DeletedData deletedData : deletedList) {
                array.put(new Record(deletedData).getFullJSON());
            }

            try {
                object.put(RV_DATA_LIST, array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }

        private String jsonToString(JSONObject object) {

            String s = "";

            try {
                s = object.toString(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return s;
        }

        private void saveToFile(String s) {

            File dir = mContext.getDir(RV_DIR_NAME, Context.MODE_PRIVATE);
//                    = new File(Environment.getExternalStorageDirectory().toString()
//                    + "/" + RV_DIR_NAME + "/");

            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(dir.getPath() + "/" + RV_FILE_NAME);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter filewriter = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(filewriter);
                PrintWriter pw = new PrintWriter(bw);
                pw.write(s);
                pw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Calendar> getDatesWithData() {

        ArrayList<Calendar> datesOfVisit = visitList.getDates();
        ArrayList<Calendar> datesOfWork = workList.getDates();
        ArrayList<Calendar> datesDoubled = new ArrayList<>();

        for (Calendar date0 : datesOfVisit) {
            for (Calendar date1 : datesOfWork) {

                if (CalendarUtil.isSameDay(date0, date1)) {
                    datesDoubled.add(date1);
                }
            }
        }

        datesOfWork.removeAll(datesDoubled);
        datesOfVisit.addAll(datesOfWork);

        Collections.sort(datesOfVisit, new Comparator<Calendar>() {
            @Override
            public int compare(Calendar calendar, Calendar t1) {
                return calendar.compareTo(t1);
            }
        });

        return new ArrayList<>(datesOfVisit);
    }

    public ArrayList<Calendar> getMonthsWithData() {

        ArrayList<Calendar> monthWithData = new ArrayList<>();
        ArrayList<Calendar> datesWithData = getDatesWithData();

        if (datesWithData.size() <= 0)
            return monthWithData;

        monthWithData.add(datesWithData.get(0));

        int dateIndex = 0;
        int monthIndex = 0;

        while (dateIndex < datesWithData.size() - 1) {
            dateIndex++;
            if (!CalendarUtil.isSameMonth(datesWithData.get(dateIndex), monthWithData.get(monthIndex))) {
                monthWithData.add(datesWithData.get(dateIndex));
                monthIndex++;
            }
        }

        return monthWithData;
    }

    public interface RVDataStoreCallback{

        void onDataSaved();

        void onDataLoaded();
    }

    public boolean hasWorkOrVisit() {
        return visitList.getList().size() > 0 || workList.getList().size() > 0;
    }

}
