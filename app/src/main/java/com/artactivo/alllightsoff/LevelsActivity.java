package com.artactivo.alllightsoff;

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

import static com.artactivo.alllightsoff.Utilities.*;

public class LevelsActivity extends AppCompatActivity {
    private static final String LOGCAT = "AllLightsOff";
    private static final String PREFS_FILENAME = "appSettings";
    private static final String LEVELS_STATUS = "levelStatusKey";
    private static final String GRID_POSITION = "gridPositionKey";
    private static final String CURRENT_LEVEL = "currentLevelKey";
    private static SharedPreferences sharedPreferences;

    private String[] mLevelCode;
    private String mSaveGameData;
    private static int numberOfColumns = 5;                 // The size of one side of the pattern

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_levels);

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);

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

            Bitmap thumbTileOn = BitmapFactory.decodeResource(getResources(), R.drawable.tile_on_thumb);
            Bitmap thumbTileOff = BitmapFactory.decodeResource(getResources(), R.drawable.tile_off_thumb);
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
}
