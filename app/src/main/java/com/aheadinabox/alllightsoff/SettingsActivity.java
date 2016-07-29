package com.aheadinabox.alllightsoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static com.aheadinabox.alllightsoff.Utilities.saveLevelStatus;
import static com.aheadinabox.alllightsoff.Utilities.setViewBackgroundWithoutResettingPadding;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
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
}
