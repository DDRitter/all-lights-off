package com.artactivo.alllightsoff;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
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
        return gameData.substring(currentLevel, currentLevel + 1);
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
    protected static void setStars(String status, View view, final int duration) {
        final ImageView starImageView1 = (ImageView) view.findViewById(R.id.star1);
        final ImageView starImageView2 = (ImageView) view.findViewById(R.id.star2);
        final ImageView starImageView3 = (ImageView) view.findViewById(R.id.star3);
        switch (status) {
            case "A":
                starImageView1.setImageResource(R.drawable.star_unlit);
                starImageView2.setImageResource(R.drawable.star_unlit);
                starImageView3.setImageResource(R.drawable.star_unlit);
                break;
            case "B":
                starImageView1.setImageResource(R.drawable.star_lit);
                starImageView2.setImageResource(R.drawable.star_unlit);
                starImageView3.setImageResource(R.drawable.star_unlit);
                break;
            case "C":
                starImageView1.setImageResource(R.drawable.star_lit);
                starImageView2.setImageResource(R.drawable.star_lit);
                starImageView3.setImageResource(R.drawable.star_unlit);
                break;
            case "D":
                starImageView1.setImageResource(R.drawable.star_lit);
                starImageView2.setImageResource(R.drawable.star_lit);
                starImageView3.setImageResource(R.drawable.star_lit);
                break;
            case "E":
                starImageView1.setImageResource(R.drawable.star_disabled);
                starImageView2.setImageResource(R.drawable.star_disabled);
                starImageView3.setImageResource(R.drawable.star_disabled);
                break;
            case "F":
                starImageView1.setImageResource(R.drawable.star_question);
                starImageView2.setImageResource(R.drawable.star_disabled);
                starImageView3.setImageResource(R.drawable.star_disabled);
                break;
            case "G":
                starImageView1.setImageResource(R.drawable.star_question);
                starImageView2.setImageResource(R.drawable.star_question);
                starImageView3.setImageResource(R.drawable.star_disabled);
                break;
            case "H":
                starImageView1.setImageResource(R.drawable.star_question);
                starImageView2.setImageResource(R.drawable.star_question);
                starImageView3.setImageResource(R.drawable.star_question);
                break;

            default:
                starImageView1.setImageResource(R.drawable.star_disabled);
                starImageView2.setImageResource(R.drawable.star_disabled);
                starImageView3.setImageResource(R.drawable.star_disabled);
        }
        if (duration > 0) {
            animateFade(starImageView1, 1, duration);
            animateFade(starImageView2, 1, duration);
            animateFade(starImageView3, 1, duration);
        }
    }

//    /**
//     * This method assigns the appropriate stars to the
//     *
//     * @param status is:
//     * A -> No Stars (Not solved at all)
//     * B -> One Star (Solved with more than 3 times the movements needed)
//     * C -> Two Stars (Solved with more than 2 times the movements needed)
//     * D -> Three Stars (Solved with the minimum movements needed)
//     *
//     * L -> Locked
//     *
//     * E -> No question mark at all (Game in progress, more than 3 times the moves needed)
//     * F -> One star as question mark (Game in progress, more than 2 times the moves needed)
//     * G -> Two stars with question mark (Game in progress, more than the moves needed)
//     * H -> All stars as question mark (Game has started, still able to win all)
//
//     */
//    protected static void setStars(String status, View view, int duration) {
//        ImageView imageView;
//        switch (status) {
//            case "A":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_unlit);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_unlit);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_unlit);
//                break;
//            case "B":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_lit);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_unlit);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_unlit);
//                break;
//            case "C":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_lit);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_lit);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_unlit);
//                break;
//            case "D":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_lit);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_lit);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_lit);
//                break;
//            case "E":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                break;
//            case "F":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_question);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                break;
//            case "G":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_question);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_question);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                break;
//            case "H":
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_question);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_question);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_question);
//                break;
//
//            default:
//                imageView = (ImageView) view.findViewById(R.id.star1);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                imageView = (ImageView) view.findViewById(R.id.star2);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//                imageView = (ImageView) view.findViewById(R.id.star3);
//                if (duration > 0) { animateFade(imageView, 1, duration); }
//                imageView.setImageResource(R.drawable.star_disabled);
//        }
//    }

    /**
     * This method animates the fade in-out of an ImageView
     *
     * @param imageView the ImageView object
     * @param toggle indicates if the animation is a fade-in or a fade-out
     * @param duration the duration of the animation in milliseconds
     *
     */
    protected static void animateFade(ImageView imageView, int toggle, int duration) {
        Animation fade = null;
        if (toggle == 1) {
            fade = new AlphaAnimation(0, 1);
        } else {
            fade = new AlphaAnimation(1, 0);
        }
        fade.setInterpolator(new DecelerateInterpolator());
        fade.setDuration(duration);
        fade.setFillAfter(true);
        imageView.setAlpha(1.0f);
        imageView.startAnimation(fade);
    }
}
