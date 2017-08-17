package com.example.dan.snake;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
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
    int[] snakeX;
    int[] snakeY;
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

        public void drawGame() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK); // The background
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap - 10);
                canvas.drawText("Score: " + score + " High: " + hi, 10, topGap - 30, paint);

                // Draw a border around screen
                paint.setStrokeWidth(3); // Set to 3 pixels
                canvas.drawLine(1, topGap, screenWidth - 1, topGap, paint);
                canvas.drawLine(screenWidth - 1, topGap, screenWidth - 1,
                        topGap + (numBlocksHigh * blockSize), paint);
                canvas.drawLine(screenWidth - 1, (screenHeight + 30) - topGap, 1,
                        (screenHeight + 30) - topGap, paint);
                canvas.drawLine(1, topGap, 1, topGap + (numBlocksHigh * blockSize), paint);

                // Draw the snake head
                canvas.drawBitmap(headBitmap, snakeX[0] * blockSize,
                        (snakeY[0] * blockSize) + topGap, paint);

                // Draw the snake body
                for (int i = 1; i < snakeLength - 1; i++) {
                    canvas.drawBitmap(bodyBitmap, snakeX[i] * blockSize,
                            (snakeY[i] * blockSize) + topGap, paint);
                }

                // Draw the tail
                canvas.drawBitmap(tailBitmap, snakeX[snakeLength - 1] * blockSize,
                        (snakeY[snakeLength - 1] * blockSize) + topGap, paint);

                // Draw the apple
                canvas.drawBitmap(appleBitmap, appleX * blockSize,
                        (appleY * blockSize) + topGap, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 100 - timeThisFrame;
            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }
            if (timeToSleep > 0) {
                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }

        public void pause() {
            playingSnake = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
            }
        }

        public void resume() {
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if (motionEvent.getX() >= screenWidth / 2) {
                        // Turn right
                        directionOfTravel++;
                        // No such direction
                        if (directionOfTravel == 4) {
                            // Loop back to 0 (up)
                            directionOfTravel = 0;
                        }
                    } else {
                        // Turn left
                        directionOfTravel--;
                        // No such direction
                        if (directionOfTravel == -1) {
                            // Loop back to 0 (up)
                            directionOfTravel = 3;
                        }
                    }
            }
            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        while (true) {
            snakeView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }

    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            snakeView.pause();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    public void loadSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // Create objects of two required classes
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            // Create 4 fx in memory for use
            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);
        } catch (IOException e) {
            Log.e("error", "loadSound: failed to load sound files");
        }
    }

    public void configureDisplay() {
        // Determine width and height of screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        topGap = screenHeight / 20;

        // Determine the size of each block on the game board
        blockSize = screenWidth / 20;
        // Determine how many blocks will fit into the height and width
        // Leave a one-block gap at the top for the score
        numBlocksWide = 20;
        numBlocksHigh = (screenHeight - topGap) / blockSize;

        // Load and scale bitmaps
        headBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        bodyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body);
        tailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apple);

        // Scale the bitmaps to match the block size
        headBitmap = Bitmap.createScaledBitmap(headBitmap, blockSize, blockSize, false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap, blockSize, blockSize, false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap, blockSize, blockSize, false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false);
    }
}
