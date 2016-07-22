package com.artactivo.alllightsoff;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;

import static android.media.AudioManager.*;
import static android.support.v7.app.ActionBar.*;
import static com.artactivo.alllightsoff.Utilities.*;

public class BoardActivity extends AppCompatActivity implements View.OnTouchListener {
    private final String LOGCAT = "AllLightsOff";
    private static final String PREFS_FILENAME = "appSettings";
    private static final String LEVELS_STATUS = "levelStatusKey";
    private static final String GRID_POSITION = "gridPositionKey";
    private static final String CURRENT_LEVEL = "currentLevelKey";

    private Toast toast;
    private long lastBackPressTime = 0;
    private long lastResetPressTime = 0;
    private long lastNextPressTime = 0;
    private long lastPrevPressTime = 0;
    private long lastLevelPressTime = 0;
    private long lastMenuPressTime = 0;

    private SoundPool soundEffects;
    private int clickSoundId;
    private static int numberOfColumns = 5;                                // The size of one side of the pattern
    private static int numberOfTiles = numberOfColumns * numberOfColumns;
    private int sizeOfTiles;
    private String[] mLevelCode;
    private String[] mLevelName;
    private String mSavegameData;
    private int mCurrentLevel;
    private int lastTileId = -1;
    private int[] mTilePattern = new int[numberOfTiles];
    private int[] mSolutionPattern = new int[numberOfTiles];
    private int mSolvedTiles;
    private int mNumberOfMoves;
    private boolean gameHasEnded;
    private boolean solutionIsDisplayed;
    private int mSolutionMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mLevelCode = getResources().getStringArray(R.array.level_codes);
        mLevelName = new String[mLevelCode.length];
        mNumberOfMoves = 0;
        mSavegameData = loadLevelStatus(this);

        // Gets the first unsolved level from the preferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);
        mCurrentLevel = sharedPreferences.getInt(CURRENT_LEVEL, 0);

        prepareSounds();
        calculateBoardLayout();
        createBackgroundBoard(numberOfColumns, sizeOfTiles);
        createForegroundBoard(numberOfColumns, sizeOfTiles, mLevelCode[mCurrentLevel]);
    }

    /**
     * This method calculates the sizes for the board and the pop-up panel
     */
    private void calculateBoardLayout() {
        // Figures out the smallest screen side available to get the board tile size
        int boardDimensionPx;
        int statusBarHeight = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resId);
        }
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            boardDimensionPx = displayMetrics.heightPixels - statusBarHeight - (int) (convertDpToPx(16, this) * 2);
        } else {
            boardDimensionPx = displayMetrics.widthPixels - (int) (convertDpToPx(16, this) * 2);
        }
        sizeOfTiles = (int) boardDimensionPx / numberOfColumns;

        // Rescales the pop-up window to cover a 4 x 4 tile windows
        View popup = (View) findViewById(R.id.pop_up);
        RelativeLayout.LayoutParams rlp;
        rlp = new RelativeLayout.LayoutParams(sizeOfTiles * 4, sizeOfTiles * 4);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        popup.setLayoutParams(rlp);
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
                setButtonState(findViewById(R.id.reset_button), 1);
            }
            setButtonState(findViewById(R.id.undo_button), 1);
            playClickSound();
            mNumberOfMoves++;
            lastTileId = view.getId();
            hideSolutionTile(lastTileId);
            changeTiles(lastTileId);
            checkBoard();
            if (solutionIsDisplayed) {
                showSolution(findViewById(R.id.help_button));
            } else {
                displayMovementsLeft(mNumberOfMoves, mSolutionMoves);
            }
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
            if (image != null) {  // It can be null if the solution is not displayed
                image.setImageResource(android.R.color.transparent);
            }
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
            animateFade(image, 0, 300);
            mSolvedTiles++;
            mTilePattern[id] = 0;
        } else {                                             // if it's the off tile
            animateFade(image, 1, 300);
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
            // Calculates the actual level status and changes the corresponding pop-up message and cup image
            ImageView cup = (ImageView) findViewById(R.id.cup);
            TextView textView;
            String greeting = "";
            String currentLevelStatus = "";
            if (mNumberOfMoves <= mSolutionMoves) {
                currentLevelStatus = "3";
                cup.setImageResource(R.drawable.cup_gold);
                greeting = getResources().getString(R.string.message_gold);
            } else if (mNumberOfMoves <= mSolutionMoves * 2) {
                currentLevelStatus = "2";
                cup.setImageResource(R.drawable.cup_silver);
                greeting = getResources().getString(R.string.message_silver);
            } else if (mNumberOfMoves <= mSolutionMoves * 3) {
                currentLevelStatus = "1";
                cup.setImageResource(R.drawable.cup_bronze);
                greeting = getResources().getString(R.string.message_bronze);
            } else {
                currentLevelStatus = "0";
                cup.setImageResource(R.drawable.cup_pewter);
                greeting = getResources().getString(R.string.message_bad);
            }

            // Updates the end message
            textView = (TextView) findViewById(R.id.pop_up_message);
            if (solutionIsDisplayed) {
                textView.setText(getResources().getString(R.string.message_hints));
            } else {
                textView.setText(greeting);
            }

            String moves = getResources().getQuantityString(R.plurals.number_of_moves, mNumberOfMoves, mNumberOfMoves);
            textView = (TextView) findViewById(R.id.pop_up_moves);
            textView.setText(moves);

            // Changes the data of the current level only if it's better
            if (!solutionIsDisplayed) {
                String savedLevelStatus = getLevelData(mSavegameData, mCurrentLevel);
                Log.i(LOGCAT, "Saved Level Status: " + savedLevelStatus);
                String newGameData = "";
                if (savedLevelStatus.compareTo(currentLevelStatus) >= 0) {
                    Log.i(LOGCAT, "This level is better on disk, is the same or is locked.");
                } else {
                    Log.i(LOGCAT, "Level has improved. Save the new data.");
                    newGameData = mSavegameData.substring(0, mCurrentLevel) + currentLevelStatus + mSavegameData.substring(mCurrentLevel + 1);
                    saveLevelStatus(newGameData, this);
                    mSavegameData = newGameData;
                }

                // Unlocks the next level if it's not unlocked already
                if (mCurrentLevel != mLevelCode.length - 1) { // We are not at the last level
                    savedLevelStatus = getLevelData(mSavegameData, mCurrentLevel + 1);
                    Log.i(LOGCAT, "Next Level Status is: " + savedLevelStatus);
                    if (savedLevelStatus.equals("L")) {
                        newGameData = mSavegameData.substring(0, mCurrentLevel + 1) + "0" + mSavegameData.substring(mCurrentLevel + 2);
                        saveLevelStatus(newGameData, this);
                        Log.i(LOGCAT, "Level " + (mCurrentLevel + 2) + " Opened");
                        mSavegameData = newGameData;
                    }
                }
            }

            // Updates the stars with the appropriate values
            setStars(currentLevelStatus, findViewById(R.id.level_star_row), 1500);

            // Sets a delay before clearing the board views to allow for the last fadeout animation
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Removes the board views
                    GridLayout board = (GridLayout) findViewById(R.id.board_tiles);
                    board.removeAllViews();
                }
            }, 300);

            // Shows the pop-up window
            View popup = findViewById(R.id.pop_up);
            animateScale(popup, 0.0f, 1.0f, 500);
            popup.setVisibility(View.VISIBLE);

            gameHasEnded = true;
            setPanelContent();
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
     * This method creates the SoundPool that stores all the sound effects from the game
     */
    private void prepareSounds() {
        soundEffects = new SoundPool(10, STREAM_MUSIC, 0);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        clickSoundId = soundEffects.load(this, R.raw.click, 1);
    }

    /**
     * This method opens the menu activity
     */
    public void mainMenu(View view) {
        if (mNumberOfMoves > 0 && !gameHasEnded) {
            if (this.lastMenuPressTime < System.currentTimeMillis() - 2500) {
                toast = Toast.makeText(this, R.string.toast_open_menu, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                this.lastMenuPressTime = System.currentTimeMillis();
                return;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
            }
        }
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * This method opens the level select activity
     */
    public void selectLevel(View view) {
        if (mNumberOfMoves > 0 && !gameHasEnded) {
            if (this.lastLevelPressTime < System.currentTimeMillis() - 2500) {
                toast = Toast.makeText(this, R.string.toast_select_level, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                this.lastLevelPressTime = System.currentTimeMillis();
                return;
            } else {
                if (toast != null) {
                    toast.cancel();
                }
            }
        }
        Intent intent = new Intent(this, LevelsActivity.class);
        startActivity(intent);
        finish();
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
        if (mNumberOfMoves > 0 && !gameHasEnded) {
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
        if (mNumberOfMoves > 0 && !gameHasEnded) {
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
     * This method process click on the end message pop-up
     */
    public void popUpClick(View view) {
        // Verify that we are not on the last level to avoid error with pop-up window click
        if (mCurrentLevel != mLevelCode.length - 1) {
            View next = (View) findViewById(R.id.next_button);
            next.performClick();
        }
    }

    /**
     * This method undoes the last move
     */
    public void undoMove(View view) {
        if (mNumberOfMoves > 0) {
            mNumberOfMoves--;
            changeTiles(lastTileId);
            setButtonState(view, 0);
            //Todo: Add solution tile if it's appropriate
            //hideSolutionTile(lastTileId);
            displayMovementsLeft(mNumberOfMoves, mSolutionMoves);
        }
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
        solutionIsDisplayed = false;
        String currentCode = "";
        int currentPos = 0;

        // Clears the solution board
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

        // Draws the tiles on screen
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
                animateFade(image, 1, 300);
            } else {
                image.setAlpha(0.0f);
                mSolvedTiles++;
            }
            board.addView(image);
        }

        // Precalculates the solution of the board to get the number of moves
        calculateSolution();

        // Sets the content of the panel
        setPanelContent();

        // Hides the pop-up window
        View popup = (View) findViewById(R.id.pop_up);
        popup.setVisibility(View.INVISIBLE);
    }

    /**
     * This method sets the appropriate mode for the navigation, reset buttons, stars,
     * level number, level name and number of moves
     */
    public void setPanelContent() {
        if (gameHasEnded) {
            // Enable the reset button
            setButtonState(findViewById(R.id.reset_button), 1);

            // Disable the help button
            setButtonState(findViewById(R.id.help_button), 0);
        } else {
            // Displays the number of moves left as a graphic row of items
            displayMovementsLeft(mNumberOfMoves, mSolutionMoves);

            // Enable the help button
            setButtonState(findViewById(R.id.help_button), 1);

            // Display the current level number
            TextView textView;
            textView = (TextView) findViewById(R.id.level_number);
            textView.setText(String.format(Locale.getDefault(), "%03d", mCurrentLevel + 1));

            // Displays the level name
            textView = (TextView) findViewById(R.id.level_name);
            textView.setText(mLevelName[mCurrentLevel]);

            // Disables the reset button and sets the stars as the saved status
            setButtonState(findViewById(R.id.reset_button), 0);
            setStars(getLevelData(mSavegameData, mCurrentLevel), findViewById(R.id.level_star_row), 500);
        }

        // Disables the undo button
        setButtonState(findViewById(R.id.undo_button), 0);

        // Set the next level button state
        if (mCurrentLevel == mLevelCode.length - 1 ||                       // We are at the last level or
            getLevelData(mSavegameData, mCurrentLevel + 1).equals("L")) {   // the next level is locked
            setButtonState(findViewById(R.id.next_button), 0);
        } else {
            setButtonState(findViewById(R.id.next_button), 1);
        }

        // Set the previous level button state
        if (mCurrentLevel == 0 ||                                           // We are at the first level or
            getLevelData(mSavegameData, mCurrentLevel - 1).equals("L")) {   // the previous level is locked
            setButtonState(findViewById(R.id.prev_button), 0);
        } else {
            setButtonState(findViewById(R.id.prev_button), 1);
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
            animateFade(image, 1, 300);
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
     * This method shows the solution on the board with green tiles
     * Note: This only works with 5 tiles
     */
    public void showSolution(View view) {
        calculateSolution();
        displaySolution(numberOfColumns, sizeOfTiles, mSolutionPattern);

        // Hide the number of moves left and show no stars
        if (!solutionIsDisplayed) {
            findViewById(R.id.moves_left).setVisibility(View.INVISIBLE);
            setStars("---", findViewById(R.id.level_star_row), 1500);
            solutionIsDisplayed = true;
        }

        // Disable the help button
        setButtonState(view, 0);

        Log.i(LOGCAT, "Solution Moves:" + mSolutionMoves);
    }

    /**
     * This method calculates the solution of the board
     * Note: This only works with 5 tiles
     */
    public void calculateSolution() {
        int[] mTemporaryPattern = new int[numberOfTiles];
        boolean ruleA = false;
        boolean ruleB = false;
        boolean ruleC = false;

        // First we copy the board pattern to another array
        System.arraycopy(mTilePattern, 0, mTemporaryPattern, 0, mTilePattern.length);
        Arrays.fill(mSolutionPattern, 0);

        // Now we run the tiles clicking below the upper ones
        sweepBoard(mTemporaryPattern);

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

            // We start the second sweep with the rules applied
            sweepBoard(mTemporaryPattern);

            Log.i(LOGCAT, "Number of tiles left after second pass: " + countTiles(mTemporaryPattern));

        }
        mSolutionPattern = optimizeSolution(mSolutionPattern);
        mSolutionMoves = countTiles(mSolutionPattern);
    }

    /**
     * This method sweeps the board from the second row down and clicks on every tile that has a lit tile above
     *
     * @param pattern the array of the board
     */
    public void sweepBoard(int[] pattern) {
        for (int id = numberOfColumns; id < numberOfTiles; id++) {
            if (pattern[id - numberOfColumns] == 1) { // If the tile above is lit we unlit it
                changePattern(id, pattern);
                mSolutionPattern[id] = 1; // we add the tile to the solution
            }
        }
    }

    /**
     * This method verifies the current tile position and checks neighbour tiles to swap its values
     *
     * @param id      The tile view id inside the board
     * @param pattern the array of the board
     */
    private void changePattern(int id, int[] pattern) {
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
        int[] tempPattern = new int[numberOfTiles];
        int[] optimizedPattern = new int[numberOfTiles];
        int length = pattern.length;
        String tileCodeOn = getString(R.string.tile_code_on);
        String[] quietPatterns = getResources().getStringArray(R.array.quiet_patterns);
        String currentCode = "";

        System.arraycopy(pattern, 0, optimizedPattern, 0, length); // As a default the original is the best

        // We check against the quiet patterns and assign the one that has less moves
        for (int p = 0; p < quietPatterns.length; p++) {
            System.arraycopy(pattern, 0, tempPattern, 0, length); // Set the temporary as the original
            for (int id = 0; id < length; id++) {
                currentCode = quietPatterns[p].substring(id, id + 1);
                if (currentCode.equals(tileCodeOn)) {
                    swapId(id, tempPattern);
                }
            }
            if (countTiles(tempPattern) < countTiles(optimizedPattern)) {
                System.arraycopy(tempPattern, 0, optimizedPattern, 0, length);
            }
            Log.i(LOGCAT, "Original tiles: " + countTiles(pattern));
            Log.i(LOGCAT, "Quiet pattern " + (p + 1) + " tiles: " + countTiles(tempPattern));
        }
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
     */
    public void displaySolution(int numColumns, int tileSize, int[] pattern) {
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

    /**
     * This displays the movements left on the panel in a graphical way
     */
    private void displayMovementsLeft(int numberOfMoves, int solutionMoves) {
        if (!gameHasEnded) {
            numberOfMoves = solutionMoves * 3 - numberOfMoves;
            Bitmap singleTick = BitmapFactory.decodeResource(getResources(), R.drawable.move_bronze);
            int singleTickWidth = singleTick.getWidth();
            int numberOfColumns = 45; // The maximum number of moves for a 5x5 board is 15
            Bitmap movesPattern = Bitmap.createBitmap(singleTickWidth * numberOfColumns, singleTickWidth, Bitmap.Config.ARGB_8888);
            Canvas patternCanvas = new Canvas(movesPattern);

            if (numberOfMoves <= 0) {
                setStars("---", findViewById(R.id.level_star_row), 0);
            } else if (numberOfMoves <= solutionMoves) {
                setStars("?--", findViewById(R.id.level_star_row), 0);
            } else if (numberOfMoves <= solutionMoves * 2) {
                setStars("??-", findViewById(R.id.level_star_row), 0);
            } else if (numberOfMoves <= solutionMoves * 3) {
                setStars("???", findViewById(R.id.level_star_row), 0);
            }

            for (int id = 0; id < numberOfMoves; id++) {
                if (id == solutionMoves) {
                    singleTick = BitmapFactory.decodeResource(getResources(), R.drawable.move_silver);
                } else if (id == solutionMoves * 2) {
                    singleTick = BitmapFactory.decodeResource(getResources(), R.drawable.move_gold);
                }
                patternCanvas.drawBitmap(singleTick, (numberOfColumns - numberOfMoves + id) * singleTickWidth, 0, null);
            }
            ImageView imageView = (ImageView) findViewById(R.id.moves_left);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(movesPattern);
        }
    }


    /**
     * Activates or deactivates a view button
     *
     * @param view  the button view
     * @param state 1 -> on, 0 -> off
     */
    private void setButtonState(View view, int state) {
        if (state == 1) {
            view.setEnabled(true);
            view.setAlpha(1.0f);
        } else {
            view.setEnabled(false);
            view.setAlpha(0.25f);
        }
    }
}