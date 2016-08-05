package com.aheadinabox.alllightsoff;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static com.aheadinabox.alllightsoff.Utilities.*;

public class LevelsActivity extends AppCompatActivity {
    private String[] mLevelCode;
    private String mSaveGameData;
    private static int numberOfColumns = 5;                 // The size of one side of the pattern

    private int drawableTileOnThumbId;
    private int drawableTileOffThumbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_levels);

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);

        setTileThumbImageDrawables();

        mLevelCode = getResources().getStringArray(R.array.level_codes);
        mSaveGameData = loadLevelStatus(this);

        final GridView levelsGrid = (GridView) findViewById(R.id.level_list);
        levelsGrid.setAdapter(new CustomAdapter(this));

        // Scroll grid view to last known scroll position by reading this from preferences
        levelsGrid.setSelection(sharedPreferences.getInt(GRID_POSITION, 0));

        levelsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getLevelData(mSaveGameData, position).equals("L")) {
                    Toast toast = Toast.makeText(getBaseContext(), R.string.toast_locked_level, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                    return;
                }
                // Save current scroll position of grid view screen when tapped
                SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
                settingsEditor.putInt(GRID_POSITION, levelsGrid.getFirstVisiblePosition());
                settingsEditor.apply();

                // Save the current level from the position of the item clicked
                settingsEditor.putInt(CURRENT_LEVEL, position);
                settingsEditor.apply();
                Intent intent = new Intent(getBaseContext(), BoardActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public class CustomAdapter extends BaseAdapter {
        private Context mContext;

        public CustomAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mLevelCode.length;
        }

        public Object getItem(int position) {
            return mLevelCode[position];
        }

        public long getItemId(int position) {
            return position;
        }

        // Create a new customized view for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View gridItem;

            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                LayoutInflater inflater = getLayoutInflater();
                gridItem = inflater.inflate(R.layout.level_layout, parent, false);
            } else {
                gridItem = convertView;
            }

            // Sets the level number
            TextView textView = (TextView) gridItem.findViewById(R.id.level_number);
            String levelNumber = String.format(Locale.getDefault(), "%03d", position + 1);
            textView.setText(levelNumber);

            // Sets the star rating based on the level status
            String status = getLevelData(mSaveGameData, position);
            setStars(status, gridItem, 0);

            // If the level is not completed, hide the remaining levels with a lock icon

            if (status.equals("L")) {
                ImageView imageView = (ImageView) gridItem.findViewById(R.id.level_pattern);
                imageView.setImageResource(R.drawable.level_lock);
                return gridItem;
            }

            // Sets the grid pattern content on a Bitmap with a Canvas

            Bitmap thumbTileOn = BitmapFactory.decodeResource(getResources(), drawableTileOnThumbId);
            Bitmap thumbTileOff = BitmapFactory.decodeResource(getResources(), drawableTileOffThumbId);
            Bitmap thumbPattern;
            String currentCode;
            String tileCodeOn = getString(R.string.tile_code_on);
            int thumbTileWidth = thumbTileOn.getWidth();

            thumbPattern = Bitmap.createBitmap(thumbTileWidth * 5, thumbTileWidth * 5, Bitmap.Config.ARGB_8888);

            Canvas patternCanvas = new Canvas(thumbPattern);

            for (int id = 0; id < numberOfColumns * numberOfColumns; id++) {
                currentCode = mLevelCode[position].substring(id, id + 1);
                int posX = id % numberOfColumns;
                int posY = id / numberOfColumns;
                if (currentCode.equals(tileCodeOn)) {
                    // Add lit tile to the grid
                    patternCanvas.drawBitmap(thumbTileOn, posX * thumbTileWidth, posY * thumbTileWidth, null);
                } else {
                    // Add off tile to the grid
                    patternCanvas.drawBitmap(thumbTileOff, posX * thumbTileWidth, posY * thumbTileWidth, null);
                }
            }
            ImageView imageView = (ImageView) gridItem.findViewById(R.id.level_pattern);
            imageView.setImageBitmap(thumbPattern);

            return gridItem;
        }
    }

    /**
     * Sets the tile thumb images drawables from the value on current settings
     */
    private void setTileThumbImageDrawables() {
        int selectedTile = sharedPreferences.getInt(TILE_STYLE, 0);
        String tileOnName = "tile" + selectedTile + "_on_thumb";
        if (selectedTile == 1 || selectedTile == 2) {  // Set tile0 background for tile1 and tile2
            selectedTile = 0;
        } else if (selectedTile == 9 || selectedTile == 10) {  // Set tile8 background for tile9 and tile10
            selectedTile = 8;
        }
        String tileOffName = "tile" + selectedTile + "_off_thumb";
        drawableTileOnThumbId = getResources().getIdentifier(tileOnName, "drawable", getPackageName());
        drawableTileOffThumbId = getResources().getIdentifier(tileOffName, "drawable", getPackageName());
    }
}
