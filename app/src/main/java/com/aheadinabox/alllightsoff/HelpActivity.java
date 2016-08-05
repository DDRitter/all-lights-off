package com.aheadinabox.alllightsoff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import static com.aheadinabox.alllightsoff.Utilities.*;

public class HelpActivity extends AppCompatActivity {

    private int drawableTileOnThumbId;
    private int drawableTileOffThumbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);

        setTileThumbImageDrawables();

        createExampleGrid();
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
     * Creates a bitmap with the example of tile use to avoid too much views on screen
     */
    private void createExampleGrid() {
        // Sets the grid pattern content on a Bitmap with a Canvas
        int numberOfColumns = 9;
        Bitmap thumbTileOn = BitmapFactory.decodeResource(getResources(), drawableTileOnThumbId);
        Bitmap thumbTileOff = BitmapFactory.decodeResource(getResources(), drawableTileOffThumbId);
        Bitmap thumbSolution =  BitmapFactory.decodeResource(getResources(), R.drawable.tile_solution_thumb);
        Bitmap arrowRight =  BitmapFactory.decodeResource(getResources(), R.drawable.arrow_right);
        Bitmap thumbExample;
        String currentCode;
        String tileCodeOn = getString(R.string.tile_code_on);
        String tileCodeOff = getString(R.string.tile_code_off);
        String exampleCode = getString(R.string.example_code);
        int thumbTileWidth = thumbTileOn.getWidth();
        thumbExample = Bitmap.createBitmap(thumbTileWidth * 9, thumbTileWidth * 3, Bitmap.Config.ARGB_8888);
        Canvas patternCanvas = new Canvas(thumbExample);

        for (int id = 0; id < 27; id++) {
            currentCode = exampleCode.substring(id, id + 1);
            int posX = id % numberOfColumns;
            int posY = id / numberOfColumns;
            if (currentCode.equals(tileCodeOn)) {
                // Add lit tile to the grid
                patternCanvas.drawBitmap(thumbTileOn, posX * thumbTileWidth, posY * thumbTileWidth, null);
            } else if (currentCode.equals(tileCodeOff)) {
                // Add off tile to the grid
                patternCanvas.drawBitmap(thumbTileOff, posX * thumbTileWidth, posY * thumbTileWidth, null);
            }
        }

        patternCanvas.drawBitmap(arrowRight, 4 * thumbTileWidth, thumbTileWidth, null);
        patternCanvas.drawBitmap(thumbSolution, thumbTileWidth, thumbTileWidth, null);

        ImageView imageView = (ImageView) findViewById(R.id.example_pattern);
        imageView.setImageBitmap(thumbExample);
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