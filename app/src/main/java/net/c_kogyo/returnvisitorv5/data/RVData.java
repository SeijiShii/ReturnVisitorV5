package net.c_kogyo.returnvisitorv5.data;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import net.c_kogyo.returnvisitorv5.data.list.BaseList;
import net.c_kogyo.returnvisitorv5.data.list.DataList;
import net.c_kogyo.returnvisitorv5.data.list.PersonList;
import net.c_kogyo.returnvisitorv5.data.list.PlaceList;
import net.c_kogyo.returnvisitorv5.data.list.VisitList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by SeijiShii on 2017/02/21.
 */
public class RVData {

    public static final String RV_DIR_NAME = "return_visitor_data_dir";
    public static final String RV_FILE_NAME = "return_visitor_data_file";

    public static final String TAG_LIST = "tag_list";
    public static final String PUBLICATION_LIST = "publication_list";
    public static final String COMPLETE_NOTE_LIST = "complete_note_list";

    public static final String PERSON_DATA = "person_data";
    public static final String PLACE_DATA = "place_data";
    public static final String VISIT_DATA = "visit_data";
    public static final String TAG_DATA = "tag_data";
    public static final String PUBLICATION_DATA = "publication_data";
    public static final String COMPLETE_NOTE_DATA = "complete_note_data";

    private static RVData instance = new RVData();

    public static RVData getInstance() {
        return instance;
    }

    private PersonList personList;
    private PlaceList placeList;
    private VisitList visitList;
    private DataList tagList, publicationList, completeNoteList;

    private RVData() {

        personList = new PersonList();
        placeList = new PlaceList();
        visitList = new VisitList();
        tagList = new DataList(TAG_LIST);
        publicationList = new DataList(PUBLICATION_LIST);
        completeNoteList = new DataList(COMPLETE_NOTE_LIST);
    }

    private static boolean isDataLoaded = false;

    public static boolean isDataLoaded() {
        return isDataLoaded;
    }

    public class LoadData extends IntentService {

        public static final String LOAD_DATA_SERVICE = "load_data_service";

        public LoadData() {
            super(LOAD_DATA_SERVICE);
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            jsonToData(stringToJson(loadStringFromFile()));
            
            // データの読み込みが終わったら
            isDataLoaded = true;
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

            try {
                return new JSONObject(string);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        private void jsonToData(JSONObject object) {

            try {
                if (object.has(PLACE_DATA))
                    placeList = new PlaceList(object.getJSONObject(PLACE_DATA));
                if (object.has(PERSON_DATA))
                    personList = new PersonList(object.getJSONObject(PERSON_DATA));
                if (object.has(VISIT_DATA))
                    visitList = new VisitList(object.getJSONObject(VISIT_DATA));

                if (object.has(TAG_DATA))
                    tagList = new DataList(TAG_LIST, object.getJSONObject(TAG_DATA));
                if (object.has(PUBLICATION_DATA))
                    publicationList = new DataList(PUBLICATION_LIST, object.getJSONObject(PUBLICATION_DATA));
                if (object.has(COMPLETE_NOTE_DATA))
                    completeNoteList = new DataList(COMPLETE_NOTE_LIST, object.getJSONObject(COMPLETE_NOTE_DATA));

            }catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private static boolean isDataSaving = false;
    public class SaveData extends IntentService {

        public static final String SAVE_DATA_SERVICE = "save_data_service";

        public SaveData() {
            super(SAVE_DATA_SERVICE);
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            // データが書き込み中だったら待つ
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
        }

        private JSONObject jsonObject() {

            JSONObject object = new JSONObject();

            try {
                object.put(PLACE_DATA, placeList.jsonObject());
                object.put(PERSON_DATA, personList.jsonObject());
                object.put(VISIT_DATA, visitList.jsonObject());
                object.put(TAG_DATA, tagList.jsonObject());
                object.put(PUBLICATION_DATA, publicationList.jsonObject());
                object.put(COMPLETE_NOTE_DATA, completeNoteList.jsonObject());

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


}
