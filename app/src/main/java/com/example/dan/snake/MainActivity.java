package com.example.dan.snake;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
    int hi;

    // To start the game from onTouchEvent
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
