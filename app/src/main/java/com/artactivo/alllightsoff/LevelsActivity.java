package com.artactivo.alllightsoff;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LevelsActivity extends AppCompatActivity {
    private final String LOGCAT = "AllLightsOff";
    // Todo: store this array in the splash and pass it to the new activities instead of creating a new one each time
    private String[] mLevelCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);

        // Todo: store this array in the splash and pass it to the new activities instead of creating a new one each time
        mLevelCode = getResources().getStringArray(R.array.level_codes);

        GridView levelsGrid = (GridView) findViewById(R.id.level_list);
        levelsGrid.setAdapter(new CustomAdapter(this));

        levelsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // We start the board activity with the selected level taken from the position on the grid
                Intent intent = new Intent(getBaseContext(), BoardActivity.class);
                intent.putExtra("level_number", position);
                startActivity(intent);
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
                gridItem = (View) convertView;
            }

            // Sets the star rating
            // Todo: Use actual data stored for this
            ImageView imageView = (ImageView) gridItem.findViewById(R.id.star1);
            imageView.setImageResource(R.drawable.star_lit);

            // Sets the level number
            TextView textView = (TextView) gridItem.findViewById(R.id.level_number);
            String levelNumber = String.format("%03d", position + 1);
            textView.setText(levelNumber);

            // Todo: If the level is not completed, hide the remaining levels with a lock icon

            // Sets the grid pattern content on a Bitmap with a Canvas

            Bitmap thumbTileOn = BitmapFactory.decodeResource(getResources(), R.drawable.tile_on_thumb);
            Bitmap thumbTileOff = BitmapFactory.decodeResource(getResources(), R.drawable.tile_off_thumb);
            Bitmap thumbPattern = null;
            String currentCode;
            String tileCodeOn = getString(R.string.tile_code_on);
            int thumbTileWidth = thumbTileOn.getWidth();

            thumbPattern = Bitmap.createBitmap(thumbTileWidth * 5, thumbTileWidth * 5, Bitmap.Config.ARGB_8888);

            Canvas patternCanvas = new Canvas(thumbPattern);

            for (int id = 0; id < 25; id++) {                               //Todo fix this HC 25
                currentCode = mLevelCode[position].substring(id, id + 1);
                int posX = id % 5;                                          //Todo fix this HC 5
                int posY = id / 5;                                          //Todo fix this HC 5
                if (currentCode.equals(tileCodeOn)) {
                    // add lit tile to gridview
                    patternCanvas.drawBitmap(thumbTileOn, posX * thumbTileWidth, posY * thumbTileWidth, null);
                } else {
                    // add off tile to gridview
                    patternCanvas.drawBitmap(thumbTileOff, posX * thumbTileWidth, posY * thumbTileWidth, null);
                }
            }
            ImageView imageView2 = (ImageView) gridItem.findViewById(R.id.level_pattern2);
            imageView2.setImageBitmap(thumbPattern);

            return gridItem;
        }
    }
}
