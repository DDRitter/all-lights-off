package com.aheadinabox.alllightsoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import static com.aheadinabox.alllightsoff.Utilities.*;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        // Sets the status for the language and audio switches
        setSwitches();

        // Sets the background for the tile currently selected and scrolls to the position
        setSelectedTile();
    }


    /*
     * Resets the level status to the default
     */
    public void resetLevels(View view) {
        String[] mLevelCode = getResources().getStringArray(R.array.level_codes);
        String data = "0000000000";  // The first 10 levels are available

        for (int id = 10; id < mLevelCode.length; id++) {
            data += "L";           // The rest of the levels are locked
        }
        saveLevelStatus(data, this);
        Toast.makeText(this, "" + data, Toast.LENGTH_LONG).show();
    }

    /*
     * Goes to the Main menu activity
     */
    public void backToMenu(View view) {
        // Sets the background of the button as focused
        setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_button_on);

        // Starts the new activity
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    /*
     * Changes the default tile image
     */
    public void setTileImage(View view) {
        removeSelectedTile();
        view.setBackgroundResource(R.drawable.tile_solution);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        settingsEditor.putInt(TILE_STYLE, Integer.parseInt(view.getTag().toString()));
        settingsEditor.apply();
    }

    /*
     * Set the border around the currently selected tile and scrolls to show the view
     */
    public void setSelectedTile() {
        int selectedTile = sharedPreferences.getInt(TILE_STYLE, 0);
        String nameId = "tile" + selectedTile;
        int viewId = getResources().getIdentifier(nameId, "id", getPackageName());
        final ImageView imageView = (ImageView) findViewById(viewId);
        imageView.setBackgroundResource(R.drawable.tile_solution);
        final HorizontalScrollView hsv = (HorizontalScrollView) findViewById(R.id.scroll_of_tiles);
        hsv.post(new Runnable() {
            @Override
            public void run() {
                hsv.scrollTo(imageView.getLeft() - 20, 0);
            }
        });
    }

    /*
     * Sets the switches for the sound effects and the background music
     */
    public void setSwitches() {
        boolean backgroundMusic = sharedPreferences.getBoolean(BACKGROUND_MUSIC, false);
        boolean soundEffects = sharedPreferences.getBoolean(SOUND_EFFECTS, false);
        String language = sharedPreferences.getString(LANGUAGE, "en");

        if (!soundEffects) {
            ((ImageView) findViewById(R.id.sound_effects)).setImageResource(R.drawable.setting_sound_off);
        }

        if (!backgroundMusic) {
            ((ImageView) findViewById(R.id.background_music)).setImageResource(R.drawable.setting_music_off);
        }

        if (language.equals("en")) {
            ((ImageView) findViewById(R.id.language_en)).setImageResource(R.drawable.setting_english_on);
            ((ImageView) findViewById(R.id.language_es)).setImageResource(R.drawable.setting_spanish_off);
        } else {
            ((ImageView) findViewById(R.id.language_en)).setImageResource(R.drawable.setting_english_off);
            ((ImageView) findViewById(R.id.language_es)).setImageResource(R.drawable.setting_spanish_on);
        }
    }

    /*
     * Removes the border on the currently selected tile
     */
    public void removeSelectedTile() {
        int selectedTile = sharedPreferences.getInt(TILE_STYLE, 0);
        String nameId = "tile" + selectedTile;
        int viewId = getResources().getIdentifier(nameId, "id", getPackageName());
        ImageView imageView = (ImageView) findViewById(viewId);
        imageView.setBackgroundResource(android.R.color.transparent);
    }

    /*
     * Switches the background music status
     */
    public void switchBackgroundMusic(View view) {
        boolean backgroundMusic = sharedPreferences.getBoolean(BACKGROUND_MUSIC, false);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        settingsEditor.putBoolean(BACKGROUND_MUSIC, !backgroundMusic);
        settingsEditor.apply();
        if (backgroundMusic) {
            ((ImageView) view).setImageResource(R.drawable.setting_music_off);
        } else {
            ((ImageView) view).setImageResource(R.drawable.setting_music_on);
        }
    }

    /*
     * Switches the sound effects status
     */
    public void switchSoundEffects(View view) {
        boolean soundEffects = sharedPreferences.getBoolean(SOUND_EFFECTS, false);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        settingsEditor.putBoolean(SOUND_EFFECTS, !soundEffects);
        settingsEditor.apply();
        if (soundEffects) {
            ((ImageView) view).setImageResource(R.drawable.setting_sound_off);
        } else {
            ((ImageView) view).setImageResource(R.drawable.setting_sound_on);
        }
    }

    /*
     * Switches the language
     */
    public void switchLanguage(View view) {
        String language = sharedPreferences.getString(LANGUAGE, "en");
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        if (language.equals("en")) {
            settingsEditor.putString(LANGUAGE, "es");
            ((ImageView) findViewById(R.id.language_en)).setImageResource(R.drawable.setting_english_off);
            ((ImageView) findViewById(R.id.language_es)).setImageResource(R.drawable.setting_spanish_on);
        } else {
            settingsEditor.putString(LANGUAGE, "en");
            ((ImageView) findViewById(R.id.language_en)).setImageResource(R.drawable.setting_english_on);
            ((ImageView) findViewById(R.id.language_es)).setImageResource(R.drawable.setting_spanish_off);
        }
        settingsEditor.apply();
    }
}