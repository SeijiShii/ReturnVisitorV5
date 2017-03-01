package net.c_kogyo.returnvisitorv5.data;

import android.os.AsyncTask;
import android.os.Environment;

import net.c_kogyo.returnvisitorv5.data.list.DataList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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

    private DataList<Place> placeList;
    private DataList<Person> personList;
    private DataList<Visit> visitList;
    private DataList<Tag> tagList;
    private DataList<NoteCompItem> noteCompList;
    private DataList<Publication> pubList;

    private static RVData instance = new RVData();
    private RVData() {

        placeList = new DataList<>();
        personList = new DataList<>();
        visitList = new DataList<>();

        tagList = new DataList<>();
        noteCompList = new DataList<>();
        pubList = new DataList<>();

    }

    public static RVData getInstance() {return instance;}

    public DataList<Person> getPersonList() {
        return personList;
    }

    public DataList<Place> getPlaceList() {
        return placeList;
    }

    public DataList<Visit> getVisitList() {
        return visitList;
    }

    public DataList<Tag> getTagList() {
        return tagList;
    }

    public DataList<NoteCompItem> getNoteCompList() {
        return noteCompList;
    }

    public DataList<Publication> getPubList() {
        return pubList;
    }

    public void saveData(RVDataStoreCallback callback){
        new SaveData(callback).execute();
    }

    public void loadData(RVDataStoreCallback callback) {
        new LoadData(callback).execute();
    }

    public class LoadData extends AsyncTask<Void, Void, Void> {

        RVDataStoreCallback mCallback;
        public LoadData(RVDataStoreCallback callback) {
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            jsonToData(stringToJson(loadStringFromFile()));
            mCallback.onDataLoaded();

            return null;
        }

        private String loadStringFromFile() {

            File dir = new File(Environment.getExternalStorageDirectory().toString()
                    + "/" + RV_DIR_NAME + "/");
            if (!dir.exists()) {
                return "";
            }

            File file = new File(dir.getPath() + RV_FILE_NAME);
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
                                placeList.add(new Place(record));
                                break;
                            case "Person":
                                personList.add(new Person(record));
                                break;
                            case "Visit":
                                visitList.add(new Visit(record));
                                break;
                            case "Tag":
                                tagList.add(new Tag(record));
                                break;
                            case "NoteCompItem":
                                noteCompList.add(new NoteCompItem(record));
                                break;
                            case "Publication":
                                pubList.add(new Publication(record));
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
    public class SaveData extends AsyncTask<Void, Void, Void> {

        RVDataStoreCallback mCallback;
        public SaveData(RVDataStoreCallback callback) {
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

            mCallback.onDataSaved();

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

            File dir = new File(Environment.getExternalStorageDirectory().toString()
                    + "/" + RV_DIR_NAME + "/");

            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(dir.getPath() + RV_FILE_NAME);
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

    public interface RVDataStoreCallback{

        void onDataSaved();

        void onDataLoaded();
    }

}
