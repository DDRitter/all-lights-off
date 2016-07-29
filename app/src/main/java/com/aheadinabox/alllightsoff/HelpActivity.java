package com.aheadinabox.alllightsoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import static com.aheadinabox.alllightsoff.Utilities.*;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);
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