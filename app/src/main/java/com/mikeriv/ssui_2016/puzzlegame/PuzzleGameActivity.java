package com.mikeriv.ssui_2016.puzzlegame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameBoard;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameState;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameTile;
import com.mikeriv.ssui_2016.puzzlegame.util.PuzzleImageUtil;
import com.mikeriv.ssui_2016.puzzlegame.view.PuzzleGameTileView;

import java.util.Random;

public class PuzzleGameActivity extends AppCompatActivity {

    // The default grid size to use for the puzzle game 4 => 4x4 grid
    private static int DEFAULT_PUZZLE_BOARD_SIZE = 3;

    // The id of the image to use for our puzzle game
    private static int TILE_IMAGE_ID = R.drawable.kitty;

    private static int[] IMAGE_IDs = {R.drawable.kitty, R.drawable.duck, R.drawable.tom, R.drawable.gua, R.drawable.anime};
    private int ID = 0;


    private final View.OnClickListener mChangeImageBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ID = (ID + 1) % IMAGE_IDs.length;
            TILE_IMAGE_ID = IMAGE_IDs[ID];
            imageView.setImageResource(TILE_IMAGE_ID);
            drawImage();
            startNewGame();
        }
    };
    /**
     * Button Listener that starts a new game - this must be attached to the new game button
     */
    private final View.OnClickListener mNewGameButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO start a new game if a new game button is clicked
            startNewGame();
        }
    };


    /**
     * Click Listener that Handles Tile Swapping when we click on a tile that is
     * neighboring the empty tile - this must be attached to every tileView in the grid
     */
    private final View.OnClickListener mGameTileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO handle swapping tiles and updating the tileViews if there is a valid swap
            // with an empty tile
            // If any changes happen, be sure to update the state of the game to check for a win
            // condition
            PuzzleGameTileView tileView = (PuzzleGameTileView) view;
            int pointRow = tileView.getTileId() / mPuzzleGameBoard.getColumnsCount();
            int pointCol = tileView.getTileId() % mPuzzleGameBoard.getColumnsCount();
            int emptyTileRow = 0;
            int emptyTileCol = 0;
            //遍历目前点击tile的周围是否存在空白快
            for (int direction = 0; direction < 4; direction++) {
                switch (direction) {
                    case 0:
                        emptyTileRow = pointRow;
                        emptyTileCol = pointCol + 1;
                        break;
                    case 1:
                        emptyTileRow = pointRow + 1;
                        emptyTileCol = pointCol;
                        break;
                    case 2:
                        emptyTileRow = pointRow;
                        emptyTileCol = pointCol - 1;
                        break;
                    case 3:
                        emptyTileRow = pointRow - 1;
                        emptyTileCol = pointCol;
                        break;
                }
                if (mPuzzleGameBoard.isWithinBounds(emptyTileRow, emptyTileCol) && mPuzzleGameBoard.isEmptyTile(emptyTileRow, emptyTileCol)) {
                    mPuzzleGameBoard.swapTiles(pointRow, pointCol, emptyTileRow, emptyTileCol);
                    updateGameState();
                    updateStep(1);
                    break;
                }
            }

        }
    };

    /**
     * Game State - what the game is currently doing
     * */
    private PuzzleGameState mGameState = PuzzleGameState.NONE;


    // The puzzle board model
    private PuzzleGameBoard mPuzzleGameBoard;

    // Views
    private TableLayout tableLayout;
    private Button newGameBtn;
    private Button changeImageBtn;
    private TextView scoreTextView;
    private TextView stepTextView;
    private TextView winTextView;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private ImageView imageView;
    private int score;
    private int step;

    // The views for the puzzleboardtile models
    private PuzzleGameTileView[][] mPuzzleTileViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        stepTextView = (TextView) findViewById(R.id.stepTextView);
        winTextView =(TextView)findViewById(R.id.winTextView);
        radioButton = (RadioButton) findViewById(R.id.easyRadioBtn);
        imageView = (ImageView) findViewById(R.id.imageView);
        // TODO initialize references to any containers views or layout groups etc.
        radioButton.setChecked(true);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        newGameBtn = (Button) findViewById(R.id.newGameBtn);
        newGameBtn.setOnClickListener(mNewGameButtonOnClickListener);
        changeImageBtn = (Button) findViewById(R.id.changeImageBtn);
        changeImageBtn.setOnClickListener(mChangeImageBtnOnClickListener);

        radioGroup = (RadioGroup) findViewById(R.id.difficultyGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectRadioButton();
            }
        });

        // Initializes the game and updates the game state
        initGame();
        shufflePuzzleTiles();
        updateGameState();
    }

    private void selectRadioButton() {
        RadioButton radiobtn = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
        String difficulty = radiobtn.getText().toString();
        switch (difficulty) {
            case "简单":
                DEFAULT_PUZZLE_BOARD_SIZE = 3;
                break;
            case "一般":
                DEFAULT_PUZZLE_BOARD_SIZE = 4;
                break;
            case "困难":
                DEFAULT_PUZZLE_BOARD_SIZE = 6;
                break;
        }
        updateStep(-step);
        tableLayout.removeAllViews();
        initGame();
        shufflePuzzleTiles();
        updateGameState();
    }

    private void drawImage() {
        // Get the original image bitmap
        Bitmap fullImageBitmap = BitmapFactory.decodeResource(getResources(), TILE_IMAGE_ID);
        // Now scale the bitmap so it fits out screen dimensions and change aspect ratio (scale) to
        // fit a square
        int fullImageWidth = fullImageBitmap.getWidth();
        int fullImageHeight = fullImageBitmap.getHeight();
        //int squareImageSize = (fullImageWidth > fullImageHeight) ? fullImageWidth : fullImageHeight;
        int squareImageSize = 2500;
        fullImageBitmap = Bitmap.createScaledBitmap(
                fullImageBitmap,
                squareImageSize,
                squareImageSize,
                false);

        // TODO calculate the appropriate size for each puzzle tile

        int rows = mPuzzleGameBoard.getRowsCount();
        int cols = mPuzzleGameBoard.getColumnsCount();
        int tileWidth = squareImageSize / cols;
        int tileHeight = squareImageSize / rows;
        // TODO create the PuzzleGameTiles for the PuzzleGameBoard using sections of the bitmap.
        // You may find PuzzleImageUtil helpful for getting sections of the bitmap
        // Also ensure the last tile (the bottom right tile) is set to be an "empty" tile
        // (i.e. not filled with an section of the original image)
        PuzzleGameTile puzzleGameTile;
        Drawable drawable;

        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                drawable = new BitmapDrawable(PuzzleImageUtil.getSubdivisionOfBitmap(fullImageBitmap, tileWidth, tileHeight, i, j));
                puzzleGameTile = new PuzzleGameTile(index, drawable);
                mPuzzleGameBoard.setTile(puzzleGameTile, i, j);
                index += 1;
            }
        }
        //empty
        mPuzzleGameBoard.setTile(new PuzzleGameTile(index, null, true), rows - 1, cols - 1);
    }

    /**
     * Creates the puzzleboard and the PuzzleGameTiles that serve as the model for the game. It
     * does image slicing to get the appropriate bitmap subdivisions of the TILE_IMAGE_ID. It
     * then creates a set for PuzzleGameTileViews that are used to display the information in models
     */
    private void initGame() {
        int mPuzzleBoardSize = DEFAULT_PUZZLE_BOARD_SIZE;
        mPuzzleTileViews =
                new PuzzleGameTileView[mPuzzleBoardSize][mPuzzleBoardSize];
        mPuzzleGameBoard = new PuzzleGameBoard(mPuzzleBoardSize, mPuzzleBoardSize);
        drawImage();

        // TODO createPuzzleTileViews with the appropriate width, height
        createPuzzleTileViews(0, 0);

    }

    /**
     * Creates a set of tile views based on the tileWidth and height
     *
     * @param minTileViewWidth  the minium width of the tile
     * @param minTileViewHeight the minimum height of the tile
     */

    private void createPuzzleTileViews(int minTileViewWidth, int minTileViewHeight) {
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();
        // TODO Set up TileViews (that will be what the user interacts with)
        // Make sure each tileView gets a click listener for interaction
        // Be sure to set the appropriate LayoutParams so that your tileViews
        // So that they fit your gameboard properly
        TableRow[] tableRows = new TableRow[rowsCount];
        int id = 0;
        for (int i = 0; i < rowsCount; i++) {

            tableRows[i] = new TableRow(this);
            tableLayout.addView(tableRows[i]);
            for (int j = 0; j < colsCount; j++) {
                mPuzzleTileViews[i][j] = new PuzzleGameTileView(this, id, minTileViewWidth, minTileViewHeight);
                mPuzzleTileViews[i][j].setOnClickListener(mGameTileOnClickListener);
                tableRows[i].addView(mPuzzleTileViews[i][j]);
                id += 1;
            }
        }
    }

    /**
     * Shuffles the puzzle tiles randomly such that tiles may only swap if they are swapping with
     * an empty tile to maintain solvability
     */
    private void shufflePuzzleTiles() {
        // TODO randomly shuffle the tiles such that tiles may only move spots if it is randomly
        // swapped with a neighboring empty tile

        //产生空白图块
        int emptyTileRow = mPuzzleGameBoard.getRowsCount() - 1;
        int emptyTileCol = mPuzzleGameBoard.getColumnsCount() - 1;


        //交换图块
        int swapRow = 0;
        int swapColume = 0;
        Random random = new Random(System.currentTimeMillis());
        int step = random.nextInt(100) + 100;
        for (int i = 0; i < step; i++) {
            do {
                int direction = random.nextInt(4);
                switch (direction) {
                    case 0:
                        swapRow = emptyTileRow + 1;
                        swapColume = emptyTileCol;
                        break;
                    case 1:
                        swapRow = emptyTileRow;
                        swapColume = emptyTileCol + 1;
                        break;
                    case 2:
                        swapRow = emptyTileRow;
                        swapColume = emptyTileCol - 1;
                        break;
                    case 3:
                        swapRow = emptyTileRow - 1;
                        swapColume = emptyTileCol;
                        break;
                }
            } while (!mPuzzleGameBoard.isWithinBounds(swapRow, swapColume));
            mPuzzleGameBoard.swapTiles(emptyTileRow, emptyTileCol, swapRow, swapColume);
            emptyTileRow = swapRow;
            emptyTileCol = swapColume;
        }
        resetEmptyTileLocation();
    }

    /**
     * Places the empty tile in the lower right corner of the grid
     */
    private void resetEmptyTileLocation() {
        // TODO
        int emptyTileRow = 0, emptyTileCol = 0;
        for (int i = 0; i < mPuzzleGameBoard.getRowsCount(); i++) {
            for (int j = 0; j < mPuzzleGameBoard.getColumnsCount(); j++) {
                if (mPuzzleGameBoard.isEmptyTile(i, j)) {
                    emptyTileRow = i;
                    emptyTileCol = j;
                }
            }
        }
        int xMove = mPuzzleGameBoard.getRowsCount() - (emptyTileRow + 1);
        for (int i = 0; i < xMove; i++) {
            mPuzzleGameBoard.swapTiles(emptyTileRow, emptyTileCol, emptyTileRow + 1, emptyTileCol);
            emptyTileRow++;
        }
        //空白快向下移动
        int yMove = mPuzzleGameBoard.getColumnsCount() - (emptyTileCol + 1);
        for (int j = 0; j < yMove; j++) {
            mPuzzleGameBoard.swapTiles(emptyTileRow, emptyTileCol, emptyTileRow, emptyTileCol + 1);
            emptyTileCol++;
        }
    }

    /**
     * Updates the game state by checking if the user has won. Also triggers the tileViews to update
     * their visuals based on the gameboard
     */
    private void updateGameState() {
        // TODO refresh tiles and handle winning the game and updating score
        if (hasWonGame()) {
            mGameState = PuzzleGameState.WON;
            for (int i = 0; i < mPuzzleGameBoard.getRowsCount(); i++) {
                for (int j = 0; j < mPuzzleGameBoard.getColumnsCount(); j++) {
                    mPuzzleTileViews[i][j].setClickable(false);
                }
            }
        } else
            mGameState = PuzzleGameState.PLAYING;
        refreshGameBoardView();
    }

    private void refreshGameBoardView() {
        // TODO update the PuzzleTileViews with the data stored in the PuzzleGameBoard
        for (int i = 0; i < mPuzzleGameBoard.getRowsCount(); i++) {
            for (int j = 0; j < mPuzzleGameBoard.getColumnsCount(); j++)
                mPuzzleTileViews[i][j].updateWithTile(mPuzzleGameBoard.getTile(i, j));
        }
    }


    /**
     * Checks the game board to see if the tile indices are in proper increasing order
     *
     * @return true if the tiles are in correct order and the game is won
     */
    private boolean hasWonGame() {
        // TODO check if the user has won the game
        int index = 0;
        int rows = mPuzzleGameBoard.getRowsCount();
        int cols = mPuzzleGameBoard.getColumnsCount();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols && index < mPuzzleGameBoard.getTotalTileCount() - 1; j++) {
                int first = mPuzzleGameBoard.getTile(index / rows, index % cols).getOrderIndex();
                int second = mPuzzleGameBoard.getTile((index + 1) / rows, (index + 1) % cols).getOrderIndex();
                if (first > second) {
                    return false;
                }
                index++;
            }
        }
        winTextView.setText("恭喜您！！！");
        updateScore(100);
        return true;
    }

    /**
     * Updates the score displayed in the text view
     */
    private void updateScore(int n) {
        // TODO update a score to be displayed to the user
        score+=n;
        String s = String.format("得分: %d", score);
        scoreTextView.setText(s);
    }
    /**
     * Updates the step displayed in the text view
     */
    private void updateStep(int n) {
        step += n;
        String s = String.format("步数: %d",step);
        stepTextView.setText(s);
    }

    /**
     * Begins a new game by shuffling the puzzle tiles, changing the game state to playing
     * and showing a start message
     */
    private void startNewGame() {
        // TODO - handle starting a new game by shuffling the tiles and showing a start message,
        // and updating the game state
        updateStep(-step);
        updateScore(-score);
        winTextView.setText("");
        for (int i = 0; i < mPuzzleGameBoard.getRowsCount(); i++) {
            for (int j = 0; j < mPuzzleGameBoard.getColumnsCount(); j++)
                mPuzzleTileViews[i][j].setClickable(true);
        }
        shufflePuzzleTiles();
        updateGameState();
    }

}
