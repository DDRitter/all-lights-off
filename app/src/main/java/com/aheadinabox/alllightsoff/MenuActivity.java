package com.aheadinabox.alllightsoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static com.aheadinabox.alllightsoff.Utilities.*;

public class MenuActivity extends AppCompatActivity {
    private static Toast toast;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);

        // Verifies that the data status exists and is not corrupted
        String[] mLevelCode = getResources().getStringArray(R.array.level_codes);
        String savedData = loadLevelStatus(this);
        if (savedData == null || savedData.length() != mLevelCode.length) {
            String data = "0000000000";  // The first 10 levels are available
            for (int id = 10; id < mLevelCode.length; id++) {
                data += "L";           // The rest of the levels are locked
            }
            saveLevelStatus(data, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore the menu buttons to the original colors
        restoreButtonBackgrounds();
    }

    public void openGame(View view) {
        // Sets the background of the button as focused
        setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_button_on);

        // Calculates the first level least completed on file
        String levelCodes = loadLevelStatus(this);
        int firstUnsolvedLevel = levelCodes.indexOf("0");
        if (firstUnsolvedLevel == -1) {
            firstUnsolvedLevel = levelCodes.indexOf("1");
        }
        if (firstUnsolvedLevel == -1) {
            firstUnsolvedLevel = levelCodes.indexOf("2");
        }
        if (firstUnsolvedLevel == -1) {
            firstUnsolvedLevel = 0;
        }

        // Saves the default level scroll position and the first unsolved level
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        settingsEditor.putInt(GRID_POSITION, firstUnsolvedLevel);
        settingsEditor.putInt(CURRENT_LEVEL, firstUnsolvedLevel);
        settingsEditor.apply();

        // Starts the intent at the first level not completed
        Intent intent = new Intent(this, BoardActivity.class);
        startActivity(intent);
    }

    public void openLevels(View view) {
        // Sets the background of the button as focused
        setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_button_on);

        // Starts the new activity
        Intent intent = new Intent(this, LevelsActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view) {
        // Sets the background of the button as focused
        setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_button_on);

        // Starts the new activity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openHelp(View view) {
        // Sets the background of the button as focused
        setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_button_on);

        // Starts the new activity
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void openAbout(View view) {
        // Sets the background of the button as focused
        setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_button_on);

    }

    /*
     * This method allows you to close the app by pressing twice the back button
     */
    @Override
    public void onBackPressed() {
        if (lastBackPressTime < System.currentTimeMillis() - 2500) {
            toast = Toast.makeText(this, R.string.toast_close_app, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            lastBackPressTime = System.currentTimeMillis();
            return;
        } else {
            if (toast != null) {
                toast.cancel();
            }
        }
        super.onBackPressed();
    }

    public void restoreButtonBackgrounds() {
        View view;
        String resourceName;
        for (int i = 1; i < 6; i++) {
            resourceName = "menu" + String.valueOf(i);
            int resId = getResources().getIdentifier(resourceName, "id", getPackageName());
            view = findViewById(resId);
            setViewBackgroundWithoutResettingPadding(view, R.drawable.bkg_menu_button_selector);
        }
    }
}
