package com.example.dan.snake;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    Canvas canvas;
    SnakeAnimView snakeAnimView;

    // The snake head sprite sheet
    Bitmap headAnimBitmap;
    // The portion of the bitmap to be drawn in the current frame
    Rect rectToBeDrawn;
    // The dimensions of a single frame
    int frameHeight = 64;
    int frameWidth = 64;
    int numFrames = 6;
    int frameNumber;
    int screenWidth;
    int screenHeight;

    // Stats
    long lastFrameTime;
    int fps;
    int hi;

    // To start the game from onTouchEvent
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Find the width and height of screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        headAnimBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head_sprite_sheet);
        snakeAnimView = new SnakeAnimView(this);
        setContentView(snakeAnimView);

        i = new Intent(this, GameActivity.class);
    }

    class SnakeAnimView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeAnimView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            frameWidth = headAnimBitmap.getWidth() / numFrames;
            frameHeight = headAnimBitmap.getHeight();
        }

        @Override
        public void run() {
            while (playingSnake) {
                update();
                draw();
                controlFPS();
            }
        }

        public void update() {
            // Which frame should we draw?
            rectToBeDrawn = new Rect((frameNumber * frameWidth) - 1, 0,
                    (frameNumber * frameWidth + frameWidth) - 1, frameHeight);
            // Now the next frame
            frameNumber++;
            // Don't try and draw frames that don't exist
            if (frameNumber == numFrames) {
                frameNumber = 0; // Back to the first frame
            }
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK); // The background
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(150);
                canvas.drawText("Snake", 10, 150, paint);
                paint.setTextSize(25);
                canvas.drawText(" High Score:" + hi, 10, screenHeight - 50, paint);

                // Draw the snake head
                // startX, startY, endX, endY
                Rect destRect = new Rect(screenWidth / 2 - 100, screenHeight / 2 - 100,
                        screenWidth / 2 + 100, screenHeight / 2 + 100);
                canvas.drawBitmap(headAnimBitmap, rectToBeDrawn, destRect, paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 500 - timeThisFrame;
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
    }
}
