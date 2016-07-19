package com.artactivo.alllightsoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import static com.artactivo.alllightsoff.Utilities.*;

public class MenuActivity extends AppCompatActivity {
    private static final String PREFS_FILENAME = "appSettings";
    private static final String LEVELS_STATUS = "levelStatusKey";
    private static SharedPreferences sharedPreferences;
    private Toast toast;
    private long lastBackPressTime = 0;
    private String[] mLevelCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);

        // Verifies that the data status exists and is not corrupted
        mLevelCode = getResources().getStringArray(R.array.level_codes);
        String savedData = loadLevelStatus(this);
        if (savedData == null || savedData.length() != mLevelCode.length) {
            resetLevelsStatus();
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

        // Starts the intent at the first level not completed
        Intent intent = new Intent(this, BoardActivity.class);
        intent.putExtra("level_number", firstUnsolvedLevel);
        startActivity(intent);
    }

    public void openLevels(View view) {
        Intent intent = new Intent(this, LevelsActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view) {

    }

    public void openHelp(View view) {

    }

    public void openAbout(View view) {

    }

    // Todo: Those two are file test methods, remove this methods at the end
    public void resetData(View view) {
        String data = "0000000000";  // The first 10 levels are available
        for (int id = 10; id < mLevelCode.length; id++) {
            data += "L";           // The rest of the levels are locked
        }
        saveLevelStatus(data, this);
        Toast.makeText(this, "" + data, Toast.LENGTH_LONG).show();
    }

    // Todo: Those two are file test methods, remove this methods at the end
    public void loadData(View view) {
        String loadText = loadLevelStatus(this);
        Toast.makeText(this, "String loaded: " + loadText, Toast.LENGTH_LONG).show();
    }

    /*
     * This method resets the level status to the default
     */
    // Todo: Move reset to settings page
    public void resetLevelsStatus() {
        String data = "0000000000";  // The first 10 levels are available
        for (int id = 10; id < mLevelCode.length; id++) {
            data += "L";           // The rest of the levels are locked
        }
        saveLevelStatus(data, this);
        Toast.makeText(this, "" + data, Toast.LENGTH_LONG).show();
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