package com.artactivo.alllightsoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import static com.artactivo.alllightsoff.Utilities.*;

public class MenuActivity extends AppCompatActivity {
    private Toast toast;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }


    public void openGame(View view) {
        // Calculates the first level least completed on file
        String levelCodes = readFromFile(this);
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

    // File test methods
    // Todo: Move reset to settings page
    public void resetData(View view) {
        String data = "0000000000";  // The first 10 levels are available
        int numberOfLevels = getResources().getStringArray(R.array.level_codes).length;
        for (int id = 10; id < numberOfLevels; id++) {
            data += "L";           // The rest of the levels are locked
        }
        writeToFile(data, this);
        Toast.makeText(this, "" + data, Toast.LENGTH_LONG).show();
    }

    public void loadData(View view) {
        // Todo: Verify that the data saved is not corrupted and that it matches the array in strings
        String loadText = readFromFile(this);
        Toast.makeText(this, "String loaded: " + loadText, Toast.LENGTH_LONG).show();
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
