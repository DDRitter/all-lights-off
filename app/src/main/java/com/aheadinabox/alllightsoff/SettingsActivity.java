package com.aheadinabox.alllightsoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static com.aheadinabox.alllightsoff.Utilities.*;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        // Sets the background for the tile currently selected
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
        settingsEditor.putInt(TILE_IMAGE, Integer.parseInt(view.getTag().toString()));
        settingsEditor.apply();


        Log.i(LOGCAT, "Tile selected: " + sharedPreferences.getInt(TILE_IMAGE, 0));

        //sharedPreferences.getInt(TILE_IMAGE, 0);

    }

    /*
     * Set the border around the currently selected tile and scrolls to show the view
     */
    public void setSelectedTile() {
        int selectedTile = sharedPreferences.getInt(TILE_IMAGE, 0);
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
     * Removes the border on the currently selected tile
     */
    public void removeSelectedTile() {
        int selectedTile = sharedPreferences.getInt(TILE_IMAGE, 0);
        String nameId = "tile" + selectedTile;
        int viewId = getResources().getIdentifier(nameId, "id", getPackageName());
        ImageView imageView = (ImageView) findViewById(viewId);
        imageView.setBackgroundResource(android.R.color.transparent);
    }

}
