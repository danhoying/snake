package com.example.dan.snake;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

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

    // Stats
    long lastFrameTime;
    int fps;
    int i;

    // To start the game from onTouchEvent
    Intent intent;

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
}
