package com.example.dan.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    Canvas canvas;
    SnakeView snakeView;

    Bitmap headBitmap;
    Bitmap bodyBitmap;
    Bitmap tailBitmap;
    Bitmap appleBitmap;

    // Initialize sound variables
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    // For snake movement
    int directionOfTravel = 0;
    // 0 = up, 1 = right, 2 = down, 3 = left

    int screenWidth;
    int screenHeight;
    int topGap;

    // Stats
    long lastFrameTime;
    int fps;
    int score;
    int hi;

    // Game objects
    int [] snakeX;
    int [] snakeY;
    int snakeLength;
    int appleX;
    int appleY;

    // The size in pixels of a place on the game board
    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }

    class SnakeView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            snakeX = new int[200];
            snakeY = new int[200];

            // The starting snake
            getSnake();
            // The starting apple
            getApple();
        }

        public void getSnake() {
            snakeLength = 3;
            // Start snake head in middle of screen
            snakeX[0] = numBlocksWide / 2;
            snakeY[0] = numBlocksHigh / 2;

            // Snake body
            snakeX[1] = snakeX[0] - 1;
            snakeY[1] = snakeY[0];

            // Snake tail
            snakeX[1] = snakeX[1] - 1;
            snakeY[1] = snakeY[0];
        }

        public void getApple() {
            Random random = new Random();
            appleX = random.nextInt(numBlocksWide - 1) + 1;
            appleY = random.nextInt(numBlocksHigh - 1) + 1;
        }

        @Override
        public void run() {
            while (playingSnake) {
                updateGame();
                drawGame();
                controlFPS();
            }
        }

        public void updateGame() {
            // Did the player get the apple?
            if (snakeX[0] == appleX && snakeY[0] == appleY) {
                // Grow the snake
                snakeLength++;
                //Replace the Apple
                getApple();
                // Add to the score
                score = score + snakeLength;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }
            // Move the body - starting at the back
            for (int i = snakeLength; i > 0; i--) {
                snakeX[i] = snakeX[i - 1];
                snakeY[i] = snakeY[i - 1];
            }

            // Move the head in the appropriate direction
            switch (directionOfTravel) {
                case 0: // Up
                    snakeY[0]--;
                    break;
                case 1: // Right
                    snakeX[0]++;
                    break;
                case 2: // Down
                    snakeY[0]++;
                    break;
                case 3: // Left
                    snakeX[0]--;
                    break;
            }

            // Has player died?
            boolean dead = false;

            // Collision with wall
            if (snakeX[0] == -1) dead = true;
            if (snakeX[0] >= numBlocksWide) dead = true;
            if (snakeY[0] == -1) dead = true;
            if (snakeY[0] == numBlocksHigh) dead = true;

            // Collision with snake body
            for (int i = snakeLength - 1; i > 0; i--) {
                if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                    dead = true;
                }
            }

            if (dead) {
                // Start again
                soundPool.play(sample4, 1, 1, 0, 0, 1);
                score = 0;
                getSnake();
            }
        }
    }
}
