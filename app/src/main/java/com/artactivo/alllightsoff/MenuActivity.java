package com.artactivo.alllightsoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import static android.view.Gravity.CENTER_VERTICAL;

public class MenuActivity extends AppCompatActivity {
    private Toast toast;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }


    public void openGame(View view) {
        Intent intent = new Intent(this, BoardActivity.class);
        // Todo: get the current unsolved level from the saved data
        //intent.putExtra("level_number", 0);
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
