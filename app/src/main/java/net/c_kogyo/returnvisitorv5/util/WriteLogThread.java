package net.c_kogyo.returnvisitorv5.util;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class WriteLogThread extends Thread {

    private Context context;
    private File dir, file;
    private static final String RV_LOG_DIR = "RVLogDir";
    private static final String RV_LOG_FILE = "RVLog.log";

    public WriteLogThread(Context context) {
        this.context = context;
        this.dir = new File(Environment.getExternalStorageDirectory().toString() + "/" + RV_LOG_DIR + "/");
        if (!dir.exists()) {
            dir.mkdir();
        }

        file = new File(dir.getPath() + "/" + RV_LOG_FILE);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {

        }

    }

    @Override
    public void run() {
        java.lang.Process proc = null;
        BufferedReader reader = null;
        FileWriter writer = null;

        final String pId =  Integer.toString(android.os.Process.myPid());

        try {
            proc = Runtime.getRuntime().exec(new String[] { "logcat", "-v", "time", "*:E"});
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()), 1024);
            String line;
            while ( true ) {
                line = reader.readLine();
                if (line == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }

                if (line.contains(pId)) {

                    try {
                        writer = new FileWriter (file, true);
                        writer.append(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}