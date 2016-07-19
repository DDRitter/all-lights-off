package com.artactivo.alllightsoff;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_SCREEN_DELAY = 500; // Usual value is 3000

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflates the layout
        setContentView(R.layout.activity_splash);

        // Set the timer for launching the app
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Start the next activity
                Intent menuIntent = new Intent().setClass(SplashActivity.this, MenuActivity.class);
                startActivity(menuIntent);

                // Close this splash activity so the user won't be able
                // to go back here by pressing the back button
                finish();
            }

        };

        // Simulate a long loading process on application startup
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}