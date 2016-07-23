package com.artactivo.alllightsoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import static com.artactivo.alllightsoff.Utilities.*;

public class MenuActivity extends AppCompatActivity {
    private static final String PREFS_FILENAME = "appSettings";
    private static final String LEVELS_STATUS = "levelStatusKey";
    private static final String GRID_POSITION = "gridPositionKey";
    private static final String CURRENT_LEVEL = "currentLevelKey";
    private static SharedPreferences sharedPreferences;
    private static Toast toast;
    private static long lastBackPressTime = 0;
    private static String[] mLevelCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);

        // Verifies that the data status exists and is not corrupted
        mLevelCode = getResources().getStringArray(R.array.level_codes);
        String savedData = loadLevelStatus(this);
        if (savedData == null || savedData.length() != mLevelCode.length) {
            String data = "0000000000";  // The first 10 levels are available
            for (int id = 10; id < mLevelCode.length; id++) {
                data += "L";           // The rest of the levels are locked
            }
            saveLevelStatus(data, this);
        }
    }

    public void openGame(View view) {
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
        Intent intent = new Intent(this, LevelsActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void openAbout(View view) {

    }

    /*
     * This method allows you to close the app by pressing twice the back button
     */
    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 2500) {
            toast = Toast.makeText(this, R.string.toast_close_app, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
            return;
        } else {
            if (toast != null) {
                toast.cancel();
            }
        }
        super.onBackPressed();
    }
}