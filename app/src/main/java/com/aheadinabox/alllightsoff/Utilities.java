package com.aheadinabox.alllightsoff;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * Some methods useful for several activities
 *
 * Created by DDRitter on 08/07/2016.
 */
public class Utilities extends AppCompatActivity {
    protected static final String LOGCAT = "AllLightsOff";
    protected static final String PREFS_FILENAME = "appSettings";
    protected static final String LEVELS_STATUS = "levelStatusKey";
    protected static final String GRID_POSITION = "gridPositionKey";
    protected static final String CURRENT_LEVEL = "currentLevelKey";
    protected static final String TILE_STYLE = "tileStyleKey";
    protected static final String BACKGROUND_MUSIC = "backgroundMusicKey";
    protected static final String SOUND_EFFECTS = "soundEffectsKey";
    protected static final String LANGUAGE = "languageKey";
    protected static SharedPreferences sharedPreferences;

    /**
     * This method loads the level status data
     *
     * @return the status
     */
    protected static String loadLevelStatus(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);
        return sharedPreferences.getString(LEVELS_STATUS, null);
    }

    /**
     * This method updates the level status data
     *
     * @param data the status
     */
    protected static void saveLevelStatus(String data, Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        settingsEditor.putString(LEVELS_STATUS, data);
        settingsEditor.apply();
    }

    /**
     * This method returns the level status letter saved on the file
     */
    protected static String getLevelData(String gameData, int currentLevel) {
        return gameData.substring(currentLevel, currentLevel + 1);
    }

    /**
     * This method assigns the appropriate stars to the
     *
     * @param status is:
     *               0 -> No Stars (Not solved at all)
     *               1 -> One Star (Solved with more than 3 times the movements needed)
     *               2 -> Two Stars (Solved with more than 2 times the movements needed)
     *               3 -> Three Stars (Solved with the minimum movements needed)
     *               <p/>
     *               L -> Locked
     *               <p/>
     *               --- -> No question mark at all (Game in progress, more than 3 times the moves needed)
     *               ?-- -> One star as question mark (Game in progress, more than 2 times the moves needed)
     *               ??- -> Two stars with question mark (Game in progress, more than the moves needed)
     *               ??? -> All stars as question mark (Game has started, still able to win all)
     */
    protected static void setStars(String status, View view, int duration) {
        // Todo: simplify this by analysing the string one character at a time
        ImageView starImageView1 = (ImageView) view.findViewById(R.id.star1);
        ImageView starImageView2 = (ImageView) view.findViewById(R.id.star2);
        ImageView starImageView3 = (ImageView) view.findViewById(R.id.star3);
        switch (status) {
            case "0":
                starImageView1.setImageResource(R.drawable.star_unlit);
                starImageView2.setImageResource(R.drawable.star_unlit);
                starImageView3.setImageResource(R.drawable.star_unlit);
                break;
            case "1":
                starImageView1.setImageResource(R.drawable.star_lit);
                starImageView2.setImageResource(R.drawable.star_unlit);
                starImageView3.setImageResource(R.drawable.star_unlit);
                break;
            case "2":
                starImageView1.setImageResource(R.drawable.star_lit);
                starImageView2.setImageResource(R.drawable.star_lit);
                starImageView3.setImageResource(R.drawable.star_unlit);
                break;
            case "3":
                starImageView1.setImageResource(R.drawable.star_lit);
                starImageView2.setImageResource(R.drawable.star_lit);
                starImageView3.setImageResource(R.drawable.star_lit);
                break;
            case "---":
                starImageView1.setImageResource(R.drawable.star_disabled);
                starImageView2.setImageResource(R.drawable.star_disabled);
                starImageView3.setImageResource(R.drawable.star_disabled);
                break;
            case "?--":
                starImageView1.setImageResource(R.drawable.star_question);
                starImageView2.setImageResource(R.drawable.star_disabled);
                starImageView3.setImageResource(R.drawable.star_disabled);
                break;
            case "??-":
                starImageView1.setImageResource(R.drawable.star_question);
                starImageView2.setImageResource(R.drawable.star_question);
                starImageView3.setImageResource(R.drawable.star_disabled);
                break;
            case "???":
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
//            animateFade(starImageView1, 1, duration);
//            animateFade(starImageView2, 1, duration);
//            animateFade(starImageView3, 1, duration);
            animateScale(starImageView1, 0.0f, 1.0f, duration);
            animateScale(starImageView2, 0.0f, 1.0f, duration);
            animateScale(starImageView3, 0.0f, 1.0f, duration);
//            animateRotation(starImageView1, -180.0f, 0.0f, duration);
//            animateRotation(starImageView2, -180.0f, 0.0f, duration);
//            animateRotation(starImageView3, -180.0f, 0.0f, duration);
        }
    }

    /**
     * This method animates the fade in or out of an ImageView
     *
     * @param view     the ImageView object
     * @param toggle   indicates if the animation is a fade-in (1) or a fade-out (0)
     * @param duration the duration of the animation in milliseconds
     */
    protected static void animateFade(View view, int toggle, int duration) {
        Animation fade;
        if (toggle == 1) {
            fade = new AlphaAnimation(0, 1);
        } else {
            fade = new AlphaAnimation(1, 0);
        }
        fade.setInterpolator(new DecelerateInterpolator());
        fade.setDuration(duration);
        fade.setFillAfter(true);
        view.setAlpha(1.0f);
        view.startAnimation(fade);
    }

    /**
     * This method animates the fade in or out of an ImageView
     *
     * @param view     the ImageView object
     * @param toggle   indicates if the animation is a fade-in (1) or a fade-out (0)
     * @param duration the duration of the animation in milliseconds
     * @param delay    the delay before starting the animation in milliseconds
     */
    protected static void animateFade(View view, int toggle, int duration, int delay) {
        Animation fade;
        if (toggle == 1) {
            fade = new AlphaAnimation(0, 1);
        } else {
            fade = new AlphaAnimation(1, 0);
        }
        fade.setInterpolator(new DecelerateInterpolator());
        fade.setDuration(duration);
        fade.setStartOffset(delay);
        fade.setFillAfter(true);
        view.setAlpha(1.0f);
        view.startAnimation(fade);
    }

    /**
     * This method animates the scale in or out of an ImageView
     *
     * @param view     the ImageView object
     * @param from     the initial scale factor
     * @param to       the final scale factor
     * @param duration the duration of the animation in milliseconds
     */
    protected static void animateScale(View view, float from, float to, int duration) {
        Animation scale = new ScaleAnimation(from, to, from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setInterpolator(new DecelerateInterpolator());
        scale.setDuration(duration);
        view.startAnimation(scale);
    }

    /**
     * This method animates the rotation of an ImageView
     *
     * @param view     the ImageView object
     * @param from     the initial angle
     * @param to       the final angle
     * @param duration the duration of the animation in milliseconds
     */
    protected static void animateRotation(View view, float from, float to, int duration) {
        Animation rotation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotation.setInterpolator(new DecelerateInterpolator());
        rotation.setDuration(duration);
        view.startAnimation(rotation);
    }

    /**
     * This method changes the background of a Button maintaining the original padding
     *
     * @param view              the View object
     * @param backgroundResId   the id of the background resource
     */
    protected static void setViewBackgroundWithoutResettingPadding(final View view, final int backgroundResId) {
        final int paddingBottom = view.getPaddingBottom(), paddingLeft = view.getPaddingLeft();
        final int paddingRight = view.getPaddingRight(), paddingTop = view.getPaddingTop();
        view.setBackgroundResource(backgroundResId);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
