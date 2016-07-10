package com.artactivo.alllightsoff;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Some methods useful for several activities
 * Created by DDRitter on 08/07/2016.
 */
public class Utilities {
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


    /**
     * This method returns the level status letter saved on the file
     *
     */
    protected static String getLevelData(String gameData, int currentLevel) {
        return gameData.substring(5 * currentLevel + 4, 5 * currentLevel + 5);
    }

    /**
     * This method assigns the appropriate stars to the
     *
     * @param status is:
     * A -> No Stars (Not solved at all)
     * B -> One Star (Solved with more than 3 times the movements needed)
     * C -> Two Stars (Solved with more than 2 times the movements needed)
     * D -> Three Stars (Solved with the minimum movements needed)
     *
     * L -> Locked
     *
     * E -> No question mark at all (Game in progress, more than 3 times the moves needed)
     * F -> One star as question mark (Game in progress, more than 2 times the moves needed)
     * G -> Two stars with question mark (Game in progress, more than the moves needed)
     * H -> All stars as question mark (Game has started, still able to win all)

     */
    protected static void setStars(String status, View view) {
        ImageView imageView;
        switch (status) {
            case "A":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_unlit);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_unlit);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_unlit);
                break;
            case "B":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_lit);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_unlit);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_unlit);
                break;
            case "C":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_lit);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_lit);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_unlit);
                break;
            case "D":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_lit);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_lit);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_lit);
                break;
            case "E":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_disabled);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_disabled);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_disabled);
                break;
            case "F":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_question);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_disabled);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_disabled);
                break;
            case "G":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_question);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_question);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_disabled);
                break;
            case "H":
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.star_question);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.star_question);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.star_question);
                break;

            default:
                // Todo: remove this when finished
                imageView = (ImageView) view.findViewById(R.id.star1);
                imageView.setImageResource(R.drawable.splash_icon);
                imageView = (ImageView) view.findViewById(R.id.star2);
                imageView.setImageResource(R.drawable.splash_icon);
                imageView = (ImageView) view.findViewById(R.id.star3);
                imageView.setImageResource(R.drawable.splash_icon);
        }
    }
}
