package com.artactivo.alllightsoff;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by DDRitter on 08/07/2016.
 */
public class FileOperations {
    private static final String DATA_FILENAME = "save_game.dat";

    protected static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(DATA_FILENAME, context.MODE_PRIVATE));
            osw.write(data);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    protected static void appendToFile(String data, Context context) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(DATA_FILENAME, context.MODE_PRIVATE | context.MODE_APPEND));
            osw.write(data);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    protected static String readFromFile(Context context) {
        String data = "";
        try {
            InputStream inputStream = context.openFileInput(DATA_FILENAME);

            if (inputStream != null) {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(isr);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                data = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return data;
    }
}
