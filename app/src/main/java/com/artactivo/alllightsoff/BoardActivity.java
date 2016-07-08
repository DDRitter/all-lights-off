package com.artactivo.alllightsoff;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import static android.media.AudioManager.*;
import static android.support.v7.app.ActionBar.*;
import static com.artactivo.alllightsoff.FileOperations.readFromFile;
import static com.artactivo.alllightsoff.FileOperations.writeToFile;

public class BoardActivity extends AppCompatActivity implements View.OnTouchListener {
    private final String LOGCAT = "AllLightsOff";
    private Toast toast;
    private long lastBackPressTime = 0;
    private long lastResetPressTime = 0;
    private long lastNextPressTime = 0;
    private long lastPrevPressTime = 0;

    private SoundPool soundEffects;
    private int clickSoundId;
    private int numberOfColumns = 5;                                // The size of one side of the pattern
    private int numberOfTiles = numberOfColumns * numberOfColumns;
    private int sizeOfTiles;
    private String[] mLevelCode;
    private String[] mLevelName;
    private int mCurrentLevel;
    private int[] mTilePattern = new int[numberOfTiles];
    private int[] mSolutionPattern = new int[numberOfTiles];
    private int mSolvedTiles;
    private int mNumberOfMoves;
    private boolean gameHasEnded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mLevelCode = getResources().getStringArray(R.array.level_codes);
        mLevelName = new String[mLevelCode.length];
        mNumberOfMoves = 0;

        Intent intent = getIntent();
        if (intent.hasExtra("level_number")) {
            mCurrentLevel = getIntent().getExtras().getInt("level_number");
        } else {
            mCurrentLevel = 0;
        }
        prepareSounds();
        calculateBoardLayout();
        createBackgroundBoard(numberOfColumns, sizeOfTiles);
        createForegroundBoard(numberOfColumns, sizeOfTiles, mLevelCode[mCurrentLevel]);
    }

    /**
     * This method calculates the sizes for the board, header and button panel
     */
    private void calculateBoardLayout() {
        // Figures out the smallest screen side available to get the board tile size
        int boardDimensionPx;
        int remainingPxSpace;
        int statusBarHeight = 0;
        boolean isPortrait;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resId);
        }
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            isPortrait = false;
            remainingPxSpace = displayMetrics.widthPixels - displayMetrics.heightPixels + statusBarHeight;
            boardDimensionPx = displayMetrics.heightPixels - statusBarHeight - (int) (convertDpToPx(16, this) * 2);
        } else {
            isPortrait = true;
            remainingPxSpace = displayMetrics.heightPixels - displayMetrics.widthPixels - statusBarHeight;
            boardDimensionPx = displayMetrics.widthPixels - (int) (convertDpToPx(16, this) * 2);
        }
        sizeOfTiles = (int) boardDimensionPx / numberOfColumns;

        // Adjust the header height or width to half of the remaining screen space
        // to keep the board centered on the screen
        RelativeLayout.LayoutParams rlp;
        ImageView header = (ImageView) findViewById(R.id.header);
        if (isPortrait) {
            rlp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, remainingPxSpace / 2);
            header.setLayoutParams(rlp);
        } else {
            rlp = new RelativeLayout.LayoutParams(remainingPxSpace / 2, LayoutParams.MATCH_PARENT);
            header.setLayoutParams(rlp);
        }
    }


    /**
     * This method manages the touch of a tile on the board
     */
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (gameHasEnded) {
            return false;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            if (mNumberOfMoves == 0) {
                View reset = (View) findViewById(R.id.reset_button);
                reset.setEnabled(true);
                reset.setAlpha(1.0f);
            }
            mNumberOfMoves++;
            playClickSound();
            hideSolutionTile(view.getId());
            changeTiles(view.getId());
            checkBoard();
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method plays the click sound
     */
    private void playClickSound() {
        soundEffects.play(clickSoundId, 1, 1, 1, 0, 1);
    }

    /**
     * This method verifies the current tile position and checks neighbour tiles to change.
     *
     * @param id The tile view id inside the board
     */
    private void changeTiles(int id) {
        int posX = id % numberOfColumns;
        int posY = id / numberOfColumns;
        changeSingleTile(id);
        if (posX != 0) {
            changeSingleTile(id - 1);
        }
        if (posX != numberOfColumns - 1) {
            changeSingleTile(id + 1);
        }
        if (posY != 0) {
            changeSingleTile(id - numberOfColumns);
        }
        if (posY != numberOfColumns - 1) {
            changeSingleTile(id + numberOfColumns);
        }
    }

    /**
     * This method verifies if the current tile has a solution mark and removes it
     *
     * @param id The tile view id inside the board
     */
    private void hideSolutionTile(int id) {
        if (mSolutionPattern[id] == 1) {
            GridLayout layout = (GridLayout) findViewById(R.id.board_solution);
            ImageView image = (ImageView) layout.findViewById(id);
            image.setImageResource(android.R.color.transparent);
        }
    }

    /**
     * This method checks the current tile and swaps it's value
     *
     * @param id The tile view id inside the board
     */
    private void changeSingleTile(int id) {
        GridLayout layout = (GridLayout) findViewById(R.id.board_tiles);
        ImageView image = (ImageView) layout.findViewById(id);
        if (mTilePattern[id] == 1) {                         //if it's the blue tile
            animateFade(image, 0);
            mSolvedTiles++;
            mTilePattern[id] = 0;
        } else {                                          // if it's the off tile
            animateFade(image, 1);
            mSolvedTiles--;
            mTilePattern[id] = 1;
        }
    }

    /**
     * This method checks if the board is solved
     * and ends the game, saving the new data
     */
    private void checkBoard() {
        if (mSolvedTiles == numberOfTiles) {
            String endMessage = getResources().getString(R.string.end_game_message) + "      Moves: " + mNumberOfMoves;
            TextView textView = (TextView) findViewById(R.id.panel);
            textView.setText(endMessage);
            // Todo: read the data from the file and add the new values from the current level
            // Reads the game data from the file
            String gameData = readFromFile(this);
            // Changes the data on the corresponding level if it's different
            String newGameData = "";
            String currentLevelStatus = String.format("%04d", mCurrentLevel);
            currentLevelStatus += "B"; // Todo: assign the appropriate value here based on the moves table (A, B or C)
            newGameData = gameData.substring(0, 5 * mCurrentLevel) + currentLevelStatus + gameData.substring(5 * (mCurrentLevel + 1));
            writeToFile(newGameData, this);

















            // Clears the board and sets some endgame values
            GridLayout board = (GridLayout) findViewById(R.id.board_tiles);
            board.removeAllViews();
            gameHasEnded = true;
            mNumberOfMoves = 0;
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    private static float convertDpToPx(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method animates the fade in-out of an ImageView
     *
     * @param imageView the ImageView object
     * @param id        the identifier location on the board
     */
    private void animateFade(ImageView imageView, int id) {
        Animation fade = null;
        if (id == 1) {
            fade = new AlphaAnimation(0, 1);
        } else {
            fade = new AlphaAnimation(1, 0);
        }
        fade.setInterpolator(new DecelerateInterpolator());
        fade.setDuration(300);
        fade.setFillAfter(true);
        imageView.setAlpha(1.0f);
        imageView.startAnimation(fade);
    }

    /**
     * This method creates the SoundPool that stores all the sound effects from the game
     */
    private void prepareSounds() {
        soundEffects = new SoundPool(10, STREAM_MUSIC, 0);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        clickSoundId = soundEffects.load(this, R.raw.click, 1);
    }

    /**
     * This method resets the current level
     */
    public void resetLevel(View view) {
        if (!gameHasEnded) {
            if (this.lastResetPressTime < System.currentTimeMillis() - 2500) {
                toast = Toast.makeText(this, R.string.toast_reset_board, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                this.lastResetPressTime = System.currentTimeMillis();
                return;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
            }
        }
        createForegroundBoard(numberOfColumns, sizeOfTiles, mLevelCode[mCurrentLevel]);
    }

    /**
     * This method goes back one level
     */
    public void prevLevel(View view) {
        if (mNumberOfMoves > 0) {
            if (this.lastPrevPressTime < System.currentTimeMillis() - 2500) {
                toast = Toast.makeText(this, R.string.toast_prev_level, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                this.lastPrevPressTime = System.currentTimeMillis();
                return;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
            }
        }
        mCurrentLevel--;
        createForegroundBoard(numberOfColumns, sizeOfTiles, mLevelCode[mCurrentLevel]);
    }

    /**
     * This method goes forward one level
     */
    public void nextLevel(View view) {
        if (mNumberOfMoves > 0) {
            if (this.lastNextPressTime < System.currentTimeMillis() - 2500) {
                toast = Toast.makeText(this, R.string.toast_next_level, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                this.lastNextPressTime = System.currentTimeMillis();
                return;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
            }
        }
        mCurrentLevel++;
        createForegroundBoard(numberOfColumns, sizeOfTiles, mLevelCode[mCurrentLevel]);
    }

    /**
     * This method creates the background board grid with unlit tiles
     */
    public void createBackgroundBoard(int numColumns, int tileSize) {
        ImageView image;
        LayoutParams lp = new LayoutParams(tileSize, tileSize);
        GridLayout backBoard = (GridLayout) findViewById(R.id.board_background);
        backBoard.setColumnCount(numColumns);
        for (int i = 0; i < numColumns * numColumns; i++) {
            image = new ImageView(this);
            image.setLayoutParams(lp);
            image.setImageResource(R.drawable.tile_off);
            backBoard.addView(image);
        }
    }

    /**
     * This method creates the foreground board grid with lit tiles
     * putting the tiles with alpha 0 or 1 depending on visibility
     */
    public void createForegroundBoard(int numColumns, int tileSize, String levelCode) {
        String tileCodeOn = getString(R.string.tile_code_on);
        String tileCodeOff = getString(R.string.tile_code_off);
        ImageView image;
        GridLayout board;
        mSolvedTiles = 0;
        mNumberOfMoves = 0;
        gameHasEnded = false;
        String currentCode = "";
        int currentPos = 0;

        // Clears the solution board
        // TOdo: the game crashes if I click on a tile after changing level if this is active
        for (int id = 0; id < numberOfTiles; id++) {
            hideSolutionTile(id);
        }

        // Stores the array of tiles on or off
        for (int id = 0; id < levelCode.length(); id++) {
            currentCode = levelCode.substring(id, id + 1);
            if (currentCode.equals(tileCodeOn)) {
                mTilePattern[currentPos] = 1;
                currentPos++;
            } else if (currentCode.equals(tileCodeOff)) {
                mTilePattern[currentPos] = 0;
                currentPos++;
            } else if (currentCode.equals("#")) {
                mLevelName[mCurrentLevel] = levelCode.substring(id + 2);
                break;
            }
        }

        TextView textView = (TextView) findViewById(R.id.panel);
        textView.setText(getString(R.string.level) + String.format("%03d", mCurrentLevel + 1) + "   " + mLevelName[mCurrentLevel]);
        LayoutParams lp = new LayoutParams(tileSize, tileSize);
        board = (GridLayout) findViewById(R.id.board_tiles);
        board.removeAllViews();
        board.setColumnCount(numColumns);
        for (int id = 0; id < numColumns * numColumns; id++) {
            image = new ImageView(this);
            image.setLayoutParams(lp);
            image.setId(id);
            image.setOnTouchListener(this);
            image.setImageResource(R.drawable.tile_on);
            if (mTilePattern[id] == 1) {
                image.setAlpha(1.0f);
            } else {
                image.setAlpha(0.0f);
                mSolvedTiles++;
            }
            board.addView(image);
        }
        setPanelButtons();
    }

    /**
     * This method sets the appropriate mode for the navigation and reset buttons,
     * depending on the level number
     */
    public void setPanelButtons() {
        View view = findViewById(R.id.reset_button);
        view.setEnabled(false);
        view.setAlpha(0.25f);

        if (mCurrentLevel == mLevelCode.length - 1) {                // We are at the last level
            view = findViewById(R.id.next_button);
            view.setEnabled(false);
            view.setAlpha(0.25f);
        } else if (mCurrentLevel == 0) {                             // We are at the first level
            view = findViewById(R.id.prev_button);
            view.setEnabled(false);
            view.setAlpha(0.25f);
        } else {
            view = findViewById(R.id.prev_button);
            view.setEnabled(true);
            view.setAlpha(1.0f);
            view = findViewById(R.id.next_button);
            view.setEnabled(true);
            view.setAlpha(1.0f);
        }
    }



    /**
     * This method animates the lights on the board
     * Todo: Not working as intended, add delay between lights
     */
    public void animateBoard(int numColumns, int tileSize) {
        ImageView image;
        LayoutParams lp = new LayoutParams(tileSize, tileSize);
        GridLayout board = (GridLayout) findViewById(R.id.board_tiles);
        board.removeAllViews();
        board.setColumnCount(numColumns);

        for (int i = 0; i < numColumns * numColumns; i++) {
            image = new ImageView(this);
            image.setLayoutParams(lp);
            image.setImageResource(R.drawable.tile_on);
            board.addView(image);
            animateFade(image, 1);
        }
    }


    /*
     * This method allows you to go to the main menu by pressing twice the back button
     */
    @Override
    public void onBackPressed() {
        if (mNumberOfMoves > 0) {
            if (this.lastBackPressTime < System.currentTimeMillis() - 2500) {
                toast = Toast.makeText(this, R.string.toast_leave_game, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                this.lastBackPressTime = System.currentTimeMillis();
                return;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
            }
        }
        super.onBackPressed();
    }








    /**
     * This method solves the board and shows the solution with green tiles
     * Note: This only works with 5 tiles
     */
    // Todo: unify the two sweeps into one method
    public void solve5x5Board(View view) {
        int[] mTemporaryPattern = new int[numberOfTiles];
        boolean ruleA = false;
        boolean ruleB = false;
        boolean ruleC = false;

        // First we copy the board pattern to another array
        System.arraycopy(mTilePattern, 0, mTemporaryPattern, 0, mTilePattern.length);
        Arrays.fill(mSolutionPattern, 0);

        // Now we run the tiles clicking below the upper ones
        // We start at the second row
        for (int id = numberOfColumns; id < numberOfTiles; id++) {
            if (mTemporaryPattern[id - numberOfColumns] == 1) { // If the tile above is lit we unlit it
                changePattern(id, mTemporaryPattern);
                mSolutionPattern[id] = 1; // we add the tile to the solution
            }
        }

        Log.i(LOGCAT, "Number of tiles left: " + countTiles(mTemporaryPattern));


        if (countTiles(mTemporaryPattern) != 0) { // if the board is not solved on the first sweep
                                                  // apply the rules and sweep again
            if (mTemporaryPattern[20] == 1) { // If A5(20) is ON, click on D1(3) and E1(4)
                ruleA = true;
            }
            if (mTemporaryPattern[21] == 1) { // If B5(21) is ON, click on B1(1) and E1(4)
                ruleB = true;
            }
            if (mTemporaryPattern[22] == 1) { // If C5(22) is ON, click on D1(3)
                ruleC = true;
            }
            // We reset the temporary pattern to the original to add the first row clicks
            System.arraycopy(mTilePattern, 0, mTemporaryPattern, 0, mTilePattern.length);
            Arrays.fill(mSolutionPattern, 0);

            // We add the rules to the solution
            if (ruleA) {
                changePattern(3, mTemporaryPattern);
                if (!ruleC) {
                    mSolutionPattern[3] = 1;
                }
                changePattern(4, mTemporaryPattern);
                if (!ruleB) {
                    mSolutionPattern[4] = 1;
                }
            }
            if (ruleB) {
                changePattern(1, mTemporaryPattern);
                mSolutionPattern[1] = 1;
                changePattern(4, mTemporaryPattern);
                if (!ruleA) {
                    mSolutionPattern[4] = 1;
                }
            }
            if (ruleC) {
                changePattern(3, mTemporaryPattern);
                if (!ruleA) {
                    mSolutionPattern[3] = 1;
                }
            }

            // we start the second sweep

            for (int id = numberOfColumns; id < numberOfTiles; id++) {
                if (mTemporaryPattern[id - numberOfColumns] == 1) { // If the tile above is lit we unlit it
                    changePattern(id, mTemporaryPattern);
                    mSolutionPattern[id] = 1; // we add the tile to the solution
                }
            }

            Log.i(LOGCAT, "Number of tiles left after second pass: " + countTiles(mTemporaryPattern));

        }
        mSolutionPattern = optimizeSolution(mSolutionPattern);

        showSolution(numberOfColumns, sizeOfTiles, mSolutionPattern);
    }


    /**
     * This method verifies the current tile position and checks neighbour tiles to swap its values
     *
     * @param id The tile view id inside the board
     * @param pattern the array of the board
     */
    private void changePattern(int id,  int[] pattern) {
        int posX = id % numberOfColumns;
        int posY = id / numberOfColumns;
        swapId(id, pattern);
        if (posX != 0) {
            swapId(id - 1, pattern);
        }
        if (posX != numberOfColumns - 1) {
            swapId(id + 1, pattern);
        }
        if (posY != 0) {
            swapId(id - numberOfColumns, pattern);
        }
        if (posY != numberOfColumns - 1) {
            swapId(id + numberOfColumns, pattern);
        }
    }

    /**
     * This method returns the optimized solution
     */
    private int[] optimizeSolution(int[] pattern) {
        //TODO: simplify the three pattern check by creating a new method

        int[] tempPattern = new int[numberOfTiles];
        int[] optimizedPattern = new int[numberOfTiles];
        int length = pattern.length;
        String tileCodeOn = getString(R.string.tile_code_on);
        String quietPatternA = getString(R.string.quiet_pattern_A);
        String quietPatternB = getString(R.string.quiet_pattern_B);
        String quietPatternC = getString(R.string.quiet_pattern_C);
        String currentCode = "";

        System.arraycopy(pattern, 0, optimizedPattern, 0, length); // As a default the original is the best

        // We check against the first pattern
        System.arraycopy(pattern, 0, tempPattern, 0, length); // Set the temporary as the original
        for (int id = 0; id < length; id++) {
            currentCode = quietPatternA.substring(id, id + 1);
            if (currentCode.equals(tileCodeOn)) {
                swapId(id, tempPattern);
            }
        }
        if (countTiles(tempPattern) < countTiles(optimizedPattern)) {
            System.arraycopy(tempPattern, 0, optimizedPattern, 0, length);
        }
        Log.i(LOGCAT, "Original tiles: " + countTiles(pattern));
        Log.i(LOGCAT, "First pattern tiles: " + countTiles(tempPattern));


        // We check against the second pattern
        System.arraycopy(pattern, 0, tempPattern, 0, length); // Set the temporary as the original
        for (int id = 0; id < length; id++) {
            currentCode = quietPatternB.substring(id, id + 1);
            if (currentCode.equals(tileCodeOn)) {
                swapId(id, tempPattern);
            }
        }
        if (countTiles(tempPattern) < countTiles(optimizedPattern)) {
            System.arraycopy(tempPattern, 0, optimizedPattern, 0, length);
        }
        Log.i(LOGCAT, "Second pattern tiles: " + countTiles(tempPattern));

        // We check against the third pattern
        System.arraycopy(pattern, 0, tempPattern, 0, length); // Set the temporary as the original
        for (int id = 0; id < length; id++) {
            currentCode = quietPatternC.substring(id, id + 1);
            if (currentCode.equals(tileCodeOn)) {
                swapId(id, tempPattern);
            }
        }
        if (countTiles(tempPattern) < countTiles(optimizedPattern)) {
            System.arraycopy(tempPattern, 0, optimizedPattern, 0, length);
        }
        Log.i(LOGCAT, "Third pattern tiles: " + countTiles(tempPattern));

        return optimizedPattern;
    }

    /**
     * This method returns the number of tiles lit on a pattern
     */
    private int countTiles(int[] pattern) {
        int count = 0;
        for (int i : pattern) {
            count += i;
        }
        return count;
    }

    /**
     * This method swaps the value (0-1) inside a pattern
     */
    private void swapId(int id, int[] pattern) {
        if (pattern[id] == 1) {
            pattern[id] = 0;
        } else {
            pattern[id] = 1;
        }
    }

    /**
     * This method draws the pattern with the solution
     *
     */
    public void showSolution(int numColumns, int tileSize, int[] pattern) {
        ImageView image;
        LayoutParams lp = new LayoutParams(tileSize, tileSize);
        GridLayout board = (GridLayout) findViewById(R.id.board_solution);
        board.removeAllViews();
        board.setColumnCount(numColumns);
        for (int id = 0; id < numColumns * numColumns; id++) {
            image = new ImageView(this);
            image.setLayoutParams(lp);
            image.setId(id);
            if (pattern[id] == 1) {
                image.setImageResource(R.drawable.tile_solution);
                board.addView(image);
            } else {
                image.setImageResource(android.R.color.transparent);
                board.addView(image);
            }
        }
    }
}