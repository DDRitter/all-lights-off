package com.artactivo.alllightsoff;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import static com.artactivo.alllightsoff.Utilities.saveLevelStatus;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }


    /*
     * This method resets the level status to the default
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
}
